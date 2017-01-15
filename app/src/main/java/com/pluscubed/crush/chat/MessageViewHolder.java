package com.pluscubed.crush.chat;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pluscubed.crush.R;
import com.pluscubed.crush.data.Message;
import com.pluscubed.crush.data.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.list_item_message_primary)
    TextView user;
    @BindView(R.id.message)
    TextView messageText;

    public MessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Message message, User otherUser, Drawable left, Drawable right) {
        messageText.setText(message.text);
        if (message.user.equals(otherUser.dbId)) {
            messageText.setBackground(left);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageText.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            messageText.setLayoutParams(layoutParams);

            user.setText(otherUser.name);
        } else {
            messageText.setBackground(right);
            user.setVisibility(View.GONE);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageText.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            messageText.setLayoutParams(layoutParams);
        }
    }
}
