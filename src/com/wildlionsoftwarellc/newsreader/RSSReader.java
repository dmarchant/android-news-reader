package com.wildlionsoftwarellc.newsreader;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wildlionsoftwarellc.newsreader.LazyAdapter;
import com.wildlionsoftwarellc.newsreader.Preferences;
import com.wildlionsoftwarellc.newsreader.R;
import com.wildlionsoftwarellc.newsreader.RSSHandler.NewsItem;

public class RSSReader extends Activity {
	private static final String FEED_ACTION = "com.wildlionsoftwarellc.rest.FEED";

	static final private int MENU_UPDATE = Menu.FIRST;
	static final private int MENU_PREFERENCES = Menu.FIRST + 1;
	static final private int MENU_EXIT = Menu.FIRST + 2;
	private static final int SHOW_PREFERENCES = 1;

	// Keys for the listview
	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_LINK = "link";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_THUMB_URL = "thumb_url";

	private Intent ChangeListenerIntent = null;
	private ListView list;
	private LazyAdapter adapter;
	ArrayList<HashMap<String, String>> articleList;

	boolean wifionly = false;
	String rssFeedUrl = "http://news.google.com/?output=rss";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Eula.showEula(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		articleList = new ArrayList<HashMap<String, String>>();
		list = (ListView) this.findViewById(R.id.rssreaderListView);
		adapter = new LazyAdapter(this, articleList);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				HashMap<String, String> map = articleList.get(position);
				// Open the URL in a browser
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(map.get(KEY_LINK)));
				startActivity(intent);
			}
		});

		updateFromPreferences();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(ChangeListenerIntent == null)
			ChangeListenerIntent = registerReceiver(receiver,new IntentFilter(FEED_ACTION));
		updateFromPreferences();
		updateArticles();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(ChangeListenerIntent != null) {
			unregisterReceiver(receiver);
			ChangeListenerIntent = null;
		}
	}

	private void updateArticles() {
		// Check if the user wants to update on wifi only
		if (wifionly) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetwork = cm
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetwork != null) {
				if (!wifiNetwork.isConnected()) {
					new AlertDialog.Builder(this)
							.setMessage(
									"You have the preference set to only update articles on WiFi. Please connect to WiFi or change this setting")
							.setCancelable(false)
							.setNeutralButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {

										}
									}).show();
					return;
				}
			}
		}
		registerReceiver(receiver, new IntentFilter(FEED_ACTION));
		// Retrieve the RSS feed
		try {
			HttpGet feedRequest = new HttpGet(new URI(rssFeedUrl));
			RestTask task = new RestTask(this, FEED_ACTION);
			task.execute(feedRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_update);
		menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);
		menu.add(0, MENU_EXIT, Menu.NONE, R.string.menu_exit);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (MENU_UPDATE): {
			updateArticles();
			return true;
		}
		case (MENU_PREFERENCES): {
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, SHOW_PREFERENCES);
			return true;
		}
		case (MENU_EXIT): {
			exitOptionsDialog();
		}
		}
		return false;
	}

	private void exitOptionsDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.menu_exit)
				.setMessage(R.string.app_exit_message)
				.setNegativeButton(R.string.str_no,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
							}
						})
				.setPositiveButton(R.string.str_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								finish();
							}
						}).show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SHOW_PREFERENCES)
			if (resultCode == Activity.RESULT_OK) {
				updateFromPreferences();
				updateArticles();
			}
	}

	private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		wifionly = prefs.getBoolean(Preferences.PREF_WIFI_ONLY, false);
		rssFeedUrl = prefs.getString(Preferences.PREF_RSS_URL,
				"http://news.google.com/?output=rss");
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
			// Process the response data
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser p = factory.newSAXParser();
				RSSHandler parser = new RSSHandler();
				p.parse(new InputSource(new StringReader(response)), parser);

				// adapter.clear();
				adapter.imageLoader.clearCache();
				articleList.clear();
				int idVal = 1;
				for (NewsItem item : parser.getParsedItems()) {
					// creating new HashMap
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(KEY_ID, Integer.toString(idVal));
					map.put(KEY_TITLE, item.title);
					map.put(KEY_DESCRIPTION, item.pubDate);
					map.put(KEY_LINK, item.link);
					map.put(KEY_THUMB_URL, item.thumbUrl);
					articleList.add(map);
					idVal++;
				}
				adapter.notifyDataSetChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}