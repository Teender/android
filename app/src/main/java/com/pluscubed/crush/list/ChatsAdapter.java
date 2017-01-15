package com.pluscubed.crush.list;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pluscubed.crush.R;

public class ChatsAdapter extends RecyclerView.Adapter<ChatHolder> {
    private final LayoutInflater layoutInflater;
    private Context context;

    public ChatsAdapter(Context context) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ChatHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatHolder(layoutInflater.inflate(R.layout.list_person, null, false));
    }

    @Override
    public void onBindViewHolder(ChatHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

}
