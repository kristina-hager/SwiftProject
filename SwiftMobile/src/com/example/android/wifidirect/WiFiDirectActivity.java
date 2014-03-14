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

import com.example.android.wifidirect.DeviceListFragment.DeviceActionListener;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements ChannelListener, DeviceActionListener {

    public static final String TAG = "wifidirectdemo";
	//not used presently: private static final String LOG_TAG = null;
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    /**
     * [AR] - Need to keep
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /* [AR] - Need to keep */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // add necessary intent values to be matched.

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

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
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
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
            /*[AR] - going to comment out because the wireless settings in
             * the action menu that is launched is not the right one to actually
             * enable wireless or wifidirect. Maybe we can figure out the right menu later?
             */
            /*case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.

                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;*/

            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.frag_list);
                /* [AR] - shows a popup progress bar */
                fragment.onInitiateDiscovery();
                /* [AR] - Actual discovery kick off */
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    /*[AR] - keep so far, don't know how this gets called yet */
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);

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
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* [AR] - Need to keep, called from DeviceDetailFragment.java when disconnect
     * [AR] - button is pressed
     * (non-Javadoc)
     * @see com.example.android.wifidirect.DeviceListFragment.DeviceActionListener#connect(android.net.wifi.p2p.WifiP2pConfig)
     */
    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
            	fragment.getView().findViewById(R.id.btn_receive_file).setVisibility(View.GONE);
            	fragment.getView().findViewById(R.id.btn_send_file).setVisibility(View.GONE);
            	fragment.getView().findViewById(R.id.edit_text_log_comment).setVisibility(View.GONE);
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }

    /* [AR] - Need to keep, not sure what calls this yet. */
    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
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
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                manager.cancelConnect(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
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
    	   Toast.makeText(WiFiDirectActivity.this, msg,
                   Toast.LENGTH_LONG).show();
        	return;
        }
    }
    
    
}
