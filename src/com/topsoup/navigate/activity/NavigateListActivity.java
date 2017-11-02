package com.topsoup.navigate.activity;

import java.util.List;

import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;

@ContentView(R.layout.activity_navigatelist)
public class NavigateListActivity extends BaseActivity implements
		OnItemSelectedListener, OnItemClickListener, OnItemLongClickListener {

	public static final void showHistoryList(BaseActivity activity) {
		activity.startActivity(new Intent(activity, NavigateListActivity.class)
				.putExtra("type", Type.MY));
	}

	public static final void showSOSList(BaseActivity activity) {
		activity.startActivity(new Intent(activity, NavigateListActivity.class)
				.putExtra("type", Type.SOS));
	}

	public static enum Type {
		MY, SOS
	}

	private Type type = Type.MY;

	@ViewInject(R.id.swipe)
	private SwipeRefreshLayout swipeRefreshLayout;

	@ViewInject(R.id.list)
	private ListView mListView;

	private ArrayAdapter<MyLocation> adapter;
	private ArrayAdapter<SOS> sosAdapter;

	private MyLocation selectedLocate;
	private SOS selectedSOS;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		readParamFromIntent();
		mListView.setOnItemSelectedListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		switch (type) {
		case MY:
			showTitle("目的地列表");
			adapter = new ArrayAdapter<MyLocation>(this,
					android.R.layout.simple_list_item_1) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					TextView tv = (TextView) super.getView(position,
							convertView, parent);
					MyLocation location = getItem(position);
					tv.setText(location.name + "\nlat:" + location.lat
							+ "\nlon:" + location.lon);
					return tv;
				}
			};
			mListView.setAdapter(adapter);
			break;
		case SOS:
			showTitle("短信目的列表");
			sosAdapter = new ArrayAdapter<SOS>(this,
					android.R.layout.simple_list_item_1) {
				@Override
				public View getView(int position, View convertView,
						ViewGroup parent) {
					TextView tv = (TextView) super.getView(position,
							convertView, parent);
					SOS sos = getItem(position);
					if (sos.hasLocation()) {
						tv.setText("[定位求助]" + "\n" + sos.getUser() + " 发起求助\n"
								+ "lon:" + sos.getLon() + " lat:"
								+ sos.getLat() + "\n"
								+ sos.getString(sos.getStartTime()));
					} else
						tv.setText("[无定位求助]\n" + sos.getUser() + " 发起求助\n"
								+ sos.getString(sos.getStartTime()));
					return tv;
				}
			};
			mListView.setAdapter(sosAdapter);
			break;
		default:
			break;
		}
		loadData();
		swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		// 设置监听
		swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				loadData();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		loadData();
	}

	private void readParamFromIntent() {
		Intent intent = getIntent();
		if (intent.hasExtra("type"))
			type = (Type) intent.getSerializableExtra("type");
	}

	private void loadData() {
		swipeRefreshLayout.setRefreshing(true);
		switch (type) {
		case MY:
			adapter.clear();
			try {
				List<MyLocation> list = app.getDbWorker().getHistoryList();
				if (list != null)
					adapter.addAll(list);
			} catch (DbException e) {
				e.printStackTrace();
				showToast("刷新失败");
			}
			break;
		case SOS:
			sosAdapter.clear();
			List<SOS> list = app.getSmsWorker().getList();
			if (list != null)
				sosAdapter.addAll(list);
			break;
		default:
			break;
		}
		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (type) {
		case MY:
			selectedLocate = adapter.getItem(position);
			break;
		case SOS:
			selectedSOS = sosAdapter.getItem(position);
			break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (type) {
		case MY:
			LoteInfoActivity.start(this, adapter.getItem(position), false);
			break;
		case SOS:
			LoteInfoActivity.start(this, sosAdapter.getItem(position), false);
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mListView.setSelected(true);
		mListView.setSelection(position);
		switch (type) {
		case MY:
			selectedLocate = adapter.getItem(position);
			break;
		case SOS:
			selectedSOS = sosAdapter.getItem(position);
			break;
		default:
			break;
		}
		showOptions();
		return true;
	}

	@Override
	public String[] buildOptionsMenu() {
		switch (type) {
		case MY:
			return new String[] { "短信发送", "新增", "编辑", "删除", "全部删除" };
		case SOS:
			return new String[] { "领航", "保存到领航" };
		default:
			return null;
		}
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		switch (which) {
		case 0:
			switch (type) {
			case MY:
				if (selectedLocate != null)
					SendLocateActivity.start(this, selectedLocate);
				else
					showToast("请选择要发送的内容");
				break;
			case SOS:
				if (selectedSOS != null)
					NavigateActivity.start(this, selectedSOS);
				else
					showToast("请选择一条内容");
				break;
			}
			break;
		case 1:
			switch (type) {
			case MY:
				LoteInfoActivity.start(this, new MyLocation(), true);
				break;
			case SOS:
				if (selectedSOS != null)
					LoteInfoActivity.start(this, selectedSOS, true);
				else
					showToast("请选择一条内容");
				break;
			default:
				break;
			}
			break;
		case 2:
			if (selectedLocate != null)
				LoteInfoActivity.start(this, selectedLocate, true);
			else
				showToast("请选择要编辑的条目");
			break;
		case 3:
			if (selectedLocate != null) {
				if (app.getDbWorker().removeMyLocateById(selectedLocate.id)) {
					adapter.remove(selectedLocate);
					selectedLocate = null;
				} else
					showToast("删除失败");
			} else {
				showToast("请选择要删除的条目");
			}
			break;
		case 4:
			switch (type) {
			case MY:
				if (app.getDbWorker().cleanHistory()) {
					showToast("清空记录");
				}
				break;
			case SOS:
				if (app.getDbWorker().cleanSOS())
					showToast("清空记录");
				break;
			default:
				break;
			}
			loadData();
			break;
		default:
			break;
		}
	}

	@Event({ R.id.left, R.id.center, R.id.right })
	private void onToolBarClick(View v) {
		switch (v.getId()) {
		case R.id.left:
			switch (type) {
			case MY:
				showOptions();
				break;
			case SOS:
				showOptions();
				break;
			default:
				break;
			}
			break;
		case R.id.center:
			switch (type) {
			case MY:
				LoteInfoActivity.start(this, selectedLocate, false);
				break;
			case SOS:
				LoteInfoActivity.start(this, selectedSOS, false);
				break;
			default:
				break;
			}
			break;
		case R.id.right:
			onBackPressed();
			break;
		default:
			break;
		}
	}
}
