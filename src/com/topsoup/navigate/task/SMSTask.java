package com.topsoup.navigate.task;

import com.topsoup.navigate.utils.XMLBuilder;
import com.topsoup.navigate.web.Repoart;

import android.location.Location;
import android.text.TextUtils;

public class SMSTask {

	/*
	 * 单次定位：
	 * //BREW:0109c274:V01#http://10.10.10.150:80/locHandle#LBS#sample#0#1#4#0#0
	 * 
	 * 周期定位：定位次数为10次，间隔为120秒，每定位1次上报1次数据
	 * //BREW:0109c274:V01#http://brew.tdtchina
	 * .cn:18851#tdt#tdt#FFFFF#2#4#0#0#120#10#1
	 * 
	 * 取消定位： //BREW:0109c274:V01#http://10.10.10.150:80/locHandle#LBS#sample#0#0
	 * 
	 * 开机自启动单次定位：
	 * //BREW:0109c274:V01#http://10.10.10.150:80/locHandle#LBS#sample
	 * #0#3#4#0#0#0#1#1
	 * 
	 * 取消开机自启动单次定位：
	 * //BREW:0109c274:V01#http://10.10.10.150:80/locHandle#LBS#sample#0#5
	 * 
	 * 开机自启动周期定位：定位次数为10次，间隔为120秒，每定位1次上报1次数据
	 * //BREW:0109c274:V01#http://10.10.10
	 * .150:80/locHandle#LBS#sample#0#3#4#0#0#120#10#1
	 * 
	 * 取消开机自启动周期定位：
	 * //BREW:0109c274:V01#http://10.10.10.150:80/locHandle#LBS#sample#0#4
	 */

	public static final String PREFIX_CHECK = "//BREW:0109c274:V01";
	/**
	 * 短信前缀://BREW:0109c274:V01
	 */
	public String PREFIX_1 = "";
	/**
	 * URL:使用该 URL,通过 HTTP POST 返回定位结果
	 */
	private String SPURL_2 = "";
	/**
	 * SP 标识:字符串,SPURL+SPID 为隐私鉴权唯一关键字
	 */
	private String SPID_3 = "";
	/**
	 * 应用标识:字符串,同一个 APPID 只能有一个跟踪定位、一个启动跟踪定位、一个启动单次定位
	 */
	private String APPID_4 = "";
	/**
	 * 请求标识:字符串,唯一标识一个定位请求
	 */
	private String REQID_5 = "";
	/**
	 * 定位类型:整数,0-取消单次定位或跟踪定位;1-单次定位; 2-跟踪定位;3-启动定位;4-取消启动跟踪定位; 5-取消启动单次定位
	 */
	private String FIXTYPE_6 = "";
	/**
	 * 定位模式:整数 0-MSA 1 或 7-Google 2-GPS 3-先 MSA 再 GPS 4 或 9-先 GPS 再 Google 5-先
	 * GPS 再 MSA 6-先 GPS,再 MSA,再 Google 11-Hybrid(待完善)
	 */
	private String FIXMODE_7 = "";
	/**
	 * 是否执行定位操作:整数,0-执行定位操作并取定位结果;1-返回最 近一次定位结果,只对单次立即 GPS 定位有效
	 */
	private String ISPOSITION_8 = "";
	/**
	 * 开始时间:数字字符串,格式为 HHMMSS,0 表示立即
	 */
	private String BEGINTIME_9 = "";

	/**
	 * 定位间隔:整数,0-65535,单位秒
	 */
	private String INTERVAL_10 = "";

	/**
	 * 定位次数:整数,0-65535,单位次,0 表示不限次数
	 */
	private String TIMES_11 = "";
	/**
	 * 上报间隔:整数,1-1024,单位次,一般取值为 1(未处理)
	 */
	private String COUNT_12 = "";

	/**
	 * 创建时间:收到短信的时间
	 */
	private long createTime;

	/**
	 * 结束时间:任务结束时间
	 */
	private long overTime;

