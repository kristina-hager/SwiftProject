package com.example.swiftdatahop;

import com.example.swiftdatahop.Constants.State;

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
	private boolean fileSent = false;
	private boolean fileReceived = false;
	
	private State operateState = State.OFF;

	public WifiP2pDevice getLastSelectedDevice() {
		return lastSelectedDevice;
	}
	public void setLastSelectedDevice(WifiP2pDevice lastSelectedDevice) {
		this.lastSelectedDevice = lastSelectedDevice;
	}
	public WifiP2pDevice getUpStreamDevice() {
		return upStreamDevice;
	}
	public boolean getIfFileSent() {
		return fileSent;
	}
	public void setIfFileSent(boolean sent) {
		fileSent = sent;
	}
	public boolean getIfFileReceived() {
		return fileReceived;
	}
	public void setIfFileReceived(boolean rec) {
		fileReceived = rec;
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
	public void setOperateState(Constants.State state) {
		this.operateState = state;
	}
	public State getOperateState() {
		return this.operateState;
	}
	
	protected AppDataManager() {
		// Exists only to defeat instantiation.
	}
	public static AppDataManager getInstance() {
		if(singleton == null) {
			simulateRandomActivity();
			singleton = new AppDataManager();
			Log.i(TAG, "created singleton: " + singleton);
		}
		return singleton;
	}
	private static void simulateRandomActivity() {
		try {
			if(firstThread) {
				firstThread = false;
				Log.i(TAG, "sleeping...");
				// This nap should give the second thread enough time
				// to get by the first thread.
				Thread.sleep(50);
			}
		}
		catch(InterruptedException ex) {
			Log.w(TAG, "Sleep interrupted");
		}
	}

}
