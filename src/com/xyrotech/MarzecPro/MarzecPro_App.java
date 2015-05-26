package com.xyrotech.MarzecPro;

import android.app.Application;

public class MarzecPro_App extends Application {
    public static final String TAG = "MarzecPro_App";

    public void onCreate() {
        super.onCreate();
        NativeHelper.setup(getApplicationContext());
        // TODO move this code from NativeHelper to here
    }
}
