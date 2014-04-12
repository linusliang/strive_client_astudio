/*
 ******************************************************************************
 * Parts of this code sample are licensed under Apache License, Version 2.0   *
 * Copyright (c) 2009, Android Open Handset Alliance. All rights reserved.    *
 *                                                                            *                                                                         *
 * Except as noted, this code sample is offered under a modified BSD license. *
 * Copyright (C) 2010, Motorola Mobility, Inc. All rights reserved.           *
 *                                                                            *
 * For more details, see MOTODEV_Studio_for_Android_LicenseNotices.pdf        * 
 * in your installation folder.                                               *
 ******************************************************************************
 */

package com.drivestrive.Activities;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;




/***
 * PreferenceActivity is a built-in Activity for preferences management
 * 
 * To retrieve the values stored by this activity in other activities use the
 * following snippet:
 * 
 * SharedPreferences sharedPreferences =
 * PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 * <Preference Type> preferenceValue = sharedPreferences.get<Preference
 * Type>("<Preference Key>",<default value>);
 */
public class PreferencesActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,GooglePlayServicesClient.OnConnectionFailedListener {
	private Switch toggleStrive;
	private boolean preferencesLoaded = false;
	private SharedPreferences preferences;
	private boolean serviceStarted;

    private ActivityRecognitionClient arclient;
    private PendingIntent pIntent;
    private BroadcastReceiver receiver;



    // Store the current request type (ADD or REMOVE)
    private ActivityUtils.REQUEST_TYPE mRequestType;



    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("testy");
		setContentView(R.layout.activity_preferences);
		
		toggleStrive = (Switch) findViewById(R.id.tbService);



        //END NEW CODE


		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
        if(!isGpsEnabled) {
        	System.out.println("GPS not enabled");
        	Intent intentGps = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        	startActivityForResult(intentGps, 0);
        } else {
			toggleStrive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			    	System.out.println("listener working");
			        if (isChecked) {
			            //toggleService();
                        //onStartUpdates();

                        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
                        if(resp == ConnectionResult.SUCCESS){
                            arclient = new ActivityRecognitionClient(PreferencesActivity.this, PreferencesActivity.this, PreferencesActivity.this);
                            arclient.connect();


                        }
                        else{
                            Toast.makeText(PreferencesActivity.this, "Please install Google Play Service.", Toast.LENGTH_SHORT).show();
                            System.out.println("Please install Google Play Service.");
                        }

                        receiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                String act = intent.getStringExtra("Activity");
                                int conf = intent.getExtras().getInt("Confidence");
                                String v =  "Activity :" + act + " " + "Confidence : " + conf + "\n";
                                System.out.println(v);

                                if(act.equals("in_vehicle"))
                                {
                                    toggleSpeedService();
                                }
                                else if(act.equals("still")) //NEED TO DO SOMETHING HERE
                                {
                                    untoggleSpeedService();
                                }

                            }
                        };
                        IntentFilter filter = new IntentFilter();
                        filter.addAction("com.drivestrive.Activities.ACTIVITY_RECOGNITION_DATA");
                        registerReceiver(receiver, filter);
			        } else {
	    		        untoggleSpeedService();//NEED TO DO SOMETHING HERE
                        //onStopUpdates();
                        if(arclient!=null){
                            arclient.removeActivityUpdates(pIntent);
                            arclient.disconnect();
                        }
                        unregisterReceiver(receiver);
			        }
			    }
			});			
        }
	}

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean ServiceNotConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            //Log.d(ActivityUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return false;

            // Google Play services was not available for some reason
        } else {
            return true;
        }
    }



    @Override
    public void onConnected(Bundle arg0) {
        Intent intent = new Intent(this, ActivityRecognitionIntentService.class);
        pIntent = PendingIntent.getService(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        arclient.requestActivityUpdates(1000, pIntent);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }


    private void toggleSpeedService() {
		if (serviceStarted == false) {
			Intent serviceIntent = new Intent();
			serviceIntent.setClassName("com.drivestrive.Activities",
					SpeedTrackingService.class.getName());
			this.startService(serviceIntent);
			serviceStarted = true;
		} 
	}

	private void untoggleSpeedService() {
		boolean isServiceRunning = isServiceRunning("SpeedTrackingService"); //NOTE 1
		if (serviceStarted == true) {
			
			Intent serviceIntent = new Intent();
			serviceIntent.setClassName("com.drivestrive.Activities",
					SpeedTrackingService.class.getName());
			this.stopService(serviceIntent);
			serviceStarted = false;
			System.out.println("Thank you for using Strive");
	    	Toast.makeText(this, "Thank you for using Strive", Toast.LENGTH_LONG).show();
		} 
	}
	
	private Boolean isServiceRunning(String serviceName) {
		 ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		    for (RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
		        if (serviceName.equals(runningServiceInfo.service.getClassName())) {
		        	return true;
		        }
		    }
		    return false;
	}
}