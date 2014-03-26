package com.example.swiftdatahop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;

public class FileHelper {
	
	public static final String TAG = "FileHelper";

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
    
	public static File getFileToSend(Fragment frag) {
		String filename = "test_data_MASTER.csv";
		File dataDir = FileHelper.getDataStorageDir(Constants.DIR_WI_FI_DIRECT_DEMO);
		File file = new File(dataDir, filename);
		if (file.exists() && file.isFile()) {
		} else {
                //[AR] Todo: Ugh...we need a better way to do this instead of pulling in frag.
				InputStream ins = frag.getResources().openRawResource(R.raw.test_data_csv);
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				byte buf[] = new byte[1024];
				int len;
				try {
					while((len=ins.read(buf))>0) {
					    fos.write(buf,0,len);
					}
					fos.close();
					ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
		}
		return file;
	}
	
    public static long copyFile(InputStream inputStream, OutputStream out, StringBuilder outErrorMessage) {
    	//[AR] - here we return the duration as a long always, but it only
    	//matters to us when the output stream is a socket connection, not
    	//an output to a file.  Output stream is only a socket connection
    	// when called from FileTransferServer.java. We can't tell the difference 
    	//here, so we always capture it, and return it from this function.
        byte buf[] = new byte[1024];
        int len;
        long duration = -1;
        try {
            long time1 = System.nanoTime();
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            long time2 = System.nanoTime();
            out.close();
            inputStream.close();
            duration = time2-time1;
            //outErrorMessage.append("CopyFile No Error"); //KH-this used to test msg return only
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            outErrorMessage.append("CopyFile Error: " + e.toString());
        }
        return duration;
    }
}
