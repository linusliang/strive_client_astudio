package com.drivestrive.Activities;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

import com.drivestrive.utils.Alert;
import com.drivestrive.utils.Session;

/**
 *
 * Game Play Logic
 *         
 */
public class PlayActivity extends Activity implements SensorEventListener {
	public static final String 	PREFS_NAME = "MyPrefsFile";
	public static boolean		isGpsGetvalue	= true;
	private SensorManager		mSensorManager;
	private Sensor				mSensorAccelerometer, mSensorGyroscope;
	private TextView			mTxtAccelYaxisValue, mTxtAccelYaxisKey, mTxtGyroXaxisValue, mTxtGyroYaxisKey,
								mTxtGpsXaxisValue, mTxtGpsYaxisKey;
	public CheckBox				mChkAccelXaxis, mChkGyroXaxis, mChkGpsXaxis;
	
	private LocationManager		locationManager	= null;
	private TextView			mtxtGps;
	private MyLocationListener	locationListener;

	// variables for sending to server
	private long 				start_time = System.currentTimeMillis();
	public long					gyroTime, accelTime, gpsTime;
	public int					gyroValue, accelValue, gpsValue;
	private double				latitude, longitude;
	private String				phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// get last instance, set content
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
    	// get the user's phone number 
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		phone = settings.getString("phone", null);
		
