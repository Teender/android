package com.pluscubed.crush.list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.RouterTransaction;
import com.facebook.AccessToken;
import com.facebook.Profile;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.pluscubed.crush.R;
import com.pluscubed.crush.add.AddController;
import com.pluscubed.crush.base.MimicActivityChangeHandler;
import com.pluscubed.crush.base.RefWatchingController;
import com.pluscubed.crush.data.ChatSession;
import com.pluscubed.crush.login.LoginController;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;

public class ListController extends RefWatchingController {

    @BindView(R.id.toolbar_actionbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private AccessToken accessToken;
    private boolean needLogin;

    public ListController() {
        this(null);
    }

    protected ListController(Bundle args) {
        super(args);
        firebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        View view = inflater.inflate(R.layout.view_list, container, false);
        return view;
    }

    @Override
    protected void onViewBound(@NonNull View view) {
        super.onViewBound(view);

        toolbar.setTitle(R.string.app_name);
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);

        checkFirebase();
    }

    private void onUserSet() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userChats = database.getReference().child("users").child(user.getUid()).child("chat");
        DatabaseReference chatsDatabase = database.getReference().child("chat");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userRef.child("fbid").setValue(accessToken.getUserId());
        Profile currentProfile = Profile.getCurrentProfile();
        userRef.child("name").setValue(currentProfile.getName());
        userRef.child("picture").setValue(currentProfile.getProfilePictureUri(720, 720).toString());


        FirebaseIndexRecyclerAdapter<ChatSession, ChatHolder> adapter = new FirebaseIndexRecyclerAdapter<ChatSession, ChatHolder>(ChatSession.class,
                R.layout.list_person,
                ChatHolder.class,
                userChats, // The Firebase location containing the list of keys to be found in dataRef.
                chatsDatabase) {

            @Override
            protected void populateViewHolder(ChatHolder viewHolder, ChatSession model, int position) {
                viewHolder.bind(model);
            }
        };
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);


        //text1.setText(user.getDisplayName()+" "+user.getEmail());

        fab.setOnClickListener(v -> {
            AddController controller = new AddController(accessToken);
            controller.setTargetController(ListController.this);
            getRouter().pushController(RouterTransaction.with(controller)
                    .popChangeHandler(new MimicActivityChangeHandler(getActivity()))
                    .pushChangeHandler(new MimicActivityChangeHandler(getActivity())));
        });
    }

    private void checkFirebase() {
        if ((user = firebaseAuth.getCurrentUser()) == null || (accessToken = AccessToken.getCurrentAccessToken()) == null) {
            Observable.just(0)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .subscribe(new Subscriber<Integer>() {
                        @Override
                        public void onCompleted() {
                            LoginController controller = new LoginController();
                            controller.setTargetController(ListController.this);
                            getRouter().pushController(RouterTransaction.with(controller)
                                    .popChangeHandler(new MimicActivityChangeHandler(getActivity())));
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Integer integer) {

                        }
                    });

        } else {
            onUserSet();
        }
    }
}
