package com.drivestrive.Activities;

import com.drivestrive.Activities.HomeKeyLocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

//import com.android.internal.telephony.ITelephony;

public class Main_locker extends Activity {

	/**
	 * Declaration of variables used...
	 */
	int choice; // Variable used for 1 and 2 variation of lock screen..
	SharedPreferences pref; // variable used to store lock screen mode as choice
							// for variation..
	Editor editor; // variable use to update shared preferences..
	ImageView red, yellow, green, cir_left_top, cir_right_top,
			cir_right_bottom, back_box_red, back_box_yellow, back_box_green; // ImageView
																				// Objects
																				// used
																				// ..
	android.widget.RelativeLayout.LayoutParams param1, param2, param3,
			cir_param1, cir_param2, cir_param3, param_box_red,
			param_box_yellow, param_box_green; // LayoutParams used for setting
												// params of Images..

	RelativeLayout parent; // Object of Parent Layout containing Images..
	int target_med_X, target_med_Y, target_final_X, target_final_Y; // variables
																	// used for
																	// Storing
																	// Coordinates
																	// of the
																	// target
																	// elements..
	int red_mid_x, red_mid_y, yellow_mid_x, yellow_mid_y, green_mid_x,
			green_mid_y, topleft_x, topleft_y, topright_x, topright_y,
			bottomright_x, bottomright_y, f1 = 0, f2 = 0, f3 = 0, c1 = 0,
			c2 = 0, c3 = 0;// variables for computation..
	int ln1, ln2, cir_len;// variable used to store Params of Images according
							// to Variation..
	int locker_choice = 0, flag = 0;
	int done = 0;
	HomeKeyLocker hm;
	DisplayMetrics dm;
	MediaPlayer mPlayer;
	boolean wasRed = false;
	public TextView tvMph;

