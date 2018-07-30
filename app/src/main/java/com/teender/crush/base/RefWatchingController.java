package com.teender.crush.base;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;

public abstract class RefWatchingController extends ButterKnifeController {

    private boolean hasExited;

    protected RefWatchingController() {
    }

    protected RefWatchingController(Bundle args) {
        super(args);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

       /* if (hasExited) {
            App.refWatcher.watch(this);
        }*/
    }

    @Override
    protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeEnded(changeHandler, changeType);

       /* hasExited = !changeType.isEnter;
        if (isDestroyed()) {
            App.refWatcher.watch(this);
        }*/
    }
}
