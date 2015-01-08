package com.example.advenguard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.support.v4.app.FragmentActivity;


import com.example.advenguard.R;



import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

 
public class MapActivity extends FragmentActivity implements LocationListener{
 
	GoogleMap mMap;
	
	// url to get all members list
	private static String url_all_loc = "http://simba.ir/advenguard/get_all_loc.php";
	
	private static String url_path = "http://simba.ir/advenguard/get_path.php";
	
	private String TAG = "MainActivity";
	
    private Context mContext;

	private LocationManager locationManager;

	private static final long MIN_TIME = 10 * 1000;

	private static final float MIN_DISTANCE = 10;
	
	public String strUserName = "";
	public String strListUser = "";
	public String strStatus = "";
	public boolean blnPaused;
	
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();
	
	// members JSONArray
	JSONArray membersLoc = null;
	JSONArray pathList = null;
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_LOC = "LOC";
	private static final String TAG_PATH = "PATH";
	private static final String TAG_NAME = "Name";
	private static final String TAG_LAT = "Latitude";
	private static final String TAG_LNG = "Longitude";
	private static final String TAG_TIME = "TimeStamp";
	
	  /**
     * Function to display simple Alert Dialog
     * @param context - application context
     * @param title - alert dialog title
     * @param message - alert message
     * @param status - success/failure (used to set icon)
     * */
    @SuppressWarnings("deprecation")
	public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
 
        // Setting Dialog Title
        alertDialog.setTitle(title);
 
        // Setting Dialog Message
        alertDialog.setMessage(message);
         
