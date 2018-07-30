package com.teender.crush.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.teender.crush.MainActivity;
import com.teender.crush.R;
import com.teender.crush.base.RefWatchingController;
import com.teender.crush.data.FbUser;
import com.teender.crush.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import rx.Emitter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LoginController extends RefWatchingController {

    @BindView(R.id.login_button)
    LoginButton loginButton;
    @BindView(R.id.invite)
    EditText inviteEdit;

    private String email;

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflater.inflate(R.layout.view_login, container, false);
        return view;
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);

        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));

        loginButton.registerCallback(((MainActivity) getActivity()).getCallbackManager(), new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Timber.d(accessToken.getUserId());

                handleFacebookAccessToken(accessToken);
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Timber.d(exception.toString());
            }
        });
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Timber.d("handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    Timber.d("signInWithCredential:onComplete:" + task.isSuccessful());

                    if (!task.isSuccessful()) {
                        Timber.w("signInWithCredential", task.getException());
                        Snackbar.make(getView(), R.string.auth_failed, Snackbar.LENGTH_SHORT).show();
                    } else {
                        signInSuccessful(token, task);
                    }
                });
    }

    private void signInSuccessful(final AccessToken token, Task<AuthResult> task) {
        FirebaseUser user = task.getResult().getUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        userRef.child("fbid").setValue(token.getUserId());
        Profile currentProfile = Profile.getCurrentProfile();
        userRef.child("name").setValue(currentProfile.getName());
        userRef.child("picture").setValue(currentProfile.getProfilePictureUri(720, 720).toString());


        DatabaseReference inviteQuery = FirebaseDatabase.getInstance().getReference().child("invites").child(inviteEdit.getText().toString());
        Util.queryFirebase(inviteQuery)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<DataSnapshot, Observable<DataSnapshot>>() {
                    @Override
                    public Observable<DataSnapshot> call(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return Observable.empty();
                        }

                        email = dataSnapshot.child("email").getValue(String.class);
                        List<FbUser> myFbFriends = queryFriends(token).toBlocking().first();

                        for (DataSnapshot chat : dataSnapshot.child("chat").getChildren()) {
                            DatabaseReference chatSessionRef = FirebaseDatabase.getInstance().getReference().child("chat").child(chat.getKey());

                            String fbId = chat.getValue(String.class);

                            boolean found = false;
                            for (FbUser friend : myFbFriends) {
                                if (fbId.equals(friend.id)) {
                                    found = true;
                                    chatSessionRef.child("users").child(email.replace(".", "%2E")).removeValue();
                                    chatSessionRef.child("users").child(user.getUid()).setValue(false);
                                    userRef.child("chat").child(chat.getKey()).setValue(false);
                                    break;
                                }
                            }
                            if (!found) {
                                chatSessionRef.removeValue();
                                FirebaseDatabase.getInstance().getReference().child("messages").child(chat.getKey()).removeValue();
                            }
                        }

                        inviteQuery.removeValue();

                        return Observable.empty();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DataSnapshot>() {
                    @Override
                    public void onCompleted() {
                        getRouter().popToRoot();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(DataSnapshot dataSnapshot) {

                    }
                });
    }

    private Observable<List<FbUser>> queryFriends(AccessToken token) {
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FbUser> userJsonAdapter = moshi.adapter(FbUser.class);
        return Observable.<JSONArray>fromEmitter(jsonObjectEmitter -> {
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                    token, (objects, response) -> {
                        if (response.getError() != null) {
                            Timber.d(response.getError().toString());
                        }
                        jsonObjectEmitter.onNext(objects);
                        jsonObjectEmitter.onCompleted();
                    });
            Bundle parameters = new Bundle();
            request.setParameters(parameters);
            GraphResponse graphResponse = request.executeAndWait();
        }, Emitter.BackpressureMode.NONE).subscribeOn(Schedulers.io())
                .map(jsonArray -> {
                    List<FbUser> users = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            users.add(userJsonAdapter.fromJson(jsonObject.toString()));
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return users;
                });
    }

}
