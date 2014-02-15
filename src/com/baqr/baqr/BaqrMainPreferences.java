package com.baqr.baqr;

import com.baqr.baqr.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class BaqrMainPreferences extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);		
		addPreferencesFromResource(R.xml.baqrmain_preferences);
		
        // Add up button functionality to send user back to home
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
