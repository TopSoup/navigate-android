package com.topsoup.navigate.activity;

import org.greenrobot.eventbus.Subscribe;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.TextView;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.R;
import com.topsoup.navigate.base.BaseActivity;
import com.topsoup.navigate.model.MyLocation;
import com.topsoup.navigate.model.SOS;
import com.topsoup.navigate.view.CompassView;

@ContentView(R.layout.activity_navigate)
public class NavigateActivity extends BaseActivity {
	public static final void start(BaseActivity baseActivity, MyLocation locate) {
		baseActivity.startActivity(new Intent(baseActivity,
				NavigateActivity.class).putExtra("locate", locate));
	}

	public static final void start(BaseActivity baseActivity, SOS sos) {
		baseActivity.startActivity(new Intent(baseActivity,
				NavigateActivity.class).putExtra("sos", sos));
	}

	private MyLocation locate;
	private SOS sos;
	private String name;
	private double lat, lon;

	@ViewInject(R.id.name)
	private TextView tvName;
	@ViewInject(R.id.angle)
	private TextView tvAngle;
	@ViewInject(R.id.distance)
	private TextView tvDistance;
	@ViewInject(R.id.swipe)
	private SwipeRefreshLayout swipeRefreshLayout;
	@ViewInject(R.id.compassView)
	private CompassView compassView;

	private String gpsTag;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		findViewById(R.id.toolbar).setVisibility(View.GONE);
		Intent intent = getIntent();
		if (intent.hasExtra("locate")) {
			locate = (MyLocation) intent.getSerializableExtra("locate");
			name = locate.name;
			lat = locate.lat;
			lon = locate.lon;
			app.getGpsWorker().start(this, AppConfig.GPS_minTime,
					gpsTag = "navigate");
		} else if (intent.hasExtra("sos")) {
			sos = (SOS) intent.getSerializableExtra("sos");
			name = sos.user;
			lat = sos.getLat();
			lon = sos.getLon();
			if (sos.hasLocation)
				app.getGpsWorker().start(this, AppConfig.GPS_minTime,
						gpsTag = "sos");
		} else {
			showToast("参数错误");
			finish();
			return;
		}
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
		loadData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		app.getGpsWorker().stop(gpsTag);
	}

	private void loadData() {
		swipeRefreshLayout.setRefreshing(true);
		Location target = new Location("test");
		target.setLatitude(lat);
		target.setLongitude(lon);
		if (sos != null && !sos.hasLocation) {
			showToast("无目标位置信息!");
		}
		float angle = app.getGpsWorker().angle(target);
		float distanceBysys = app.getGpsWorker().distance(target);
		float angleRun = app.getGpsWorker().angleRun();
		tvDistance.setText(String.format("距离：%.2f米", distanceBysys));
		tvDistance.setVisibility(View.GONE);
		tvName.setText(String.format("目标：%s", name));
		tvName.setVisibility(View.GONE);
		showTitle(String.format("目标：%s", name));
		tvAngle.setText(String.format("方位角：%.2f°", angle));
		tvAngle.setVisibility(View.GONE);
		compassView.setDegrees(angle, distanceBysys, angleRun);
		swipeRefreshLayout.setRefreshing(false);
	}

	@Override
	public String[] buildOptionsMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onOptionMenuSelect(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub

	}

	@Subscribe(sticky = true)
	public void onLocate(Location last) {
		loadData();
	}

	@Subscribe()
	public void onSOS(SOS sos) {
		if (sos.hasLocation && sos.user.equals(tvName.getText().toString())) {
			lat = sos.getLat();
			lon = sos.getLon();
			loadData();
		}
	}
}
