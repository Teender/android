package com.pluscubed.crush.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pluscubed.crush.R;
import com.pluscubed.crush.data.ChatSession;
import com.pluscubed.crush.data.DbUser;

import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    View root;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.list_item_message_primary)
    TextView name;
    @BindView(R.id.list_item_message_secondary1)
    TextView secondary;

    private boolean loading;
    private boolean hide;

    private ChatsController controller;

    public ChatViewHolder(View itemView, ChatsController controller) {
        super(itemView);
        this.controller = controller;
        ButterKnife.bind(this, itemView);
        root = itemView;
    }


    public void bind(Context context, ChatSession model) {
        name.setText("");
        secondary.setText("");
        loading = true;

        Iterator<String> iterator = model.getUsers().keySet().iterator();

        boolean anonymous1 = false;
        boolean anonymous2 = false;

        for (String user : model.getUsers().keySet()) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user)) {
                anonymous1 = model.getUsers().get(iterator.next());

                if (anonymous1) {
                    name.setText(R.string.facebook_friend);

                    avatar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            Glide.with(context)
                                    .load("http://presentationvoice.com/wp-content/uploads/2014/03/Photo-March-Issue_Facebook--1024x1024.jpg")
                                    .into(avatar);
                            avatar.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        }
                    });

                    loading = false;
                } else {
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DbUser dbUser;
                            if (root != null) {
                                if ((dbUser = dataSnapshot.getValue(DbUser.class)) != null) {
                                    name.setText(dbUser.name);
                                    Glide.with(context)
                                            .load(dbUser.picture)
                                            .into(avatar);
                                } else {
                                    name.setText(user.replace("%2E", "."));
                                }
                            }

                            loading = false;
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            } else {
                anonymous2 = model.getUsers().get(iterator.next());
            }

            if (!anonymous1 && !anonymous2) {
                secondary.setText(R.string.connected);
            } else if (anonymous1 ^ anonymous2) {
                secondary.setText(R.string.anonymous);
            }

            if (anonymous2) {
                hide = true;
            } else {
                hide = false;
            }
        }

        root.setOnClickListener(v -> {
            if (!loading)
                controller.onItemClick(getAdapterPosition(), hide);
        });
    }
}
