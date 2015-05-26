package com.xyrotech.MarzecPro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;

public class BootCompletedReceiver {

    private MarzecPro_ac action;

    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (NativeHelper.installInInternalStorage && !NativeHelper.isStarted()) {
            if (prefs.getBoolean(
                    context.getString(R.string.pref_start_automatically_key), false)) {
                if (new File(NativeHelper.image_path).exists()
                        && new File(NativeHelper.mnt).exists()) {
                    action = new MarzecPro_ac(context, null);
                    action.startDebian();
                }
            }
        }

    }

}
