package com.baqr.locate;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.baqr.baqr.R;
import com.baqr.database.MyPanicNumbers;
import com.baqr.database.PanicDatabaseHandler;
import com.baqr.maps.GPSTracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class BaqrSend extends Fragment {
	
	// Constants
	private static final int THREE_SECONDS = 3000;	
	private static final long ONE_SECOND = 1000L;
	private static final long ONE_MINUTE = 60000L;
	private static final Double FIVE_DIGIT = 100000.0D;
	private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
	private static final String PANIC_TEXT = "Panic";
	private static final String STOP_TEXT = "Stop";	
	private static final String DEFAULT_TAGID = "BAQR";
	private static final String MESSAGE = "Location Update";
	private static final String PANIC_MESSAGE = "Panic Initiated!";
	private static final String CMD_ZERO = "0";
	private static final String COORDS_CMD = "*11*:";
	private static final String PANIC_CMD = "*911*:";
	
	// Instances
	private Button btnShowLocation;
	private Button panicButton;
	private SharedPreferences preferences;
	private Context context;
	private Timer mytimer;
	private Vibrator vibrator;
	private GPSTracker gps;
	
	// Initialize variables
	private boolean panic = false;
	private double latitude = 0.0;
	private double longitude = 0.0;
	private String uid = null;
	private boolean isSolo = false;
	private String panicMsg = null;
	
	/**
	 * SenderSend Constructor
	 * */
	public BaqrSend(Context ctx) {
		context = ctx;
	}
	
	/**
	 * SenderSend OnCreate
	 * */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.baqr_send, container, false);
		
		// GPS Instance
        gps = new GPSTracker(context);
        gps.getLocation();
		
		// Buttons for main layout
		panicButton = (Button) rootView.findViewById(R.id.panicBtn);
        btnShowLocation = (Button) rootView.findViewById(R.id.sendSABtn);
		vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		// Load preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(context); 
        isSolo = preferences.getBoolean("runSolo", false);
		
    	/**
    	 * Normal location sender button for sending coordinates
    	 * */
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {	
				
				uid = preferences.getString("tagID", DEFAULT_TAGID);
				
		        latitude = GetLat();
		        longitude = GetLong();
		        
		        try {
		            if (gps != null) {		
		            	if (isSolo) {
		            		String sendMsg = ("TagID: " + uid + "\n" + GOOGLE_STRING + String.valueOf(latitude) + "," + String.valueOf(longitude) + "(" + uid + ")");
		            		SendToMyPanicNumbers(sendMsg);
		            	} else {
		            		StringBuilder sendMulti = new StringBuilder(COORDS_CMD);
		            		sendMulti.append(String.valueOf(latitude) + ":" + String.valueOf(longitude) + ":" + CMD_ZERO + ":" + MESSAGE);
		            		SendToMyPanicNumbers(String.valueOf(sendMulti)); 	
		            	}
		            
		            	Toast.makeText(context, "Your Location is - \nLat: " + String.valueOf(latitude) + "\nLong: " + String.valueOf(longitude), Toast.LENGTH_LONG).show();
		            }
		        } catch (Exception e) {
		        	Log.e("Error sending SMS: ", "" +e );
		        }
			}
		});      
		
    	/**
    	 * Listener for panic button. Sends message every minute for ten minutes
    	 * */
        panicButton.setOnClickListener(new View.OnClickListener() {
        	
        	@Override
        	public void onClick(View arg0) {
        		
        		// Change adjust panic button text based on boolean
        		if (panic == false) {
        			panic = true;
        			panicButton.setText(STOP_TEXT);
        		}
        		else if (panic == true) {
        			panic = false;
        			panicButton.setText(PANIC_TEXT);
        		}
        		
        		if (panic == true) {        			
        			latitude = GetLat();
        			longitude = GetLong();
        			
        			if (isSolo) {
        				panicMsg = "Panic!\n" + GOOGLE_STRING + String.valueOf(latitude) + "," + String.valueOf(longitude) + "(" + uid + ")";
        			}
        			else {
        				StringBuilder sendMulti = new StringBuilder(PANIC_CMD);
	            		sendMulti.append(String.valueOf(latitude) + ":" + String.valueOf(longitude) + ":" + CMD_ZERO + ":" + PANIC_MESSAGE);
	            		SendToMyPanicNumbers(String.valueOf(sendMulti)); 	
        			}
		                    			
        			// Start timer
        			mytimer = new Timer(true);

                    final TimerTask mytask = new TimerTask() {
                        public void run() {
                        	SendToMyPanicNumbers(panicMsg);
                        }
                    };
                    
                    // Start timer after 1 second and send new SMS every 60 seconds
                    mytimer.schedule(mytask, ONE_SECOND, ONE_MINUTE);
            		
                    // Vibrate for 3000 milliseconds or 3 seconds
            		vibrator.vibrate(THREE_SECONDS);
            		
            		// Display activation
            		Toast.makeText(context, "Panic Activated!", Toast.LENGTH_LONG).show();
        		}
        		// Cancel panic
        		else if (panic == false) {
        			// Cancel timer
        			mytimer.cancel();
        			
        			// Display cancel, set panic false, and revert text
					Toast.makeText(context, "Panic Cancelled", Toast.LENGTH_LONG).show();
					panic = false;
					panicButton.setText(PANIC_TEXT);
        		}
        	}
        }); 
        return rootView;
	}
	
	private Double GetLat() {
		gps.getLocation();
		return Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT;	
	}
	
	private Double GetLong() {
		return Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT;
	}
	
	/**
	 * Function to send SMS to tags
	 * */
	public void SendToMyPanicNumbers(String msg) {
    	PanicDatabaseHandler mytdb = new PanicDatabaseHandler(context);
    	ArrayList<MyPanicNumbers> mytag_array_from_db = mytdb.Get_Numbers();
    	
		for (int i = 0; i < mytag_array_from_db.size(); i++) {
			String mobile = null;
			mobile = mytag_array_from_db.get(i).getMyPanicPhoneNumber();			
			SmsManager.getDefault().sendTextMessage(mobile, null, msg, null, null);
		}
		mytdb.close();
	}
}
