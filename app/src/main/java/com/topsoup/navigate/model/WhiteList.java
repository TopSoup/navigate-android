package com.topsoup.navigate.model;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "white_list")
public class WhiteList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3830757310246181223L;
	@Column(name = "id", isId = true)
	private String url;

	@Column(name = "type")
	private int type;

}
