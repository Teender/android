package com.pluscubed.crush.list;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pluscubed.crush.R;
import com.pluscubed.crush.data.ChatSession;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatHolder extends RecyclerView.ViewHolder {

    View root;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.list_item_message_primary)
    TextView listItem;

    public ChatHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        root = itemView;
    }


    public void bind(ChatSession model) {
        listItem.setText(model.getTimestamp().toString());

        for (String user : model.getUsers().keySet()) {

        }

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
