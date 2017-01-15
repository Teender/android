package com.pluscubed.crush.add;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pluscubed.crush.R;
import com.pluscubed.crush.base.RefWatchingController;
import com.pluscubed.crush.data.Message;
import com.pluscubed.crush.utils.BundleBuilder;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddController extends RefWatchingController {

    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.message)
    EditText message;
    @BindView(R.id.send)
    Button send;
    /*@BindView(R.id.recyclerview)
    RecyclerView recyclerView;*/

    private AccessToken token;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseUser firebaseUser;
    private Retrofit retrofit;
    private Service service;

    /*private UserAdapter adapter;*/

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

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.view_add_crush, container, false);
    }

 /*   private void load(Observable<String> text, int page){

    }*/

    /*private void setUsers(List<User> data) {
        adapter.setUsers(data);
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
        adapter = new UserAdapter(getActivity());
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
                    setUsers(fbSearchResponse.data);

                    Timber.d(fbSearchResponse.toString());
                });*/
    }

    private void requestUser(String email, String text) {
        if (isValidEmail(email)) {
            String encodedEmail = email.replace(".", "%2E");
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

            firebaseDatabase.getReference().child("invites").child(encodedEmail).push().setValue(chatRandom.getKey());

            firebaseDatabase.getReference().child("users").child(firebaseUser.getUid()).child("chat").child(chatRandom.getKey()).setValue(false);

            firebaseDatabase.getReference().child("messages").child(chatRandom.getKey()).push().setValue(message);

            service.sendEmail(email, "Mountain View High School", text)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {
                            Snackbar.make(getView(), R.string.email_sent, Snackbar.LENGTH_SHORT).show();

                            getRouter().popToRoot();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Snackbar.make(getView(), R.string.error_occurred, Snackbar.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onNext(Void response) {

                        }
                    });
        } else {
            Snackbar.make(getView(), R.string.invalid_email_address, Snackbar.LENGTH_SHORT).show();
        }
    }

    public interface Service {
        @POST("sendEmail")
        Observable<Void> sendEmail(@Query(value = "email", encoded = true) String email, @Query("school") String school, @Query("message") String message);
    }


}
