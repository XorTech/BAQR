package com.baqr.database;

public class MulticastData {

    // private variables
    public int _id;
    public String _uid;
    public String _latitude;
    public String _longitude;
    public String _command;
    public String _message;
    public String _time;

    public MulticastData() {
    }

    // constructor
    public MulticastData(int id, String uid, String lat, String lon, String cmd, String msg, String time) {
	this._id = id;
	this._uid = uid;
	this._latitude = lat;
	this._longitude = lon;
	this._command = cmd;
	this._message = msg;
	this._time = time;
    }

    // constructor
    public MulticastData(String uid, String lat, String lon, String cmd, String msg, String time) {
    this._uid = uid;
    this._latitude = lat;
    this._longitude = lon;
    this._command = cmd;
    this._message = msg;
    this._time = time;
    }

    // getting ID
    public int getID() {
	return this._id;
    }

    // setting id
    public void setID(int id) {
	this._id = id;
    }

    // getting name
    public String getUID() {
	return this._uid;
    }

    // setting name
    public void setUID(String uid) {
	this._uid = uid;
    }

    // getting latitude
    public String getLatitude() {
	return this._latitude;
    }

    // setting latitude
    public void setLatitude(String lat) {
	this._latitude = lat;
    }
    
    // getting longitude
    public String getLongitude() {
	return this._longitude;
    }

    // setting longitude
    public void setLongitude(String lon) {
	this._longitude = lon;
    }
    
    // getting phone number
    public String getCommand() {
	return this._command;
    }

    // setting phone number
    public void setCommand(String cmd) {
	this._command = cmd;
    }
    // getting phone number
    public String getMessage() {
	return this._message;
    }

    // setting phone number
    public void setMessage(String msg) {
	this._message = msg;
    }
    
    // getting time
    public String getTime() {
	return this._time;
    }

    // setting time
    public void setTime(String time) {
	this._time = time;
    }
}