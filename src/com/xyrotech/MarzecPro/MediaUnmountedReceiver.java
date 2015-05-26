package com.xyrotech.MarzecPro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This receiver tries to stopDebian when sdcard is not present
public class MediaUnmountedReceiver extends BroadcastReceiver {

    private MarzecPro_ac action;

    public void onReceive(Context context, Intent intent) {
        // '!NativeHelper.isInstalled()' check tells us that the image is not
        // installed after SDcard is unmounted
        if (!NativeHelper.isInstalled() && NativeHelper.isStarted()) {
            action = new MarzecPro_ac(context, null);
            action.stopDebian();
        }
        if (!NativeHelper.installInInternalStorage)
            MarzecPro.sdcardUnmounted();
    }
}
