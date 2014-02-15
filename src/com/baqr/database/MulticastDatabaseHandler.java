package com.baqr.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MulticastDatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "multicast_db";

    // Contacts table name
    private static final String TABLE_MULTICAST = "multicast";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_MSG = "message";
    private static final String KEY_CMD = "command";
    private static final String KEY_TIME = "time";
    
    private final ArrayList<MulticastData> message_list = new ArrayList<MulticastData>();

    public MulticastDatabaseHandler(Context context) {
	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
	String CREATE_MESSAGE_TABLE = "CREATE TABLE " + TABLE_MULTICAST + "(" + 
    KEY_ID + " INTEGER PRIMARY KEY," + 
	KEY_UID + " TEXT," + 
	KEY_LAT + " TEXT," +
    KEY_LONG + " TEXT," +
	KEY_CMD + " TEXT," +
    KEY_MSG + " TEXT," +
	KEY_TIME + " TEXT" +
    ")";
	db.execSQL(CREATE_MESSAGE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// Drop older table if existed
	db.execSQL("DROP TABLE IF EXISTS " + TABLE_MULTICAST);

	// Create tables again
	onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    // Adding new msg
    public void Add_Message(MulticastData multicast) {
	SQLiteDatabase db = this.getWritableDatabase();
	ContentValues values = new ContentValues();
	values.put(KEY_UID, multicast.getUID()); 
	values.put(KEY_LAT, multicast.getLatitude()); 
	values.put(KEY_LONG, multicast.getLongitude());
	values.put(KEY_CMD, multicast.getCommand());
	values.put(KEY_MSG, multicast.getMessage());
	values.put(KEY_TIME, multicast.getTime());
	// Inserting Row
	db.insert(TABLE_MULTICAST, null, values);
	db.close(); // Closing database connection
    }

    // Getting single message
    MulticastData Get_Message(int id) {
	SQLiteDatabase db = this.getReadableDatabase();

	Cursor cursor = db.query(TABLE_MULTICAST, new String[] { KEY_ID,
			KEY_UID, KEY_LAT, KEY_LONG, KEY_CMD, KEY_MSG, KEY_TIME }, KEY_ID + "=?",
		new String[] { String.valueOf(id) }, null, null, null, null);
	if (cursor != null)
	    cursor.moveToFirst();

	MulticastData multicast = new MulticastData(Integer.parseInt(cursor.getString(0)),
		cursor.getString(1), cursor.getString(2), cursor.getString(3), 
		cursor.getString(4), cursor.getString(5), cursor.getString(6));
	// return message
	cursor.close();
	db.close();

	return multicast;
    }

    // Getting All Messages
    public ArrayList<MulticastData> Get_Messages() {
	try {
	    message_list.clear();

	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_MULTICAST;

	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
		do {
		    MulticastData multicast = new MulticastData();
		    multicast.setID(Integer.parseInt(cursor.getString(0)));
		    multicast.setUID(cursor.getString(1));
		    multicast.setLatitude(cursor.getString(2));
		    multicast.setLongitude(cursor.getString(3));
		    multicast.setCommand(cursor.getString(4));
		    multicast.setMessage(cursor.getString(5));
		    multicast.setTime(cursor.getString(6));
		    // Adding message to list
		    message_list.add(multicast);
		} while (cursor.moveToNext());
	    }

	    // return message list
	    cursor.close();
	    db.close();
	    return message_list;
	} catch (Exception e) {
	    // TODO: handle exception
	    Log.e("all_message", "" + e);
	}

	return message_list;
    }

    // Updating single message
    public int Update_Message(MulticastData message) {
	SQLiteDatabase db = this.getWritableDatabase();

	ContentValues values = new ContentValues();
	values.put(KEY_UID, message.getUID());
	values.put(KEY_LAT, message.getLatitude());
	values.put(KEY_LONG, message.getLongitude());
	values.put(KEY_CMD, message.getCommand());
	values.put(KEY_MSG, message.getMessage());
	values.put(KEY_TIME, message.getTime());

	// updating row
	return db.update(TABLE_MULTICAST, values, KEY_ID + " = ?",
		new String[] { String.valueOf(message.getID()) });
    }

    // Deleting single message
    public void Delete_Message(int id) {
	SQLiteDatabase db = this.getWritableDatabase();
	db.delete(TABLE_MULTICAST, KEY_ID + " = ?",
		new String[] { String.valueOf(id) });
	db.close();
    }
    
    public void Delete_All_Messages() {
    	try {
    	    message_list.clear();
    	    SQLiteDatabase db = this.getWritableDatabase();
    	    db.delete(TABLE_MULTICAST, null, null);
    	}
    	catch (Exception e) {
    		 Log.e("delete_all_message", "" + e);
    	} 	    
    }

    // Getting message Count
    public int Get_Total_Messages() {
	String countQuery = "SELECT  * FROM " + TABLE_MULTICAST;
	SQLiteDatabase db = this.getReadableDatabase();
	Cursor cursor = db.rawQuery(countQuery, null);
	cursor.close();

	// return count
	return cursor.getCount();
    }
}
