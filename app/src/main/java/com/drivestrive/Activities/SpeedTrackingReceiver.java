package com.drivestrive.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SpeedTrackingReceiver extends BroadcastReceiver {
	/**
	 * @see android.content.BroadcastReceiver#onReceive(Context,Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		Intent iServ = new Intent();
		iServ.setClass(context, SpeedTrackingService.class);
		context.startService(iServ);// /Calls another activity, by name, without
									// passing data

		try {
			Intent iExp = new Intent(context, Main_locker.class);
			// Adding FLAG_ACTIVITY_NEW_TASK to get rid of this error messagE:
			// Exception: Calling startActivity() from outside of an Activity
			// context requires the FLAG_ACTIVITY_NEW_TASK flas. Is this really
			// what you want?
			iExp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(iExp);
		} catch (Exception err) {

		}

	}
}
