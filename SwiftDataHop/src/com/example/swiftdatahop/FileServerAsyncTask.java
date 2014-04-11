package com.example.swiftdatahop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream. As in, accepts data from stream and writes that data to a file
     */
    public class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        @SuppressWarnings("unused")
		private Context context;
        private TextView statusText;
        private static final String TAG = "FileServerAsync";

        /**
         * @param context
         * @param statusText
         */
        public FileServerAsyncTask(Context context, View statusText) {
            this.context = context;
            this.statusText = (TextView) statusText;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(Constants.PORT);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done for receive.");
                final File f = new File(FileHelper.getDataStorageDir(Constants.DIR_WI_FI_DIRECT_DEMO), 
                		"test_data." + System.currentTimeMillis() + ".csv");
                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                //[AR] - This call to copy file reads in from the socket and writes to a file
                StringBuilder errMsg = new StringBuilder();
                long duration = FileHelper.copyFile(inputstream, new FileOutputStream(f), errMsg);
                Log.d(TAG,"Socket, copyFile errMsg: " + errMsg);
                serverSocket.close();
                logDuration(duration, f.getName());
                if (duration < 0){
                	return errMsg.toString();
                } else {
                	return f.getAbsolutePath();
                }                       
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return "IO Exception receiving the file and writing it to disk: " + e;
            }
        }
        
        private void logDuration(long duration, String filename) {
       		// log file transfer capture time to receive file
    		String logFileName = "receiveTimeLog.txt";
    		BufferedWriter out = null;

    		if(FileHelper.isExternalStorageWritable()){
    			try {
    			File dataDir = FileHelper.getDataStorageDir(Constants.DIR_TIMELOGS);

    			File file = new File(dataDir, logFileName);
    			if(!file.exists()){
    				file.createNewFile();
    			}

    				out = new BufferedWriter(new FileWriter(file, true));
    				out.write((Long.toString(duration)) + " ns to receive file " + filename + "\n");
    				Log.d(TAG, (Long.toString(duration)) + " ns to receive file " + filename + "\n");
    				out.close();
    			} catch (IOException e) {
    				Log.d(TAG, "IO Exception writing to log file.");
    			}
    		} //isExternalStorageWritable    	
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
            	File file = new File(result);
                statusText.setText("File copied to - " + result 
                		+ "\nAt: " + Utils.getDateAndTime() 
                		+ "\nSize: " + file.length() + " B"                		
                		+ "\n\nSelect Ready Receive to prepare for new file.");
                Log.d(TAG,"File copied to - " + result + "\nAt: " + Utils.getDateAndTime());
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            statusText.setText("Prepared to receive file via socket");
        }

    }