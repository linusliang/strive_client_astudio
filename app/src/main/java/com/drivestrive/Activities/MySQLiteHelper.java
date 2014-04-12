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
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Looper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final int    DATABASE_VERSION = 1;			// Database Version
    private static final String DATABASE_NAME = "sensorDB";		// Database Name
    private static final String TABLE_NAME = "sensors";			// Table name
	public static final String 	PREFS_NAME = "MyPrefsFile";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }
 
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create table
        String CREATE_SENSOR_TABLE = "CREATE TABLE " + TABLE_NAME +  " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                "phone TEXT, "+
                "latitude TEXT, "+
                "longitude TEXT, "+
                "accel TEXT, "+
                "gyro TEXT, "+
                "speed TEXT, " +
                "time TEXT)";
        db.execSQL(CREATE_SENSOR_TABLE);
    }
 
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 
        // create fresh table
        this.onCreate(db);
    }
    
    /**
     * CRUD operations (create "add", read "get", update, delete)
     */
    public void addEntry(String phone, double latitude, double longitude, int accel, int gyro, int speed){
        //Log.d("Adding entry to SQL Lite DB", "Lat/Long/Accel/Gyro/Speed - " + latitude + "/" + longitude + "/" + accel + "/" + gyro + "/" + speed + "/");
        SQLiteDatabase db = this.getWritableDatabase();	// get reference to writable DB
        ContentValues values = new ContentValues();
        values.put("phone", phone); 
        values.put("latitude", latitude); 
        values.put("longitude", longitude); 
        values.put("accel", accel); 
        values.put("gyro", gyro); 
        values.put("speed", speed); 
        values.put("time", System.currentTimeMillis()); 
        db.insert(TABLE_NAME, null, values); 
        db.close(); 
    }
    
	public void send_data(int lastIDsent, final SharedPreferences settings) {	    
		
		// Get all the rows starting from the last successful row sent (limited to 100 rows)
		//
		int lastXrows = 100;
	    SQLiteDatabase db = this.getWritableDatabase();
	    String query = "SELECT  * FROM " + TABLE_NAME + " WHERE ID > " + lastIDsent + " ORDER BY id ASC LIMIT " + lastXrows;
	    final Cursor cursor = db.rawQuery(query, null);
        //Log.d("Gathered Rows to Send Using Query", query);
        
	    // Create a thread to go through all the rows and send each row one
	    // at a time to the server. 
	    Thread t = new Thread() {
			public void run() {
				Looper.prepare(); // For Preparing Message Pool for the child Thread
				HttpClient client = new DefaultHttpClient();
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); // Timeout Limit
				
				try {
					JSONObject json = new JSONObject(); // the JSON request
	                HttpResponse response;
	                
					// set the server to use
					//
					HttpPost post = new HttpPost("http://drivestrive.com/api/post_data");
					// HttpPost("http://192.168.1.65:3000/api/post_data");

					// create and send JSON requests from DB to Server
					//
				    if (cursor.moveToFirst()) {
				    	do {
							json.put("phone", cursor.getString(1));
							json.put("latitude", cursor.getString(2));
							json.put("longitude", cursor.getString(3));
							json.put("accel", cursor.getString(4));
							json.put("gyro", cursor.getString(5));
							json.put("speed", cursor.getString(6));
							json.put("time", cursor.getString(7));
							StringEntity se = new StringEntity(json.toString());
							se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
							post.setEntity(se);
							//Log.d("Sending JSON Post to Server", json.toString());
		                    response = client.execute(post);        

							// Now lets look at the response
							//
		                    int responseCode = response.getStatusLine().getStatusCode();
		                    switch(responseCode) {
		                    case 201:
		                    	HttpEntity entity = response.getEntity();
		                        if(entity != null) {
		                            String responseBody = EntityUtils.toString(entity);
		                            if (responseBody.equals("\"1\"")) {															// update last successful sent
		                        		SharedPreferences.Editor editor = settings.edit();									// get object to edit setting
		                        		editor.putInt("lastIDsent", Integer.parseInt(cursor.getString(0))); 				// edit the setting
		                        		editor.commit(); // save the setting
		    							//Log.d("JSON Post Successful, updating lastIDsent", cursor.getString(0));		
		                            }else{
		                            }
		                        }
		                        break;
		                    } 
				        } while (cursor.moveToNext());
				    }
				} catch (Exception e) {
					//e.printStackTrace();
					//Log.d("Unable to Connect", "Server is unreachable");
				}
				Looper.loop(); // Loop in the message queue
			}
		};
		t.start();
	}
    
}
