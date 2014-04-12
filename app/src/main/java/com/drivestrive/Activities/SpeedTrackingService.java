package com.drivestrive.Activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;



public class SpeedTrackingService extends Service implements SensorEventListener{
	public static final String 	PREFS_NAME = "MyPrefsFile";
	private LocationManager locationManager;
	private MyLocationListener	locationListener;
	SharedPreferences pref;  // variable used to store lock screen mode as choice for variation..
	Editor editor;    // variable use to update shared preferences..
	private double speedThreshold = (double)(5);
	private boolean showBlockActivity = true;

	// sensor variables
	private SensorManager		mSensorManager;
	private Sensor				mSensorAccelerometer, mSensorGyroscope;
	
	// variables for sending to server
	private long 				start_time = System.currentTimeMillis();
	public long					gyroTime, accelTime, gpsTime;
	public int					gyroValue, accelValue, gpsValue;
	private double				latitude, longitude;
	private String				phone;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

    @Override
    public void onCreate() {
//    	Notification notification = new Notification(R.drawable.strive_status_icon, "Strive", System.currentTimeMillis());
//    	notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//    	NotificationManager notifier = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
//    	notifier.notify(1, notification);
    	
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!isGpsEnabled) {
        	System.out.println("GPS not enabled");
        	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        	startActivity(intent);
        	stopSelf();
        } else {
        	System.out.println("GPS is enabled, proceeding..");
    		mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    		mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        	handleGPS();
        }
    }

    @Override
    public void onDestroy() {
    	locationManager.removeUpdates(locationListener);
    	 super.onDestroy();
    	//Toast.makeText(this, "Speed Tracking Service Destroyed ", Toast.LENGTH_LONG).show();    
    }
    
    private void handleGPS() {
    	locationListener = new MyLocationListener();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
		locationManager.addNmeaListener(new NmeaListener() {
			public void onNmeaReceived(long timestamp, String nmea) {
				if (nmea.contains("GPRMC")) {
					// Separate out everything for parsing
					String[] nmeaParts = nmea.split(",");
					String speed = nmeaParts[7].toString();
					String latStr = nmeaParts[3].toString();
					String lonStr = nmeaParts[5].toString();
					String conn = nmeaParts[2].toString();
											
					if (speed.equalsIgnoreCase("")){
						speed = "0";
					}
					float meterSpeed = 0.0f;
					float temp1 = Float.parseFloat(speed);
					// convert speed to miles per hour
					meterSpeed = temp1 * 1.15078f;
					// meterSpeed = (temp1 * 1.852f) * 1000f;
					// meterSpeed = (temp1 * 1.852f) * 0.277777778f;
					gpsValue = Math.abs((int) (meterSpeed));
					//Toast.makeText(SpeedTrackingService.this, "Speed: "+Double.toString(gpsValue)+", "+conn, Toast.LENGTH_LONG).show();
					pref = getApplicationContext().getSharedPreferences("starlocker", 0);
					editor = pref.edit();
					if(gpsValue >= speedThreshold)
					{
						editor.putString("carInMotion", "1");
						if(showBlockActivity){
							Intent intent11 = new Intent(SpeedTrackingService.this, Main_locker.class); //CORRECT CONTEXT!?!??!
					       	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					       	intent11.putExtra("speed1", Double.toString(gpsValue));
					       	startActivity(intent11);
					       	showBlockActivity = false;
						}
						Intent intent = new Intent("speedExceeded");
				        intent.putExtra("speed", Double.toString(gpsValue));
				        LocalBroadcastManager.getInstance(SpeedTrackingService.this).sendBroadcast(intent);
					}
					else if(gpsValue < speedThreshold)  //This is backwards
			        {
						 System.out.println("value < threshhold");
		    	         editor.putString("carInMotion", "0");
		        		 Intent intent = new Intent("carIdling");
		        		 
		    	         intent.putExtra("speed", Double.toString(gpsValue));
		    	         LocalBroadcastManager.getInstance(SpeedTrackingService.this).sendBroadcast(intent);
		    	         System.out.println("Broadcast to Blocking Activity");
			        }
					editor.commit();
					
					if (locationManager != null) {
		                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		                if (location != null) {
		                    latitude = location.getLatitude();
		                    longitude = location.getLongitude();
		                }
		            }	
				}
			}
		});
    }
    
//	@Override
//	public void onLocationChanged(Location location) {	
//	}
//
//	@Override
//	public void onProviderDisabled(String provider) {
//		stopSelf();
//		//Toast.makeText(this, "Please enable your GPS ", Toast.LENGTH_LONG).show();
//    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//    	startActivity(intent);
//	}
//
//	@Override
//	public void onProviderEnabled(String provider) {
//		// TODO Auto-generated method stub	
//	}
//
//	@Override
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		// TODO Auto-generated method stub
//	}
	
	
	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		//Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
		 return START_STICKY;
	}
	
	private class MyLocationListener implements LocationListener, NmeaListener {
		@Override
		public void onLocationChanged(Location loc) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onNmeaReceived(long timestamp, String nmea) {
		}
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			handleAccelerometer(event.values);
			break;
		case Sensor.TYPE_GYROSCOPE:
			handleGyroscope(event.values);
			break;
		}
		post_data_to_server();
	}
	
	/**
	 * This method handles the value Gyroscope
	 */
	private void handleGyroscope(float[] values) {
		int gyroint = Math.abs((int) (Math.round(values[0] * 100.0) / 100.0));
		gyroValue = gyroint * 10;
	}

	/**
	 * This method handles the Accelerometer
	 */
	private void handleAccelerometer(float[] values) {
		int accelint = Math.abs((int) (Math.round(values[1] * 100.0) / 100.0));
		accelValue = accelint * 10;
	}
	
	/**
	 * Every X seconds, send over all the data to the server
	 */
	private void post_data_to_server() {
		int seconds = 30000;
		if (start_time + seconds < System.currentTimeMillis()){
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			int lastIDsent = settings.getInt("lastIDsent", 0);
			
    		// add entry to the DB
			MySQLiteHelper db = new MySQLiteHelper(this);
			db.addEntry(phone, latitude, longitude, accelValue, gyroValue, gpsValue);
			db.send_data(lastIDsent, settings);
			start_time = System.currentTimeMillis();
		}
	}
}


