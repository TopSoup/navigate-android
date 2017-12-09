package com.topsoup.navigate.utils;

import android.content.Context;
import android.os.PowerManager;

public class PowerUtils {
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	private PowerUtils(Context context, int type, String tag) {
		if (pm == null) {
			pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(type, tag);
		}
	}

	public static final PowerUtils makeCpuLock(Context context, String tag) {
		return new PowerUtils(context, PowerManager.PARTIAL_WAKE_LOCK, tag);
	}

	public static final PowerUtils makeScreenLock(Context context, String tag) {
		return new PowerUtils(context,
				PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, tag);
	}

	public static final PowerUtils makeNotifyLock(Context context, String tag) {
		return new PowerUtils(context, PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, tag);
	}

	public PowerUtils acquire() {
		if (wakeLock != null)
			wakeLock.acquire();
		return this;
	}

	public PowerUtils acquire(long time) {
		if (wakeLock != null)
			wakeLock.acquire(time);
		return this;
	}

	public PowerUtils release() {
		if (isHeld())
			wakeLock.release();
		return this;
	}

	public boolean isHeld() {
		if (wakeLock != null)
			return wakeLock.isHeld();
		return false;
	}

}
