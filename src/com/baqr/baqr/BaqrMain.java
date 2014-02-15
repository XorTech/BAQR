package com.baqr.baqr;

import com.baqr.extras.MyUpdateReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

public class BaqrMain extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current drop-down position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private final int RQS_GooglePlayServices = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_baqr_main);
		
        final String myPackageName = getPackageName();
        
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

	        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(myPackageName)) {
	        	
	    	    new AlertDialog.Builder(this)
		        .setTitle("KitKat Warning")
		        .setMessage("Due to your running kitkat, all messages will sound the SMS notification. To prevent this, downgrade to JellyBean!")
		        .setNegativeButton(android.R.string.no, null)
		        .setPositiveButton("OK, I Understand", null).create().show();
	        }
        }
        
		// Check status of Google Play Services
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // Check Google Play Service Available
        try {
            if (status != ConnectionResult.SUCCESS) {
            	GooglePlayServicesUtil.getErrorDialog(status, this, RQS_GooglePlayServices).show();
            }
        } catch (Exception e) {
        	Log.e("Error: GooglePlayServiceUtil: ", "" + e);
        }

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_section1),
								getString(R.string.title_section2),
								getString(R.string.title_section3), }), this);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.baqr_main, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), 1);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), 1);
		return true;
	}
	
	public void quitBaqr() {
		MyUpdateReceiver.trimCache(this);
		finish();		
	}
	
	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.options:
			// Starts QualityListActivity where user can change the streaming quality
			Intent optionsIntent = new Intent(BaqrMain.this, BaqrMainPreferences.class);
			startActivityForResult(optionsIntent, 0);
			return true;
		case R.id.quit:
			quitBaqr();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new BaqrCommand(BaqrMain.this);
		Bundle args = new Bundle();
		args.putInt(BaqrCommand.ARG_SECTION_NUMBER, position + 1);
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
		return true;
	}
     
 	@Override
 	public void onStart() {
 		super.onStart();
 	}
 	
 	@Override
 	public void onRestart() {
 		super.onRestart();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		
 		
 	}
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onStop();
 		
 	    try {
 	    	MyUpdateReceiver.trimCache(this);
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 	}
 	
	@Override
	public void onBackPressed() {
		
	    new AlertDialog.Builder(this)
	        .setTitle("Comfirm Exit")
	        .setMessage("Are you sure you want to quit Baqr now?")
	        .setNegativeButton(android.R.string.no, null)
	        .setPositiveButton(android.R.string.yes, new OnClickListener() {

	            public void onClick(DialogInterface arg0, int arg1) {
	                BaqrMain.super.onBackPressed();    
	                quitBaqr();
	            }
	    }).create().show();
	}
}
