package io.weichao.plugin_demo;

import android.app.Application;

import io.weichao.plugin_demo.hook.HookUtil;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        HookUtil.hookAMS();
        HookUtil.hookHandler();
    }
}