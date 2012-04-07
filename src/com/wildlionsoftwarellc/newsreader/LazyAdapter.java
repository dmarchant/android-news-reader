package com.wildlionsoftwarellc.newsreader;

import java.util.ArrayList;
import java.util.HashMap;

import com.wildlionsoftwarellc.newsreader.R;
 
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {
	 
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; 
 
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);
 
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView description = (TextView)vi.findViewById(R.id.description); // description
        ImageView thumb_image = (ImageView)vi.findViewById(R.id.list_image); // thumb image
 
        HashMap<String, String> article = new HashMap<String, String>();
        article = data.get(position);
 
        // Setting all values in listview
        title.setText(article.get(RSSReader.KEY_TITLE));
        description.setText(article.get(RSSReader.KEY_DESCRIPTION));
        imageLoader.DisplayImage(article.get(RSSReader.KEY_THUMB_URL), thumb_image);
        return vi;
    }
}
