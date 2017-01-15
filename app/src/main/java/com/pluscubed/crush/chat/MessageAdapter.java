package com.pluscubed.crush.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pluscubed.crush.R;
import com.pluscubed.crush.data.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private final LayoutInflater layoutInflater;
    private List<Message> messages;
    private Context context;

    public MessageAdapter(Context context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        messages = new ArrayList<>();

        setHasStableIds(true);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(layoutInflater.inflate(R.layout.list_message, null, false));
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        //holder.bind(messages.get(position), otherUser);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

   /* @Override
    public long getItemId(int position) {
        return messages.get(position).id.hashCode();
    }

    public void setMessages(List<Message> newUsers) {
        messages = new ArrayList<>(newUsers);
        notifyDataSetChanged();
    }

    public void addUsers(List<FbUser> newUsers) {
        int size = messages.size();
    }*/

}
