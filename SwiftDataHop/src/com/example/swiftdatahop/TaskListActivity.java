package com.example.swiftdatahop;


import com.example.swiftdatahop.Constants.State;
import com.example.swiftdatahop.Fragment_ShowPeers.DeviceActionListener;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * An activity representing a list of Tasks. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link TaskDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link TaskListFragment} and the item details (if present) is a
 * {@link TaskDetailFragment_Configure_prev}.
 * <p>
 * This activity also implements the required {@link TaskListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class TaskListActivity extends FragmentActivity implements
		TaskListFragment.Callbacks, ChannelListener, DeviceActionListener {

	public static final String TAG = "TaskListActivity";
	//not used presently: private static final String LOG_TAG = null;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    ProgressDialog progressDialog = null;
    
    //other fragments need this data. not sure if this is the best way or not, but it's a test
    AppDataManager mAppData = AppDataManager.getInstance();

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

    /**
     * [AR] - Need to keep
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If exposing deep links into your app, handle intents here.
		//WIFI p2p intents
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //wifi P2P setup
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        
        /*
         * Creates an intent filter for ResponseReceiver that intercepts broadcast Intents
         */
        
        // The filter's action is BROADCAST_ACTION
        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);
        
        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        
        // Instantiates a new DownloadStateReceiver
        ResponseReceiver mResponseReceiver = new ResponseReceiver();
        
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
        		mResponseReceiver,
                statusIntentFilter);
        
		setContentView(R.layout.activity_task_list);

		if (findViewById(R.id.task_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((TaskListFragment) getSupportFragmentManager().findFragmentById(
					R.id.task_list)).setActivateOnItemClick(true);
			
			//kh - let's try initiating the show peers fragment now.. will need it for discover
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			TaskChooser.configTaskFragment(Constants.FRAG_SHOWPEERS_ID, transaction);
			transaction.commit();
		}
        
	}

	/**
	 * Callback method from {@link TaskListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			TaskChooser.configTaskFragment(id, transaction);
			transaction.commit();
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, TaskDetailActivity.class);
			TaskChooser.putExtraOnIntent(id, detailIntent);
			startActivity(detailIntent);
		}
	}
	
	public boolean doDiscovery() {
		if (!isWifiP2pEnabled) {
            Toast.makeText(TaskListActivity.this, R.string.p2p_off_warning,
                    Toast.LENGTH_SHORT).show();
            return true;
        }
		final Fragment_ShowPeers fragment = 
				(Fragment_ShowPeers) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_SHOWPEERS_NAME);
        /* [AR] - shows a popup progress bar */
		if (fragment != null)
			fragment.onInitiateDiscovery(); 
		else 
			showToastShort("config fragment null-fix?");
        /* [AR] - Actual discovery kick off */
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            	showToastShort("Discovery Initiated");
            }

            @Override
            public void onFailure(int reasonCode) {
            	String reason;
            	if (reasonCode == WifiP2pManager.BUSY)
            		reason = new String("BUSY");
            	else if (reasonCode == WifiP2pManager.ERROR)
            		reason = new String("ERROR");
            	else if (reasonCode == WifiP2pManager.P2P_UNSUPPORTED)
            		reason = new String("P2P_UNSUPPORTED");
            	else
            		reason = new String("error unknown");
            	showToastShort("Discovery Failed : " + reason);
            }
        });
        return true;
	}

	private void showToastShort(String msg) {
		Toast.makeText(TaskListActivity.this, msg,
		        Toast.LENGTH_SHORT).show();
	}

    /** register the BroadcastReceiver with the intent values to be matched
     * [AR] - need to keep */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    /* [AR] - need to keep */
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * [AR] - Need to keep for now
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
    	Fragment_ShowPeers fragmentList = 
    			(Fragment_ShowPeers) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_SHOWPEERS_NAME);
    	Fragment_PeerDetails fragmentDetails = 
    			(Fragment_PeerDetails) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_PEERDETAILS_NAME);   	
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }        
    }

    @Override
    /* [AR] - Need to keep */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     * [AR] - Need to keep, options menu to launch system wireless settings if wireless is disabled
     * [AR] - or to kick off discovery, an action listener waits to be notified that peers discovered 
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_discover:
            	Log.d("top", "discover menu button hit");
                return doDiscovery();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    /*[AR] - keep so far, don't know how this gets called yet */
    public void showDetails(WifiP2pDevice device) {
    	//once a user selects a device, set it in master activity as selected
    	setSelectedDevice(device);
    	//if the "Peer details" fragment is available, switch to it or start it up
    	Fragment_PeerDetails fragment = 
    			(Fragment_PeerDetails) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_PEERDETAILS_NAME);
    	//todo - kh - this seems a bit messy. just seeing what works for now though.
    	if (fragment != null)
    		fragment.showDetails(device);
    	else {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			TaskChooser.configTaskFragment(Constants.FRAG_PEERDETAILS_ID, transaction);
			transaction.commit();
    	}
    }

    @Override
    /* [AR] - Need to keep, called from DeviceDetailFragment.java when connect
     * [AR] - button is pressed
     * (non-Javadoc)
     * @see com.example.android.wifidirect.DeviceListFragment.DeviceActionListener#connect(android.net.wifi.p2p.WifiP2pConfig)
     */
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
            	showToastShort("Connect failed. Retry.");
            }
        });
    }

    /* [AR] - Need to keep, called from TaskDetailFragment_PeerDetails.java when disconnect
     * [AR] - button is pressed
     * (non-Javadoc)
     * @see com.example.android.wifidirect.DeviceListFragment.DeviceActionListener#connect(android.net.wifi.p2p.WifiP2pConfig)
     */
    @Override
    public void disconnect() {
        if(mAppData.getOperateState()== State.OFF) {
    	    Fragment_PeerDetails fragment = 
    			(Fragment_PeerDetails) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_PEERDETAILS_NAME);
            fragment.resetViews();
        }
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
            	//TODO - do this another way, or fix errs the way it is below
            	//fragment.getView().findViewById(R.id.btn_receive_file).setVisibility(View.GONE);
            	//fragment.getView().findViewById(R.id.btn_send_file).setVisibility(View.GONE);
            	//fragment.getView().findViewById(R.id.edit_text_log_comment).setVisibility(View.GONE);
                //fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    /* [AR] - Need to keep, not sure what calls this yet. */
    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
        	showToastLong("Channel lost. Trying again");
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
        	showToastLong("Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.");
        }
    }
 
    @Override
    /* [AR] - May not be needed, the only place it appears to be used is commented out. */
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
        	Fragment_ShowPeers fragment = 
        			(Fragment_ShowPeers) getSupportFragmentManager().findFragmentByTag(Constants.FRAG_SHOWPEERS_NAME);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                    	showToastShort("Aborting connection");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                    	showToastShort("Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }
    }
    
    public boolean activityConnectUpstream() {
    	if (mAppData.getUpStreamDevice()==null) {
    		Log.d(TAG,"Upstream device is null, no connect attempt");
    		return false;
    	}
    	Log.d(TAG, "about to connect to upstream device");
		Log.d(TAG,"operate mode connect attempted");
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = mAppData.getUpStreamDevice().deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		if (progressDialog != null && progressDialog.isShowing()) {
		    progressDialog.dismiss();
		}
		progressDialog = ProgressDialog.show(this, "Press back to cancel",
		    "Connecting to :" + mAppData.getUpStreamDevice().deviceAddress, true, true
		    );
		this.connect(config);
		return true;
    }
    
    public WifiP2pDevice getSelectedDevice() {
		return mAppData.getLastSelectedDevice();
	}

	public void setSelectedDevice(WifiP2pDevice selectedDevice) {
		mAppData.setLastSelectedDevice(selectedDevice);
	}
	
    private void showToastLong(String msg) {
		Toast.makeText(TaskListActivity.this, msg,
                   Toast.LENGTH_LONG).show();
	}

	// Broadcast receiver for receiving status updates from the IntentService
    private class ResponseReceiver extends BroadcastReceiver
    {
    	private static final String TAG = "DDFragResponseReceiver";
        // Prevents instantiation
        private ResponseReceiver() {
        }
        
        /**
        *
        * This method is called by the system when a broadcast Intent is matched by this class'
        * intent filters
        *
        * @param context An Android context
        * @param intent The incoming broadcast Intent
        */
       @Override
        public void onReceive(Context context, Intent intent) {
    	   String msg = intent.getStringExtra(Constants.EXTENDED_STATUS_LOG);
    	   Log.d(TAG, "onReceive of ResponseReceiver called: " + msg);
    	   showToastLong(msg);
        	return;
        }
    }
}
