package com.baqr.locate;

import java.util.Locale;


import com.baqr.baqr.R;
import com.baqr.extras.MyUpdateReceiver;
import com.baqr.multipanic.PanicAddMain;
import com.baqr.multitag.TagAddMain;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class BaqrLocateMain extends FragmentActivity implements ActionBar.TabListener {
	
	// Constants
	public static final int DIALOG_ASK_PASSWORD = 1;
	public static final String DEFAULT_CODE = "12345";	
	private static final int ZERO = 0;
	private static final int MENU_TAG = Menu.FIRST;
	private static final int MENU_PANIC = Menu.FIRST + 1;

	private int numberRuns = 0;
		
	// Instances
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	SharedPreferences preferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baqr_main);
		
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Load preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences prefs = getPreferences(MODE_PRIVATE); 
		
		if (prefs.getInt("number_runs", ZERO) == ZERO) {
			// Edit preference +1 for number of runs
			SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
			editor.putInt("number_runs", numberRuns + 1);
			editor.commit();
			
			// Set secret code and mobile id
			InitializeBaqr();
		}
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		// actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.White)));
		actionBar.setStackedBackgroundDrawable(getResources().getDrawable(
	            R.drawable.selector_tab_arsenic));
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {

			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.baqrlocate_main, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), 1);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), 1);
		menu.add(0, MENU_TAG, MENU_TAG, "Tag List");	
		menu.add(0, MENU_PANIC, MENU_PANIC, "Panic Numbers");
		return true;
	}
	
	public void quitBaqrLocate() {
        try {
        	MyUpdateReceiver.trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
		finish();	
	}
	
	@SuppressWarnings("deprecation")
	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.options:
			showDialog(DIALOG_ASK_PASSWORD);
			return true;
		case R.id.quit:
			quitBaqrLocate();
			return true;
		case MENU_TAG:
			try {
				Intent myTagIntent = new Intent(this, TagAddMain.class);
				startActivity(myTagIntent);
			} catch (Exception e) {
				Log.e("Error launching mytags: ", "" + e);
			}
			return true;
		case MENU_PANIC:
			try {
				Intent myPanicIntent = new Intent(this, PanicAddMain.class);
				startActivity(myPanicIntent);
			} catch (Exception e) {
				Log.e("Error launching panic: ", "" + e);
			}
			return true;
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
		    Fragment fragment = null;

		    switch(position){
		    case 0:
		        fragment = new BaqrSend(BaqrLocateMain.this);
		        break;
		    case 1:
		        fragment = new BaqrReceive(BaqrLocateMain.this);
		        break;
		    default:
		    	break;
		    }
		    
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_send).toUpperCase(l);
			case 1:
				return getString(R.string.title_receive).toUpperCase(l);
			}
			return null;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {		
			case DIALOG_ASK_PASSWORD:
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = inflater.inflate(R.layout.baqr_login, (ViewGroup) findViewById(R.id.root));
                final EditText password1 = (EditText) layout.findViewById(R.id.EditText_Pwd1);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Secure Area");
                builder.setMessage("Please enter your passcode to verify your identity.");
                builder.setView(layout);

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int whichButton) {
                        removeDialog(DIALOG_ASK_PASSWORD);
                    }
                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @SuppressWarnings("deprecation")
					public void onClick(DialogInterface dialog, int which) {
                        String strPassword1 = password1.getText().toString();           
                        String locCode = preferences.getString("lockCode", DEFAULT_CODE);
                        // Validate user input
                        if (strPassword1.equals(locCode)) {
                        	Intent intent = new Intent(BaqrLocateMain.this, BaqrLocatePreferences.class);
                        	startActivity(intent);
                        	// Toast successful login
                        	Toast.makeText(BaqrLocateMain.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                        	// Toast fail login
                        	Toast.makeText(BaqrLocateMain.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                        removeDialog(DIALOG_ASK_PASSWORD);
                    }
                });
                AlertDialog passwordDialog = builder.create();
                return passwordDialog;
		}
		return null;
	}
	
	/**
	 * Functions for handling App state
	 * */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	    
	    // Call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
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
		quitBaqrLocate();
		super.onStop();	
	}
	
	@Override
	public void onBackPressed() {
		quitBaqrLocate();
		super.onBackPressed();
	}

 	public void InitializeBaqr() {
		InitializeTagID();
		InitializeSecret();
	}
	
	public void InitializeTagID() {
		final EditText input = new EditText(this);
		
		new AlertDialog.Builder(this)
	    .setTitle("Enter Mobile ID")
	    .setMessage("Enter the name associated with this mobile phone: ")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText(); 
	            Editor editor = preferences.edit();
	            editor.putString("tagID", String.valueOf(value));
	            editor.commit();
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
	
	public void InitializeSecret() {
		final EditText input = new EditText(this);
		
		new AlertDialog.Builder(this)
	    .setTitle("Enter Secret")
	    .setMessage("Enter the secret associated with this mobile phone: ")
	    .setView(input)
	    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            Editable value = input.getText(); 
	            Editor editor = preferences.edit();
	            editor.putString("secretCode", String.valueOf(value));
	            editor.commit();
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
}
