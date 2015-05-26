package com.xyrotech.MarzecPro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This receiver tries to stopDebian when sdcard is present
public class MediaEjectReceiver extends BroadcastReceiver {

    private MarzecPro_ac action;

    public void onReceive(Context context, Intent intent) {
        if (!NativeHelper.installInInternalStorage) {
            if (NativeHelper.isStarted()) {
            action = new MarzecPro_ac(context, null);
            action.stopDebian();
            }
            MarzecPro.sdcardUnmounted();
        }
    }
}