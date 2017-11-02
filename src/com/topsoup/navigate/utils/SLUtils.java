package com.topsoup.navigate.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.annotation.SuppressLint;

@SuppressLint("SimpleDateFormat")
public class SLUtils {
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy年M月d日HH时mm分");// 小写的mm表示的是分钟

	public static final String format(long time) {
		return format(new Date(time));
	}

	public static final String format(Date date) {
		if (date == null)
			return "";
		return sdf.format(date);
	}

	public static final Date parse(String timeStr) throws ParseException {
		return sdf.parse(timeStr);
	}

	public static final String[] dd2dm(Double d) {
		String[] result = new String[2];
		String[] array = d.toString().split("[.]");
		result[0] = array[0];// 得到度

		Double m = Double.parseDouble("0." + array[1]) * 60;
		// String[] array1 = m.toString().split("[.]");
		result[1] = m.toString();// 得到分
		return result;
	}

	/**
	 * 大陆号码或香港号码均可
	 */
	public static boolean isPhoneLegal(String str)
			throws PatternSyntaxException {
		return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
	}

	/**
	 * 大陆手机号码11位数，匹配格式：前三位固定格式+后8位任意数 此方法中前三位格式有： 13+任意数 15+除4的任意数 18+除1和4的任意数
	 * 17+除9的任意数 147
	 */
	public static boolean isChinaPhoneLegal(String str)
			throws PatternSyntaxException {
		String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	/**
	 * 香港手机号码8位数，5|6|8|9开头+7位任意数
	 */
	public static boolean isHKPhoneLegal(String str)
			throws PatternSyntaxException {
		String regExp = "^(5|6|8|9)\\d{7}$";
		Pattern p = Pattern.compile(regExp);
		Matcher m = p.matcher(str);
		return m.matches();
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}
}