	// ITelephony telephonyService;
	private SmsManager smsManager;

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onCreate
	 * @date: Mar 26,2014
	 * @param: bundle
	 * @description : first method called when activity is launched. Used for initial processing and working..
	 */
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("hey ho");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main_locker);
		// Setup Speed
		Intent sender = getIntent();
		String extraDataMPH = sender.getExtras().getString("speed1");

		tvMph = (TextView) findViewById(R.id.textView_mph);
		tvMph.setText(extraDataMPH);

		// Finding References...
		hm = new HomeKeyLocker();
		hm.lock(this);
		red = (ImageView) findViewById(R.id.red);
		yellow = (ImageView) findViewById(R.id.yellow);
		green = (ImageView) findViewById(R.id.green);
		parent = (RelativeLayout) findViewById(R.id.parent);
		cir_right_top = (ImageView) findViewById(R.id.cir_right_top);
		cir_left_top = (ImageView) findViewById(R.id.cir_left_top);
		cir_right_bottom = (ImageView) findViewById(R.id.cir_right_bottom);
		back_box_red = (ImageView) findViewById(R.id.back_box_red);
		back_box_yellow = (ImageView) findViewById(R.id.back_box_yellow);
		back_box_green = (ImageView) findViewById(R.id.back_box_green);
		mPlayer = MediaPlayer.create(Main_locker.this, R.raw.sound);

		// Getting Screen's Default Resolution for computation..
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		ln1 = (dm.heightPixels * 34) / 100;
		ln2 = (dm.heightPixels * 17) / 100;
		cir_len = (int) (dm.heightPixels * 10 / 100);
		// Setting params for the different View's used..
		cir_param1 = new RelativeLayout.LayoutParams(cir_len, cir_len);
		cir_param1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);
		cir_param1
				.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		cir_param1.setMargins(0, 3, 3, 0);
		cir_param2 = new RelativeLayout.LayoutParams(cir_len, cir_len);
		cir_param2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
				RelativeLayout.TRUE);
		cir_param2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
				RelativeLayout.TRUE);
		cir_param2.setMargins(0, 0, 3, 3);
		cir_param3 = new RelativeLayout.LayoutParams(cir_len, cir_len);
		cir_param3.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
				RelativeLayout.TRUE);
		cir_param3
				.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		cir_param3.setMargins(3, 3, 0, 0);

		if (getIntent() != null && getIntent().hasExtra("kill")
				&& getIntent().getExtras().getInt("kill") == 1) {

			finish();
		}

		try {
			// Starting Service for the Lock Screen..
			startService(new Intent(this, LockService.class));
			// Listening Phone's Call State..
			StateListener phoneStateListener = new StateListener();
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			telephonyManager.listen(phoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
		} catch (Exception e) {
			Log.i("Exception : ", "" + e);
		}

		cir_left_top.setLayoutParams(cir_param3);
		cir_right_bottom.setLayoutParams(cir_param2);
		cir_right_top.setLayoutParams(cir_param1);

		// Getting Shared Preferences..
		pref = getApplicationContext().getSharedPreferences("starlocker", 0);
		editor = pref.edit();
		// Adding Values of Screen Variation to the Preferences..
		if (pref.getString("carInMotion", null).equals("1")) {
			choice = 1;
			System.out.println("choice 1");

			// editor.putString("cntno", "1");
		} else if (pref.getString("carInMotion", null).equals("0")) {
			choice = 2;
			System.out.println("choice 2");
			// editor.putString("cntno", "2");
		}

		editor.commit();

		pref.registerOnSharedPreferenceChangeListener(mPrefListener);
		// Adjusting Layout of the App if the Variation is 1..
		if (choice == 1) {
			param1 = new RelativeLayout.LayoutParams(ln1 - (ln1 * 10 / 100),
					ln1 - (ln1 * 10 / 100));
			param1.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param1.setMargins(0, dm.heightPixels * 5 / 100, 0, 0);

			param_box_red = new RelativeLayout.LayoutParams(ln1, ln1);
			param_box_red.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_red.setMargins(0, dm.heightPixels * 5 / 100, 0, 0);

			param2 = new RelativeLayout.LayoutParams(ln2 - (ln2 * 10 / 100),
					ln2 - (ln2 * 10 / 100));
			param2.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param2.setMargins(0, (dm.heightPixels * 15 / 100) + ln1, 0, 0);

			param_box_yellow = new RelativeLayout.LayoutParams(ln2, ln2);
			param_box_yellow.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_yellow.setMargins(0, dm.heightPixels * 15 / 100 + ln1, 0,
					0);

			param3 = new RelativeLayout.LayoutParams(ln2 - (ln2 * 10 / 100),
					ln2 - (ln2 * 10 / 100));
			param3.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param3.setMargins(0, (dm.heightPixels * 25 / 100) + ln1 + ln2, 0, 0);

			param_box_green = new RelativeLayout.LayoutParams(ln2, ln2);
			param_box_green.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_green.setMargins(0, dm.heightPixels * 25 / 100 + ln1
					+ ln2, 0, 0);

			back_box_red.setLayoutParams(param_box_red);
			back_box_yellow.setLayoutParams(param_box_yellow);
			back_box_green.setLayoutParams(param_box_green);
			red.setLayoutParams(param1);
			yellow.setLayoutParams(param2);
			green.setLayoutParams(param3);

			yellow.setAlpha(55);
			green.setAlpha(55);
		}
		// Adjusting Layout of the App if the Variation is 2..
		else if (choice == 2) {
			param1 = new RelativeLayout.LayoutParams(ln2 - (ln2 * 10 / 100),
					ln2 - (ln2 * 10 / 100));
			param1.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param1.setMargins(0, dm.heightPixels * 5 / 100, 0, 0);

			param_box_red = new RelativeLayout.LayoutParams(ln2, ln2);
			param_box_red.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_red.setMargins(0, dm.heightPixels * 5 / 100, 0, 0);

			param2 = new RelativeLayout.LayoutParams(ln1 - (ln1 * 10 / 100),
					ln1 - (ln1 * 10 / 100));
			param2.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param2.setMargins(0, (dm.heightPixels * 15 / 100) + ln2, 0, 0);

			param_box_yellow = new RelativeLayout.LayoutParams(ln1, ln1);
			param_box_yellow.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_yellow.setMargins(0, dm.heightPixels * 15 / 100 + ln2, 0,
					0);

			param3 = new RelativeLayout.LayoutParams(ln2 - (ln2 * 10 / 100),
					ln2 - (ln2 * 10 / 100));
			param3.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param3.setMargins(0, (dm.heightPixels * 25 / 100) + ln1 + ln2, 0, 0);

			param_box_green = new RelativeLayout.LayoutParams(ln2, ln2);
			param_box_green.addRule(RelativeLayout.CENTER_HORIZONTAL,
					RelativeLayout.TRUE);
			param_box_green.setMargins(0, dm.heightPixels * 25 / 100 + ln1
					+ ln2, 0, 0);

			back_box_red.setLayoutParams(param_box_red);
			back_box_yellow.setLayoutParams(param_box_yellow);
			back_box_green.setLayoutParams(param_box_green);

			red.setLayoutParams(param1);
			yellow.setLayoutParams(param2);
			green.setLayoutParams(param3);
			yellow.setAlpha(255);
			green.setAlpha(55);
		}

		// Creating a setonTouchListener for the Parent Layout..
		parent.setOnTouchListener(new View.OnTouchListener() {

			@Override
			/**
			 * @author: Anmol Chanana
			 * @name: onTouch
			 * @date: Mar 26,2014
			 * @param: View,MotionEvent
			 * @description : Listening parent touch events.
			 */
			public boolean onTouch(View v, MotionEvent event) {
				// Logic to find height , width for computation..
				red_mid_x = (red.getWidth() / 2) + red.getLeft();
				red_mid_y = (red.getHeight() / 2) + red.getTop();
				yellow_mid_x = (yellow.getWidth() / 2) + yellow.getLeft();
				yellow_mid_y = (yellow.getHeight() / 2) + yellow.getTop();
				green_mid_x = (green.getWidth() / 2) + green.getLeft();
				green_mid_y = (green.getHeight() / 2) + green.getTop();

				Vibrator vib = (Vibrator) getApplicationContext()
						.getSystemService(Context.VIBRATOR_SERVICE);

				switch (event.getAction()) {
				// Event to handle the First Touch..
				case MotionEvent.ACTION_DOWN: {
					// for the first variation..
					if (c1 == 0 && (cir_right_top.getRight() > event.getX())
							&& (cir_right_top.getLeft() < event.getX())
							&& (cir_right_top.getTop() < event.getY())
							&& (cir_right_top.getBottom() > event.getY())) {
						cir_right_top.setVisibility(View.INVISIBLE);
						cir_right_bottom.setVisibility(View.VISIBLE);
						locker_choice = 1;
						vib.vibrate(100);
						c1 = 1;
					} else {
						if (choice == 1) {
							if (f1 == 0
									&& (red_mid_x + (int) (red.getHeight() / 4)) > event
											.getX()
									&& (red_mid_x - (int) (red.getHeight() / 4)) < event
											.getX()
									&& (red_mid_y - (int) (red.getHeight() / 4)) < event
											.getY()
									&& (red_mid_y + (int) (red.getHeight() / 4)) > event
											.getY()) {
								f1 = 1;
								vib.vibrate(100);

							} else {
								return false;
							}
						}
						// for the second variation..
						else if (choice == 2) {
							if (f2 == 0
									&& f1 == 0
									&& (yellow_mid_x + (int) (yellow
											.getHeight() / 4)) > event.getX()
									&& (yellow_mid_x - (int) (yellow
											.getHeight() / 4)) < event.getX()
									&& (yellow_mid_y - (int) (yellow
											.getHeight() / 4)) < event.getY()
									&& (yellow_mid_y + (int) (yellow
											.getHeight() / 4)) > event.getY()) {
								f2 = 1;
								vib.vibrate(100);
								// yellow_cir.setVisibility(View.VISIBLE);
							} else
								return false;
						}
					}
					break;
				}
				// Event to handle Move gesture..
				case MotionEvent.ACTION_MOVE: {
					// logic for the Alternative variation..

					if (locker_choice == 1) {

						if (c1 == 1
								&& c2 == 0
								&& (cir_right_bottom.getRight() > event.getX())
								&& (cir_right_bottom.getLeft() < event.getX())
								&& (cir_right_bottom.getTop() < event.getY())
								&& (cir_right_bottom.getBottom() > event.getY())) {
							cir_left_top.setVisibility(View.VISIBLE);
							cir_right_bottom.setVisibility(View.INVISIBLE);
							vib.vibrate(100);
							c2 = 1;
						}

						if (c1 == 1 && c2 == 1 && c3 == 0
								&& (cir_left_top.getRight() > event.getX())
								&& (cir_left_top.getLeft() < event.getX())
								&& (cir_left_top.getTop() < event.getY())
								&& (cir_left_top.getBottom() > event.getY())) {
							cir_left_top.setVisibility(View.INVISIBLE);
							vib.vibrate(100);
							flag = 1;
							done = 1;
							c3 = 1;
							mPlayer.start();
							finish();

						}

					} else {
						// logic for the first variation..
						if (choice == 1) {
							if (f2 == 0
									&& f1 == 1
									&& (yellow_mid_x + (int) (yellow
											.getHeight() / 4)) > event.getX()
									&& (yellow_mid_x - (int) (yellow
											.getHeight() / 4)) < event.getX()
									&& (yellow_mid_y - (int) (yellow
											.getHeight() / 4)) < event.getY()
									&& (yellow_mid_y + (int) (yellow
											.getHeight() / 4)) > event.getY()) {
								f2 = 1;
								vib.vibrate(100);

							}
							if (f3 == 0
									&& f1 == 1
									&& f2 == 1
									&& (green_mid_x + (int) (green.getHeight() / 4)) > event
											.getX()
									&& (green_mid_x - (int) (green.getHeight() / 4)) < event
											.getX()
									&& (green_mid_y - (int) (green.getHeight() / 4)) < event
											.getY()
									&& (green_mid_y + (int) (green.getHeight() / 4)) > event
											.getY()) {
								vib.vibrate(100);
								done = 1;
								f3 = 1;
								// Thread for wait for 0.2 sec after 2nd screen
								Thread th = new Thread() {

									public void run() {
										try {
											sleep(300);
										} catch (InterruptedException e) {
											e.printStackTrace();
										} finally {
											mPlayer.start();
											// Toast.makeText(Main_locker.this,
											// "Trip Failed",
											// Toast.LENGTH_LONG).show();
											stopService(new Intent(
													Main_locker.this,
													LockService.class)); //REDUNDANT with finish()
											finish();
										}

									}
								};

								th.start();
							}
						}
						// logic for the second variation..
						else if (choice == 2) {
							if (f3 == 0
									&& f1 == 0
									&& f2 == 1
									&& (green_mid_x + (int) (green.getHeight() / 4)) > event
											.getX()
									&& (green_mid_x - (int) (green.getHeight() / 4)) < event
											.getX()
									&& (green_mid_y - (int) (green.getHeight() / 4)) < event
											.getY()
									&& (green_mid_y + (int) (green.getHeight() / 4)) > event
											.getY()) {

								param2 = new RelativeLayout.LayoutParams(ln2
										- (ln2 * 10 / 100), ln2
										- (ln2 * 10 / 100));
								param2.addRule(
										RelativeLayout.CENTER_HORIZONTAL,
										RelativeLayout.TRUE);
								param2.setMargins(0,
										(dm.heightPixels * 15 / 100) + ln2, 0,
										0);

								param_box_yellow = new RelativeLayout.LayoutParams(
										ln2, ln2);
								param_box_yellow.addRule(
										RelativeLayout.CENTER_HORIZONTAL,
										RelativeLayout.TRUE);
								param_box_yellow.setMargins(0, dm.heightPixels
										* 15 / 100 + ln2, 0, 0);

								param3 = new RelativeLayout.LayoutParams(ln1
										- (ln1 * 10 / 100), ln1
										- (ln1 * 10 / 100));
								param3.addRule(
										RelativeLayout.CENTER_HORIZONTAL,
										RelativeLayout.TRUE);
								param3.setMargins(0,
										(dm.heightPixels * 25 / 100) + ln2
												+ ln2, 0, 0);

								param_box_green = new RelativeLayout.LayoutParams(
										ln1, ln1);
								param_box_green.addRule(
										RelativeLayout.CENTER_HORIZONTAL,
										RelativeLayout.TRUE);
								param_box_green.setMargins(0, dm.heightPixels
										* 25 / 100 + ln2 + ln2, 0, 0);

								back_box_yellow
										.setLayoutParams(param_box_yellow);
								back_box_green.setLayoutParams(param_box_green);
								yellow.setLayoutParams(param2);
								yellow.setAlpha(55);
								green.setLayoutParams(param3);
								green.setAlpha(255);
								vib.vibrate(100);
								f3 = 1;
								done = 1;

								// Thread for wait for 0.2 sec after 2nd screen
								Thread th = new Thread() {

									public void run() {
										try {
											sleep(300);
										} catch (InterruptedException e) {
											e.printStackTrace();
										} finally {
											mPlayer.start();
											stopService(new Intent(
													Main_locker.this,
													LockService.class));   //REDUNDANT
											// Toast.makeText(Main_locker.this,
											// "Trip Completed",
											// Toast.LENGTH_LONG).show();
											finish();
										}

									}
								};

								th.start();

							}
						}

					}
					break;
				}
				// Event to handle release gesture..
				case MotionEvent.ACTION_UP: {
					if (flag == 0) {
						cir_right_top.setVisibility(View.VISIBLE);
					}
					cir_left_top.setVisibility(View.INVISIBLE);
					cir_right_bottom.setVisibility(View.INVISIBLE);

					f1 = f2 = f3 = flag = locker_choice = c1 = c2 = c3 = 0;

					break;
				}
				}
				return true;
			}

		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent sender = getIntent();
		// if(sender.getExtras().getString("finish") == "destroyMe") { finish();
		// }
		IntentFilter iFilter = new IntentFilter("speedExceeded");
		iFilter.addAction("carIdling");
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, iFilter);
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// System.out.println("yup");
			String action = intent.getAction();
			String currentSpeedMPH = intent.getStringExtra("speed");
			Double currentSpeedDbl = Double.parseDouble(currentSpeedMPH);
			System.out.println("testtting");
			if (action.equals("speedExceeded")) {
				System.out.println("attention please");

				wasRed = true;
			} else if (action.equals("carIdling")) {
				System.out.println("2 cents is free");
				wasRed = false;
			}

			tvMph = (TextView) findViewById(R.id.textView_mph);
			tvMph.setText(currentSpeedMPH + "mph");
		}
	};

	private OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			SharedPreferences SP = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext());
