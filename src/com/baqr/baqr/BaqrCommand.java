package com.baqr.baqr;

import com.baqr.baqrcam.ui.BaqrcamActivity;
import com.baqr.coptic.CopticActivity;
import com.baqr.locate.BaqrLocateMain;
import com.bluetooth.DeviceSelectActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class BaqrCommand extends Fragment {

	public static final String ARG_SECTION_NUMBER = "section_number";
	
	private Context context;
	private Button videoBtn;
	private Button rVideoBtn;
	private Button bluetoothBtn;
	private Button vpnBtn;
	private Button locationBtn;
	private Button mapBtn;
	
	public BaqrCommand(Context ctx) {
		context = ctx;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.baqr_command, container, false);
		
		videoBtn = (Button) rootView.findViewById(R.id.videoBtn);
		rVideoBtn = (Button) rootView.findViewById(R.id.playVidBtn);
		bluetoothBtn = (Button) rootView.findViewById(R.id.bluetoothBtn);
		vpnBtn = (Button) rootView.findViewById(R.id.vpnBtn);
		locationBtn = (Button) rootView.findViewById(R.id.locationBtn);
		mapBtn = (Button) rootView.findViewById(R.id.mapBtn);
				
		videoBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					Intent videoIntent = new Intent(getActivity(), BaqrcamActivity.class);
					startActivity(videoIntent);	
				} catch (Exception e) {
					Log.e("Error loading video intent: ", "" + e);
				} 
			}
		});
		
		rVideoBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		bluetoothBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				try {
					Intent blueIntent = new Intent(getActivity(), DeviceSelectActivity.class);
					startActivity(blueIntent);	
				} catch (Exception e) {
					Log.e("Error loading video intent: ", "" + e);
				}
			}
		});
		
		vpnBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				PackageManager manager = getActivity().getPackageManager();
				Intent i = manager.getLaunchIntentForPackage("net.openvpn.openvpn");
				
				if (i == null) {
					try {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=net.openvpn.openvpn")));
					} catch (android.content.ActivityNotFoundException anfe) {
					    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=net.openvpn.openvpn")));
					}
				}
				else {
				    i.addCategory(Intent.CATEGORY_LAUNCHER);
				    startActivity(i);
				}
			}
		});
		
		locationBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Intent locationIntent = new Intent(getActivity(), BaqrLocateMain.class);
					startActivity(locationIntent);	
				} catch (Exception e) {
					Log.e("Error loading location intent: ", "" + e);
				}
			}
		});
		
		mapBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent copticIntent = new Intent(getActivity(), CopticActivity.class);
				startActivity(copticIntent);
			}
		});
	
		return rootView;
	}
}
