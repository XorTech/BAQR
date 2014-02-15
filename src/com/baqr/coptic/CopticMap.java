package com.baqr.coptic;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.baqr.baqr.R;
import com.baqr.database.MulticastData;
import com.baqr.database.MulticastDatabaseHandler;
import com.baqr.maps.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class CopticMap extends Fragment {
	
    private GoogleMap gMap;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    public static final int DOUBLE_ZERO = 00;
    private static final Double FIVE_DIGIT = 100000.0D;
	private static final int NORMAL_MAP = R.id.normal_map;
	private static final int SATELLITE_MAP = R.id.satellite_map;
	private static final int TERRAIN_MAP = R.id.terrain_map;
	private static final int HYBRID_MAP = R.id.hybrid_map;
    private LatLngBounds.Builder bounds;
    
    int tagCounter = 0;
    boolean dataExists = false;
    boolean tagsAvailable =false;
    private LatLng gCoord;
    private Context context;
    private static double latitude = 0.0;
    private static double longitude = 0.0;
    
    private ArrayList<String> tagArrayList;
    private ArrayList<Integer> markerArrayList;
    private static GPSTracker gps;
    
    Button clearBtn;
    Button drawBtn;
    
    public CopticMap(Context ctx) {
    	context = ctx;
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.baqrcoptic_map,container,false);
        
        // Add up button functionality to send user back to home
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        
        clearBtn = (Button) rootView.findViewById(R.id.copticClearBtn);
        drawBtn = (Button) rootView.findViewById(R.id.copticDrawBtn);
        
        // GPSTracker Instance
        gps = new GPSTracker(context);
        
        // Check data connection
        dataExists = CheckInternet();
        
        if (!dataExists) {
        	Toast.makeText(context, "No Data Connection!", Toast.LENGTH_SHORT).show();
        }
        else {
        	// Set up map
        	setUpMapIfNeeded();
        	
        	// Initialize arrays 
            tagArrayList = new ArrayList<String>();
            markerArrayList = new ArrayList<Integer>();
            bounds = new LatLngBounds.Builder();
        	
            // Put icons into array
            InitializeTagIcons();
                   
            if(gps.canGetLocation()){ 
            	SetLatitude();
            	SetLongitude();
                
                // Initialize latlng and set zoom
                LatLng cameraLatLng = new LatLng(latitude,longitude);
                float cameraZoom = 12;
                
                // Check if instance is saved when changing landscape
                if(savedInstanceState != null){
                    mapType = savedInstanceState.getInt("map_type", GoogleMap.MAP_TYPE_NORMAL);
                    double savedLat = savedInstanceState.getDouble("lat");
                    double savedLng = savedInstanceState.getDouble("lng");
                    cameraLatLng = new LatLng(savedLat, savedLng);
                    cameraZoom = savedInstanceState.getFloat("zoom", 12);
                }
                
                // Move and animate camera to current position
                CameraPosition cameraPosition = new CameraPosition.Builder().target(cameraLatLng).zoom(cameraZoom).build();
                gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                
                try {
                	// Get my tags
                	tagsAvailable = MapMyTags();
                	
                	// Only move if tags available
                	if (tagsAvailable) {
                    	// Set listener for tag bounds
                    	gMap.setOnCameraChangeListener(new OnCameraChangeListener() {

                    		@Override
                    		public void onCameraChange(CameraPosition arg0) {
                    			if (tagCounter == 1) {
                    	            // If one tag, move to it without bounds
                    				float cameraZoom = 15;
                    	            CameraPosition newCameraPosition = new CameraPosition.Builder().target(gCoord).zoom(cameraZoom).build();
                    	            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
                    			}
                    			else {
                        		    // Else show bounds
                        		    gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
                    			}
                    		    // Remove listener to prevent position reset on camera move.
                    		    gMap.setOnCameraChangeListener(null);
                    		}
                    	});
                	}
                }
                // Catch error if unable to get tags
                catch (Exception e){
                	Toast.makeText(context, "Failed to load tags!", Toast.LENGTH_SHORT).show();
                }
            }
            if (!gps.canGetLocation())
            {
            	showSettingsAlert();
            }     	
        }
		return rootView;      
    }
    
	/**
	 * Function to listen for options item selected
	 * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        
        boolean backPressed = false;
        
        // Map options
        switch(item.getItemId()){
        case NORMAL_MAP:
            mapType = GoogleMap.MAP_TYPE_NORMAL;
            break;
        case SATELLITE_MAP:
            mapType = GoogleMap.MAP_TYPE_SATELLITE;
            break;
        case TERRAIN_MAP:
            mapType = GoogleMap.MAP_TYPE_TERRAIN;
            break;
        case HYBRID_MAP:
            mapType = GoogleMap.MAP_TYPE_HYBRID;
            break;
        }
        
        if (!backPressed) {
        	gMap.setMapType(mapType);
        }
        
        return super.onOptionsItemSelected(item);
    }
    
	/**
	 * Function to save map instance for portrait/landscape movements
	 * */
    @Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // Save the map type so when we change orientation, the map type can be restored
        LatLng cameraLatLng = gMap.getCameraPosition().target;
        float cameraZoom = gMap.getCameraPosition().zoom;
        outState.putInt("map_type", mapType);
        outState.putDouble("lat", cameraLatLng.latitude);
        outState.putDouble("lng", cameraLatLng.longitude);
        outState.putFloat("zoom", cameraZoom);
    }
    
	/**
	 * Function to get tag data from DB
	 * */
	public boolean MapMyTags() {
		boolean tagsAvailable = false;
		
		// Initialize db and arrayList for db elements
		MulticastDatabaseHandler mdb = new MulticastDatabaseHandler(context);
		ArrayList<MulticastData> message_array_from_db = mdb.Get_Messages();
		
		// Check if tags exist. If so, process and add markers
		if (message_array_from_db.size() > 0) {
			// Strip data from db and send as a marker
			for (int i = message_array_from_db.size(); i > 0; i--) {
				
			    String tag = message_array_from_db.get(i).getUID();
			    String latitude = message_array_from_db.get(i).getLatitude();
			    String longitude = message_array_from_db.get(i).getLongitude();
			    String message = message_array_from_db.get(i).getMessage();
			    String _time = message_array_from_db.get(i).getTime();
			   		    
			    Double lat = Double.parseDouble(latitude);
			    Double lon = Double.parseDouble(longitude);
			    
		        LatLng coords = new LatLng(lat,lon);
		        gCoord = coords;
		          
				try {
					SetMapMarker(tag, coords, message + "\n" + _time);
					bounds.include(coords);
				}
				catch (Exception e) {
					Log.e("Error: ", "" +e);
				}
				
			}
			mdb.close();
			tagsAvailable = true;
		}
		else {
			tagsAvailable = false;
		}
		
		return tagsAvailable;
	}
	
	/**
	 * Function to place markers on map per tag
	 * */
	public void SetMapMarker(String tag, LatLng coords, String msg) {
		int position = 0;
		boolean found = false;

		// Search array for value
		for (String tagList : tagArrayList) {
			if (tag.equals(tagList)) {
				found = true;
				break;
			}
			else {
				position++;
			}       
		}
		
		// If not found, add it to the list
		if (!found) {
			position = 0;
			tagArrayList.add(tag);
			// Find tag position in array. If exist use that position to match icon color
			for (String tagList : tagArrayList) {
				if (tag.equals(tagList)) {
					break;
				}
				else {
					position++;
				}
			}
		} 
		
		// Match array positions for icons. If greater than 100 all are black
		if (position <= 19 && !found) {					
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(msg)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 19 && position <= 39 && !found) {
			position -= 20;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(msg)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 39 && position <= 59 && !found) {
			position -= 40;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(msg)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 59 && position <= 79 && !found) {
			position -= 60;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(msg)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
		else if (position > 79 && position <= 99 && !found) {
			position -= 80;
			gMap.addMarker(new MarkerOptions()
				.position(coords)
				.title("TagID: " + tag)
				.snippet(msg)
				.icon(BitmapDescriptorFactory.fromResource(markerArrayList.get(position))));
		}
	} 
	
	/**
	 * Function to set up gMap, if required
	 * */
    private void setUpMapIfNeeded() {
        if (gMap == null) 
        {
        	gMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.copticMap)).getMap();
        	
            if (gMap != null) 
            {
                setUpMap();
            }
        }
    }
    
	/**
	 * Function to set up map controls
	 * */
    private void setUpMap() {
        gMap.setMyLocationEnabled(true);
        gMap.getUiSettings().setCompassEnabled(true);
        gMap.getUiSettings().setRotateGesturesEnabled(true);
        gMap.getUiSettings().setMyLocationButtonEnabled(true);
        gMap.setMapType(mapType);
    }
    
	/**
	 * Function to initialize icon array
	 * */
    public void InitializeTagIcons() {
    	markerArrayList.add(R.drawable.black_radar);
    	markerArrayList.add(R.drawable.blue_radar);
    	markerArrayList.add(R.drawable.red_radar);
    	markerArrayList.add(R.drawable.green_radar);
    	markerArrayList.add(R.drawable.orange_radar);
    	markerArrayList.add(R.drawable.pink_radar);
    	markerArrayList.add(R.drawable.yellow_radar);
    	markerArrayList.add(R.drawable.purple_radar);
    	markerArrayList.add(R.drawable.teal_radar);
    	markerArrayList.add(R.drawable.white_radar);
    	markerArrayList.add(R.drawable.brown_radar);
    	markerArrayList.add(R.drawable.deep_radar);
    	markerArrayList.add(R.drawable.gray_radar);
    	markerArrayList.add(R.drawable.light_radar);
    	markerArrayList.add(R.drawable.lime_radar);
    	markerArrayList.add(R.drawable.pea_radar);
    	markerArrayList.add(R.drawable.peach_radar);
    	markerArrayList.add(R.drawable.strawberry_radar);
    	markerArrayList.add(R.drawable.violet_radar);
    	markerArrayList.add(R.drawable.zinc_radar);
    }
    
	/**
	 * Function to for data connection
	 * */
    public boolean CheckInternet() {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);      

        if (wifi.isConnected()) {
        	return true;
        } else if (mobile.isConnected()) {
            return true;
        }
        return false;
    }
    
	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will launch Settings Options
	 * */
	public void showSettingsAlert(){
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setMessage("GPS is disabled in your device. Enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                CopticMap.this.startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
	}
    
	public static void SetLatitude() {
        gps.getLocation();
        
        latitude = Math.round(FIVE_DIGIT * gps.getLatitude()) / FIVE_DIGIT;
   	}
	
	public static void SetLongitude() {
		longitude = Math.round(FIVE_DIGIT * gps.getLongitude()) / FIVE_DIGIT;
	}
}


