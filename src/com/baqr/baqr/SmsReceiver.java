package com.baqr.baqr;

import java.util.Timer;
import java.util.TimerTask;

import com.baqr.baqr.R;
import com.baqr.database.MessageData;
import com.baqr.database.MsgDatabaseHandler;
import com.baqr.extras.MyUpdateReceiver;
import com.baqr.extras.VerifyTagExists;
import com.baqr.maps.GPSTracker;
import com.baqr.multicast.SendMulticast;
import com.bluetooth.BluetoothActivity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {	
	
	private static final String SMS_EXTRA_NAME = "pdus";   
    private static final String CMD_A = "*1*";
    private static final String CMD_A1 = "*11*:";
    private static final String CMD_B = "*2*";
    private static final String CMD_C = "*3*";
    private static final String CMD_D = "*999*";
    private static final String CMD_911 = "*911*";
    private static final String GOOGLE_STRING = "http://maps.google.com/maps?q=";
    private static final String DEFAULT_CODE = "1234";
    private static final String DEFAULT_TAGID = "BAQR";
    private static final String PANIC_TXT = "Panic Detected!";
    private static final String UNDER_DURRESS = " is under durress!";
    private static final String BEGIN_PATH = "android.resource://";
    private static final String FILE_PATH = "/raw/panic";
    private static final Double FIVE_DIGIT = 100000.0D;
    private static final long FIVE_SECONDS = 5000L;
    private static final String NO_COORDS = "Unable to retrieve coordinates from: ";
    private static final String DEFAULT_MSG = "Location Update";
    private static final String DEFAULT_CMD = "0";
	
    private SharedPreferences preferences;
    private NotificationManager notificationManager;
    private Timer mytimer;	
    private Uri soundUri;
    private TimerTask mytask;
    private Context context;
    private GPSTracker gps;
		
    private String secretCode = null;
    private String uid = null;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean baseEnabled = false;
    private String receiveLatitude = null;
    private String receiveLongitude = null;
    private String receiveCMD = null;
    private String receiveMsg = null;
    private String _time = null;
    private boolean isSenderMsg = false;
    private boolean isKitKat = false;
    
	public void onReceive( final Context ctx, Intent intent ) {
		
		// Check if the user is using API19, KITKAT
		if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
			isKitKat = true;
		}
		
		// Get SMS map from Intent
	    Bundle extras = intent.getExtras();
	    context = ctx;
	    
	    // GPS Instance
        gps = new GPSTracker(context);
	    
        if (extras != null) {
        	
        	// Load preferences into variables
        	preferences = PreferenceManager.getDefaultSharedPreferences(context);
        	secretCode = preferences.getString("secretCode", DEFAULT_CODE);
        	uid = preferences.getString("tagID", DEFAULT_TAGID);
        	baseEnabled = preferences.getBoolean("isBaseStation", false);
        	
        	// Get received SMS array
            Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
            
            for ( int i = 0; i < smsExtra.length; ++i ) {
            	
            	// Get message
            	SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
            	
            	// Parse message data
            	String body = sms.getMessageBody().toString();
            	String address = sms.getOriginatingAddress();
            	long time = System.currentTimeMillis();
            	
            	// Verify if tag exist in our database
				VerifyTagExists vt = new VerifyTagExists(context);
				String addressExists = vt.VerifyTag(address);
				
				// If exists, name the tag as uid, else name it as the phone number
            	if (addressExists != null) {
            		uid = addressExists;
            	}
            	else {
            		uid = address;
            	}
            	
        	    // Get coordinates and create message           	           	       	
    			latitude = GetLat();
    			longitude = GetLong(); 	
    			
    			
    			// Get request and respond with location 
    			if (body.equals(CMD_A + secretCode)) { 
            		StringBuilder respondCmd = new StringBuilder(CMD_A1);
            		respondCmd.append(String.valueOf(latitude) + ":" + String.valueOf(longitude) + ":" + DEFAULT_CMD + ":" + DEFAULT_MSG);
          
    				RespondLocation(1, address, String.valueOf(respondCmd));
    				isSenderMsg = true;
    			}
    			// Get request and respond over bluetooth 
    			else if (body.contains(CMD_B + secretCode)) {    				
    				BluetoothActivity.write(receiveMsg); 
    				
    				isSenderMsg = true;
            	
    			}
    			// Get request and respond with bluetooth socket
    			else if (body.equals(CMD_C + secretCode)) {
    				StringBuilder respondCmd = new StringBuilder();
    				respondCmd.append("Tag_ID:" + uid + ":Location:" + latitude + "," + longitude);
    				RespondLocation(1, address, String.valueOf(respondCmd));
    				
	        	    isSenderMsg = true;
    			}
    			// Get request and respond with Google String only
    			else if (body.contains(CMD_D + secretCode)) {
    				StringBuilder sendMulti = new StringBuilder("TagID: " + uid + "\n");
            		sendMulti.append(GOOGLE_STRING + String.valueOf(latitude) + ":" + String.valueOf(longitude) + "(" + uid + ")");
          
    				RespondLocation(1, address, String.valueOf(sendMulti));
    				
    				isSenderMsg = true;
    			}
    			else if (body.contains(CMD_A1)) {
    				
    				// Parse the body of the SMS message
    				String[] splitBody = body.split(":");
    				receiveLatitude = splitBody[1];
    				receiveLongitude = splitBody[2];
    				receiveCMD = splitBody[3];
    				receiveMsg = splitBody[4];
    				_time = String.valueOf(time);  
    				
    				if (!baseEnabled) {
    					// Add to DB
        				MsgDatabaseHandler dbHandler = new MsgDatabaseHandler(context);  			
        				String toastMsg = null;
        				
        				// Check if address exist for naming purposes
        				if (addressExists == null) {    					   					
        					dbHandler.Add_Message(new MessageData(address, address, receiveLatitude, receiveLongitude, _time));
        					toastMsg = "Response Received: " + address;
        				}
        				else {
        					dbHandler.Add_Message(new MessageData(addressExists, address, receiveLatitude, receiveLongitude, _time));
        					toastMsg = "Response Received: " + addressExists;
        				} 
        				
        				dbHandler.close();
        				Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show();
    				}
    				else {
    					StringBuilder sendMsg = new StringBuilder();
    					sendMsg.append(receiveLatitude + ":" + receiveLongitude + ":" + receiveCMD + ":" + receiveMsg);
                	
    	        	    try {
    	        	    	// Send multi-cast
    	        	    	SendMulticast sm = new SendMulticast(context);
    	        	    	sm.SendMyMulticast(String.valueOf(sendMsg), uid);
    	        	    } catch (Exception e){
    	        	    	Log.e("Error sending CoT message", "" + e);
    	        	    }
    				}
    				isSenderMsg = true;
    			}
    			else if (body.contains(CMD_911)) {
    				
    				// Override silent 
    				AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    				int max = audio.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
    				audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    				audio.setStreamVolume(AudioManager.STREAM_RING, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    				
    				//Define Notification Manager
    				notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    				
    				// Start timer
        			mytimer = new Timer(true);
        			
        			// Sound location
    				soundUri = Uri.parse(BEGIN_PATH + context.getPackageName() + FILE_PATH);
    				
    				// Timer for notifications
                    mytask = new TimerTask() {
                        public void run() {
            				// Run notification on timer
            				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
            				        .setSmallIcon(R.drawable.emergency)
            				        .setContentTitle(PANIC_TXT)
            				        .setContentText(uid + UNDER_DURRESS + "\nLast Known: " + receiveLatitude + "," + receiveLongitude)
            				        .setSound(soundUri); //This sets the sound to play

            				//Display notification
            				notificationManager.notify(0, mBuilder.build());
                        }
                    };                    
                    // Start timer after 5 seconds of receiving an SMS
                    mytimer.schedule(mytask, FIVE_SECONDS);
                    
        			// If base is enabled, multicast the panic to all out stations
        			if (baseEnabled) {
    					StringBuilder sendMsg = new StringBuilder();
    					sendMsg.append(receiveLatitude + ":" + receiveLongitude + ":" + receiveCMD + ":" + receiveMsg);
                	
    	        	    try {
    	        	    	// Send multi-cast
    	        	    	SendMulticast sm = new SendMulticast(context);
    	        	    	sm.SendMyMulticast(String.valueOf(sendMsg), uid);
    	        	    } catch (Exception e){
    	        	    	Log.e("Error sending CoT message", "" + e);
    	        	    }
        			}
    			}
    			
    	        if (isSenderMsg && !isKitKat) {
    	        	this.abortBroadcast();
    	        	isSenderMsg = false;
    	        }
            }            
        }     
	    // Clear cache on receive
        try {
            MyUpdateReceiver.trimCache(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 * Function to handle sending SMS messages based on the received codes
	 * */
	public void RespondLocation(int code, String addr, String msg) {
		switch (code) {
		case 1:
			if (latitude == 0.0) {
				SmsManager.getDefault().sendTextMessage(addr, null, NO_COORDS + uid, null, null);
			}
			else {
				SmsManager.getDefault().sendTextMessage(addr, null, msg, null, null);
			}
	        break;
		case 2:
			// TODO:
			break;
		default:
			SmsManager.getDefault().sendTextMessage(addr, null, NO_COORDS + uid, null, null);
		}
	}
	
	private Double GetLat() {
		gps.getLocation();
		latitude = Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT;
		return latitude;	
	}
	
	private Double GetLong() {
		longitude = Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT;
		return longitude;
	}
}