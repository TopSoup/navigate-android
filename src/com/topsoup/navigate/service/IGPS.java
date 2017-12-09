package com.topsoup.navigate.service;

import android.content.Context;
import android.location.Location;

public interface IGPS {

	public enum DIRECTION {
		north, // 北
		Northeast, // 东北
		east, // 东
		Southeast, // 东南
		south, // 南
		Southwest, // 西南
		west, // 西
		Northwest// 西北
	}

	public IGPS start(Context context, int interval, String tag);

	public IGPS stop(String tag);

	public IGPS setListener(IGPSListener listener);

	public Location last();

	public int getInterval();

	public float angle(Location target);

	public DIRECTION direction(Location target);

	public float distance(Location target);
}
