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

package com.example.android.wifidirect;

/**
 *
 * Constants used by multiple classes in this package
 */
public final class Constants {

	private static final String appName = "com.example.android.wifidirect";
	 // Defines a custom Intent action
    public static final String BROADCAST_ACTION = appName + ".BROADCAST";
 // Defines the key for the status "extra" in an Intent
    public static final String EXTENDED_DATA_STATUS = appName + ".STATUS";
 // Defines the key for the log "extra" in an Intent
    public static final String EXTENDED_STATUS_LOG = appName + ".LOG";
}
