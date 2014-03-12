/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.wifidirect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	public static final int PORT = 8988;
	private static final String TAG = "DeviceDetailFrag";
    //protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    TextView statusText;
    Button sendFile;
    Button receiveFile;
    EditText logComment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        statusText = (TextView) mContentView.findViewById(R.id.status_text);
        sendFile = (Button) mContentView.findViewById(R.id.btn_send_file);
        receiveFile = (Button) mContentView.findViewById(R.id.btn_receive_file);
        logComment = (EditText) mContentView.findViewById(R.id.edit_text_log_comment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_send_file).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	if (info != null)
                    		sendFile(info);
                    }
                });
        
        mContentView.findViewById(R.id.btn_receive_file).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	receiveFile();
                    }
                });

        return mContentView;
    }

    //[AR] Need to figure out if this is still needed?
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.e(TAG, "Don't expect onActivityResult to be called");
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
        	sendFile.setVisibility(View.GONE);
        	Log.d(TAG, "group is formed and isGroupOwner true - execute FileServerAsyncTask" 
        			+ " which accepts connection and writes data from stream to file");
        	receiveFile();
        } else if (info.groupFormed) {
        	Log.d(TAG, "group is formed and isGroupOwner false. " 
        			+ "Want to create and send file via FileTransferService");

            // The other device acts as the client. 
        	/*[AR] - Here we get the file we want to transfer and perform
        	 *[AR] - the file transfer
        	 */
        	statusText.setText(getResources().getString(R.string.client_text));
        	sendFile.setVisibility(View.VISIBLE);
        	logComment.setVisibility(View.VISIBLE);
        	receiveFile.setVisibility(View.GONE);
        } else {
        	sendFile.setVisibility(View.GONE);
        	receiveFile.setVisibility(View.GONE);
        	logComment.setVisibility(View.VISIBLE);
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

	private void receiveFile() {
		receiveFile.setVisibility(View.GONE);
		new FileServerAsyncTask(getActivity(), statusText).execute();
		receiveFile.setVisibility(View.VISIBLE);
	}

    /*
     * send file to other device via wifi-d
     */
	private void sendFile(final WifiP2pInfo info) {
		String logcomments = "Default log comment";
		//KH - getFileToSend now writes to external storage, so keep this if
		if(isExternalStorageWritable()) { 
			File file = getFileToSend();
			if (file != null) {		
				Uri uri = Uri.fromFile(file);
				statusText.setText("Sending file: " + uri.toString() 
						+ "\nAt: " + Utils.getDateAndTime() 
						+ "\n of size: " + file.length() + " B");
				Log.d(TAG, "Sending file: " + uri.toString() + "\nAt: " + Utils.getDateAndTime());
				Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
				serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
				serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
						info.groupOwnerAddress.getHostAddress());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, PORT);
				logcomments = logComment.getText().toString();
				serviceIntent.putExtra(FileTransferService.EXTRAS_LOG_COMMENT, logcomments);
				getActivity().startService(serviceIntent);     
			} else {
				statusText.setText("File to send does not exist! ");
				Log.d(TAG, "File to send does not exist! ");
		    }
		} else {
			statusText.setText("Storage is not writeable");
			Log.d(TAG,"Storage is not writeable");
		}
	}

	private File getFileToSend() {
		String dirname = "WiFiDirect_Demo_Dir";
		String filename = "test_data_MASTER.csv";
		File dataDir = getDataStorageDir(dirname);
		File file = new File(dataDir, filename);
		if (file.exists() && file.isFile()) {
			Log.d(TAG, "To send file of size: " + file.length());
		} else {

				InputStream ins = getResources().openRawResource(R.raw.test_data_csv);
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

    /**
     * Updates the UI with device data
     * [AR] Called when a peer device is clicked in the 
     * [AR] list view, makes a menu appear with a connect/disconnect
     * [AR] button for that peer
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream. As in, accepts data from stream and writes that data to a file
     */
    public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

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
                ServerSocket serverSocket = new ServerSocket(PORT);
                Log.d(TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(TAG, "Server: connection done for receive.");
                final File f = new File(getDataStorageDir("WifiDirect_Demo_Dir"), 
                		"test_data." + System.currentTimeMillis() + ".csv");
                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

                Log.d(TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                //[AR] - This call to copy file reads in from the socket and writes to a file
                long duration = copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
                logDuration(duration, f.getName());
                return f.getAbsolutePath();          
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        
        private void logDuration(long duration, String filename) {
       		// log file transfer capture time to receive file
    		String logFileName = "receiveTimeLog.txt";
    		BufferedWriter out = null;
    		if(DeviceDetailFragment.isExternalStorageWritable()){
    			try {
    			File dataDir = DeviceDetailFragment.getDataStorageDir("Timelogs");
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

    public static long copyFile(InputStream inputStream, OutputStream out) {
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
            
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            return duration;
        }
        return duration;
    }
    
    /* [AR] - Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public static File getDataStorageDir(String dataName) {
        // Get the directory for the user's public documents directory. 
        File path = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), dataName);
        if (!path.mkdirs()) {
//[AR] need a log here

        }
        return path;
    }
}
