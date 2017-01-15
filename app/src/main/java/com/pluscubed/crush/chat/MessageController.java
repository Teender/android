package com.pluscubed.crush.chat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pluscubed.crush.R;
import com.pluscubed.crush.base.RefWatchingController;
import com.pluscubed.crush.data.ChatSession;
import com.pluscubed.crush.data.Message;
import com.pluscubed.crush.data.User;
import com.pluscubed.crush.utils.BundleBuilder;

import butterknife.BindView;

public class MessageController extends RefWatchingController {

    @BindView(R.id.toolbar_actionbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.send)
    ImageView send;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private AccessToken accessToken;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> adapter;
    private User otherUser;
    private String session;
    private DatabaseReference sessionRef;

    public MessageController() {
        super();
    }

    public MessageController(String session) {
        this(new BundleBuilder(new Bundle()).putString("session", session).build());
    }

    protected MessageController(Bundle args) {
        super(args);
    }

    @Override
    protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
        return inflater.inflate(R.layout.view_chat, container, false);
    }

    @Override
    protected void onAttach(@NonNull View view) {
        super.onAttach(view);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        accessToken = AccessToken.getCurrentAccessToken();

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRouter().popToRoot();
            }
        });

        session = getArgs().getString("session");
        sessionRef = FirebaseDatabase.getInstance().getReference().child("messages").child(session);


        LinearLayoutManager layout = new LinearLayoutManager(getActivity());
        layout.setStackFromEnd(true);
        recyclerView.setLayoutManager(layout);
        recyclerView.setHasFixedSize(true);

        FirebaseDatabase.getInstance().getReference().child("chat").child(session).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatSession session = dataSnapshot.getValue(ChatSession.class);

                for (String userId : session.getUsers().keySet()) {
                    if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userId)) {
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((otherUser = dataSnapshot.getValue(User.class)) != null) {

                                } else {
                                    otherUser = new User();
                                    otherUser.name = userId.replace("%2E", ".");
                                }

                                otherUser.dbId = userId;

                                toolbar.setTitle(otherUser.name);

                                initAdapter();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        send.setOnClickListener(v -> {
            Message message = new Message();
            message.text = editText.getText().toString();
            message.user = firebaseUser.getUid();
            message.timestamp = System.currentTimeMillis();

            sessionRef.push().setValue(message);
        });
    }

    private void initAdapter() {
        Query messagesQuery = sessionRef.orderByChild("timestamp");

        adapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(Message.class,
                R.layout.list_message,
                MessageViewHolder.class,
                messagesQuery) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.bind(model, otherUser, ContextCompat.getDrawable(getActivity(), R.drawable.bg_message_left),
                        ContextCompat.getDrawable(getActivity(), R.drawable.bg_message_right));
            }
        };
        recyclerView.setAdapter(adapter);
    }
}
