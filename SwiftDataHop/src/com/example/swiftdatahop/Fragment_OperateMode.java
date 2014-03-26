package com.example.swiftdatahop;

<<<<<<< HEAD:SwiftDataHop/src/com/example/swiftdatahop/Fragment_OperateMode.java
=======
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
>>>>>>> started adding operating mode. created file helper class.:SwiftDataHop/src/com/example/swiftdatahop/TaskDetailFragment_OperateMode.java
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
import com.example.swiftdatahop.R;
import com.example.swiftdatahop.TaskInfo;
import com.example.swiftdatahop.TaskDetailFragment_PeerDetails.FileServerAsyncTask;

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
    private String operateState = Constants.STATE_OFF;
    private Button operateModeOnOff;
    private Button operateSendFile;
    private View mContentView = null;
    private WifiP2pInfo info;
    private final String TAG = "TaskDetailFragment_OperateMode";
    ProgressDialog progressDialog = null;
	public static final int PORT = 8988;

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

	    mContentView.findViewById(R.id.btn_operate_on_off).setOnClickListener(
	            new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
	                	if(operateState == Constants.STATE_OFF) {
	                	    operateState = Constants.STATE_WAITING;
	                	} else {
	                		operateState = Constants.STATE_OFF;
	                	}
	                	showToastShort("State operate: " + operateState);
	                    Log.d(TAG, "Operate on-off clicked");
	                    
	                }
	            });
	    mContentView.findViewById(R.id.btn_operate_send_file).setOnClickListener(
	            new View.OnClickListener() {

	                @Override
	                public void onClick(View v) {
	                	showToastShort("operate send file clicked");
	                    Log.d(TAG, "Operate send file clicked");
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
		if (info!=null)
			onConnectionInfoAvailable(info);

	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;

        // The owner IP is now known.
        //TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        //view.setText(getResources().getString(R.string.group_owner_text)
        //        + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
        //                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        //view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            showToastShort("Group formed and is group owner");
        	Log.d(TAG, "group is formed and isGroupOwner true - execute FileServerAsyncTask" 
        			+ " which accepts connection and writes data from stream to file");
        } else if (info.groupFormed) {
        	showToastShort("Group formed, not group owner");
        	Log.d(TAG, "group is formed and isGroupOwner false. " 
        			+ "Want to create and send file via FileTransferService");

            // The other device acts as the client. 
        	//*[AR] - Here we get the file we want to transfer and perform
        	//*[AR] - the file transfer

        } else {

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
			File file = getFileToSend();
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
	
	private File getFileToSend() {
		String filename = "test_data_MASTER.csv";
		File dataDir = FileHelper.getDataStorageDir(Constants.DIR_WI_FI_DIRECT_DEMO);
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


}
