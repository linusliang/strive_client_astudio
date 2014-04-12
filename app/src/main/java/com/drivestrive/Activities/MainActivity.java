package com.drivestrive.Activities;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*
 * 
 *  						Launcher to start the game
 * 
 *	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*	*/
public class MainActivity extends Activity implements OnClickListener {

	public Button	btnPlayGame, btnSaveNumber, btnTOS, btnVerifyPhone, btnWelcomeNext, btnAcceptTOS;
	public TextView txtTOS;
	public CheckBox chkTOS; 
	LocationManager	locationManager;
	public static final String 	PREFS_NAME = "MyPrefsFile";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Toast.makeText(this.getApplicationContext(), "Please complete all the details", Toast.LENGTH_SHORT).show();
		System.out.println("yeah");
		super.onCreate(savedInstanceState);			
		
		// Restore Settings and check if this is the first time using the
		// application
		//
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String phone = settings.getString("phone", null);
		Boolean verified = settings.getBoolean("verified", false);
		if (phone == null) {
			setContentView(R.layout.activity_welcome);
			btnWelcomeNext = (Button) findViewById(R.id.btn_welcome_next);		
			btnWelcomeNext.setOnClickListener(this);
		} else if (verified == false) {
			setContentView(R.layout.activity_verify_phone);				// set to the verify view
			btnVerifyPhone = (Button) findViewById(R.id.btn_verify_phone);  // get the verify phone
			btnVerifyPhone.setOnClickListener(this);	
		}
		else {
	        log_start_game();											// send information to the server
			Intent prefIntent = new Intent(this, PreferencesActivity.class);
			prefIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(prefIntent);	
		}
	}
		
	@Override
	public void onClick(View v) {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0); 	// get setting
		SharedPreferences.Editor editor = settings.edit();					// get object to edit setting
		
		switch (v.getId()) {
		case R.id.btn_welcome_next:
			setContentView(R.layout.activity_accept_tos);
			btnAcceptTOS =  (Button) findViewById(R.id.btn_accept_tos);
			btnAcceptTOS.setOnClickListener(this);
			txtTOS = (TextView) findViewById(R.id.txt_tos);
			txtTOS.setMovementMethod(new ScrollingMovementMethod());
			break;
		case R.id.btn_accept_tos:
			setContentView(R.layout.activity_send_verification);
			btnSaveNumber = (Button) findViewById(R.id.btn_save_number);// find the button, set its reference
			btnSaveNumber.setOnClickListener(this);						// now listen for the event
			break;
		case R.id.btn_verify_phone:
			String phone = ((EditText)findViewById(R.id.input_text_verify_phone)).getText().toString();
			if (phone.equals("123")){
				// Save the user's verification state
				//
				editor.putBoolean("verified", true); // edit the setting
				editor.commit(); // save the setting
				startActivity(new Intent(MainActivity.this, MainActivity.class));
			} else {
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
	            alert.setTitle("Error");
	            alert.setMessage("Wrong Code");
	            alert.setPositiveButton("OK", null);
	            alert.show();
	            
				editor.putString("phone", null); // edit the setting
				editor.commit(); // save the setting

				setContentView(R.layout.activity_send_verification);
				btnSaveNumber = (Button) findViewById(R.id.btn_save_number);// find the button, set its reference
				btnSaveNumber.setOnClickListener(this);						// now listen for the event
			}
			break;
		case R.id.btn_save_number: 											// if user clicked the save button
			// Save the user's number
			//
			editor.putString("phone", ((EditText)findViewById(R.id.input_text_phone_number)).getText().toString()); // edit the setting
			editor.commit(); // save the setting

			setContentView(R.layout.activity_verify_phone);					// set to the verify view
			btnVerifyPhone = (Button) findViewById(R.id.btn_verify_phone);  // get the verify phone
			btnVerifyPhone.setOnClickListener(this);	

			break;
		default:
			break;
		}
	}
		
	private void log_start_game(){
		Thread t = new Thread() {
            public void run() {
                Looper.prepare(); //For Preparing Message Pool for the child Thread
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
                HttpResponse response;
                JSONObject json = new JSONObject();

                try {
                    
                	// set the server to use
                	//
                	HttpPost post = new HttpPost("http://drivestrive.com/api/post_start_time");
                	//HttpPost post = new HttpPost("http://192.168.1.65:3000/api/post_start_time");
                    
                	// create the JSON request
                	//
            		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            		String phone = settings.getString("phone", null);
                    json.put("phone", phone);
                    json.put("start_time", System.currentTimeMillis());
                    StringEntity se = new StringEntity(json.toString());  
                    
                    // send the JSON request
                    //
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    
                    // read the response
                    //
                    response = client.execute(post);                    
                    int responseCode = response.getStatusLine().getStatusCode();
                    switch(responseCode) {
                    case 201:
                    HttpEntity entity = response.getEntity();
                        if(entity != null) {
                        }
                        break;
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
                Looper.loop(); //Loop in the message queue
            }
        };
        t.start();    		
	}
}