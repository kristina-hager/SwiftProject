package com.example.swiftdatahop;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.swiftdatahop.AppDataManager;
import com.example.swiftdatahop.Constants.State;
import com.example.swiftdatahop.R;
import com.example.swiftdatahop.TaskInfo;
import com.example.swiftdatahop.Fragment_ShowPeers.DeviceActionListener;

public class Fragment_OperateMode extends Fragment implements ConnectionInfoListener {

	
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private TaskInfo.TaskItem mItem;
    private AppDataManager mAppData = AppDataManager.getInstance();
    private Button operateModeOnOff;
    private Button operateSendFile;
    private View mContentView = null;
    private WifiP2pInfo info;
    private final String TAG = "TaskDetailFragment_OperateMode";
    ProgressDialog progressDialog = null;
	public static final int PORT = 8988;
	TextView statusBox;
	

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 * @return 
	 */
	public Fragment_OperateMode() {
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        operateModeOnOff = (Button) mContentView.findViewById(R.id.btn_operate_on_off);
        operateSendFile = (Button) mContentView.findViewById(R.id.btn_operate_send_file);
        statusBox = ((TextView) mContentView.findViewById(R.id.operate_status));
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.tdf_operatemode, null);
        
		// Show the dummy content as text in a TextView.
		if (mItem != null) {
			((TextView) mContentView.findViewById(R.id.task_detail))
					.setText(mItem.content);
		}

		updateStatusText("My state: " + Constants.getOperateStateString(mAppData.getOperateState()));
		
	    mContentView.findViewById(R.id.btn_operate_on_off).setOnClickListener(
	            new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
	                	if(mAppData.getOperateState() == State.OFF) {
	                		//todo: check that upstream or downstream device is set
	                		//(check we are good to go for autonomous)
	                		//if downstream device is null, show 'send file' button
	                		mAppData.setOperateState(State.IDLE_WAIT);
	                	} else if (mAppData.getOperateState() == State.RECEIVE_FILE) {
	                		//todo: abort, disconnect, etc
	                	} else if (mAppData.getOperateState() == State.SEND_FILE) {
	                		//todo: abort, disconnect etc
	                		//if there is some connection, do a disconnect?
	                        //((DeviceActionListener) getActivity()).disconnect();
	                	} else if (mAppData.getOperateState() == State.IDLE_WAIT) {                		
	                		mAppData.setOperateState(State.OFF);
	                	} else {
	                		assert(false);
	                	}	
	                	updateStatusText("My state: " + Constants.getOperateStateString(mAppData.getOperateState()));
	                    Log.d(TAG, "Operate on-off clicked");                
	                }
	            });
	    mContentView.findViewById(R.id.btn_operate_send_file).setOnClickListener(
	            new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
	                	showToastShort("operate send file clicked");
	                    Log.d(TAG, "Operate send file clicked");
	                    //we know: downstream device is null
	                    //upstream device is not null
	                    assert(mAppData.getDownStreamDevice() == null);
	                    assert(mAppData.getDownStreamDevice() != null);
	                    mAppData.setOperateState(State.SEND_FILE);
	                    /*bool success =*/ connectToUpstream();   //make wifi direct connection
	                    //todo: updateStatusText and Log.d - at each step
	                    
	                }
	            });

		return mContentView;
	}
	@Override
	public void onResume () {
		super.onResume();
		//device = ((TaskListActivity) getActivity()).getSelectedDevice();
		//if (device != null)
		//	showDetails(device);
		//kh - does the below help? not sure.
		updateStatusText("My state: "+ Constants.getOperateStateString(mAppData.getOperateState()));
		if (info!=null)
			onConnectionInfoAvailable(info);

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;

        if (info.groupFormed) {
        	if (info.isGroupOwner) {
        		showToastShort("Group formed and is group owner");
            	Log.d(TAG, "group is formed and isGroupOwner true");
        	} else {
        		showToastShort("Group formed, not group owner");
            	Log.d(TAG, "group is formed and isGroupOwner false. ");
        	}
        		
        	//if I am in State.SEND_FILE
        		//todo: wait?
        		//todo: open socket to send file
        		//todo: actually send file
        		//if all successful
        		//mAppData.setOperateState(State.IDLE_WAIT);
        	//else if I am receiving device, in IDLE_WAIT
        		//mAppData.setOperateState(State.RECEIVE_FILE);
        		//open socket to receive file "receiveFile()
        		//if success, disconnect
        		//if you have upstream device, go into 'send file' mode (open wifi group w/ upstream (, open socket, sendfile))
        			//mAppData.setOperateState(State.SEND_FILE);
        			//connectToUpstream
        		//else
        			//mAppData.setOperateState(State.IDLE_WAIT);
        			//(display message about file receive if not already done)
        	
        } else {
        	//todo? error?
        }
        
	}

	private void receiveFile() {
		Log.d(TAG, "Recieve File Called");
		showToastShort("Recieve File Called");
//		new FileServerAsyncTask(getActivity(), statusText).execute(); [AR] Need to add a text field
	}
	
    /*
     * send file to other device via wifi-d
     */
	private void sendFile(final WifiP2pInfo info) {
		String logcomments = "Default log comment";
		//KH - getFileToSend now writes to external storage, so keep this if
		if(isExternalStorageWritable()) { 
			File file = FileHelper.getFileToSend(this);
			if (file != null) {		
				Uri uri = Uri.fromFile(file);
				showToastShort("Sending file: " + uri.toString() 
						+ "\nAt: " + Utils.getDateAndTime() 
						+ "\n of size: " + file.length() + " B");
				Log.d(TAG,"Sending file: " + uri.toString() 
						+ "\nAt: " + Utils.getDateAndTime() 
						+ "\n of size: " + file.length() + " B");
				Log.d(TAG, "Sending file: " + uri.toString() + "\nAt: " + Utils.getDateAndTime());
				Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
				serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
				serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
						info.groupOwnerAddress.getHostAddress());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, PORT);
				serviceIntent.putExtra(FileTransferService.EXTRAS_LOG_COMMENT, logcomments);
				getActivity().startService(serviceIntent);     
			} else {
				//statusText.setText("File to send does not exist! "); [AR] probably need to get rid of or add a text field
				Log.d(TAG, "File to send does not exist! ");
		    }
		} else {
			//statusText.setText("Storage is not writeable"); [AR] Delete? or add a text field
			Log.d(TAG,"Storage is not writeable");
		}
	}
	

	
	private void showToastShort(String msg) {
		Toast.makeText((TaskListActivity)getActivity(), msg,
		        Toast.LENGTH_SHORT).show();
	}
	
    /* [AR] - Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private void updateStatusText(String status) {
    	if (statusBox!= null)
    		statusBox.setText(status);
    }

	private void connectToUpstream() {
		if (mAppData.getUpStreamDevice()==null) {
			showToastShort("Upstream device is null!");
			return;
		}
			
		Log.d(TAG,"operate mode connect attempted");
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = mAppData.getUpStreamDevice().deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		if (progressDialog != null && progressDialog.isShowing()) {
		    progressDialog.dismiss();
		}
		progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
		    "Connecting to :" + mAppData.getUpStreamDevice().deviceAddress, true, true
		    );
		((DeviceActionListener) getActivity()).connect(config);
	}

}
