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

package com.example.swiftdatahop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.swiftdatahop.Fragment_ShowPeers.DeviceActionListener;


/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class Fragment_PeerDetails extends Fragment implements ConnectionInfoListener {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private TaskInfo.TaskItem mItem;	

	public static final int PORT = 8988;
	private static final String TAG = "TaskDetailFragment_PeerDetails";
    //protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    TextView statusText;
    Button sendFile;
    Button receiveFile;
    EditText logComment;
    AppDataManager mAppData = AppDataManager.getInstance();
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        statusText = (TextView) mContentView.findViewById(R.id.status_text);
        sendFile = (Button) mContentView.findViewById(R.id.btn_send_file);
        receiveFile = (Button) mContentView.findViewById(R.id.btn_receive_file);
        logComment = (EditText) mContentView.findViewById(R.id.edit_text_log_comment);
    }
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = TaskInfo.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.d(TAG,"oncreateview attempted");
        mContentView = inflater.inflate(R.layout.tdf_peerdetails, null);
        
		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) mContentView.findViewById(R.id.task_detail))
					.setText(mItem.content);
		}
        
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	Log.d(TAG,"onclick connect attempted");
            	if (device == null) return; //todo - add code to remove connect/disconnect button if no device
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
        
        mContentView.findViewById(R.id.btn_set_upstream_device).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	if (device == null) {
                    		Log.e("TaskDetailFragment_PeerDetails", "Set upstream device failed with null device");
                    		return;
                    	}
                    	WifiP2pDevice downstream = mAppData.getDownStreamDevice();
                    	if(downstream != null) {
                    	    if(downstream.deviceName.equals(device.deviceName)) {
                    		    Log.e("TaskDetailFragment_PeerDetails", "Upstream device chosen, matches downstream device " + downstream.deviceName);
                    		    showToastShort("Warning: upstream device chosen, matches downstream device " + downstream.deviceName);
                    	    }
                    	}
                    	mAppData.setUpStreamDevice(device);
                    	Log.d("TaskDetailFragment_PeerDetails", "Upstream device set " + device.deviceName);
                    	showToastShort("Upstream device set " + device.deviceName);
                    }
                });
        
        mContentView.findViewById(R.id.btn_set_downstream_device).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	if (device == null) {
                    		Log.e("TaskDetailFragment_PeerDetails", "Set downstream device failed with null device.");
                    		return;
                    	} else { 
                    		WifiP2pDevice upStream = mAppData.getUpStreamDevice();
                    		if(upStream != null) {
                    		    if(upStream.deviceName.equals(device.deviceName)) {
                        		    Log.e("TaskDetailFragment_PeerDetails", "Downstream device chosen, matches downstream device " + upStream.deviceName);
                        		    showToastShort("WARNING: Downstream device chosen, matches upStream device " + upStream.deviceName);                  			
                    		    }
                    		}
                            mAppData.setDownStreamDevice(device);
                            showToastShort("Downstream device set.");
                            Log.d("TaskDetailFragment_PeerDetails", "Downstream device set " + device.deviceName);   
                    	}
                    }
                });
     

        
        return mContentView;
    }

	/*
	 * This fx is called when the code is resumed..
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume () {
		super.onResume();
		device = ((TaskListActivity) getActivity()).getSelectedDevice();
		if (device != null)
			showDetails(device);
		//kh - does the below help? not sure.
		if (info!=null)
			onConnectionInfoAvailable(info);
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
		if(FileHelper.isExternalStorageWritable()) { 
			File file = FileHelper.getFileToSend(this);
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
                long duration = copyFile(inputstream, new FileOutputStream(f), errMsg);
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
    


    
	private void showToastShort(String msg) {
		Toast.makeText((TaskListActivity)getActivity(), msg,
		        Toast.LENGTH_SHORT).show();
	}

}
