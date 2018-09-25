package com.topsoup.navigate.model;

import java.io.Serializable;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "contact")
public class Contact implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5125757535999251712L;
	@Column(name = "id", isId = true)
	public int id;

	@Column(name = "index")
	public int index;
	@Column(name = "name")
	public String name;
	@Column(name = "phone")
	public String phoneNumber;

	public Contact() {
	}

	public Contact(String name, String phoneNumber) {
		this.name = name;
		this.phoneNumber = phoneNumber;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
