package com.example.advenguard;

import android.app.Application;

public class Globals extends Application
{
	private String data=null;
	 
	   public String getData()
	   {
	     return this.data;
	   }
	 
	   public void setData(String d)
	   {
	     this.data=d;
	   }
}
