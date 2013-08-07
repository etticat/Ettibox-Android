package net.etticat.dokabox.models;


import java.io.Console;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.etticat.dokabox.R;
import net.etticat.dokabox.dto.FileSystemEntry;

 
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class LazyAdapter extends BaseAdapter {
 
    private Activity activity;
    private List<FileSystemEntry> data;
    private static LayoutInflater inflater=null;
 
    public LazyAdapter(Activity a, List<FileSystemEntry> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return data.size();
    }
 
    public FileSystemEntry getItem(int position) {
		
    	return data.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.list_row, null);
 
        TextView title = (TextView)vi.findViewById(R.id.title); // title
        TextView artist = (TextView)vi.findViewById(R.id.artist); // artist name 
        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
 
        FileSystemEntry song = data.get(position);
 
        title.setText(song.getName());
        artist.setText(song.getAlternationDate().toString());
        duration.setText(""+song.getId());
        // thumb_image.setImageURI(Uri.parse(song.getUri()));
        return vi;
    }
}