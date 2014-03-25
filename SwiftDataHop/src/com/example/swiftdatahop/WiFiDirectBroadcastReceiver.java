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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "BroadcastReceiver";
    private WifiP2pManager manager;
    private Channel channel;
    private TaskListActivity activity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
    		TaskListActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
                Log.d(TAG, "P2P state changed - Enabled : " + state);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
                Log.d(TAG, "P2P state changed (reset data) - Disabled : " + state);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
        	//TODO-maybe: could be better to send this  to activity itself vs to fragment?
            if (manager != null) {
            	PeerListListener listListener = 
            			(PeerListListener) activity.getSupportFragmentManager().findFragmentByTag(Constants.FRAG_SHOWPEERS_NAME);
                manager.requestPeers(channel, listListener);  //kdh - change from orig, hope it works!
            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

            	// we are connected with the other device, request connection
                // info to find group owner IP
            	/*
                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment); 
                */
            	//TODO-maybe: could be better to send this to activity itself vs to fragment?
            	Fragment_PeerDetails fragment = 
            			(Fragment_PeerDetails) activity.getSupportFragmentManager().findFragmentByTag(Constants.FRAG_PEERDETAILS_NAME);
            	manager.requestConnectionInfo(channel, fragment);
                Log.d(TAG, "P2P connection changed - request connection info");
            } else {
                // It's a disconnect
                activity.resetData();
                Log.d(TAG, "P2P connection changed - disconnect - reset data");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            /*DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/
        	//TODO-maybe: could be better to send this  to activity itself vs to fragment?
        	Fragment_ShowPeers fragment = 
        			(Fragment_ShowPeers) activity.getSupportFragmentManager().findFragmentByTag(Constants.FRAG_SHOWPEERS_NAME);
        	if (fragment != null)
        	fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            Log.d(TAG, "P2P connection - this device changed");
        }
    }
}
