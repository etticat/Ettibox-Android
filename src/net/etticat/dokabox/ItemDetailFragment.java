package net.etticat.dokabox;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import net.etticat.dokabox.DownloadService.BoundServiceListener;
import net.etticat.dokabox.DownloadService.LocalBinder;
import net.etticat.dokabox.dto.FileSystemEntry;


import net.etticat.dokabox.dbmodels.EntryDbHandler;
/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment implements OnClickListener, BoundServiceListener {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	private EntryDbHandler mEntryDbHandler;
	
	private FileSystemEntry mEntry;
	
	private TextView tv_name;
	private TextView tv_size;
	private CheckBox cb_syncSubscription;
	private Button bt_download;
	private Button bt_open;
	
	private View mView;

	private DownloadService mService = null;
    private Boolean mBound = false;
	
    private ProgressBar downloadProgressBar;
    private Intent downloadServiceIntent;
    
    
	/**
	 * The dummy content this fragment is presenting.
	 */
	

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		if (!getArguments().containsKey(ARG_ITEM_ID)) {
			getActivity().finish();
			return;
		}
		
		mEntryDbHandler = new EntryDbHandler(getActivity());
		
		String stringId = getArguments().getString(ARG_ITEM_ID);
		if(stringId != null)
			mEntry = mEntryDbHandler.getFileSystemEntry(Integer.valueOf(stringId));
		else {
			getActivity().finish();
			return;
		}

		downloadServiceIntent = new Intent(getActivity(), DownloadService.class);
		downloadServiceIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, ""+mEntry.getId());
	}
	

	@Override
	public void onStart() {

		getActivity().getApplicationContext().bindService(downloadServiceIntent, mConnection, 0);
		super.onStart();
	}

	@Override
	public void onStop() {
        if (mBound) {
        	getActivity().getApplicationContext().unbindService(mConnection);
            mBound = false;
        }
		super.onStop();
	}

	private void mapViews() {
		tv_name = (TextView) mView.findViewById(R.id.tv_detail_name);
		tv_size = (TextView) mView.findViewById(R.id.tv_detail_size);
		cb_syncSubscription = (CheckBox) mView.findViewById(R.id.cb_detail_sync_subscription);
		bt_download = (Button) mView.findViewById(R.id.bt_detail_download);
		bt_download.setOnClickListener(this);
		bt_open = (Button) mView.findViewById(R.id.bt_detail_open);
		bt_open.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		mapViews();

		
		if (mEntry != null) {
			tv_name.setText(String.format(getResources().getString(R.string.detail_name), mEntry.getName()));
			tv_size.setText(String.format(getResources().getString(R.string.detail_name), readableFileSize(mEntry.getSize())));
			cb_syncSubscription.setChecked(mEntry.getSyncSubscribed());

		}

		
		return mView;
	}
	public static String readableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_detail_download:
			getActivity().startService(downloadServiceIntent);
			
			break;
		case R.id.bt_detail_open:
			getActivity().startService(downloadServiceIntent);
			break;
		default:
			break;
		}
	}
	
	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {


		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {

			LocalBinder binder = (LocalBinder) service;
            binder.setListener(ItemDetailFragment.this);
            mService = binder.getService();
            
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mBound = false;
        }
    };

	@Override
	public void refreshProgress(final Integer value) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if(downloadProgressBar == null){
					FrameLayout frameLayout = (FrameLayout) mView.findViewById(R.id.download_container);
					frameLayout.removeAllViews();
					downloadProgressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
					frameLayout.addView(downloadProgressBar);
				}
				downloadProgressBar.setProgress(value);
			}
		});

	}
}
