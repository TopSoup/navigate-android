package com.topsoup.navigate.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneReader {
	private static TelephonyManager geTelephonyManager(Context context) {
		return (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * 获取IMEI号
	 * 
	 * @param context
	 * @return
	 */
	public static final String getIMEI(Context context) {
		return geTelephonyManager(context).getDeviceId();
	}

	/**
	 * 获取IMSI号
	 * 
	 * @param context
	 * @return
	 */
	public static final String getIMSI(Context context) {
		return geTelephonyManager(context).getSubscriberId();
	}

	/**
	 * 获取MEID号
	 * 
	 * @param context
	 * @return
	 */
	public static final String getMEID(Context context) {
		return getIMEI(context);
	}

	/**
	 * 获取手机号码
	 * 
	 * @param context
	 * @return
	 */
	public static final String getPhoneNumber(Context context) {
		return geTelephonyManager(context).getLine1Number();
	}

	/**
	 * 获取手机信号强度
	 * 
	 * @param context
	 * @return
	 */
	public static final String getPhoneSignal(Context context) {
		return null;
	}

	/**
	 * 获取运营商
	 * 
	 * @param context
	 * @return
	 */
	public static final String getPhoneOperator(Context context) {
		return geTelephonyManager(context).getSimOperatorName();
	}

	public static final void printInfo(Context context) {
		log("getDeviceId:" + geTelephonyManager(context).getDeviceId());
		log("getLine1Number:" + geTelephonyManager(context).getLine1Number());
		log("getSimOperator:" + geTelephonyManager(context).getSimOperator());
		log("getSimOperatorName:"
				+ geTelephonyManager(context).getSimOperatorName());
		log("getSimSerialNumber:"
				+ geTelephonyManager(context).getSimSerialNumber());
		log("getSimState:" + geTelephonyManager(context).getSimState());
		log("getSubscriberId:" + geTelephonyManager(context).getSubscriberId());
	}

	private static final void log(String msg) {
		Log.i("SL", msg);
	}
}