		// setup the sensors and initialize the UI
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		initUI();
	}

	/**
	 * Initialization of UI for the Activity
	 */
	private void initUI() {
		mTxtAccelYaxisValue = (TextView) findViewById(R.id.txt_accelValue);
		mTxtAccelYaxisKey   = (TextView) findViewById(R.id.txt_accelKey);

		mTxtGyroXaxisValue = (TextView) findViewById(R.id.txt_gyroValue);
		mTxtGyroYaxisKey   = (TextView) findViewById(R.id.txt_gyroKey);

		mTxtGpsXaxisValue = (TextView) findViewById(R.id.txt_gpsValue);
		mTxtGpsYaxisKey   = (TextView) findViewById(R.id.txt_gpsKey);
		mtxtGps = (TextView) findViewById(R.id.txt_gps);

		mChkAccelXaxis = (CheckBox) findViewById(R.id.chk_accel);
		mChkGyroXaxis = (CheckBox) findViewById(R.id.chk_gyro);
		mChkGpsXaxis = (CheckBox) findViewById(R.id.chk_gps);
		populateUI();
	}

	/**
	 * Start the game with random numbers to achieve
	 */
	private void populateUI() {
		mTxtGyroYaxisKey.setText("" + (int) Session.getGyroValue());
		mTxtAccelYaxisKey.setText("" + (int) Session.getAccerlValue());
		mTxtGpsYaxisKey.setText("" + (int) Session.getGpsValue());
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
		handleGPS();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO for future implementations
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
		mTxtGyroXaxisValue.setText("" + gyroValue);
		mChkGyroXaxis.setChecked(false);
		if (gyroValue == Integer.parseInt(mTxtGyroYaxisKey.getText().toString())) {
			mChkGyroXaxis.setChecked(true);
			gyroTime = System.currentTimeMillis();
			checkResult();
		}
	}

	/**
	 * This method handles the Accelerometer
	 */
	private void handleAccelerometer(float[] values) {
		int accelint = Math.abs((int) (Math.round(values[1] * 100.0) / 100.0));
		accelValue = accelint * 10;
		mTxtAccelYaxisValue.setText("" + accelValue);
		mChkAccelXaxis.setChecked(false);
		if (accelValue == Integer.parseInt(mTxtAccelYaxisKey.getText().toString())) {
			mChkAccelXaxis.setChecked(true);
			accelTime = System.currentTimeMillis();
			checkResult();
		}
	}

	/**
	 * This method handles the GPS speed
	 */
	private void handleGPS() {
		boolean flag = displayGpsStatus();
		if (flag) {
			locationListener = new MyLocationListener();
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
			locationManager.addNmeaListener(new NmeaListener() {
				public void onNmeaReceived(long timestamp, String nmea) {
					if (nmea.contains("GPRMC")) {
						// Separate out everything for parsing
						String[] nmeaParts = nmea.split(",");
						String speed = nmeaParts[7].toString();
						String conn = nmeaParts[2].toString();
												
						// ========================
						if (conn.equalsIgnoreCase("V")) {
							// color change of texts
							mTxtGpsYaxisKey.setTextColor(Color.RED);
							mtxtGps.setTextColor(Color.RED);
							mTxtGpsXaxisValue.setTextColor(Color.RED);
						} else {
							mTxtGpsXaxisValue.setTextColor(Color.BLACK);
							mTxtGpsYaxisKey.setTextColor(Color.BLACK);
							mtxtGps.setTextColor(Color.BLACK);
						}
						// ========================

						if (speed.equalsIgnoreCase(""))
							speed = "0";
						float meterSpeed = 0.0f;
						float temp1 = Float.parseFloat(speed);
						// convert speed to km per hour
						meterSpeed = temp1 * 1.852f;
						// meterSpeed = (temp1 * 1.852f) * 1000f;
						// meterSpeed = (temp1 * 1.852f) * 0.277777778f;
						gpsValue = Math.abs((int) (meterSpeed));
						// if (isGpsGetvalue)
						{
							mTxtGpsXaxisValue.setText("" + gpsValue);
							mChkGpsXaxis.setChecked(false);
							// gpsTime = 0;
							if (gpsValue == Integer.parseInt(mTxtGpsYaxisKey.getText().toString())) {
								mChkGpsXaxis.setChecked(true);
								gpsTime = System.currentTimeMillis();
								checkResult();
							}
						}
					}
				}
			});
			
			if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }	
		}
	}

	/*----------Method to Check GPS is enable or disable ------------- */
	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = getBaseContext().getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;
		} else {
			return false;
		}
	}

	/*----------Listener class to get coordinates ------------- */
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

	/**
	 * This method checks result and show alert if all values match
	 */
	public void checkResult() {
		long deltaTime1 = Math.abs(accelTime - gyroTime);
		long deltaTime2 = Math.abs(gyroTime - gpsTime);
		long deltaTime3 = Math.abs(gpsTime - accelTime);
		if ((deltaTime1 > 1 && deltaTime1 < 1000) && (deltaTime2 > 1 && deltaTime2 < 1000)
				&& (deltaTime3 > 1 && deltaTime3 < 1000)) {
			mSensorManager.unregisterListener(this, mSensorGyroscope);
			mSensorManager.unregisterListener(this, mSensorAccelerometer);
			locationManager.removeUpdates(locationListener);
			disableAll();

			TextView tvAcceler = (TextView) findViewById(R.id.txt_accelValueDisable);
			TextView tvGyro = (TextView) findViewById(R.id.txt_gyroValueDisable);
			TextView tvGPS = (TextView) findViewById(R.id.txt_gpsValueDisable);
			tvAcceler.setVisibility(View.VISIBLE);
			tvGyro.setVisibility(View.VISIBLE);
			tvGPS.setVisibility(View.VISIBLE);
			tvAcceler.setText(mTxtAccelYaxisKey.getText().toString());
			tvGyro.setText(mTxtGyroYaxisKey.getText().toString());
			tvGPS.setText(mTxtGpsYaxisKey.getText().toString());

			CheckBox cbAccelerDisable = (CheckBox) findViewById(R.id.chk_accelDisable);
			CheckBox cbGpsDisable = (CheckBox) findViewById(R.id.chk_gpsDisable);
			CheckBox cbGyroDisable = (CheckBox) findViewById(R.id.chk_gyroDisable);
			cbAccelerDisable.setVisibility(View.VISIBLE);
			cbGpsDisable.setVisibility(View.VISIBLE);
			cbGyroDisable.setVisibility(View.VISIBLE);
			cbAccelerDisable.setChecked(true);
			cbGyroDisable.setChecked(true);
			cbGpsDisable.setChecked(true);

			mSensorManager.unregisterListener(this, mSensorGyroscope);
			mSensorManager.unregisterListener(this, mSensorAccelerometer);
			locationManager.removeUpdates(locationListener);

			new Alert(this).showAlert("You won..!!! \nAll Sensor's Match:- Jackpot!!! " + "\n" + deltaTime1 + "\n"
					+ deltaTime2 + "\n" + deltaTime3);
			// handler used for finish activity after 5 seconds
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					PlayActivity.this.finish();
				}
			}, 2000);
			return;
		}
	}

	private void disableAll() {
		findViewById(R.id.txt_gpsValue).setVisibility(View.GONE);
		findViewById(R.id.txt_accelValue).setVisibility(View.GONE);
		findViewById(R.id.txt_gyroValue).setVisibility(View.GONE);

		findViewById(R.id.chk_accel).setVisibility(View.GONE);
		findViewById(R.id.chk_gps).setVisibility(View.GONE);
		findViewById(R.id.chk_gyro).setVisibility(View.GONE);

	}

	@Override
	public void onBackPressed() {
		mSensorManager.unregisterListener(this, mSensorGyroscope);
		mSensorManager.unregisterListener(this, mSensorAccelerometer);
		locationManager.removeUpdates(locationListener);
		super.onBackPressed();
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
