package org.lyh.c4.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.os.Environment;
import android.util.Log;
import android.util.Xml;

public class PropGeter {
	private static final String TAG = "prop";

	public  PropGeter(){
		try {
			readProp();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private  int time;
	private  int bar_max;


	private  void readProp() throws Exception{
		int [] temp = new int[2];
		int i = 0;
		File file = new File(Environment.getExternalStorageDirectory(),"prop.xml");
		InputStream  prop = new FileInputStream(file);
		XmlPullParser pullParser = Xml.newPullParser();
		
		pullParser.setInput(prop, "UTF-8");
		int event = pullParser.getEventType();
		while(event != XmlPullParser.END_DOCUMENT){
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				String tagName = pullParser.getName();
				if("prop".equals(tagName)){
					temp[i++] = Integer.valueOf(pullParser.nextText());
				}
				break;
			default:
				break;
			}
			
			event = pullParser.next();
		}
		
		time = temp[0];
		bar_max = temp[1];
	}
	public  int getTime(){
		Log.i(TAG, String.valueOf(time));
		return time;
	}
	
	public  int getBarMax(){
		Log.i(TAG, String.valueOf(bar_max));
		return bar_max;
	}
}
