package com.pluscubed.crush.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pluscubed.crush.R;
import com.pluscubed.crush.data.ChatSession;
import com.pluscubed.crush.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatHolder extends RecyclerView.ViewHolder {

    View root;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.list_item_message_primary)
    TextView listItem;
    @BindView(R.id.list_item_message_secondary1)
    TextView secondary;

    private ListController controller;

    public ChatHolder(View itemView, ListController controller) {
        super(itemView);
        this.controller = controller;
        ButterKnife.bind(this, itemView);
        root = itemView;
    }


    public void bind(ChatSession model) {
        listItem.setText("Loading...");
        secondary.setText(model.getTimestamp().toString());


        for (String user : model.getUsers().keySet()) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user)) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User lianshuUser;
                        if (listItem != null) {
                            if ((lianshuUser = dataSnapshot.getValue(User.class)) != null)
                                listItem.setText(lianshuUser.name);
                            else {
                                listItem.setText(user.replace("%2E", "."));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.onItemClick(getAdapterPosition());
            }
        });
    }
}
