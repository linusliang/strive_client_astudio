package com.drivestrive.utils;

import android.app.Activity;
import android.app.AlertDialog; 
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.drivestrive.Activities.R; 

/**
 * 
 * @author master software solutions
 * 
 *         This class must be used to show Alert dialog throughout the App
 * 
 */

public class Alert {
	private Activity	mActivity; 
	public Alert(Activity activity) {
		mActivity = activity;
	}

	public void showAlert(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.app_name);
		builder.setMessage(message);
		builder.setCancelable(false);
//		Session.setAccerlValue(new Utils().random(1, 6));
//		Session.setGyroValue(new Utils().random(1, 4));
//		Session.setGpsValue(new Utils().random(0, 0));

		AlertDialog dialog = builder.show();
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();

		wlp.gravity = Gravity.TOP;
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		window.setAttributes(wlp);

		TextView txtMessage = (TextView) dialog.findViewById(android.R.id.message);
		if (txtMessage != null)
			txtMessage.setGravity(Gravity.CENTER);
		dialog.show();

	}
	
	/**
	 * This method handles enabling of GPS
	 * 
	 * @param message
	 */
	public void showGPSAlert(String message)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(R.string.app_name);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mActivity.startActivity(callGPSSettingIntent); 
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
	}
	
}
