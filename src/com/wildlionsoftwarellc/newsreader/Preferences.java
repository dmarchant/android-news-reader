package com.wildlionsoftwarellc.newsreader;

import com.wildlionsoftwarellc.newsreader.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.webkit.URLUtil;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

  public static final String PREF_WIFI_ONLY = "PREF_WIFI_ONLY";
  public static final String PREF_RSS_URL = "PREF_RSS_URL";

  SharedPreferences prefs;

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    addPreferencesFromResource(R.xml.userpreferences);	
  }
  
  @Override
  protected void onResume() {
      super.onResume();
      // Set up a listener whenever a key changes
      getPreferenceScreen().getSharedPreferences()
              .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
      super.onPause();
      // Unregister the listener whenever a key changes
      getPreferenceScreen().getSharedPreferences()
              .unregisterOnSharedPreferenceChangeListener(this);
  }
  
  @Override
  public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
    if (key.equals(PREF_RSS_URL)) {
        String value = sp.getString(key, null);
        if (!URLUtil.isValidUrl(value)){
        	new AlertDialog.Builder(this)
			.setMessage(
					"Please enter a valid URL")
			.setCancelable(false)
			.setNeutralButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog, int id) {
							
						}
					}).show();
        	EditTextPreference p = (EditTextPreference) findPreference(PREF_RSS_URL);
            p.setText("http://news.google.com/?output=rss");
        }
        //if (!Pattern.matches(pattern, value)) {
            // The value is not a valid email address.
            // Do anything like advice the user or change the value
        //}
    }        
  }
}