package com.topsoup.navigate.worker;

import java.util.ArrayList;
import java.util.List;

import org.xutils.DbManager;
import org.xutils.DbManager.DaoConfig;
import org.xutils.x;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.ex.DbException;

import android.content.Context;

import com.topsoup.navigate.model.Contact;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;

public class DBWorker {
	private DbManager db;
	private static final DBWorker instance = new DBWorker();

	public static final DBWorker instance() {
		return instance;
	}

	public synchronized void init(Context context) {
		if (db == null) {
			db = x.getDb(new DaoConfig().setDbName("Navigate.db"));
		}
	}

	public List<MyLocation> getHistoryList() throws DbException {
		return db.selector(MyLocation.class).orderBy("createtime", true)
				.findAll();
	}

	public MyLocation getLastHistory() throws DbException {
		return db.selector(MyLocation.class).orderBy("createtime", true)
				.findFirst();
	}

	public boolean removeMyLocateById(int id) {
		try {
			db.deleteById(MyLocation.class, id);
			return true;
		} catch (DbException e) {
			e.printStackTrace();
		}
		return false;
	}

	public List<SOS> getSOSList() throws DbException {
		return db.selector(SOS.class).orderBy("create_time", true).findAll();
	}

	public SOS getLastSOS() throws DbException {
		return db.selector(SOS.class).orderBy("create_time", true).findFirst();
	}

	public boolean addHistory(MyLocation myLocation) throws DbException {
		db.saveOrUpdate(myLocation);
		return true;
	}

	public boolean addSOS(SOS sos) throws DbException {
		db.saveOrUpdate(sos);
		return true;
	}

	public boolean removeSOSById(int id) {

		try {
			db.deleteById(SOS.class, id);
			return true;
		} catch (DbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean cleanSOS() {
		try {
			db.delete(SOS.class);
			return true;
		} catch (DbException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean cleanHistory() {
		try {
			db.delete(MyLocation.class);
			return true;
		} catch (DbException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean addContact(Contact contact) {
		try {
			db.delete(Contact.class,
					WhereBuilder.b("index", "=", contact.getIndex()));
			db.saveOrUpdate(contact);
			return true;
		} catch (DbException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean remove(Contact contact) {
		try {
			db.deleteById(Contact.class, contact.getId());
			return true;
		} catch (DbException e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<Contact> getContactList() {
		try {
			return db.findAll(Contact.class);
		} catch (DbException e) {
			e.printStackTrace();
		}
		return new ArrayList<Contact>();
	}
}
