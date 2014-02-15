package com.baqr.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.baqr.multicast.transmitter.GenerateCoT;

import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.preference.PreferenceManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SendMulticast extends Service {
	
	private static String MULTICAST_ADDR = "224.10.10.1";
	private static String  MULTICAST_PORT = "17012";
	private static MulticastSocket mSocket;
	
	// CoT Variables and default values
	private String message = null;
	private String command = null;
	private String latitude = null;
	private String longitude = null;
	private String mCastAddr = null;
	private String mCastPort = null;
	private boolean isBaseStation = false;
	
	SharedPreferences preferences;
	InetAddress broadcastAddr = null;
	InetAddress broadcastAddr2 = null;
	Context context;
	
	public SendMulticast(Context ctx) {
		context = ctx;	
	}
	
	public void SendMyMulticast(String msg, String id) {
		
		// Load preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(context); 
        mCastAddr = preferences.getString("ipAddrOne", MULTICAST_ADDR);
        mCastPort = preferences.getString("multiPort", MULTICAST_PORT);
        isBaseStation = preferences.getBoolean("isBaseStation", false);

		// Get WiFi instance
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// If not null, create multicast lock
		if(wifi != null) {
		    WifiManager.MulticastLock lock = wifi.createMulticastLock("Log_Tag");
		    lock.acquire();
		}

		// Required to allow multicast on Android - optionally, adjust developer settings
		ThreadPolicy tp = ThreadPolicy.LAX;
		StrictMode.setThreadPolicy(tp);

		// Parse message for lat, long, cmd, msg
		String[] splitBody = msg.split(":");
		
		latitude = splitBody[0];
		longitude = splitBody[1];
		command = splitBody[2];
		message  = splitBody[3];
		
		if (isBaseStation) {
			try {
			    // Set parameters for CoT message	    	
			    broadcastAddr = InetAddress.getByName(mCastAddr);
				mSocket = new MulticastSocket(Integer.parseInt(mCastPort));		        	    	
				mSocket.joinGroup(broadcastAddr);
				
				String buildCoT = GenerateCoT.RelayChat(message, id, latitude, longitude, context);

				byte[] tMessage = new byte[65535];
				tMessage = buildCoT.getBytes();
				 
				// Build packet and send
				DatagramPacket dPacket = new DatagramPacket(tMessage, tMessage.length, broadcastAddr, Integer.parseInt(mCastPort));
				// System.out.println("Sent: " + dPacket);
				
				try {
				    mSocket.send(dPacket);
				} catch (IOException e) {
				    e.printStackTrace();
				}
				
			} catch (UnknownHostException e1) {
			    e1.printStackTrace();
			} catch (IOException e1) {
			    e1.printStackTrace();
			}
			    
			try {
			    mSocket.leaveGroup(broadcastAddr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	} 
}

