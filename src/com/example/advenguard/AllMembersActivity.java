package com.example.advenguard;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.bu;
import com.example.advenguard.GPSTracker;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class AllMembersActivity extends ListActivity {
	
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
    
	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();
	ArrayList<HashMap<String, String>> membersList;
	ArrayList<HashMap<String, String>> new_membersList;

	// url to get all members list
	private static String url_all_members = "http://simba.ir/advenguard/get_all_members.php";
	
	// url to change the SOS status
	private static final String url_update_status = "http://simba.ir/advenguard/update_status.php";	
	
	// url to store the Location
	private static final String url_add_location = "http://simba.ir/advenguard/add_location.php";
	
	// url to get sos names
	private static final String url_sos_members = "http://simba.ir/advenguard/get_sos_members.php";
	
	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SOS = "SOS";
	private static final String TAG_NAME = "name";
	private static final String TAG_STATUS = "status";
	private static final String TAG_TIME = "time";
	
	ArrayList<String> old_sos_list = new ArrayList<String>();
	
	public String strUserName = "";
	
	 GPSTracker gps;
	 Boolean blnSetting = false;
	
	
	Timer timer = new Timer();
	Timer SOSTimer = new Timer();
	final Handler handler = new Handler(){
		@SuppressWarnings("null")
		public void handleMessage(Message msg) {
			switch (msg.what){
			case 1:
				try{
	                gps = new GPSTracker(AllMembersActivity.this);
	                String strLat = "";
	                String strLng = "";
	                // check if GPS enabled     
	                if(gps.canGetLocation())
	                	{   
	                    double lat = gps.getLatitude();
	                    double lng = gps.getLongitude();
	                    strLat = String.format("%.6f", lat);
	                    strLng = String.format("%.6f", lng);
	    				//Store location to DB here:
	     				if (lat != 0.0 && lng != 0.0) store(strUserName, strLat, strLng);
	                	}
	                else if(blnSetting == false)
	                	{               	
	                	gps.showSettingsAlert();
	                	blnSetting = true;
	                	}
					}
				catch (Exception e)
					{
					Toast.makeText(getApplicationContext(), e.getMessage() ,Toast.LENGTH_SHORT).show();
					}
				break;
			case 2:
				try 
				{
					
					//new_sos_list = ...
					ArrayList<String> new_sos_list = new ArrayList<String>();
					
					// Building Parameters
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					
					// getting JSON string from URL
					JSONObject json = jParser.makeHttpRequest(url_sos_members, "GET", params);
					
					// Check your log cat for JSON response
					Log.d("New SOS names: ", json.toString());
					
					// Checking for SUCCESS TAG
					int success = json.getInt(TAG_SUCCESS);
					if (success == 1) 
					{
						// Getting Array of members
						JSONArray members2 = json.getJSONArray(TAG_SOS);
						String SOSName = "";
						JSONObject c;
						for (int i = 0; i < members2.length(); i++) 
						{
							c = members2.getJSONObject(i);
							SOSName = c.getString(TAG_NAME);
							new_sos_list.add(SOSName);
						}
					}
					else
					{
						//new_sos_list.clear();
					}
					
					if(!old_sos_list.equals(new_sos_list))
					{
						//Refresh the ListView
						RefreshList(new_sos_list);
						
						//old_sos_list = new_sos_list
						old_sos_list = new_sos_list;
						//CheckAlert(new_sos_list);
					}
				} 
				catch (Exception e) 
				{
					Toast.makeText(getApplicationContext(), "Error in connecting to web server: " + e.getMessage() ,Toast.LENGTH_SHORT).show();
					// TODO: handle exception
				}
				break;
			}
			super.handleMessage(msg);	
		}

		private void CheckAlert(ArrayList<String> new_sos_list) {
			// TODO Auto-generated method stub
			
			
		}

		private void store(String strName, String strLat, String strLng) {
			try {
				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("Name", strName));
				params.add(new BasicNameValuePair("Latitude", strLat));
				params.add(new BasicNameValuePair("Longitude", strLng));
	
				// getting JSON Object
				// Note that create product url accepts POST method
				JSONObject json = jParser.makeHttpRequest(url_add_location, "POST", params);
				
				// check log cat fro response
				Log.d("Create Response", json.toString());
	
				// check for success tag
	
				int success = json.getInt(TAG_SUCCESS);
				if (success == 1) {
					// successfully registered the location
					Toast.makeText(getApplicationContext(),
							"Location for " + strName + " successfully recorded.\nLatitude: " + strLat + "\nLongitude: " + strLng
							,Toast.LENGTH_SHORT).show();
					
					// closing this screen
					//finish();
				} else {
					// failed to store
					Toast.makeText(getApplicationContext(), "Failed to Store...",Toast.LENGTH_SHORT).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		}
		};

		TimerTask task = new TimerTask(){
		public void run() {	
			Message message = new Message();
			message.what = 1;
			handler.sendMessage(message);
		}
		};
		
		TimerTask SOSTask = new TimerTask(){
		public void run() {	
			Message message = new Message();
			message.what = 2;
			handler.sendMessage(message);
		}
		};
	
	//@SuppressWarnings("null")
	public void RefreshList(ArrayList<String> ar){
		new_membersList.clear();
		for(int i = 0; i < membersList.size(); i++)
		{
			String strName = membersList.get(i).get(TAG_NAME);
			String strStatus = "Safe";
			int j;
			for(j = 0; j < ar.size(); j++)
			{
				if (membersList.get(i).get(TAG_NAME).contentEquals(ar.get(j)))
				{
					strStatus = "SOS";
					break;
				}
			}
			
			HashMap<String, String> map2 = new HashMap<String, String>();
			map2.put(TAG_NAME, strName);
			map2.put(TAG_STATUS, strStatus);
			new_membersList.add(map2);
		}
		ListAdapter adapter = new SimpleAdapter(AllMembersActivity.this, new_membersList,
		R.layout.list_item, new String[] {TAG_NAME,TAG_STATUS},new int[] { R.id.name, R.id.status });
		// updating listview
		setListAdapter(adapter);
		String strSOSName = "";
		for (int k = 0; k < ar.size(); k++)
		{
			strSOSName = ar.get(k);
			if (!strSOSName.equals(strUserName)) SOSAlarm(strSOSName);
		}
	}
	
	private void SOSAlarm(String SOSName) 
		{
			final MediaPlayer alarm = MediaPlayer.create(AllMembersActivity.this, R.raw.s);
			alarm.start();
			alarm.setLooping(true);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(AllMembersActivity.this);
			builder.setMessage(SOSName + " is sending an SOS !!!")
			.setCancelable(false)
			.setIcon(R.drawable.red)
			.setTitle(SOSName + ": SOS")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               alarm.stop();
			           }
			       });
			AlertDialog alert = builder.create();
			alert.show();
		}

	// members JSONArray
	JSONArray members = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.members_list);
		// getting user name from Global var
		strUserName = ((Globals) this.getApplication()).getData();
		
		//New code (Needs 2.3.3)
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
		Boolean internetIsPresent = cd.isConnectingToInternet();
		if (!internetIsPresent)
	    	showAlertDialog(AllMembersActivity.this, "No Internet Connection","You need internet connection to refresh.", false); 
		else
			// Loading members in Background Thread
			new LoadAllMembers().execute();
		
		Timer timer = new Timer();
		timer.schedule(task, 45 * 1000 ,2 * 60 * 1000);
		
		Timer SOSTimer = new Timer();
		SOSTimer.schedule(SOSTask, 7 * 000, 15 * 000);
		
		ListView lv = getListView();
		
		// Hashmap for ListView
		membersList = new ArrayList<HashMap<String, String>>();
		new_membersList = new ArrayList<HashMap<String, String>>();
	
		//Taping SOS button
		Button btnSOS = (Button) findViewById(R.id.btnSOS);
		btnSOS.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
				Boolean internetIsPresent = cd.isConnectingToInternet();
				if (!internetIsPresent)
			    	showAlertDialog(AllMembersActivity.this, "No Internet Connection !","You need internet connection to send an SOS.", false); 
				else{				
					try {
						Toast.makeText(getApplicationContext(), "Updating SOS Status for " + strUserName + " ...\nList will refresh soon !" ,Toast.LENGTH_LONG).show();
						// Toggling the status and button text
						String name = strUserName;
						String status = "";
						String strBtn = ((Button) view.findViewById(R.id.btnSOS)).getText().toString();
						if (strBtn.equals("Send SOS")) status = "SOS";
						else if (strBtn.equals("Return to safe mode")) status = "Safe";
						
						// Building Parameters
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair(TAG_NAME, name));
						params.add(new BasicNameValuePair(TAG_STATUS, status));
					
						// sending modified data through http request. Notice that update url accepts POST method
						JSONObject json = jParser.makeHttpRequest(url_update_status, "POST", params);
						int success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							// DB Successfully Updated
							if (strBtn.equals("Send SOS"))
								((Button) view.findViewById(R.id.btnSOS)).setText("Return to safe mode");
							else if (strBtn.equals("Return to safe mode"))
								((Button) view.findViewById(R.id.btnSOS)).setText("Send SOS");			
							
							//refreshing list item.
							
							//new_sos_list = ...
							ArrayList<String> new_sos_list = new ArrayList<String>();
							
							// Building Parameters
							List<NameValuePair> params1 = new ArrayList<NameValuePair>();
							
							// getting JSON string from URL
							JSONObject json1 = jParser.makeHttpRequest(url_sos_members, "GET", params1);
							
							// Check your log cat for JSON response
							Log.d("New SOS names: ", json1.toString());
							
							// Checking for SUCCESS TAG
							int success1 = json1.getInt(TAG_SUCCESS);
							if (success1 == 1) 
							{
								// Getting Array of members
								JSONArray members2 = json1.getJSONArray(TAG_SOS);
								String SOSName = "";
								JSONObject c;
								for (int i = 0; i < members2.length(); i++) 
								{
									c = members2.getJSONObject(i);
									SOSName = c.getString(TAG_NAME);
									new_sos_list.add(SOSName);
								}
							}
							else
							{
								//new_sos_list.clear();
							}
							
							if(!old_sos_list.equals(new_sos_list))
							{
								//Refresh the ListView
								RefreshList(new_sos_list);
								
								//old_sos_list = new_sos_list
								old_sos_list = new_sos_list;
								//CheckAlert(new_sos_list);
							}	
							
						} 
						else
						{
							// failed to update status
						}
					} 
					catch (JSONException e) 
					{
						e.printStackTrace();
					}
			}}

		});
		
		//Taping list items
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String strSelectedName = ((TextView) view.findViewById(R.id.name)).getText().toString();
			    String strStatus = ((TextView) view.findViewById(R.id.status)).getText().toString();
				Toast.makeText(getApplicationContext(),"Loading path history for " + strSelectedName + "...", Toast.LENGTH_LONG).show();
				//sending name and status to MapActivity
				getParent().getIntent().putExtra("ListUser",strSelectedName);
				getParent().getIntent().putExtra("Status",strStatus);
				TabsActivity main = (TabsActivity) getParent(); // get the main 'TabActivity' from a Child (tab)
				TabHost tabHost = main.getTabHost(); // get a handle to the TabHost
				tabHost.setCurrentTab(1);	
			}

			private TabHost getTabHost() {
				return null;
			}
		});
		

	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		task.cancel();
		SOSTask.cancel();
	    timer.cancel();
	    timer.purge();
	    SOSTimer.cancel();
	    SOSTimer.purge();
	    //unRegisterBaseActivityReceiver();
	}
	
	@Override
	public void onBackPressed()
	{
		finish();
	}
	
	/**
	 * Background Async Task to Load all members by making HTTP Request
	 * */
	class LoadAllMembers extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(AllMembersActivity.this);
			pDialog.setMessage("Loading Members. Please wait... \n You need internet connection to load members list.");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All members from url
		 * */
		protected String doInBackground(String... args) {

			try {

				// Building Parameters
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				// getting JSON string from URL
				JSONObject json = jParser.makeHttpRequest(url_all_members, "GET", params);
				
				// Check your log cat for JSON response
				Log.d("All members: ", json.toString());		
				
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// members found
					// Getting Array of members
					members = json.getJSONArray(TAG_SOS);

					// looping through All members
					for (int i = 0; i <members.length(); i++) {
						JSONObject c = members.getJSONObject(i);

						// Storing each json item in variable
						String name = c.getString(TAG_NAME);
						String status = c.getString(TAG_STATUS);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_NAME, name);
						map.put(TAG_STATUS, status);
						if (status.equals("SOS")) old_sos_list.add(name);
						// adding HashList to ArrayList
						membersList.add(map);
					}
				}
				else {
					// no member found
					// Launch Add New member Activity
//					Intent i = new Intent(getApplicationContext(),
//							NewProductActivity.class);
//					// Closing all previous activities
//					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					startActivity(i);
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
					//Delete any current row
					//setListAdapter(null);
					//call notifyDataSetChanged();
					
					/**
					 * Updating parsed JSON data into ListView
					 * */
					try {
						ListAdapter adapter = new SimpleAdapter(AllMembersActivity.this, membersList,
						R.layout.list_item, new String[] { TAG_NAME,TAG_STATUS},new int[] { R.id.name, R.id.status });
						// updating listview
						setListAdapter(adapter);

						//Update btnSOS text
						for(int i = 0; i < membersList.size(); i++)
							if (membersList.get(i).containsValue(strUserName) && membersList.get(i).containsValue("SOS")) 
								{
									Button btnSOS = (Button) findViewById(R.id.btnSOS);
									btnSOS.setText("Return to safe mode");
									break;
								}
						//loading SOS Alarms
						String strSOSName = "";
						for (int k = 0; k < old_sos_list.size(); k++)
						{
							strSOSName = old_sos_list.get(k);
							if (!strSOSName.equals(strUserName)) SOSAlarm(strSOSName);
						}
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}