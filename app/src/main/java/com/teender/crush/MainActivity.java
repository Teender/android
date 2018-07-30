package com.teender.crush;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.facebook.CallbackManager;
import com.teender.crush.list.ChatsController;
import com.teender.crush.widget.ContentFrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.content_frame)
    ContentFrameLayout contentView;

    @BindView(R.id.root)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigationview)
    NavigationView navigationView;

    private Router router;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        callbackManager = CallbackManager.Factory.create();


        router = Conductor.attachRouter(this, contentView, savedInstanceState);
        router.addChangeListener(contentView);

        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new ChatsController()));
        }
    }

    public CallbackManager getCallbackManager() {
        return callbackManager;
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        router.onActivityResult(requestCode, resultCode, data);
    }
}
