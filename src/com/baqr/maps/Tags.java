package com.baqr.maps;

import com.mapswithme.maps.api.MWMPoint;

import java.util.Arrays;
import java.util.Comparator;

public class Tags {
	private final String id;
	private final String name;
	private final double lat;
	private final double lon;
	private final String reportMsg;
	private final String reportTime;
	private final String reportAddress;
	private final int reportCmd;
	
	public Tags(String id, String name, double lat, double lon,
	           String reportMsg, String reportTime, String reportAddress,
	           int reportCmd)
	{
	 this.id = id;
	 this.name = name;
	 this.lat = lat;
	 this.lon = lon;
	 this.reportMsg = reportMsg;
	 this.reportTime = reportTime;
	 this.reportAddress = reportAddress;
	 this.reportCmd = reportCmd;
	}
	
	@Override
	public String toString()     	{ return name; }
	public MWMPoint toMWMPoint() 	{ return new MWMPoint(lat, lon, name, id); }
	
	public String getId()          	{ return id; }
	public String getName()        	{ return name; }
	public double getLat()         	{ return lat; }
	public double getLon()         	{ return lon; }
	public String getMsg() 			{ return reportMsg; }
	public String getTime()  		{ return reportTime; }
	public String getAddress()   	{ return reportAddress; }
	public int getCmd()    			{ return reportCmd; }
	
	public static Tags fromMWMPoint(MWMPoint point)
	{
	 Tags result = null;
	 final String id = point.getId();
	 if (id != null)
	 {
	   for (Tags tag : TAG)
	     if (tag.getId().equals(id))
	     {
	       result = tag;
	       break;
	     }
	 }
	 return result;
	}
	
	public static class TagComparator implements Comparator<Tags>
	{
	 @Override
	 public int compare(Tags lhs, Tags rhs) { return lhs.getName().compareTo(rhs.getName()); }
	}
	
	public static Tags[] TAG = {new Tags("0001", "tagName", 42.50779, 1.52109, "Message", "Time", "Address", 0)
		//Seperate by comma --> FORMAT from above
	};
	
	static {
	 // Sort'em all at class load!
	 Arrays.sort(Tags.TAG, new Tags.TagComparator());
	}
}

