package com.teender.crush.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;

public class ContentFrameLayout extends FrameLayout implements ControllerChangeHandler.ControllerChangeListener {
    private int inProgressTransactionCount;

    public ContentFrameLayout(Context context) {
        super(context);
    }

    public ContentFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContentFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return (inProgressTransactionCount > 0) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onChangeStarted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler) {
        inProgressTransactionCount++;
    }

    @Override
    public void onChangeCompleted(Controller to, Controller from, boolean isPush, ViewGroup container, ControllerChangeHandler handler) {
        inProgressTransactionCount--;
    }
}
