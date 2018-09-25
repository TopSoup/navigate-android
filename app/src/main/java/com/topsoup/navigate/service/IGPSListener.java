package com.topsoup.navigate.service;

import android.location.Location;

public interface IGPSListener {
	public void onStatus(int status);

	public void onLocate(Location last);

	public void onSatelliteCount(int count);
	
	public void onMsg(String msg);
}
