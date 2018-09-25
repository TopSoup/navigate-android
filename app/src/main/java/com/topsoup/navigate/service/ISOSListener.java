package com.topsoup.navigate.service;

import android.location.Location;

public interface ISOSListener {
	public void onSOS(Location location, String msg);
}
