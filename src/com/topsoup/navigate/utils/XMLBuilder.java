package com.topsoup.navigate.utils;

import android.location.Location;

import com.topsoup.navigate.task.SMSTask;

public class XMLBuilder {
	public static final String build(SMSTask task, Location location) {
		StringBuilder sb = new StringBuilder(
				"<?xml version = \"1.0\"?><ANS VER=\"1.0\"><RESULT>0</RESULT><LIA><APP_ID>");
		sb.append(task.getAPPID_4()).append("</APP_ID><REQ_ID>");
		sb.append(task.getREQID_5()).append("</REQ_ID><MSIDS><MSID>");
		sb.append(PhoneReader.imsi).append(
				"</MSID><MSID_TYPE>2</MSID_TYPE></MSIDS><POSINFOS><POSINFO>");
		if (location != null) {
			sb.append("<POSITIONRESULT>0</POSITIONRESULT><FIXMODE>2</FIXMODE><FIXTIME>");
			sb.append(location.getTime()).append("</FIXTIME><LATITUDETYPE>");
			sb.append(location.getLatitude() > 0 ? 1 : 0)
					.append("</LATITUDETYPE><LATITUDE>")
					.append(location.getLatitude())
					.append("</LATITUDE><LONGITUDETYPE>");
			sb.append(location.getLongitude() > 0 ? 1 : 0)
					.append("</LONGITUDETYPE><LONGITUDE>")
					.append(location.getLongitude())
					.append("</LONGITUDE><ALTITUDE>");
			sb.append(location.getAltitude()).append(" </ALTITUDE><DIRECTION>");
			sb.append(location.getBearing()).append("</DIRECTION><VELOCITY>");
			sb.append(location.getSpeed()).append("</VELOCITY><PRECISION>");
			sb.append(location.getAccuracy()).append("</PRECISION>");
		} else {
			sb.append("<POSITIONRESULT>2</POSITIONRESULT><FIXMODE>2</FIXMODE>");
			sb.append("<FIXTIME>").append(System.currentTimeMillis())
					.append("</FIXTIME>");
		}
		sb.append("</POSINFO></POSINFOS></LIA></ANS>");
		return sb.toString();
	}
}
