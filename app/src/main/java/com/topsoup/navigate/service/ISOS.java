package com.topsoup.navigate.service;

import java.util.List;

import com.topsoup.navigate.model.SOS;

import android.content.Context;
import android.location.Location;

public interface ISOS {
	public void setListener(ISOSListener listener);

	public ISOS start(Context context);

	public void stop();

	public Location getLastLocate();

	public List<SOS> getList();
}