//			String changeLayout = SP.getString("cntno", "2");
//
//			Toast.makeText(Main_locker.this,
//					"Preferences changed" + changeLayout, Toast.LENGTH_LONG)
//					.show();
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	};

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onCreateOptionsMenu
	 * @date: Mar 26,2014
	 * @param: menu
	 * @description : Not inflating any menus on the App.. 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onBackPressed
	 * @date: Mar 26,2014
	 * @param: null
	 * @description : Not returning anything to super(for disabling back key..)
	 */
	public void onBackPressed() {
		// Don't allow back to dismiss.
		return;
	}

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onPause
	 * @date: Mar 26,2014
	 * @param: null
	 * @description : Called when the Activity is Paused..
	 */
	protected void onPause() {
		super.onPause();

	}

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onStop
	 * @date: Mar 26,2014
	 * @param: null
	 * @description : Called the When the Activity is Stopped..
	 */
	protected void onStop() {
		super.onStop();

	}

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onDestroy
	 * @date: Mar 26,2014
	 * @param: null
	 * @description :  Called when the Activity is Destroyed..
	 */
	public void onDestroy() {

		super.onDestroy();
        stopService(new Intent(
                Main_locker.this,
                LockService.class));

	}

	/**
	 * 
	 * @author : Anmol Chanana
	 * @description : Created a class for listening the phone's CALL STATE..
	 * @date : Mar 26,2014;
	 */
	class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:

				finish();

				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

}
