package com.baqr.extras;

import java.util.ArrayList;

import android.content.Context;

import com.baqr.database.MyTags;
import com.baqr.database.TagDatabaseHandler;

public class VerifyTagExists {
	
	Context context;
	
	public VerifyTagExists(Context ctx) {
		context = ctx;
	}
	
	public String VerifyTag (String address) {
		String found = null;
		
		// Get data from DB and check if address exists
    	TagDatabaseHandler mytdb = new TagDatabaseHandler(context);
    	ArrayList<MyTags> mytag_array_from_db = mytdb.Get_Tags();

		for (int ix = 0; ix < mytag_array_from_db.size(); ix++) {		
			String mobile = null;
			String tag = null;
			
			mobile = mytag_array_from_db.get(ix).getMyTagPhoneNumber();
			tag = mytag_array_from_db.get(ix).getMyTag();
			
			// If exists, use the name from our tag DB, not the sender name
			if (address.equals(mobile)) {
				found = tag;
			}
			else if (address.equals("+1" + mobile)) {
				found = tag;
			}
		}
		mytdb.close();
		return found;		
	}
}
