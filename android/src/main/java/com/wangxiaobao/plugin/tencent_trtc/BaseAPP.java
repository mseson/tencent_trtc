package com.wangxiaobao.plugin.tencent_trtc;

import android.app.Application;

public class BaseAPP extends Application {
    public static Application app;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }
}
