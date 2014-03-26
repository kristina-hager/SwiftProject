package com.example.swiftdatahop;

import java.io.File;

import android.os.Environment;

public class FileHelper {

    public static File getDataStorageDir(String dataName) {
        // Get the directory for the user's public documents directory. 
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), dataName);
        if (!path.mkdirs()) {
//[AR] need a log here

        }
        return path;
    }
    
    /* [AR] - Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
