package com.example.swiftdatahop;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

public class AppDataManager {
	private static String TAG = "AppDataManager";
	private static AppDataManager singleton = null;
	private static boolean firstThread = true;

	//data to store
	private WifiP2pDevice lastSelectedDevice;

	public WifiP2pDevice getLastSelectedDevice() {
		return lastSelectedDevice;
	}
	public void setLastSelectedDevice(WifiP2pDevice lastSelectedDevice) {
		this.lastSelectedDevice = lastSelectedDevice;
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
