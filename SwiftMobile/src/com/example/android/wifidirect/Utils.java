package com.example.android.wifidirect;

import java.text.DateFormat;
import java.util.Date;

public class Utils {

	public static String getDateAndTime() {
		return DateFormat.getDateTimeInstance().format(new Date());
	}

}