package com.baqr.maps;

import java.util.ArrayList;

import com.baqr.database.MessageData;
import com.baqr.database.MsgDatabaseHandler;
import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;

public class BaqrMapsWithMe extends Activity {
	
	private static final int DOUBLE_ZERO = 00;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        // Map tags if available else close
        MapMyTags();
              
    }
    
    /**
	 * Function to get tag data from DB
	 * */
	public void MapMyTags() {
		//MapMyData(34.39483, -104.38493, "Billy\n29 Seconds Ago", 1);
		//finish();
		// Initialize db and arrayList for db elements
		MsgDatabaseHandler mdb = new MsgDatabaseHandler(getApplicationContext());
		ArrayList<MessageData> message_array_from_db = mdb.Get_Messages();
		
		// Check if tags exist. If so, process 
		if (message_array_from_db.size() > 0) {
			
			for (int i = 0; i < message_array_from_db.size(); i++) {

			    String tag = message_array_from_db.get(i).getTag();
			    String latitude = message_array_from_db.get(i).getLatitude();
			    String longitude = message_array_from_db.get(i).getLongitude();
			    String _time = message_array_from_db.get(i).getTime();
			   		    
			    Double lat = Double.parseDouble(latitude);
			    Double lon = Double.parseDouble(longitude);
			     
		        // Process Time
			    Long then = Long.parseLong(_time);
				Long now = System.currentTimeMillis();
				String difference = getDifference(now, then);
				
				// counter for time split
				int colon = 0;
				
				// Count colons for proper output
				for(int ix = 0; ix < difference.length(); ix++) {
				    if(difference.charAt(ix) == ':') colon++;
				}
				
				// Split the difference by colon
				String[] splitDiff = difference.split(":");
				String hours = null, minutes = null, seconds = null, str = null;
				
				switch (colon) {
				case 1:
					if (Integer.parseInt(splitDiff[0]) == DOUBLE_ZERO) {
						seconds = splitDiff[1];
						if (Integer.parseInt(seconds) > 1) {
							str = "Occurred: " + splitDiff[1] + " seconds ago";
						}
						else if (Integer.parseInt(seconds) == 1) {
							str = "Occurred: " + splitDiff[1] + " second ago";
						}
						else if (Integer.parseInt(seconds) == DOUBLE_ZERO) {
							str = "Occurred: " + "Happening Now";
						}		
					}
					else {
						minutes = splitDiff[0];
						if (Integer.parseInt(minutes) > 1) {						
							str = "Occurred: " + splitDiff[0] + " minutes ago";						
						}
						else {
							str = "Occurred: " + splitDiff[0] + " minute ago";
						}		
					}
					break;
				case 2:
					hours = splitDiff[0];
					if (Integer.parseInt(hours) > 1) {
						str = "Occurred: " + splitDiff[0] + " hours ago";
					}
					else {
						str = "Occurred: " + splitDiff[0] + " hour ago";
					}	
					break;
				default:
					str = "Happening Now";
				}
				
				String mPoint = tag + "\n" + str;
				// Call Maps with Me and plot points on the map
				MapMyData(lat, lon, mPoint, message_array_from_db.size());
			}
			mdb.close();
		}
		else {
			finish();
		}
	}
	
	public void MapMyData(Double lat, Double lon, String name, int size) {
		try {
			// Convert objects to MMWPoints
		    final MWMPoint[] points = new MWMPoint[size];
		    
		    for (int ix = 0; ix < size; ix++) {
		    	// Get lat, lon, and name from object and assign it to new MMWPoint
		        points[ix] = new MWMPoint(lat, lon, name);
		    }
		    // Show all point on the map, you could also provide some title
		    MapsWithMeApi.showPointsOnMap(this, "Total Points: " + size, points);
		}
		catch (Exception e) {
			Log.e("Error loading points from Map With Me", " " + e);
		}
	}
	
	/**
	 * Function to check time difference of SMS and current time
	 * */
	public String getDifference(long now, long then){
		
		try {
	        if(now > then) {
	            return DateUtils.formatElapsedTime((now - then)/1000L);
	        }
	        else {
	            return "Time Error!";
	        }
		}
		catch (Exception e) {
			Log.e("Error: ", "" + e);
			return "Time Error!";
		}
    }
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
