package com.topsoup.navigate.worker;

import java.util.Iterator;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.model.Satellite;
import com.topsoup.navigate.service.IGPS;
import com.topsoup.navigate.service.IGPSListener;
import com.topsoup.navigate.service.ILOG;

public class GPSWorker implements IGPS, ILOG, LocationListener, Listener {
	private static final String TAG = "SL-GPSWorker";
	private Context mContext;
	private LocationManager mLocationManager;
	private Location last;
	private IGPSListener listener;
	private int interval;

	@Override
	public IGPS setListener(IGPSListener listener) {
		this.listener = listener;
		return this;
	}

	public IGPS init(Context context) {
		if (mContext == null) {
			mContext = context.getApplicationContext();
			mLocationManager = (LocationManager) mContext
					.getSystemService(Context.LOCATION_SERVICE);
//			last = mLocationManager
//					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			logi("初始化完成");
		}
		return this;
	}

	@Override
	public IGPS start(Context context, int interval) {
		if (mLocationManager == null) {
			init(context);
		}
		if (mLocationManager != null) {
			mLocationManager.addGpsStatusListener(this);
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, AppConfig.GPS_minTime,
					AppConfig.GPS_minDistance, this);
			last = mLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (last != null)
				EventBus.getDefault().postSticky(last);
		}
		return this;
	}

	@Override
	public IGPS stop() {
		if (mLocationManager != null) {
			mLocationManager.removeGpsStatusListener(this);
			mLocationManager.removeUpdates(this);
		}
		return this;
	}

	@Override
	public Location last() {
		return last;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public DIRECTION direction(Location target) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float distance(Location target) {
		if (last == null || target == null
				|| (target.getLatitude() == 0 && target.getLongitude() == 0))
			return 0;
		return last.distanceTo(target);
	}

	@Override
	public float angle(Location target) {
		if (last == null || target == null
				|| (target.getLatitude() == 0 && target.getLongitude() == 0))
			return 0;
		float result = last.bearingTo(target);
		while (result > 360 || result < 0) {
			if (result > 360)
				result -= 360;
			else if (result < 0)
				result += 360;
		}
		return result;
	}

	public float distanceBySys1(Location target) {
		if (last == null)
			return -999;
		return last.distanceTo(last);
	}

	public double distance(double lon, double lat) {
		if (last == null)
			return -999;
		return gpsDistance(last.getLatitude(), last.getLongitude(), lat, lon);
	}

	@Override
	public void onLocationChanged(Location location) {
		last = location;
		if (listener != null)
			listener.onLocate(last);
		EventBus.getDefault().postSticky(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		switch (status) {
		// GPS状态为可见时
		case LocationProvider.AVAILABLE:
			logi("当前GPS状态为可见状态");
			break;
		// GPS状态为服务区外时
		case LocationProvider.OUT_OF_SERVICE:
			logi("当前GPS状态为服务区外状态");
			break;
		// GPS状态为暂停服务时
		case LocationProvider.TEMPORARILY_UNAVAILABLE:
			logi("当前GPS状态为暂停服务状态");
			break;
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (listener != null) {
			listener.onMsg("Provider 可用：" + provider);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (listener != null) {
			listener.onMsg("Provider 不可用：" + provider);
		}
	}

	@Override
	public void onGpsStatusChanged(int event) {
		fetchCurGpsStatus(mLocationManager.getGpsStatus(null));
	}

	@Override
	public void logi(String msg) {
		Log.i(TAG, msg);
		printStatusMsg(msg);
	}

	@Override
	public void logd(String msg) {
		Log.d(TAG, msg);
	}

	@Override
	public void loge(String msg) {
		Log.e(TAG, msg);
	}

	/**
	 * 
	 * 计算两地之间的距离（给定经纬度）
	 * 
	 * @param lat1
	 *            出发地经度
	 * @param lng1
	 *            出发地纬度
	 * @param lat2
	 *            目的地经度
	 * @param lng2
	 *            目的地纬度
	 * @return double 两点之间的距离
	 */
	private double gpsDistance(double lat1, double lng1, double lat2,
			double lng2) {
		double distance = 0;
		double lonRes = 102900, latRes = 110000;
		distance = Math.sqrt(Math.abs(lat1 - lat2) * latRes
				* Math.abs(lat1 - lat2) * latRes + Math.abs(lng1 - lng2)
				* lonRes * Math.abs(lng1 - lng2) * lonRes);
		// System.out.println( "两点间距离:" + distance );
		return distance;
	}

	private void fetchCurGpsStatus(GpsStatus mStatus) {
		// 获取卫星颗数的默认最大值
		int maxSatellites = mStatus.getMaxSatellites();
		// 创建一个迭代器保存所有卫星
		Iterator<GpsSatellite> iters = mStatus.getSatellites().iterator();
		// 卫星数
		int count = 0;
		if (iters != null) {
			while (iters.hasNext() && count <= maxSatellites) {
				GpsSatellite s = iters.next();
				if (s.usedInFix()) {
					count++;
				}
			}
		}
		if (count < 3) {
			// 定位失败
		} else {
			// 定位成功
		}
		if (listener != null)
			listener.onSatelliteCount(count);
		EventBus.getDefault().postSticky(new Satellite(count));
	}

	private void printStatusMsg(String msg) {
		if (listener != null)
			listener.onMsg(msg);
	}
}
