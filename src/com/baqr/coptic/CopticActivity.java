package com.baqr.coptic;

import com.baqr.baqr.R;
import com.baqr.extras.MyUpdateReceiver;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class CopticActivity extends FragmentActivity {
	
	SharedPreferences preferences;
	
	private ViewPager mViewPager;
	private SectionsPagerAdapter mAdapter;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);	
	
		// Set back button as home
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Get preferences
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Listens to changes of preferences
		// preferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
		
		setContentView(R.layout.baqrcam_baqrcam);

		mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.handset_pager);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSurfaceView = (SurfaceView)findViewById(R.id.handset_camera_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		// We still need this line for backward compatibility reasons with android 2
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mViewPager.setAdapter(mAdapter);

	}

	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
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
	public void onDestroy() {
		super.onDestroy();
	}

	@Override    
	public void onBackPressed() {
		super.onBackPressed();
		quitCoptic();
	}

	@Override    
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.baqrbluetooth_menu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), 1);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), 1);
		return true;
	}

	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.options:
			// Starts QualityListActivity where user can change the streaming quality
			intent = new Intent(CopticActivity.this, CopticOptions.class);
			startActivityForResult(intent, 0);
			return true;
		case R.id.quit:
			quitCoptic();
			return true;
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void quitCoptic() {
        try {
        	MyUpdateReceiver.trimCache(this);
        	
        	CopticChat.stopTask();
        } catch (Exception e) {
            e.printStackTrace();
        }
		finish();	
	}

	public void log(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
				switch (i) {
				case 0: return new CopticChat(CopticActivity.this);
				case 1: return new CopticMap(CopticActivity.this);
				}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		public CopticChat getHandsetFragment() {
			return (CopticChat) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":0");
		}

		public CopticMap getPreviewFragment() {
			return (CopticMap) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":1");
		}

		@Override
		public CharSequence getPageTitle(int position) {

			switch (position) {
			case 0: return getString(R.string.copticPage0);
			case 1: return getString(R.string.copticPage1);   		
			}
			return null;
		}
	}
}
