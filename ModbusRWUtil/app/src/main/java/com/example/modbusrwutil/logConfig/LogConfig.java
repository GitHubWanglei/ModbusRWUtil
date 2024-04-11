package com.example.modbusrwutil.logConfig;

import android.content.Context;
import android.os.Environment;

import org.apache.log4j.Level;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class LogConfig {
    public static void config(Context context) {
        LogConfigurator logConfigurator = new LogConfigurator();
        // 日志输出位置：/Android/data/com.example.appName/files/Documents/log.txt
        String filePath = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/log.txt";
        logConfigurator.setFileName(filePath);
        logConfigurator.setRootLevel(Level.INFO);
        logConfigurator.setMaxBackupSize(5);
        logConfigurator.setMaxFileSize(1024*1024*10L);
        logConfigurator.configure();
    }
}
