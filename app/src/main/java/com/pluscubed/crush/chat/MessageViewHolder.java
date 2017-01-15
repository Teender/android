package com.pluscubed.crush.chat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pluscubed.crush.R;
import com.pluscubed.crush.data.DbUser;
import com.pluscubed.crush.data.Message;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.message)
    TextView messageText;

    public MessageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(Context context, Message message, DbUser otherUser, Drawable left, Drawable right, boolean hide) {
        messageText.setText(message.text);
        if (message.user.equals(otherUser.dbId)) {
            messageText.setBackground(left);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageText.getLayoutParams();
            layoutParams.gravity = Gravity.START;
            messageText.setLayoutParams(layoutParams);

            avatar.setVisibility(View.VISIBLE);

            if (hide) {
                Glide.with(context)
                        .load("http://presentationvoice.com/wp-content/uploads/2014/03/Photo-March-Issue_Facebook--1024x1024.jpg")
                        .into(avatar);
            } else {
                Glide.with(context)
                        .load(otherUser.picture)
                        .into(avatar);
            }
        } else {
            messageText.setBackground(right);
            avatar.setVisibility(View.GONE);

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageText.getLayoutParams();
            layoutParams.gravity = Gravity.END;
            messageText.setLayoutParams(layoutParams);
        }
    }
}
