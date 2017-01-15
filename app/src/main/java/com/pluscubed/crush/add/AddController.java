package com.pluscubed.crush.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pluscubed.crush.R;
import com.pluscubed.crush.base.RefWatchingController;
import com.pluscubed.crush.data.DbUser;
import com.pluscubed.crush.data.Message;
import com.pluscubed.crush.utils.BundleBuilder;
import com.pluscubed.crush.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Emitter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddController extends RefWatchingController {

    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.send)
    FloatingActionButton send;
    /*@BindView(R.id.recyclerview)
    RecyclerView recyclerView;*/

    private AccessToken token;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private Retrofit retrofit;
    private Service service;
    private String inviteCode;
    private MaterialDialog dialog;

    /*private MessageAdapter adapter;*/

    public AddController() {
        super();
    }

    public AddController(AccessToken token) {
        this(new BundleBuilder(new Bundle())
                .putParcelable("access_token", token)
                .build());
    }

    protected AddController(Bundle args) {
        super(args);
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static String generateString(int length) {
        Random random = new Random();
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }

 /*   private void load(Observable<String> text, int page){

    }*/

    /*private void setMessages(List<FbUser> data) {
        adapter.setMessages(data);
    }*/

//    private Observable<JSONObject> searchJson(String text){
//        return Observable.fromEmitter(jsonObjectEmitter -> {
//            GraphRequest request = GraphRequest.newGraphPathRequest(
//                    token, "search", response -> jsonObjectEmitter.onNext(response.getJSONObject()));
//            Bundle parameters = new Bundle();
//            parameters.putString("type", "user");
//            parameters.putString("q", text);
//            request.setParameters(parameters);
//            request.executeAsync();
//        }, Emitter.BackpressureMode.NONE);
//
//    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.view_add_crush, container, false);
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);
        token = getArgs().getParcelable("access_token");
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://teender.herokuapp.com/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        service = retrofit.create(Service.class);

        send.setOnClickListener((v) -> {
            requestUser(editText.getText().toString(), message.getText().toString());
        });


        // requestUser(editText.getText().toString());

       /* LinearLayoutManager layout = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layout);
        adapter = new MessageAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        //UserListener listener = new UserListener(layout);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layout) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

            }
        });*/


        //Moshi moshi = new Moshi.Builder().build();
        //JsonAdapter<FbSearchResponse> userJsonAdapter = moshi.adapter(FbSearchResponse.class);

        /*RxTextView.textChangeEvents(editText)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .filter(s -> s.toString().length() > 2)
                .debounce(100, TimeUnit.MILLISECONDS)
                .switchMap(text-> searchJson(text.text().toString()))
                .filter(json->json!=null)
                .map(jsonObject -> {
                    try {
                        return userJsonAdapter.fromJson(jsonObject.toString());
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fbSearchResponse -> {
                    setMessages(fbSearchResponse.data);

                    Timber.d(fbSearchResponse.toString());
                });*/
    }

    private void requestUser(String email, String text) {
        if (isValidEmail(email)) {

            final boolean[] found = new boolean[1];
            final boolean[] chatExists = {false};
            final DbUser[] otherUser = new DbUser[1];

            com.google.firebase.database.Query query = FirebaseDatabase.getInstance().getReference()
                    .child("users").orderByChild("email").startAt(email).limitToFirst(1);

            Util.queryFirebase(query)
                    .subscribeOn(Schedulers.io())
                    .flatMap(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();
                            otherUser[0] = snapshot.getValue(DbUser.class);
                            otherUser[0].dbId = snapshot.getKey();

                            Observable<DataSnapshot> snapshotObservable = Observable.empty();
                            for (String session : otherUser[0].chat.keySet()) {
                                DatabaseReference query1 = firebaseDatabase.getReference().child("chat")
                                        .child(session).child("users").child(firebaseUser.getUid());

                                snapshotObservable = snapshotObservable.mergeWith(Util.queryFirebase(query1));
                            }

                            return snapshotObservable;
                        } else {
                            sendAnonymousEmail(email, text);
                            return Observable.empty();
                        }
                    })
                    .flatMap(new Func1<DataSnapshot, Observable<Object>>() {
                        @Override
                        public Observable<Object> call(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (!dataSnapshot.getValue(Boolean.class)) {
                                    dataSnapshot.getRef().getParent().child(otherUser[0].dbId).setValue(false);
                                    found[0] = true;
                                } else {
                                    chatExists[0] = true;
                                }
                            }
                            return Observable.just(new Object());
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                            if (found[0]) {
                                Snackbar.make(getView(), R.string.you_are_matched, Snackbar.LENGTH_SHORT).show();
                                getRouter().popToRoot();
                            } else if (!chatExists[0]) {
                                Message message = new Message();
                                message.text = text;
                                message.user = firebaseUser.getUid();
                                message.timestamp = System.currentTimeMillis();

                                DatabaseReference chatRandom = firebaseDatabase.getReference().child("chat").push();
                                Map<String, Boolean> users = new HashMap<>();
                                users.put(firebaseUser.getUid(), true);
                                users.put(otherUser[0].dbId, false);
                                chatRandom.child("users").setValue(users);
                                chatRandom.child("timestamp").setValue(message.timestamp);

                                firebaseDatabase.getReference().child("users").child(firebaseUser.getUid()).child("chat").child(chatRandom.getKey()).setValue(false);
                                firebaseDatabase.getReference().child("users").child(otherUser[0].dbId).child("chat").child(chatRandom.getKey()).setValue(false);

                                firebaseDatabase.getReference().child("messages").child(chatRandom.getKey()).push().setValue(message);

                                Snackbar.make(getView(), "They're on Teender! They will see your message shortly.", Snackbar.LENGTH_SHORT).show();
                                getRouter().popToRoot();
                            } else {
                                Snackbar.make(getView(), "Chat already exists.", Snackbar.LENGTH_SHORT).show();
                                getRouter().popToRoot();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Object o) {

                        }
                    });

        } else {
            Snackbar.make(getView(), R.string.invalid_email_address, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void sendAnonymousEmail(final String email, final String text) {
        new MaterialDialog.Builder(getActivity())
                .content("Your crush hasn't installed Teender yet. Would you like to send an anonymous invite?")
                .positiveText("Send")
                .negativeText("Cancel")
                .onPositive((dialog1, which) -> {
                    actualSendAnonymousEmail(email, text);
                });
    }

    private void actualSendAnonymousEmail(String email, String text) {
        dialog = new MaterialDialog.Builder(getActivity())
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .content("Sending anonymous invite email...")
                .show();

        String encodedEmail = email.replace(".", "%2E");
        inviteCode = generateString(4);

        Observable.fromEmitter(emitter -> {
            DatabaseReference invitesRef = FirebaseDatabase.getInstance().getReference().child("invites").child(inviteCode);
            invitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        inviteCode = generateString(4);
                        emitter.onError(new Throwable("hi"));
                    }
                    emitter.onCompleted();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }, Emitter.BackpressureMode.NONE).retry()
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        onInviteCodeGenerated(encodedEmail, text, email);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    private void onInviteCodeGenerated(String encodedEmail, String text, String email) {
        Map<String, Boolean> users = new HashMap<>();
        users.put(firebaseUser.getUid(), true);
        users.put(encodedEmail, false);

        Message message = new Message();
        message.text = text;
        message.user = firebaseUser.getUid();
        message.timestamp = System.currentTimeMillis();

        DatabaseReference chatRandom = firebaseDatabase.getReference().child("chat").push();
        chatRandom.child("users").setValue(users);
        chatRandom.child("timestamp").setValue(message.timestamp);

        firebaseDatabase.getReference().child("invites").child(inviteCode).child("chat").child(chatRandom.getKey()).setValue(token.getUserId());
        firebaseDatabase.getReference().child("invites").child(inviteCode).child("email").setValue(email);

        firebaseDatabase.getReference().child("users").child(firebaseUser.getUid()).child("chat").child(chatRandom.getKey()).setValue(false);

        firebaseDatabase.getReference().child("messages").child(chatRandom.getKey()).push().setValue(message);

        service.sendEmail(email, "Mountain View High School", text, inviteCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Snackbar.make(getView(), R.string.email_sent, Snackbar.LENGTH_SHORT).show();

                        dialog.dismiss();

                        getRouter().popToRoot();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(getView(), R.string.error_occurred, Snackbar.LENGTH_SHORT).show();

                        dialog.dismiss();
                    }

                    @Override
                    public void onNext(Void response) {

                    }
                });
    }

    public interface Service {
        @POST("sendEmail")
        Observable<Void> sendEmail(@Query(value = "email", encoded = true) String email, @Query("school") String school, @Query("message") String message, @Query("code") String code);
    }


}
