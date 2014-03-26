package com.example.swiftdatahop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.os.Environment;
import android.support.v4.app.Fragment;

public class FileHelper {
	
	public final String TAG = "FileHelper";

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
}
