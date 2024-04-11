package com.example.modbusrwutil.logConfig;

import android.util.Log;

import com.example.modbusrwutil.BuildConfig;

public class LogUtils {
    public static void log(String tag, String msg) {
        if (BuildConfig.NEED_LOG) {
            Log.d(tag, msg);
        }
    }
}
