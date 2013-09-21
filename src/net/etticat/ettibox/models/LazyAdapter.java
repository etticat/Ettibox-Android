package net.etticat.ettibox.models;


import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.etticat.ettibox.R;
import net.etticat.ettibox.dto.FileSystemEntry;
import net.etticat.ettibox.dto.FileSystemEntry.FileSystemEntryType;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class LazyAdapter extends BaseAdapter {
 
    private List<FileSystemEntry> mData;
    private static LayoutInflater mInflater=null;
    private Resources mResources;
    private Date mZeroDate = new Date(0);
 
    public LazyAdapter(Activity activity, List<FileSystemEntry> fileSystemEntries) {
        mData=fileSystemEntries;
        mResources = ContextProvider.getContext().getResources();
        mInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return mData.size();
    }
 
    public FileSystemEntry getItem(int position) {
		
    	return mData.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = mInflater.inflate(R.layout.list_row, null);
 
        TextView title = (TextView)vi.findViewById(R.id.list_item_title); // title
        TextView date = (TextView)vi.findViewById(R.id.list_item_date); // artist name 
        TextView size = (TextView)vi.findViewById(R.id.list_item_size); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.list_image); // thumb image
        ImageView sync_image =(ImageView)vi.findViewById(R.id.list_image_sync); // thumb image (Sync button)
 
        FileSystemEntry song = mData.get(position);
 
        title.setText(song.getName());
        if(!song.getAlternationDate().equals(mZeroDate))
        	date.setText(song.getAlternationDate().toString());
        else 
        	date.setText("");
        if(song.getType() != FileSystemEntryType.FOLDER)
        	size.setText(readableFileSize(song.getSize()));
        
        if(song.getSyncSubscribed())
        	sync_image.setImageDrawable(mResources.getDrawable(R.drawable.issynce));
        thumb_image.setImageDrawable(getIcon(song));
        return vi;
    }
    
    private Drawable getIcon(FileSystemEntry song){
    	
    	if(song.getType() == FileSystemEntryType.FOLDER)
    		return mResources.getDrawable(R.drawable.ic_folder);
    	
    	String extension = "";

    	int i = song.getName().lastIndexOf('.');
    	int p = Math.max(song.getName().lastIndexOf('/'), song.getName().lastIndexOf('\\'));

    	if (i > p) {
    	    extension = song.getName().substring(i+1).toLowerCase(Locale.ENGLISH);
    	}
    	
		if(extension.equals("acc")){
	    	return mResources.getDrawable(R.drawable.ic_file_aac);
		}
		else if(extension.equals("ai")){
			return mResources.getDrawable(R.drawable.ic_file_ai);
		}
		else if(extension.equals("aiff")){
			return mResources.getDrawable(R.drawable.ic_file_aiff);
		}
		else if(extension.equals("avi")){
			return mResources.getDrawable(R.drawable.ic_file_avi);
		}
		else if(extension.equals("bmp")){
			return mResources.getDrawable(R.drawable.ic_file_bmp);
		}
		else if(extension.equals("c")){
			return mResources.getDrawable(R.drawable.ic_file_c);
		}
		else if(extension.equals("cpp")){
			return mResources.getDrawable(R.drawable.ic_file_cpp);
		}
		else if(extension.equals("css")){
			return mResources.getDrawable(R.drawable.ic_file_css);
		}
		else if(extension.equals("dat")){
			return mResources.getDrawable(R.drawable.ic_file_dat);
		}
		else if(extension.equals("dmg")){
			return mResources.getDrawable(R.drawable.ic_file_dmg);
		}
		else if(extension.equals("doc") || extension.equals("docx")){
			return mResources.getDrawable(R.drawable.ic_file_doc);
		}
		else if(extension.equals("dotx")){
			return mResources.getDrawable(R.drawable.ic_file_dotx);
		}
		else if(extension.equals("dwg")){
			return mResources.getDrawable(R.drawable.ic_file_dwg);
		}
		else if(extension.equals("dxf")){
			return mResources.getDrawable(R.drawable.ic_file_dxf);
		}
		else if(extension.equals("eps")){
			return mResources.getDrawable(R.drawable.ic_file_eps);
		}
		else if(extension.equals("exe")){
			return mResources.getDrawable(R.drawable.ic_file_exe);
		}
		else if(extension.equals("flv")){
			return mResources.getDrawable(R.drawable.ic_file_flv);
		}
		else if(extension.equals("gif")){
			return mResources.getDrawable(R.drawable.ic_file_gif);
		}
		else if(extension.equals("h")){
			return mResources.getDrawable(R.drawable.ic_file_h);
		}
		else if(extension.equals("hpp")){
			return mResources.getDrawable(R.drawable.ic_file_hpp);
		}
		else if(extension.equals("html")){
			return mResources.getDrawable(R.drawable.ic_file_html);
		}
		else if(extension.equals("ics")){
			return mResources.getDrawable(R.drawable.ic_file_ics);
		}
		else if(extension.equals("iso")){
			return mResources.getDrawable(R.drawable.ic_file_iso);
		}
		else if(extension.equals("java")){
			return mResources.getDrawable(R.drawable.ic_file_java);
		}
		else if(extension.equals("jpg") || extension.equals("jpeg")){
			return mResources.getDrawable(R.drawable.ic_file_jpg);
		}
		else if(extension.equals("key")){
			return mResources.getDrawable(R.drawable.ic_file_key);
		}
		else if(extension.equals("mid")){
			return mResources.getDrawable(R.drawable.ic_file_mid);
		}
		else if(extension.equals("mp3")){
			return mResources.getDrawable(R.drawable.ic_file_mp3);
		}
		else if(extension.equals("mp4")){
			return mResources.getDrawable(R.drawable.ic_file_mp4);
		}
		else if(extension.equals("mpg") || extension.equals("mpeg")){
			return mResources.getDrawable(R.drawable.ic_file_mpg);
		}
		else if(extension.equals("odf")){
			return mResources.getDrawable(R.drawable.ic_file_odf);
		}
		else if(extension.equals("ods")){
			return mResources.getDrawable(R.drawable.ic_file_ods);
		}
		else if(extension.equals("odt")){
			return mResources.getDrawable(R.drawable.ic_file_odt);
		}
		else if(extension.equals("otp")){
			return mResources.getDrawable(R.drawable.ic_file_otp);
		}
		else if(extension.equals("ots")){
			return mResources.getDrawable(R.drawable.ic_file_ots);
		}
		else if(extension.equals("ott")){
			return mResources.getDrawable(R.drawable.ic_file_ott);
		}
		else if(extension.equals("pdf")){
			return mResources.getDrawable(R.drawable.ic_file_pdf);
		}
		else if(extension.equals("php")){
			return mResources.getDrawable(R.drawable.ic_file_php);
		}
		else if(extension.equals("png")){
			return mResources.getDrawable(R.drawable.ic_file_png);
		}
		else if(extension.equals("ppt") || extension.equals("pptx")){
			return mResources.getDrawable(R.drawable.ic_file_ppt); 
		}
		else if(extension.equals("psd")){
			return mResources.getDrawable(R.drawable.ic_file_psd);
		}
		else if(extension.equals("py")){
			return mResources.getDrawable(R.drawable.ic_file_py);
		}
		else if(extension.equals("qt")){
			return mResources.getDrawable(R.drawable.ic_file_qt);
		}
		else if(extension.equals("rar")){
			return mResources.getDrawable(R.drawable.ic_file_rar);
		}
		else if(extension.equals("rb")){
			return mResources.getDrawable(R.drawable.ic_file_rb);
		}
		else if(extension.equals("rtf")){
			return mResources.getDrawable(R.drawable.ic_file_rtf);
		}
		else if(extension.equals("sql")){
			return mResources.getDrawable(R.drawable.ic_file_sql);
		}
		else if(extension.equals("tga")){
			return mResources.getDrawable(R.drawable.ic_file_tga);
		}
		else if(extension.equals("tgz")){
			return mResources.getDrawable(R.drawable.ic_file_tgz);
		}
		else if(extension.equals("tiff")){
			return mResources.getDrawable(R.drawable.ic_file_tiff);
		}
		else if(extension.equals("txt")){
			return mResources.getDrawable(R.drawable.ic_file_txt);
		}
		else if(extension.equals("wav")){
			return mResources.getDrawable(R.drawable.ic_file_wav);
		}
		else if(extension.equals("xls") || extension.equals("xlsx")){
			return mResources.getDrawable(R.drawable.ic_file_xls);
		}
		else if(extension.equals("xml")){
			return mResources.getDrawable(R.drawable.ic_file_xml);
		}
		else if(extension.equals("yml")){
			return mResources.getDrawable(R.drawable.ic_file_yml);
		}
		else if(extension.equals("zip")){
			return mResources.getDrawable(R.drawable.ic_file_zip);
		}

    	return mResources.getDrawable(R.drawable.ic_file);
    }
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}