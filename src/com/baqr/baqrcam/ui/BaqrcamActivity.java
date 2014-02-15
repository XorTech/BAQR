package com.baqr.baqrcam.ui;

import com.baqr.baqr.BaqrApplication;
import com.baqr.baqr.R;
import com.baqr.baqrcam.api.CustomHttpServer;
import com.baqr.baqrcam.api.CustomRtspServer;
import com.baqr.baqrcam.http.TinyHttpServer;
import com.baqr.baqrcam.streaming.SessionBuilder;
import com.baqr.baqrcam.streaming.rtsp.RtspServer;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/** 
 * Baqrcam basically launches an RTSP server and an HTTP server, 
 * clients can then connect to them and start/stop audio/video streams on the phone.
 */
public class BaqrcamActivity extends FragmentActivity {

	static final public String TAG = "Baqrcam";

	private ViewPager mViewPager;
	private PowerManager.WakeLock mWakeLock;
	private SectionsPagerAdapter mAdapter;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private BaqrApplication mApplication;
	private CustomHttpServer mHttpServer;
	private RtspServer mRtspServer;

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mApplication = (BaqrApplication) getApplication();
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.baqrcam_baqrcam);

		mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.handset_pager);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mSurfaceView = (SurfaceView)findViewById(R.id.handset_camera_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		// We still need this line for backward compatibility reasons with android 2
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		SessionBuilder.getInstance().setSurfaceHolder(mSurfaceHolder);

		mViewPager.setAdapter(mAdapter);

		// Prevents the phone from going to sleep mode
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wakelock");

		// Starts the service of the HTTP server
		this.startService(new Intent(this,CustomHttpServer.class));

		// Starts the service of the RTSP server
		this.startService(new Intent(this,CustomRtspServer.class));

	}

	public void onStart() {
		super.onStart();

		// Lock screen
		mWakeLock.acquire();

		// Did the user disabled the notification ?
		if (mApplication.notificationEnabled) {
			Intent notificationIntent = new Intent(this, BaqrcamActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			Notification notification = builder.setContentIntent(pendingIntent)
					.setWhen(System.currentTimeMillis())
					.setTicker(getText(R.string.notification_title))
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(getText(R.string.notification_title))
					.setContentText(getText(R.string.notification_content)).build();
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(0,notification);
		} else {
			removeNotification();
		}

		bindService(new Intent(this,CustomHttpServer.class), mHttpServiceConnection, Context.BIND_AUTO_CREATE);
		bindService(new Intent(this,CustomRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);

	}

	@Override
	public void onStop() {
		super.onStop();
		// A WakeLock should only be released when isHeld() is true !
		if (mWakeLock.isHeld()) mWakeLock.release();
		if (mHttpServer != null) mHttpServer.removeCallbackListener(mHttpCallbackListener);
		unbindService(mHttpServiceConnection);
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
	}

	@Override
	public void onResume() {
		super.onResume();
		mApplication.applicationForeground = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		mApplication.applicationForeground = false;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"Baqrcam destroyed");
		super.onDestroy();
	}

	@Override    
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override    
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.baqrcam_menu, menu);
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
			intent = new Intent(this.getBaseContext(),OptionsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		case R.id.quit:
			quitSpydroid();
			return true;
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void quitSpydroid() {
		// Removes notification
		if (mApplication.notificationEnabled) removeNotification();       
		// Kills HTTP server
		this.stopService(new Intent(this,CustomHttpServer.class));
		// Kills RTSP server
		this.stopService(new Intent(this,CustomRtspServer.class));
		// Returns to home menu
		finish();
	}
	
	private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mRtspServer = (CustomRtspServer) ((RtspServer.LocalBinder)service).getService();
			mRtspServer.addCallbackListener(mRtspCallbackListener);
			mRtspServer.start();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}

	};

	private RtspServer.CallbackListener mRtspCallbackListener = new RtspServer.CallbackListener() {

		@Override
		public void onError(RtspServer server, Exception e, int error) {
			// We alert the user that the port is already used by another app.
			if (error == RtspServer.ERROR_BIND_FAILED) {
				new AlertDialog.Builder(BaqrcamActivity.this)
				.setTitle(R.string.port_used)
				.setMessage(getString(R.string.bind_failed, "RTSP"))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivityForResult(new Intent(BaqrcamActivity.this, OptionsActivity.class),0);
					}
				})
				.show();
			}
		}

		@Override
		public void onMessage(RtspServer server, int message) {
			if (message==RtspServer.MESSAGE_STREAMING_STARTED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
			} else if (message==RtspServer.MESSAGE_STREAMING_STOPPED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
			}
		}

	};	

	private ServiceConnection mHttpServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mHttpServer = (CustomHttpServer) ((TinyHttpServer.LocalBinder)service).getService();
			mHttpServer.addCallbackListener(mHttpCallbackListener);
			mHttpServer.start();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}

	};

	private TinyHttpServer.CallbackListener mHttpCallbackListener = new TinyHttpServer.CallbackListener() {

		@Override
		public void onError(TinyHttpServer server, Exception e, int error) {
			// We alert the user that the port is already used by another app.
			if (error == TinyHttpServer.ERROR_HTTP_BIND_FAILED ||
					error == TinyHttpServer.ERROR_HTTPS_BIND_FAILED) {
				String str = error==TinyHttpServer.ERROR_HTTP_BIND_FAILED?"HTTP":"HTTPS";
				new AlertDialog.Builder(BaqrcamActivity.this)
				.setTitle(R.string.port_used)
				.setMessage(getString(R.string.bind_failed, str))
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						startActivityForResult(new Intent(BaqrcamActivity.this, OptionsActivity.class),0);
					}
				})
				.show();
			}
		}

		@Override
		public void onMessage(TinyHttpServer server, int message) {
			if (message==CustomHttpServer.MESSAGE_STREAMING_STARTED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
				if (mAdapter != null && mAdapter.getPreviewFragment() != null)	
					mAdapter.getPreviewFragment().update();
			} else if (message==CustomHttpServer.MESSAGE_STREAMING_STOPPED) {
				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
					mAdapter.getHandsetFragment().update();
				if (mAdapter != null && mAdapter.getPreviewFragment() != null)	
					mAdapter.getPreviewFragment().update();
			}
		}

	};

	private void removeNotification() {
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(0);
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
			case 0: return new HandsetFragment();
			case 1: return new PreviewFragment();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 2;
		}

		public HandsetFragment getHandsetFragment() {
			return (HandsetFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":0");

		}

		public PreviewFragment getPreviewFragment() {
			return (PreviewFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.handset_pager+":1");

		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: return getString(R.string.page0);
			case 1: return getString(R.string.page1);
			}        		

			return null;
		}

	}
}