package com.example.swiftdatahop;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

public class AppDataManager {
	private static String TAG = "AppDataManager";
	private static AppDataManager singleton = null;
	private static boolean firstThread = true;

	//data to store
	private WifiP2pDevice lastSelectedDevice;
	private WifiP2pDevice upStreamDevice;
	private WifiP2pDevice downStreamDevice;

	public WifiP2pDevice getLastSelectedDevice() {
		return lastSelectedDevice;
	}
	public void setLastSelectedDevice(WifiP2pDevice lastSelectedDevice) {
		this.lastSelectedDevice = lastSelectedDevice;
	}
	public WifiP2pDevice getUpStreamDevice() {
		return upStreamDevice;
	}
	public void setUpStreamDevice(WifiP2pDevice upDevice) {
		this.upStreamDevice = upDevice;
	}
	public WifiP2pDevice getDownStreamDevice() {
		return downStreamDevice;
	}
	public void setDownStreamDevice(WifiP2pDevice downDevice) {
		this.downStreamDevice = downDevice;
	}
	
	protected AppDataManager() {
		// Exists only to defeat instantiation.
	}
	public static AppDataManager getInstance() {
		if(singleton == null) {
			simulateRandomActivity();
			singleton = new AppDataManager();
		}
		Log.i(TAG, "created singleton: " + singleton);
		return singleton;
	}
	private static void simulateRandomActivity() {
		try {
			if(firstThread) {
				firstThread = false;
				Log.i(TAG, "sleeping...");
				// This nap should give the second thread enough time
				// to get by the first thread.
				Thread.currentThread().sleep(50);
			}
		}
		catch(InterruptedException ex) {
			Log.w(TAG, "Sleep interrupted");
		}
	}

}
