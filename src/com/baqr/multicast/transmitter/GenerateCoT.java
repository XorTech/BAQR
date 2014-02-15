package com.baqr.multicast.transmitter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.baqr.maps.GPSTracker;
import com.baqr.multicast.GenerateUUID;

public class GenerateCoT {
	
	private static String MULTICAST_ADDR = "224.10.10.1";
	private static String MULTICAST_PORT = "17012";
	private static String DEFAULT_ROOM = "All Chat Rooms";
	private static final Double FIVE_DIGIT = 100000.0D;
	
	private static String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static String time="2005-04-05T11:43:38.07Z";
	private static String start="2005-04-05T11:43:38.07Z";
	private static String stale="2005-04-05T11:45:38.07Z";
	
	private static String message = null;
	private static String latitude = null;
	private static String longitude = null;
	private static String room = null;
	private static String randomUUID = null;
	private static String uid = null;
	
	private static String mCastAddr = null;
    private static String mCastPort = null;
    
	private static SharedPreferences preferences;
	private static GPSTracker gps;
	
	private static Context context;
	
	public synchronized static String GenerateChat(String msg, Context ctx) {
		
		message = msg;
		context = ctx;
		
		// New GPS instance
		gps = new GPSTracker(context);
		
		// Get and Set CoT elements
		LoadPreferences(context);
		GenerateTimeDate();
		SetLatitude();
		SetLongitude();
		SetUUID(context);
				
		// Generate CoT string
		String buildCoT = String.format("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" +
				"<event version='2.0' uid='GeoChat.%s.%s.%s' type='b-t-f' time='%s' start='%s' stale='%s' how='h-g'>" +
				"<point lat='%s' lon='%s' hae='0.0' ce='0.0' le='0.0' />" +
				"<detail><__chat chatroom='%s'/>" +
				"<link uid='%s' type='a-f-G-U-C-I' relation='p-p'/>" +
				"<remarks source='BAO.F.ATAK.%s' time='%s'>%s</remarks>" +
				"<__serverdestination destinations='udp:%s:%s'/></detail></event>", uid, room, randomUUID, start, start, stale, latitude, longitude, room, uid, uid, time, message, mCastAddr, mCastPort);
		
		return buildCoT;		
	}
	
	public synchronized static String RelayChat(String msg, String id, String lat, String lon, Context ctx) {
		
		context = ctx;
		message = msg;
		latitude = lat;
		longitude = lon;
		uid = id;

		LoadPreferences(context);
		GenerateTimeDate();
		SetUUID(context);
		
		// Generate CoT string
		String buildCoT = String.format("<?xml version='1.0' encoding='UTF-8' standalone='yes'?>" +
				"<event version='2.0' uid='GeoChat.%s.%s.%s' type='b-t-f' time='%s' start='%s' stale='%s' how='h-g'>" +
				"<point lat='%s' lon='%s' hae='0.0' ce='0.0' le='0.0' />" +
				"<detail><__chat chatroom='%s'/>" +
				"<link uid='%s' type='a-f-G-U-C-I' relation='p-p'/>" +
				"<remarks source='BAO.F.ATAK.%s' time='%s'>%s</remarks>" +
				"<__serverdestination destinations='udp:%s:%s'/></detail></event>", uid, room, randomUUID, start, start, stale, latitude, longitude, room, uid, uid, time, message, mCastAddr, mCastPort);
		
		
		return buildCoT; 
		
	}
	
	public static void GenerateTimeDate() {
		// Get time and format
		Date now = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		time = sdf.format(now);
			
		// Start time equals time now
		start = time;
		
		// Set time two hours ahead for expiration time
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+2"));
		
		stale = sdf.format(now);
	}
	
	public static void LoadPreferences(Context context) {
		// Load preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(context); 
        mCastAddr = preferences.getString("ipAddrOne", MULTICAST_ADDR);
        mCastPort = preferences.getString("multiPort", MULTICAST_PORT);
        room = preferences.getString("chatRoomName", DEFAULT_ROOM);
        uid = preferences.getString("multicastName", "BAQR-001");
	}
	
	public static void SetLatitude() {
        gps.getLocation();
        
        latitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT);
   	}
	
	public static void SetLongitude() {
		longitude = String.valueOf(Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT);
	}
	
	public static void SetUUID(Context context) {
		randomUUID = GenerateUUID.GenerateRandomUUID(context);
	}
}
