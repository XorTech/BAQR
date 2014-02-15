package com.baqr.coptic;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.baqr.baqr.R;

public class CopticOptions extends PreferenceActivity  {
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);		
		addPreferencesFromResource(R.xml.coptic_preferences);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	/**
	 * Function to listen for options item selected
	 * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
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
        // TODO Auto-generated method stub
        super.onBackPressed();
    }
}
