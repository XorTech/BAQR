package com.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import com.baqr.baqr.BaqrApplication;
import com.baqr.baqr.R;
import com.baqr.extras.MyUpdateReceiver;
import com.bluetooth.Device;
import com.bluetooth.DeviceListBaseAdapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/**
 * This class manages the initial Bluetooth device discovery and shows them in a
 * list. Paired devices and available devices are in different lists, available
 * devices also show the signal strength.
 */
public class DeviceSelectActivity extends Activity implements Handler.Callback
{
	private ArrayList<Device> devAvailableList, devPairedList;
	private DeviceListBaseAdapter devAvailableListAdapter, devPairedListAdapter;
	private ListView devAvailableListView, devPairedListView;
	private ProgressDialog connectionProgressDialog;
	private Button bFindDevices;
	private BluetoothAdapter bluetoothAdapter;
	private BaqrApplication appState;

	private static final String TAG = "DeviceSelect";

	private static final int ACTION_LIST = 0;
	private static final int BT_ENABLE = 1;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Request the spinner in upper right corner
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.baqrblue_device_select);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Setup Bluetooth devices lists with custom rows
		devPairedListView = (ListView) findViewById(R.id.lvPairedDevices);
		devPairedList = new ArrayList<Device>();
		devPairedListAdapter = new DeviceListBaseAdapter(this, devPairedList);
		devPairedListView.setAdapter(devPairedListAdapter);
		devPairedListView.setOnItemClickListener(deviceClickListener);

		devAvailableListView = (ListView) findViewById(R.id.lvAvailableDevices);
		devAvailableList = new ArrayList<Device>();
		devAvailableListAdapter = new DeviceListBaseAdapter(this, devAvailableList);
		devAvailableListView.setAdapter(devAvailableListAdapter);
		devAvailableListView.setOnItemClickListener(deviceClickListener);

		appState = (BaqrApplication) getApplicationContext();

		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// Register a receiver to handle Bluetooth actions
		registerReceiver(Receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(Receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

		// Setup bottom button to search for devices
		bFindDevices = (Button) findViewById(R.id.bFindDevices);
		bFindDevices.setOnClickListener(new Button.OnClickListener()
		{
			public void onClick(View v)
			{
				startDiscovery();
			}
		});

		// Automatically start discovery the first time the application starts
		startDiscovery();
	}

	/**
	 * When a Bluetooth item is clicked this method tries to connect to it. It
	 * shows a dialog that informs the user it's trying to establish a
	 * connection.
	 */
	final OnItemClickListener deviceClickListener = new OnItemClickListener()
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			// Cancel discovery because it's costly and we're about to connect
			bluetoothAdapter.cancelDiscovery();

			// Get the selected device
			Device device = (Device) parent.getItemAtPosition(position);

			// Show connection dialog and make so that the connection it can be canceled
			connectionProgressDialog = ProgressDialog.show(DeviceSelectActivity.this, "", "Establishing connection...", false, true);
			connectionProgressDialog.setOnCancelListener(new OnCancelListener()
			{
				public void onCancel(DialogInterface di)
				{
					// The user can back out at any moment
					connectionProgressDialog.dismiss();
					appState.disconnect();
					if(BaqrApplication.D) Log.i(TAG, "Canceled connection progress");
					return;
				}
			});

			// Try to connect to the selected device, once connected the handler will do the rest
			appState.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(device.getAddress()));
		}
	};

	/**
	 * When a message comes from either the Bluetooth activity (when enabling
	 * Bluetooth) or the child activity it's processed here.
	 * 
	 * @return
	 */
	public boolean handleMessage(Message msg)
	{
		// In case the connection dialog hasn't disappeared yet
		if(connectionProgressDialog != null)
		{
			connectionProgressDialog.dismiss();
		}

		switch(msg.what)
		{
		case BaqrApplication.MSG_OK:
			// The child activity ended gracefully
			break;
		case BaqrApplication.MSG_CANCEL:
			// The child activity did not end gracefully (connection lost, failed...)
			if(msg.obj != null)
			{
				// If a some text came with the message show in a toast 
				if(BaqrApplication.D) Log.i(TAG, "Message: " + msg.obj);
				Toast.makeText(DeviceSelectActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
			}
			break;
		case BaqrApplication.MSG_CONNECTED:
			// When connected to a device start the activity select
			if(BaqrApplication.D) Log.i(TAG, "Connection successful to " + msg.obj);
			startActivityForResult(new Intent(getApplicationContext(), ActionListActivity.class), ACTION_LIST);
			break;
		}
		return false;
	}

	/**
	 * Start discovering Bluetooth devices. It will check if Bluetooth is
	 * enabled and then disable the search button before searching for visible
	 * devices.
	 */
	public void startDiscovery()
	{
		// Prevent phones without Bluetooth from using this application
		if(!checkBlueToothState())
		{
			finish();
			return;
		}

		// Show search progress spinner
		setProgressBarIndeterminateVisibility(true);
		// Disable button
		bFindDevices.setText(R.string.searching);
		bFindDevices.setEnabled(false);

		// Remove title for available devices
		findViewById(R.id.tvAvailableDevices).setVisibility(View.GONE);

		devPairedList.clear();
		devPairedListAdapter.notifyDataSetChanged();

		// Show already paired devices in the upper list
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0)
		{
			findViewById(R.id.tvPairedDevices).setVisibility(View.VISIBLE);
			for(BluetoothDevice device : pairedDevices)
			{
				// Signal strength isn't available for paired devices
				devPairedList.add(new Device(device.getName(), device.getAddress(), (short) 0));
			}
			// Tell the list adapter that its data has changed so it would update itself
			devPairedListAdapter.notifyDataSetChanged();
		}

		devAvailableList.clear();
		devAvailableListAdapter.notifyDataSetChanged();

		bluetoothAdapter.startDiscovery();
	}

	// Add found device to the devices list
	private final BroadcastReceiver Receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Found a device in range
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Device foundDevice = new Device(device.getName(), device.getAddress(), intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));
				// If it's not a paired device add it to the list
				if(device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					devAvailableList.add(foundDevice);
					// Signal list content change
					devAvailableListAdapter.notifyDataSetChanged();
					// Make the available devices title visible
					findViewById(R.id.tvAvailableDevices).setVisibility(View.VISIBLE);
				}
			}
			else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				// When finished (timeout) remove the progress indicator and re-enable button
				setProgressBarIndeterminateVisibility(false);
				bFindDevices.setText(R.string.search);
				bFindDevices.setEnabled(true);
			}
		}
	};

	/**
	 * This method tells the caller if the Bluetooth is enabled or if it even
	 * exists on the phone.
	 * 
	 * @return State of Bluetooth
	 */
	private boolean checkBlueToothState()
	{
		// Inform user that the phone does not have Bluetooth
		if(bluetoothAdapter == null)
		{
			Toast.makeText(getApplicationContext(), "Bluetooth not available.", Toast.LENGTH_SHORT).show();
			return false;
		}
		else if(!bluetoothAdapter.isEnabled())
		{
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BT_ENABLE);
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode)
		{
		case ACTION_LIST:
			// Send messages from the parent to the handler
			Message.obtain(new Handler(this), resultCode);
			break;
		case BT_ENABLE:
			// For the Bluetooth connection handle messages here
			if(!bluetoothAdapter.isEnabled())
			{
				Toast.makeText(getApplicationContext(), "Bluetooth must be enabled", Toast.LENGTH_SHORT).show();
			}
			else
			{
				startDiscovery();
			}
			break;
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		quitBluetooth();
	}

	@Override
	protected void onResume()
	{
		if(BaqrApplication.D) Log.i(TAG, "Set handler");
		appState.setActivityHandler(new Handler(this));
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		bluetoothAdapter.cancelDiscovery();
		super.onPause();
	}
	
	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
		case R.id.options:
			// Starts QualityListActivity where user can change the streaming quality
			intent = new Intent(DeviceSelectActivity.this, BaqrBluetoothOptions.class);
			startActivityForResult(intent, 0);
			return true;
		case R.id.quit:
			quitBluetooth();
			return true;
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public void quitBluetooth() {
		
		if(bluetoothAdapter != null)
		{
			bluetoothAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(Receiver);
		
        try {
        	MyUpdateReceiver.trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
		finish();	
	}
}