        // Setting alert dialog icon
        alertDialog.setIcon((status) ? R.drawable.green : R.drawable.red);
 
        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        
        // Showing Alert Message
        alertDialog.show();
    };
	
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        
        try{
        		mLocationSource = new LongPressLocationSource();
    			strUserName = ((Globals) this.getApplication()).getData();
    			strListUser = getParent().getIntent().getStringExtra("ListUser");
        		strStatus = getParent().getIntent().getStringExtra("Status");
        		//New code (Needs 2.3.3)
        		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        		StrictMode.setThreadPolicy(policy);
        		
        		// Showing status
        		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        		Boolean internetIsPresent = cd.isConnectingToInternet();
        		if (internetIsPresent)
        		{
            		// Getting Google Play availability status
            		int status = GooglePlayServicesUtil
            				.isGooglePlayServicesAvailable(getBaseContext());
            		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            			int requestCode = 10;
            			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,	requestCode);
            			dialog.show();
            		} else {
            			setUpMapIfNeeded();
            			testLocation();
                   		// Loading locations in Background Thread
            			new LoadLastLocation().execute();
            		//	new LoadPath().execute();
            		}
        		}	
        		else
        			showAlertDialog(MapActivity.this, "No Internet Connection","You need internet connection to load the map!", false);
  
            		// locationManager = (LocationManager)
            		// getSystemService(Context.LOCATION_SERVICE);
            		// locationManager.requestLocationUpdates(
            		// LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //
 
        	}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			Toast.makeText(getApplicationContext(), "Check your GPS and Internet Connection!" ,Toast.LENGTH_LONG).show();
    			showSettingsAlert();
    		}
     	}
    
    @Override
    public void onStart(){
        super.onStart();
        
        strListUser = getParent().getIntent().getStringExtra("ListUser");
    //	new LoadLastLocation().execute();
    }
    
    
    
    @Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		 mLocationSource.onResume();
		if (blnPaused)
		{
			
			mMap.clear();
	        try{
        		mLocationSource = new LongPressLocationSource();
    			strUserName = ((Globals) this.getApplication()).getData();
    			strListUser = getParent().getIntent().getStringExtra("ListUser");
        		strStatus = getParent().getIntent().getStringExtra("Status");
        		//New code (Needs 2.3.3)
        		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        		StrictMode.setThreadPolicy(policy);
        		
        		// Showing status
        		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        		Boolean internetIsPresent = cd.isConnectingToInternet();
        		if (internetIsPresent)
        		{
            		// Getting Google Play availability status
            		int status = GooglePlayServicesUtil
            				.isGooglePlayServicesAvailable(getBaseContext());
            		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            			int requestCode = 10;
            			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,	requestCode);
            			dialog.show();
            		} else {
            			setUpMapIfNeeded();
            			testLocation();
                   		// Loading locations in Background Thread
            			new LoadLastLocation().execute();
            		//	new LoadPath().execute();
            		}
        		}	
        		else
        			showAlertDialog(MapActivity.this, "No Internet Connection","You need internet connection to load the map!", false);
  

        	}
    		catch (Exception e)
    		{
    			e.printStackTrace();
    			Toast.makeText(getApplicationContext(), "Check your GPS and Internet Connection!" ,Toast.LENGTH_LONG).show();
    			showSettingsAlert();
    		}
			
			blnPaused = false;
		}
		
		
	}
    
    private void setUpMapIfNeeded() {
		// TODO Auto-generated method stub
    	// Do a null check to confirm that we have not already instantiated the
    			// map.
    			if (mMap == null) {
    				// Try to obtain the map from the SupportMapFragment.
    				mMap = ((SupportMapFragment) getSupportFragmentManager()
    						.findFragmentById(R.id.map)).getMap();
    				// Check if we were successful in obtaining the map.
    				if (mMap != null) {
    					// setUpMap();
	}
    			}
    }
    
	public void testLocation() {
		
		try{
			// Enabling MyLocation Layer of Google Map
			mMap.setMyLocationEnabled(true);
			// Getting LocationManager object from System Service LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// Creating a criteria object to retrieve provider

			Criteria criteria = new Criteria();
			// Getting the name of the best provider
			String provider = locationManager.getBestProvider(criteria, true);
			// Getting Current Location
			
			Location location = locationManager.getLastKnownLocation(provider);
			if (location != null) {
				onLocationChanged(location);
			}
			locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
			
		}
		catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "GPS ?!" ,Toast.LENGTH_LONG).show();
			showSettingsAlert();
		}

	}
	
	private void setUpMap() {
		mMap.setLocationSource(mLocationSource);
		mMap.setOnMapLongClickListener(mLocationSource);
		mMap.setMyLocationEnabled(true);
	}
	

	/**
	 * A {@link LocationSource} which reports a new location whenever a user
	 * long presses the map at the point at which a user long pressed the map.
	 */
	private static class LongPressLocationSource implements LocationSource,
			OnMapLongClickListener {
		private OnLocationChangedListener mListener;
		/**
		 * Flag to keep track of the activity's life cycle. This is not strictly
		 * necessary in this case because onMapLongPress events don't occur
		 * while the activity containing the map is paused but is included to
		 * demonstrate best practices (e.g., if a background service were to be
		 * used).
		 */
		private boolean mPaused;

		@Override
		public void activate(OnLocationChangedListener listener) {
			mListener = listener;
		}

		@Override
		public void deactivate() {
			mListener = null;
		}

		@Override
		public void onMapLongClick(LatLng point) {
			if (mListener != null && !mPaused) {
				Location location = new Location("LongPressLocationProvider");
				location.setLatitude(point.latitude);
				location.setLongitude(point.longitude);
				location.setAccuracy(100);
				mListener.onLocationChanged(location);
			}
		}

		public void onPause() {
			mPaused = true;
		}

		public void onResume() {
			mPaused = false;
		}
	}

	private LongPressLocationSource mLocationSource;

	@Override
	protected void onPause() {
		super.onPause();
		// mLocationSource.onPause();
		blnPaused = true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	   /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
      
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
  
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		//LatLng latLng = new LatLng(location.getLatitude(),
		// location.getLongitude());
		// CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
		// 10);
		// Log.i(TAG, "onLocationChanged");
		//
		// mMap.animateCamera(cameraUpdate);
		// locationManager.removeUpdates(this);
		// Getting latitude of the current location
		double latitude = location.getLatitude();
		// Getting longitude of the current location
		double longitude = location.getLongitude();
		// Creating a LatLng object for the current location
		LatLng latLng = new LatLng(latitude, longitude);
	
		// Showing the current location in Google Map
		mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		// Zoom in the Google Map
		mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

		
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	/**
	 * Background Async Task to Load all locations by making HTTP Request
	 * */
	class LoadLastLocation extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@SuppressWarnings("deprecation")
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MapActivity.this);
			pDialog.setMessage("Loading locations and paths... \n You need internet connection to load other members' location.");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.setButton("Continue", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		           
		           }
		       });
			pDialog.show();
		}

		/**
		 * getting All members last locations from url
		 * */
		protected String doInBackground(String... args) {

			try {
				
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				// getting JSON string from URL
				JSONObject json = jParser.makeHttpRequest(url_all_loc, "GET", params);
				
				// Check your log cat for JSON response
				Log.d("Latest Locations: ", json.toString());		
				
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// members found
					// Getting Array of members
					membersLoc = json.getJSONArray(TAG_LOC);
				}
				else {
					//Toast.makeText(getApplicationContext(), "No member has recorded a location yet !" ,Toast.LENGTH_LONG).show();
				}
				

				// Building Parameters
				List<NameValuePair> params2 = new ArrayList<NameValuePair>();
				params2.add(new BasicNameValuePair("Name", strListUser));

				// getting product details by making HTTP request
				// Note that product details url will use GET request
				JSONObject json2 = jParser.makeHttpRequest(url_path, "GET", params2);

				
				// Check your log cat for JSON response
				Log.d("Path for" + strListUser, json2.toString());		
				
				// Checking for SUCCESS TAG
				int success2 = json2.getInt(TAG_SUCCESS);

				if (success2 == 1) {
					// members found
					// Getting Array of members
					pathList = json2.getJSONArray(TAG_PATH);
				}
				else {
					//Toast.makeText(getApplicationContext(), "No path is recorded for " + strListUser ,Toast.LENGTH_LONG).show();
				}
				
				
			} catch (JSONException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), "Error in accessing database!" ,Toast.LENGTH_SHORT).show();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all members
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {	
					/**
					 * Updating parsed JSON data into ListView
					 * */
					try {
						// looping through All members
						for (int i = 0; i <membersLoc.length(); i++) {
								
							JSONObject c = membersLoc.getJSONObject(i);

							// Storing each json item in variable
							String strName = c.getString(TAG_NAME);
								
							String strLat = c.getString(TAG_LAT);
							String strLng = c.getString(TAG_LNG);
							String strTime = c.getString(TAG_TIME); 
							LatLng latLng = new LatLng(Double.valueOf(strLat),Double.valueOf(strLng));
							//Log.d("Location Loop:", latLng.toString());
							float pinColor = ((strStatus.equals("SOS") && strListUser.equals(strName)) ? BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_BLUE);
							if (!strName.equals(strUserName))
							{
								mMap.addMarker(new MarkerOptions().position((latLng)).icon(BitmapDescriptorFactory
			        				     .defaultMarker(pinColor)).title(strName).snippet("Recorded at: \n" + strTime));
							}
    
						}

							// looping through All points
						PolylineOptions rectOptions = new PolylineOptions();	
						for (int j = 0; j <pathList.length(); j++) {
								JSONObject c = pathList.getJSONObject(j);
								// Storing each json item in variable								
								String strLat = c.getString(TAG_LAT);
								String strLng = c.getString(TAG_LNG); 
								LatLng latLng = new LatLng(Double.valueOf(strLat),Double.valueOf(strLng));
								Log.d("LoadPath Loop:", latLng.toString());
				                rectOptions.add(latLng);
							}
						//Toast.makeText(getApplicationContext(), strStatus ,Toast.LENGTH_LONG).show();
						int pathColor = ((strStatus.equals("SOS")) ? Color.RED : Color.BLUE);
						rectOptions.color(pathColor);
						rectOptions.width((float) 3.2);
						Polyline polyline = mMap.addPolyline(rectOptions);
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		}

	}
	

	
}