	public SMSTask(String PREFIX, String SPURL, String SPID, String APPID,
			String REQID, String FIXTYPE) {
		this.PREFIX_1 = PREFIX;
		this.SPURL_2 = SPURL;
		this.SPID_3 = SPID;
		this.APPID_4 = APPID;
		this.REQID_5 = REQID;
		this.FIXTYPE_6 = FIXTYPE;
	}

	public SMSTask(String PREFIX, String SPURL, String SPID, String APPID,
			String REQID, String FIXTYPE, String FIXMODE, String ISPOSITION,
			String BEGINTIME, String INTERVAL, String TIMES, String COUNT) {
		this.PREFIX_1 = PREFIX;
		this.SPURL_2 = SPURL;
		this.SPID_3 = SPID;
		this.APPID_4 = APPID;
		this.REQID_5 = REQID;
		this.FIXTYPE_6 = FIXTYPE;
		this.FIXMODE_7 = FIXMODE;
		this.ISPOSITION_8 = ISPOSITION;
		this.BEGINTIME_9 = BEGINTIME;
		this.INTERVAL_10 = INTERVAL;
		this.TIMES_11 = TIMES;
		this.COUNT_12 = COUNT;
	}

	public static final boolean isTrue(String sms) {
		return !TextUtils.isEmpty(sms) && sms.startsWith(PREFIX_CHECK);
	}

	public static final SMSTask paste(String sms) {
		if (isTrue(sms)) {
			String[] params = TextUtils.split(sms, "#");
			if (params != null) {
				switch (params.length) {
				case 6:
					return new SMSTask(params[0], params[1], params[2],
							params[3], params[4], params[5]);
				case 12:
					return new SMSTask(params[0], params[1], params[2],
							params[3], params[4], params[5], params[6],
							params[7], params[8], params[9], params[10],
							params[11]);
				default:
					return null;
				}

			}
		}
		return null;
	}

	public SMSTask report(Location location) {
		String xml = XMLBuilder.build(this, location);
		Repoart.run(getSPURL_2(), xml);
		return this;
	}

	public String getPREFIX_1() {
		return PREFIX_1;
	}

	public void setPREFIX_1(String pREFIX_1) {
		PREFIX_1 = pREFIX_1;
	}

	public String getSPURL_2() {
		return SPURL_2;
	}

	public void setSPURL_2(String sPURL_2) {
		SPURL_2 = sPURL_2;
	}

	public String getSPID_3() {
		return SPID_3;
	}

	public void setSPID_3(String sPID_3) {
		SPID_3 = sPID_3;
	}

	public String getAPPID_4() {
		return APPID_4;
	}

	public void setAPPID_4(String aPPID_4) {
		APPID_4 = aPPID_4;
	}

	public String getREQID_5() {
		return REQID_5;
	}

	public void setREQID_5(String rEQID_5) {
		REQID_5 = rEQID_5;
	}

	public String getFIXTYPE_6() {
		return FIXTYPE_6;
	}

	public void setFIXTYPE_6(String fIXTYPE_6) {
		FIXTYPE_6 = fIXTYPE_6;
	}

	public String getFIXMODE_7() {
		return FIXMODE_7;
	}

	public void setFIXMODE_7(String fIXMODE_7) {
		FIXMODE_7 = fIXMODE_7;
	}

	public String getISPOSITION_8() {
		return ISPOSITION_8;
	}

	public void setISPOSITION_8(String iSPOSITION_8) {
		ISPOSITION_8 = iSPOSITION_8;
	}

	public String getBEGINTIME_9() {
		return BEGINTIME_9;
	}

	public void setBEGINTIME_9(String bEGINTIME_9) {
		BEGINTIME_9 = bEGINTIME_9;
	}

	public String getINTERVAL_10() {
		return INTERVAL_10;
	}

	public void setINTERVAL_10(String iNTERVAL_10) {
		INTERVAL_10 = iNTERVAL_10;
	}

	public String getTIMES_11() {
		return TIMES_11;
	}

	public void setTIMES_11(String tIMES_11) {
		TIMES_11 = tIMES_11;
	}

	public String getCOUNT_12() {
		return COUNT_12;
	}

	public void setCOUNT_12(String cOUNT_12) {
		COUNT_12 = cOUNT_12;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public static String getPrefixCheck() {
		return PREFIX_CHECK;
	}
}
