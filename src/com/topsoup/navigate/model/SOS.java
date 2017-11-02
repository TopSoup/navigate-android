package com.topsoup.navigate.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import com.topsoup.navigate.utils.SLUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("unused")
@SuppressLint({ "SimpleDateFormat", "DefaultLocale" })
@Table(name = "sos")
public class SOS implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6028038264912465571L;
	@Column(name = "id", isId = true)
	public int id;
	@Column(name = "start_time")
	public Date startTime;
	@Column(name = "stop_time")
	public Date stopTime;
	@Column(name = "create_time")
	public Date createTime;
	@Column(name = "lat")
	public double lat;
	@Column(name = "lon")
	public double lon;
	@Column(name = "user")
	public String user;

	public boolean hasLocation = false;

	public SOS() {
	}

	public SOS(String user) {
		this.user = user;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean hasLocation() {
		return hasLocation;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getString(Date date) {
		return SLUtils.format(date);
	}

	public SOS setStartTime(Date startTime) {
		this.startTime = startTime;
		return this;
	}

	public Date getStopTime() {
		return stopTime;
	}

	public SOS setStopTime(Date stopTime) {
		this.stopTime = stopTime;
		return this;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public SOS setCreateTime(Date createTime) {
		this.createTime = createTime;
		return this;
	}

	public double getLat() {
		return lat;
	}

	public SOS setLat(double lat) {
		this.lat = lat;
		this.hasLocation = this.lat > 0;
		return this;
	}

	public double getLon() {
		return lon;
	}

	public SOS setLon(double lon) {
		this.lon = lon;
		this.hasLocation = this.lon > 0;
		return this;
	}

	public String getUser() {
		return user;
	}

	public SOS setUser(String user) {
		this.user = user;
		return this;
	}

	private static final String SMSEXTRA_SOS_HEADER = "求助信息! ";
	private static final String SMSEXTRA_SOS_START = "开启求助";
	private static final String SMSEXTRA_SOS_STOP = "结束求助";
	private static final String SMSEXTRA_SOS_LOCATE = "最后位置:";
	private static final String SMSEXTRA_SOS_LOCATE_LAT = "纬度:E,";
	private static final String SMSEXTRA_SOS_LOCATE_LON = "经度:N,";

	private static final String SOS1 = "求助信息! %s开启求助";
	private static final String SOS2 = "求助信息! 最后位置:%s#纬度:E,%f#经度:N,%f";

	public static SOS parseSMS(String user, String sos) throws ParseException {
		if (sos != null && sos.length() > 0
				&& sos.startsWith(SMSEXTRA_SOS_HEADER)) {// 有效求助信息
			Log.i("SL", "=========开始解析========\n" + sos);
			if (sos.endsWith(SMSEXTRA_SOS_START)) {// 无定位开始求助
				String timeStr = sos.replace(SMSEXTRA_SOS_HEADER, "").replace(
						SMSEXTRA_SOS_START, "");
				System.out.println("无定位开始求助时间：" + timeStr);
				Date time = SLUtils.parse(timeStr);
				return new SOS(user).setCreateTime(new Date()).setStartTime(
						time);
			} else if (sos.indexOf(SMSEXTRA_SOS_LOCATE) > 0) {// 有位置开始求助
				sos = sos.replace(SMSEXTRA_SOS_HEADER, "").replace(
						SMSEXTRA_SOS_LOCATE, "");
				String timeStr = sos.substring(0, sos.indexOf("#"));
				System.out.println("定位求助开始时间：" + timeStr);
				sos = sos.replace(timeStr, "");
				String[] latlon = sos.split("#");
				double lat = 0, lon = 0;
				for (String str : latlon) {
					if (str != null && str.length() > 0) {
						if (str.indexOf("E") > 0) {
							lat = Double.parseDouble(str.replace(
									SMSEXTRA_SOS_LOCATE_LAT, ""));
							System.out.println("拿到纬度了：" + lat);
						} else if (str.indexOf("N") > 0) {
							lon = Double.parseDouble(str.replace(
									SMSEXTRA_SOS_LOCATE_LON, ""));
							System.out.println("拿到经度了：" + lon);
						}
					}
				}
				if (lat > 0 && lon > 0) {
					return new SOS(user).setCreateTime(new Date()).setLat(lat)
							.setLon(lon).setStartTime(SLUtils.parse(timeStr));
				}
			}
		}
		return null;
	}

	public static final String buildSOS(Location location) {
		if (location != null) {
			return buildSOS(location.getLatitude(), location.getLongitude());
		} else {
			return buildSOS(0, 0);
		}
	}

	public static final String buildSOS(MyLocation location) {
		if (location != null) {
			return buildSOS(location.lat, location.lon);
		} else {
			return buildSOS(0, 0);
		}
	}

	/**
	 * 直接调用短信接口发短信
	 * 
	 * @param phoneNumber
	 * @param message
	 */
	public static final void sendSOS(String target, double lat, double lon) {
		if (TextUtils.isEmpty(target))
			return;
		// 获取短信管理器
		android.telephony.SmsManager smsManager = android.telephony.SmsManager
				.getDefault();
		String message = null;
		if (lat == 0 || lon == 0)
			message = SMSEXTRA_SOS_HEADER + SLUtils.format(new Date())
					+ SMSEXTRA_SOS_START;
		else
			message = String.format(SOS2, SLUtils.format(new Date()), lat, lon);
		// 拆分短信内容（手机短信长度限制）
		java.util.List<String> divideContents = smsManager
				.divideMessage(message);
		Log.i("SL", "发送短信条数：" + divideContents.size());

		// 处理返回的接收状态
		String SEND_SMS_ACTION = "SEND_SMS_ACTION";
		String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		// create the deilverIntent parameter
		for (String text : divideContents) {
			Log.i("SL", "发送短信：" + target + "|" + text);
			smsManager.sendTextMessage(target, null, text, null, null);
		}
	}

	public static void sendSOS(String target, String msg) {
		// 获取短信管理器
		android.telephony.SmsManager smsManager = android.telephony.SmsManager
				.getDefault();
		// 拆分短信内容（手机短信长度限制）
		java.util.List<String> divideContents = smsManager.divideMessage(msg);
		Log.i("SL", "发送短信条数：" + divideContents.size());

		// 处理返回的接收状态
		String SEND_SMS_ACTION = "SEND_SMS_ACTION";
		String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		// create the deilverIntent parameter
		for (String text : divideContents) {
			Log.i("SL", "发送短信：" + target + "|" + text);
			smsManager.sendTextMessage(target, null, text, null, null);
		}
	}

	/**
	 * 调起系统发短信功能
	 * 
	 * @param phoneNumber
	 * @param message
	 */
	public void sendSOSBySYS(Activity activity, String phoneNumber, double lat,
			double lon) {
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"
				+ phoneNumber));
		String message = buildSOS(lat, lon);
		if (message != null) {
			intent.putExtra("sms_body", message);
			activity.startActivity(intent);
		}
	}

	public static final String buildSOS(double lat, double lon) {
		String message = null;
		if (lat != 0 && lon != 0) {
			message = String.format(SOS2, SLUtils.format(new Date()), lat, lon);
		} else {
			message = SMSEXTRA_SOS_HEADER + SLUtils.format(new Date())
					+ SMSEXTRA_SOS_START;
		}
		return message;
	}
}
