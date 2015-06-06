package com.xyrotech.MarzecPro;

import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class StreamThread extends Thread {
    InputStream i;
    StreamUpdate update;

    StreamThread(InputStream i) {
        this.i = i;
    }

    StreamThread(InputStream i, StreamUpdate update) {
        this.i = i;
        this.update = update;
    }

    @Override
    public void run() {
        try {
            byte[] readBuffer = new byte[512];
            int readCount = -1;
            while ((readCount = i.read(readBuffer)) > 0) {
                String readString = new String(readBuffer, 0, readCount);
                update.update(readString);
            }
        } catch (IOException e) {
            Log.e(MarzecPro.TAG, "", e);
        }
    }

    public String dump() {
        if (update instanceof StringBufferStreamUpdate) {
            return ((StringBufferStreamUpdate) update).dump();
        }

        return null;
    }

    public static abstract class StreamUpdate {
        public abstract void update(String val);
    }

    public class StringBufferStreamUpdate extends StreamUpdate {
        StringBuilder sb = new StringBuilder();

        @Override
        public void update(String val) {
            sb.append(val);
        }

        public String dump() {
            return sb.toString();
        }
    }
}