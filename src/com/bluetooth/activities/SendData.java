package com.bluetooth.activities;

import com.baqr.baqr.BaqrApplication;
import com.baqr.baqr.R;
import com.bluetooth.BaqrBluetoothOptions;
import com.bluetooth.BluetoothActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This activity lets the user send absolutely any data to the robot and display
 * whatever the robot response, basically it's an echo.
 */
public class SendData extends BluetoothActivity {
	
	private final static String DEFAULT_CMD = "AT";
	
	// Set up layout
	private Button btnPreset1;
	private Button btnPreset2;
	private Button btnPreset3;
	private Button btnPreset4;
	private Button btnPreset5;
	private Button btnPreset6;
	private Button btnPreset7;
	private Button btnPreset8;
	// private Button btnSendCmd;
	private EditText blueTxtBox;
	// private ListView listView1;
	// private TextView cmdTv;
	private LogView tvData;
	
	// Preference commands
	private String cmd1 = null;
	private String cmd2 = null;
	private String cmd3 = null;
	private String cmd4 = null;
	private String cmd5 = null;
	private String cmd6 = null;
	private String cmd7 = null;
	private String cmd8 = null;
	
	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.baqrblue_send_data);
		
    	SetUpLayout();
    	
		blueTxtBox.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				// When the enter button of the keyboard is pressed the message is sent
				if(actionId == EditorInfo.IME_ACTION_SEND)
				{
					// Sending a command clears the input
					String msg = blueTxtBox.getText().toString();
					if(!msg.equals(""))
					{
						write(msg);
						blueTxtBox.setText("");
					}
				}
				// Must return true or the keyboard will be hidden
				return true;
			}
		});
		
    	btnPreset1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd1 != DEFAULT_CMD) {
					write(cmd1);
					//cmdAdapter.add("<CMD> " + cmd1);
					//cmdAdapter.notifyDataSetChanged();
				}
			}
		});
    	
    	btnPreset2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd2 != DEFAULT_CMD) {
					write(cmd2);
					//cmdAdapter.add("<CMD> " + cmd2);
					//cmdAdapter.notifyDataSetChanged();	
				}
				
			}
		}); 
    	
    	btnPreset3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd3 != DEFAULT_CMD) {
					write(cmd3);
					//cmdAdapter.add("<CMD> " + cmd3);
					//cmdAdapter.notifyDataSetChanged();
				}
				
			}
		});
    	
    	btnPreset4.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd4 != DEFAULT_CMD) {
					write(cmd4);
					//cmdAdapter.add("<CMD> " + cmd4);
					//cmdAdapter.notifyDataSetChanged();	
				}
				
			}
		});
    	
    	btnPreset5.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd5 != DEFAULT_CMD) {
					write(cmd5);
					// cmdAdapter.add("<CMD> " + cmd5);
					// cmdAdapter.notifyDataSetChanged();
				}
				
			}
		});
    	
    	btnPreset6.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd6 != DEFAULT_CMD) {
					write(cmd6);
					//cmdAdapter.add("<CMD> " + cmd6);
					//cmdAdapter.notifyDataSetChanged();		
				}
				
			}
		});
    	
    	btnPreset7.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd7 != DEFAULT_CMD) {
					write(cmd7);
					//cmdAdapter.add("<CMD> " + cmd7);
					//cmdAdapter.notifyDataSetChanged();	
				}
				
			}
		});
    	
    	btnPreset8.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd8 != DEFAULT_CMD) {
					write(cmd8);
					//cmdAdapter.add("<CMD> " + cmd8);
					//cmdAdapter.notifyDataSetChanged();	
				}
				
			}
		});
    	
    	/* btnSendCmd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String cmd = blueTxtBox.getText().toString();
				write(cmd);
				//cmdAdapter.add("<CMD> " + cmd);
				//cmdAdapter.notifyDataSetChanged();
				blueTxtBox.setText("");
				UpdateList();
			}
		}); */
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.baqrbluetooth_menu, menu);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.quit), 1);
		MenuItemCompat.setShowAsAction(menu.findItem(R.id.options), 1);
		
		return true;
	}

	@Override    
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.options:
			// Starts QualityListActivity where user can change the streaming quality
			Intent intent = new Intent(SendData.this, BaqrBluetoothOptions.class);
			startActivityForResult(intent, 0);
			return true;
		case R.id.quit:
			quitSendData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void quitSendData() {
		finish();
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		super.handleMessage(msg);
		switch(msg.what)
		{
		case BaqrApplication.MSG_READ:
			tvData.append("<RESP>: " + msg.obj + "\n");
			break;
		case BaqrApplication.MSG_WRITE:
			tvData.append("<CMD> " + msg.obj + "\n");
			break;
		}
		return false;
	}
	
	public void SetUpLayout() {
    	// Set up the layout
    	btnPreset1 = (Button) findViewById(R.id.btnPreset1);
    	btnPreset2 = (Button) findViewById(R.id.btnPreset2);
    	btnPreset3 = (Button) findViewById(R.id.btnPreset3);
    	btnPreset4 = (Button) findViewById(R.id.btnPreset4);
    	btnPreset5 = (Button) findViewById(R.id.btnPreset5);
    	btnPreset6 = (Button) findViewById(R.id.btnPreset6);
    	btnPreset7 = (Button) findViewById(R.id.btnPreset7);
    	btnPreset8 = (Button) findViewById(R.id.btnPreset8);
    	// btnSendCmd = (Button) findViewById(R.id.btnSendCmd);
    	blueTxtBox = (EditText) findViewById(R.id.blueTxtBox);
    	// listView1 = (ListView) findViewById(R.id.listView1);
    	// cmdTv = (TextView) findViewById(R.id.cmdTv);
    	tvData = (LogView) findViewById(R.id.tvData);
    	
    	// Get preferences
    	preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	cmd1 = preferences.getString("cmd_1", DEFAULT_CMD);
    	cmd2 = preferences.getString("cmd_2", DEFAULT_CMD);
    	cmd3 = preferences.getString("cmd_3", DEFAULT_CMD);
    	cmd4 = preferences.getString("cmd_4", DEFAULT_CMD);
    	cmd5 = preferences.getString("cmd_5", DEFAULT_CMD);
    	cmd6 = preferences.getString("cmd_6", DEFAULT_CMD);
    	cmd7 = preferences.getString("cmd_7", DEFAULT_CMD);
    	cmd8 = preferences.getString("cmd_8", DEFAULT_CMD);
    	
	}
	
	// Update the list view so the most current cmd is above the EditText box
	/*public void UpdateList() {
		
		tvData.post(new Runnable(){
			
			public void run() {
				tvData.setSelection(tvData.getCount() - 1);
			}});
	} */
}
