// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.swiftdatahop;

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
    private static final int SOCKET_TIMEOUT = 30000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String EXTRAS_LOG_COMMENT = "log_comment";
    public static final String EXTRAS_DELAY_BEFORE_CONNECT = "connect_delay";
    public static final String EXTRAS_NUMBER_CONNECT_TRIES = "num_retry";
    private AppDataManager mAppData;

    private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
        mAppData = AppDataManager.getInstance();
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
            String logComment = intent.getExtras().getString(EXTRAS_LOG_COMMENT);
            int connectDelay = intent.getExtras().getInt(EXTRAS_DELAY_BEFORE_CONNECT,0);
            //int numConnectTries = intent.getExtras().getInt(EXTRAS_NUMBER_CONNECT_TRIES,1);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            long clientDuration = -1;
            
            try {
            	if (connectDelay != 0) {
            		doDelay(connectDelay);
            	}
            	            	
            	socket.bind(null);
            	boolean connectSuccess = false;
            	//kh - having trouble getting retry to work w/ below. not sure why yet.
//            	for (int connectAttempt=0; connectAttempt<numConnectTries; connectAttempt++) {
//            		Log.d(TAG,"Attempting to open client socket, attempt " + connectAttempt);
//            		try {
            			socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
            			connectSuccess = true;
//            			break;
//            		} catch (IOException e) {
//            			Log.e(TAG, "IOException on connect attempt: " + e.getMessage());
//            			socketCleanup(socket);
//            		}
//            		doDelay(connectDelay);
//            	}

            	if (connectSuccess) {
            		Log.d(TAG, "FTSintentService: (client socket)" + socket.isConnected());
            		OutputStream stream = socket.getOutputStream();
            		ContentResolver cr = context.getContentResolver();
            		InputStream is = null;
            		try {
            			is = cr.openInputStream(Uri.parse(fileUri));
            		} catch (FileNotFoundException e) {
            			Log.e(TAG, e.toString());
            			mBroadcaster.broadcastIntentWithMessage("File not found error: " + e.toString());
            		}
            		//don't try the next bit of code if the file doesn't exist
            		if (is != null) {
            			//write data from fileUri to socket
            			StringBuilder errMsg = new StringBuilder();
            			clientDuration = FileHelper.copyFile(is, stream, errMsg);
            			if (clientDuration == -1 ) {
            				Log.d(TAG, "FTSintentService:(Client) Data write error. ErrMsg: " + errMsg );
            				mBroadcaster.broadcastIntentWithMessage("FTSintentService:(Client)Data write error: " +errMsg);
            			} else {
            				logDuration(clientDuration, logComment);
            				mAppData.setIfFileSent(true);
            				mBroadcaster.broadcastIntentWithMessage("FTSintentService:(Client)File sent! Took this long: " + clientDuration);
            			}
            		} else {
            			Log.e(TAG, "FTSintentService: File to send not opened.");
            		}
            	} else {
            		Log.e(TAG, "FTS intentService: was unable to connect to socket.");
            	}
            	//\todo - consider adding output message to user indicating file doesn't exist
            } catch (IOException e) {
            	mBroadcaster.broadcastIntentWithMessage("FTSintentService:(Client)IOException Error: " + e.getMessage());
                Log.e(TAG, "FTSintentService:(Client)IOException on FileXfer: " + e.getMessage());
            } finally {
                socketCleanup(socket);
            }
        }
    }

	private void doDelay(int connectDelay) {
		Log.d(TAG, "Delaying " + connectDelay);
		try {
			Thread.sleep(connectDelay);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void socketCleanup(Socket socket) {
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
    
    private void logDuration(long duration, String comment) {
   		// log file transfer capture time to wirelessly send file
		String logFileName = "clientTimeLog.txt";
		BufferedWriter out = null;





		if(FileHelper.isExternalStorageWritable()){
			try {
			File dataDir = FileHelper.getDataStorageDir(Constants.DIR_TIMELOGS);

			File file = new File(dataDir, logFileName);
			if(!file.exists()){
				file.createNewFile();
			}

				out = new BufferedWriter(new FileWriter(file, true));
				out.write(comment + ": " + (Long.toString(duration)) + " ns is send file transfer duration.\n");
				Log.d(TAG,comment + ": " + (Long.toString(duration)) + " ns is send file transfer duration.\n");
				out.close();
			} catch (IOException e) {
				Log.d(TAG, "IO Exception writing to log file.");
			}
		} //isExternalStorageWritable    	
    }
}
