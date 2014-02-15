package com.bluetooth.activities;

import com.baqr.baqr.BaqrApplication;
import com.baqr.baqr.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PIDSettings extends Dialog implements View.OnClickListener, OnSeekBarChangeListener
{
	private Button bOk, bCancel;
	private SeekBar sbP, sbI, sbD, sbMaxSpeed;
	private TextView tvP, tvI, tvD, tvMaxSpeed;
	private float kp, ki, kd;
	private final Handler hander;
	private int maxSpeed;

	public PIDSettings(Context context, float init_kp, float init_ki, float init_kd, int initSpeed, Handler dataHandler)
	{
		super(context);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setTitle("PID Settings");
		setContentView(R.layout.baqrblue_pid_settings);
		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.settings);

		// Save to handler to send values back
		hander = dataHandler;

		// Load the current values
		kp = init_kp;
		ki = init_ki;
		kd = init_kd;
		maxSpeed = initSpeed;

		// Build the UI and set current values
		sbP = (SeekBar) findViewById(R.id.sbP);
		sbP.setProgress((int) (kp * 100));
		sbP.setOnSeekBarChangeListener(this);
		sbI = (SeekBar) findViewById(R.id.sbI);
		sbI.setProgress((int) (ki * 100));
		sbI.setOnSeekBarChangeListener(this);
		sbD = (SeekBar) findViewById(R.id.sbD);
		sbD.setProgress((int) (kd * 100));
		sbD.setOnSeekBarChangeListener(this);
		sbMaxSpeed = (SeekBar) findViewById(R.id.sbMaxSpeed);
		sbMaxSpeed.setProgress(maxSpeed);
		sbMaxSpeed.setOnSeekBarChangeListener(this);

		tvP = (TextView) findViewById(R.id.tvP);
		tvP.setText(String.format("%.02f", kp));
		tvI = (TextView) findViewById(R.id.tvI);
		tvI.setText(String.format("%.02f", ki));
		tvD = (TextView) findViewById(R.id.tvD);
		tvD.setText(String.format("%.02f", kd));
		tvMaxSpeed = (TextView) findViewById(R.id.tvMaxSpeed);
		tvMaxSpeed.setText(String.valueOf(initSpeed));

		bOk = (Button) findViewById(R.id.bOk);
		bOk.setOnClickListener(this);
		bCancel = (Button) findViewById(R.id.bCancel);
		bCancel.setOnClickListener(this);
	}

	public void onClick(View v)
	{
		switch(v.getId())
		{
		case R.id.bOk:
			float[] control = {kp, ki, kd, maxSpeed};
			// Reuse a message object from the pool
			Message.obtain(hander, BaqrApplication.MSG_2, control).sendToTarget();
		case R.id.bCancel:
			dismiss();
			break;
		}
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		switch(seekBar.getId())
		{
		case R.id.sbP:
			kp = (float) progress / 100;
			tvP.setText(String.format("%.02f", kp));
			break;
		case R.id.sbI:
			ki = (float) progress / 100;
			tvI.setText(String.format("%.02f", ki));
			break;
		case R.id.sbD:
			kd = (float) progress / 100;
			tvD.setText(String.format("%.02f", kd));
			break;
		case R.id.sbMaxSpeed:
			maxSpeed = progress;
			tvMaxSpeed.setText(String.valueOf(progress));
			break;
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar)
	{

	}

	public void onStopTrackingTouch(SeekBar seekBar)
	{

	}
}
