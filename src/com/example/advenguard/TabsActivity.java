package com.example.advenguard;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;


public class TabsActivity extends TabActivity
{
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maintab_layout);
        Boolean FirstUse = true;
        
        TabHost tabHost = getTabHost();
        String strUserName = ((Globals) this.getApplication()).getData();
        setTitle("AdvenGuard-"+strUserName);
        
        TabSpec members = tabHost.newTabSpec(" ");
        members.setIndicator(" ", getResources().getDrawable(R.drawable.members_icon));
        Intent membersIntent = new Intent(this, AllMembersActivity.class);
        members.setContent(membersIntent);
        
        TabSpec maps = tabHost.newTabSpec("  ");
        maps.setIndicator("  ", getResources().getDrawable(R.drawable.maap_icon));
        Intent mapsIntent = new Intent(this, MapActivity.class);
        maps.setContent(mapsIntent);
        
        tabHost.addTab(members);
        tabHost.addTab(maps); 
	}
	
	@Override
	protected void onStop() {
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed()
	{
		finish();
	}
	
	
	
}
