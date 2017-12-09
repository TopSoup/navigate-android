package com.topsoup.navigate.worker;

import android.app.AlertDialog;

import com.topsoup.navigate.AppConfig;
import com.topsoup.navigate.MainActivity;
import com.topsoup.navigate.task.SMSTask;

public class TaskWorker {
	private static final TaskWorker instance = new TaskWorker();

	private TaskWorker() {
	}

	public static final TaskWorker instance() {
		return instance;
	}

	public void handle(SMSTask task) {
		if (task != null) {
			// TODO 1 保存到DB
			// TODO 2 检测授权,也是从数据库中检查
			// TODO 2.1 提醒用户授权,弹提示框
			// TODO 2.2 授权通过
			// TODO 3 执行任务
			// TODO 4 更新数据库
		}
	}

	private void showDialog(SMSTask task) {
		
		AlertDialog alertDialog = new AlertDialog.Builder(null).create();
	}
}
