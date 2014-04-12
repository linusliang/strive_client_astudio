package com.drivestrive.Activities;

import receiver.LockReceiver;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockService extends Service {
	BroadcastReceiver mReceiver;

	// Intent myIntent;
	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: IBinder
	 * @date: Mar 26,2014
	 * @param: Intent
	 */
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onCreate
	 * @date: Mar 26,2014
	 * @param: null
	 * @description : Called when the service is created..
	 */
	
	public void onCreate() {
		KeyguardManager.KeyguardLock k1;
//
		KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		k1 = km.newKeyguardLock("IN");
		k1.disableKeyguard();

		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);

		mReceiver = new LockReceiver();
		registerReceiver(mReceiver, filter);

		super.onCreate();

	}

	@SuppressWarnings("deprecation") 
	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onStart
	 * @date: Mar 26,2014
	 * @param: Intent,Int
	 * @description : Called when the service is Start..
	 */
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub

		super.onStart(intent, startId);
	}

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onDestroy
	 * @date: Mar 26,2014
	 * @param: null
	 * @description : Called when the service is destroyed..
	 */
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
