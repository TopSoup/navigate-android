package com.topsoup.navigate.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.topsoup.navigate.model.SOS;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("unused")
public class SMSReader {
	public static List<SOS> getSmsInPhone(Context context) {
		final String SMS_URI_ALL = "content://sms/";
		final String SMS_URI_INBOX = "content://sms/inbox";
		final String SMS_URI_SEND = "content://sms/sent";
		final String SMS_URI_DRAFT = "content://sms/draft";
		final String SMS_URI_OUTBOX = "content://sms/outbox";
		final String SMS_URI_FAILED = "content://sms/failed";
		final String SMS_URI_QUEUED = "content://sms/queued";

		List<SOS> list = new ArrayList<SOS>();
		try {
			Uri uri = Uri.parse(SMS_URI_INBOX);
			String[] projection = new String[] { "_id", "address", "person",
					"body", "date", "type" };
			Cursor cur = context.getContentResolver().query(uri, projection,
					null, null, "date desc"); // 获取手机内部短信
			if (cur.moveToFirst()) {
				int index_Address = cur.getColumnIndex("address");
				int index_Person = cur.getColumnIndex("person");
				int index_Body = cur.getColumnIndex("body");
				int index_Date = cur.getColumnIndex("date");
				int index_Type = cur.getColumnIndex("type");
				do {
					String strAddress = cur.getString(index_Address);
					int intPerson = cur.getInt(index_Person);
					String strbody = cur.getString(index_Body);
					long longDate = cur.getLong(index_Date);
					int intType = cur.getInt(index_Type);
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd hh:mm:ss");
					Date d = new Date(longDate);
					String strDate = dateFormat.format(d);
					String strType = "";
					if (intType == 1) {
						strType = "接收";
					} else if (intType == 2) {
						strType = "发送";
					} else {
						strType = "null";
					}
					Log.i("SL", strAddress + "|" + strbody);
					try {
						SOS sos = SOS.parseSMS(strAddress, strbody);
						if (sos != null)
							list.add(sos);
						else
							continue;
					} catch (ParseException e) {
						e.printStackTrace();
						continue;
					}
					// smsBuilder.append("[ ");
					// smsBuilder.append(strAddress + ", ");
					// smsBuilder.append(intPerson + ", ");
					// smsBuilder.append(strbody + ", ");
					// smsBuilder.append(strDate + ", ");
					// smsBuilder.append(strType);
					// smsBuilder.append(" ]\n\n");
				} while (cur.moveToNext());
				if (!cur.isClosed()) {
					cur.close();
					cur = null;
				}
			} else {
				// smsBuilder.append("no result!");
			} // end if
				// smsBuilder.append("getSmsInPhone has executed!");
		} catch (SQLiteException ex) {
			Log.d("SQLiteException in getSmsInPhone", ex.getMessage());
		}
		return list;
	}
}
