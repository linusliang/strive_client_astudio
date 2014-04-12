package com.drivestrive.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class EnableLockScreen extends Activity {

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onCreate 
	 * @date: Mar 26,2014
	 * @param: Bundle
	 * @description : Called when the Activity is created to start lock service..
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(new Intent(this,LockService.class));
		finish();

         


	}

}
