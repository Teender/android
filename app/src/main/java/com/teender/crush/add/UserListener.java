package com.teender.crush.add;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class UserListener extends EndlessRecyclerViewScrollListener {
    public UserListener(LinearLayoutManager layoutManager) {
        super(layoutManager);
    }

    public UserListener(GridLayoutManager layoutManager) {
        super(layoutManager);
    }

    public UserListener(StaggeredGridLayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

    }
}
