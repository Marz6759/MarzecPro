package com.xyrotech.MarzecPro;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MediaMountedReceiver extends BroadcastReceiver {

    private MarzecPro_ac action;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (!NativeHelper.installInInternalStorage) {
            if (!NativeHelper.isStarted()
                    && prefs.getBoolean(context
                            .getString(R.string.pref_start_automatically_key),
                            false)) {
                if (new File(NativeHelper.image_path).exists()
                        && new File(NativeHelper.mnt).exists()) {
                    action = new MarzecPro_ac(context, null);
                    action.startDebian();
                }
            }
            MarzecPro.sdcardMounted();
        }

    }

}

