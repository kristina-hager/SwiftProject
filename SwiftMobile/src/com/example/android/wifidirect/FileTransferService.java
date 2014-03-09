// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.android.wifidirect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

	private static final String TAG = "FileTransferService";
    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            long clientDuration = -1;

            try {
            	Log.d(TAG, "Opening client socket - ");
            	socket.bind(null);
            	socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

            	Log.d(TAG, "Client socket - " + socket.isConnected());
            	OutputStream stream = socket.getOutputStream();
            	ContentResolver cr = context.getContentResolver();
            	InputStream is = null;
            	try {
            		is = cr.openInputStream(Uri.parse(fileUri));
            	} catch (FileNotFoundException e) {
            		Log.e(TAG, e.toString());
            	}
            	//don't try the next bit of code if the file doesn't exist
            	if (is != null) {
            		//write data from fileUri to socket
            		clientDuration = DeviceDetailFragment.copyFile(is, stream);
            		Log.d(TAG, "Client: Data written");
            		// log file transfer capture time to wirelessly send file
            		String logFileName = "clientTimeLog.txt";
            		BufferedWriter out = null;
            		if(DeviceDetailFragment.isExternalStorageWritable()){
            			File dataDir = DeviceDetailFragment.getDataStorageDir("Timelogs");
            			File file = new File(dataDir, logFileName);
            			if(!file.exists()){
            				file.createNewFile();
            			}
            			try {
            				out = new BufferedWriter(new FileWriter(file, true));
            				out.write((Long.toString(clientDuration)) + " is file transfer duration.\n");
            				Log.d(TAG,(Long.toString(clientDuration)) + " is file transfer duration.\n");
            				out.close();
            			} catch (IOException e) {
            				out.close(); 	
            			}
            		} //isExternalStorageWritable
            	} //is!=null
            	//\todo - consider adding output message to user indicating file doesn't exist
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
