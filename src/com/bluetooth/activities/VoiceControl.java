package com.bluetooth.activities;

import java.util.ArrayList;
import java.util.List;

import com.baqr.baqr.BaqrApplication;
import com.baqr.baqr.R;
import com.bluetooth.BluetoothActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;

/**
 * This activity uses the built-in speech recognition routines and listens to
 * instructions. The instructions are then translated in commands to be sent to
 * the device. The available commands are in the "commands" string array.
 */
public class VoiceControl extends BluetoothActivity
{
	private LogView tvData;
	// Available commands
	private static final String[] commands = {"go", "back", "left", "right", "stop", "fast", "slow"};
	// TODO: Make it so the user can edit, add and remove commands
	private int speed;
	boolean foundCommand;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baqrblue_voice_control);

		tvData = (LogView) findViewById(R.id.tvData);
		Button bSpeak = (Button) findViewById(R.id.bSpeak);

		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if(activities.size() == 0)
		{
			bSpeak.setEnabled(false);
			bSpeak.setText("Speech recognizer not present");
		}
	}

	@Override
	protected void onResume()
	{
		preventCancel = false;
		speed = 20;
		super.onResume();
	}

	/**
	 * This is triggered by the button, it's binded inside the layout XML file.
	 * 
	 * @param v
	 */
	public void speakButtonClicked(View v)
	{
		startVoiceRecognitionActivity();
	}

	private void startVoiceRecognitionActivity()
	{
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		preventCancel = true;
		startActivityForResult(intent, BaqrApplication.MSG_1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == BaqrApplication.MSG_1 && resultCode == RESULT_OK)
		{
			// ArrayList contains the words the speech recognition thought it heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			foundCommand = false;
			for(String command : commands)
			{
				// If the array contains at least one word that's in the commands array
				if(matches.contains(command))
				{
					foundCommand = true;
					tvData.append("Command: " + command + "\n");
					if(command == "go")
					{
						write("s," + speed + "," + speed);
					}
					else if(command == "left")
					{
						write("s," + (-speed) + "," + speed);
					}
					else if(command == "right")
					{
						write("s," + speed + "," + (-speed));
					}
					else if(command == "back")
					{
						write("s," + (-speed) + "," + (-speed));
					}
					else if(command == "stop")
					{
						write("s,0,0");
					}
					else if(command == "fast" && speed <= 90)
					{
						speed += 10;
						tvData.append("New speed: " + speed + "\n");
					}
					else if(command == "slow" && speed > 10)
					{
						speed -= 10;
						tvData.append("New speed: " + speed + "\n");
					}
					break;
				}
			}

			if(!foundCommand)
			{
				tvData.append("Command not recognised\n");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		super.handleMessage(msg);
		switch(msg.what)
		{
		case BaqrApplication.MSG_READ:
			tvData.append("Read: " + msg.obj + "\n");
			break;
		case BaqrApplication.MSG_WRITE:
			tvData.append("Sent: " + msg.obj + "\n");
			break;
		}
		return super.handleMessage(msg);
	}
}
