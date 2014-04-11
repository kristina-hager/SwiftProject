/*
 * Copyright (C) 2012 The Android Open Source Project
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

/**
 *
 * Constants used by multiple classes in this package
 */
public final class Constants {

	private static final String appName = "com.example.swiftdatahop";
	 // Defines a custom Intent action
    public static final String BROADCAST_ACTION = appName + ".BROADCAST";
 // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = appName + ".STATUS";
 // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = appName + ".LOG";
	//
	public static final int PORT = 8988;//todo: use this constant instead of Local PORT
	public static final int SEND_WAIT = 4000;
	public static final int SEND_RETRY = 3; //not implemented yet
    
    //fragment related constants
	public static final String FRAG_PEERDETAILS_NAME = "PEER";
	public static final String FRAG_SHOWPEERS_NAME = "SHOW";
	public static final String FRAG_MOREINFO_NAME = "MORE";
	public static final String FRAG_OPERATEMODE_NAME = "OPERATE";
    public static final String FRAG_OPERATEMODE_ID = "4";
	public static final String FRAG_MOREINFO_ID = "3";
	public static final String FRAG_PEERDETAILS_ID = "2";
	public static final String FRAG_SHOWPEERS_ID = "1";
	

	
	//data storage related
	public static final String DIR_TIMELOGS = "Timelogs";
	public static final String DIR_WI_FI_DIRECT_DEMO = "WiFiDirect_Demo_Dir";
	
	public enum State {
		SEND_FILE, IDLE_WAIT, RECEIVE_FILE, OFF
	}
	public static final String getOperateStateString(State inState) {
		if (inState==null)
			return "NULL";
		switch(inState) {
			case SEND_FILE: return "SEND_FILE";
			case IDLE_WAIT: return "WAITING/IDLE";
			case RECEIVE_FILE: return "RECEIVE_FILE";
			case OFF: return "OFF";
			default: return "UNKNOWN";
		}
	}

	
}
