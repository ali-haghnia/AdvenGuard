package com.example.advenguard;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.*;
import android.view.View.OnClickListener;
import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import java.util.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import com.example.advenguard.AllMembersActivity;

public class MainActivity extends Activity implements OnItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addListenerOnSpinnerItemSelection();
		
		
		Button button1 = (Button) findViewById(R.id.button1);
		final Spinner spinner1=(Spinner)findViewById(R.id.spinner1);
		
		button1.setOnClickListener(new View.OnClickListener() {
			 
			@Override
			  public void onClick(View view) {
				ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
				Boolean internetIsPresent = cd.isConnectingToInternet();
				if (!internetIsPresent)
					showAlertDialog(MainActivity.this, "No Internet Connection !","You need internet connection to start.", false); 
				else{				
					String strUserName= String.valueOf(spinner1.getSelectedItem());
					
					// Starting new intent
					Intent in = new Intent(getApplicationContext(),TabsActivity.class);
					
					// storing UserName in Global Variable
					Globals glbName=(Globals)getApplication();
					glbName.setData(strUserName);
					finish();
					startActivity(in);
				}
			  }
			});
	}

	public void addListenerOnSpinnerItemSelection(){
		Spinner spinner1=(Spinner)findViewById(R.id.spinner1);
		
		spinner1.setOnItemSelectedListener(this);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        R.array.user_arrays, android.R.layout.simple_spinner_item);
		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner1.setAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		
//		Toast.makeText(parent.getContext(), 
//				parent.getItemAtPosition(pos).toString(),
//				Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
	
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
	
}
