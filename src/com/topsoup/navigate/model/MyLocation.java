package com.topsoup.navigate.model;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "history")
public class MyLocation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7286377835473150862L;
	@Column(name = "id", isId = true)
	public int id;
	@Column(name = "name")
	public String name;// 名称
	@Column(name = "msg")
	public String msg;// 消息？
	@Column(name = "lat")
	public double lat;// 经纬度
	@Column(name = "lon")
	public double lon;// 经纬度
	@Column(name = "createtime")
	public long createTime;// 创建时间？
	@Column(name = "finishTime")
	public long finishTime;// 创建时间？

	public final double angle(MyLocation target) {
		double d = 0;
		lat = lat * Math.PI / 180;
		lon = lon * Math.PI / 180;
		target.lat = target.lat * Math.PI / 180;
		target.lon = target.lon * Math.PI / 180;

		d = Math.sin(lat) * Math.sin(target.lat) + Math.cos(lat)
				* Math.cos(target.lat) * Math.cos(target.lon - lon);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(target.lat) * Math.sin(target.lon - lon) / d;
		d = Math.asin(d) * 180 / Math.PI;
		// d = Math.round(d*10000);
		return d;
	}

	public final double angle(SOS target) {
		double d = 0;
		lat = lat * Math.PI / 180;
		lon = lon * Math.PI / 180;
		target.lat = target.lat * Math.PI / 180;
		target.lon = target.lon * Math.PI / 180;

		d = Math.sin(lat) * Math.sin(target.lat) + Math.cos(lat)
				* Math.cos(target.lat) * Math.cos(target.lon - lon);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(target.lat) * Math.sin(target.lon - lon) / d;
		d = Math.asin(d) * 180 / Math.PI;
		// d = Math.round(d*10000);
		return d;
	}
	
	
}
