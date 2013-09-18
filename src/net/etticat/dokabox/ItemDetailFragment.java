package net.etticat.dokabox;

import java.io.File;

import net.etticat.dokabox.DownloadService.BoundDownloadServiceListener;
import net.etticat.dokabox.DownloadService.LocalBinder;
import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.models.FileInfoChecker;
import net.etticat.dokabox.models.FileInfoChecker.FileInfoStatus;
import net.etticat.dokabox.models.LazyAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends SherlockFragment implements OnClickListener, BoundDownloadServiceListener {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	private EntryDbHandler mEntryDbHandler; 

	private FileSystemEntry mEntry;

	private TextView mTvName;
	private TextView mTvSize;
	private Button mBtDownload;
	private Button mBtOpen;
	private ProgressBar mDownloadProgressBar;

	private View mView;

	private DownloadService mService = null;
	private Boolean mBound = false;

	private Intent mDownloadServiceIntent;
	private FileInfoStatus mStatus;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDownloadServiceIntent = new Intent(getActivity(), DownloadService.class);

		if (!getArguments().containsKey(ARG_ITEM_ID)) {
			getActivity().finish();
			return;
		}

		mEntryDbHandler = EntryDbHandler.getInstance();

		mEntry = mEntryDbHandler.getFileSystemEntry(getArguments().getInt(ARG_ITEM_ID));
		if(mEntry == null){
			getActivity().finish();
			return;
		}

		mDownloadServiceIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, mEntry.getId());
	}


	@Override
	public void onStart() {

		if(getActivity() != null)
			getActivity().getApplicationContext().bindService(mDownloadServiceIntent, mConnection, 0);
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
		mTvName = (TextView) mView.findViewById(R.id.tv_detail_name);
		mTvSize = (TextView) mView.findViewById(R.id.tv_detail_size);
		mBtDownload = (Button) mView.findViewById(R.id.bt_detail_download);
		mBtDownload.setOnClickListener(this);
		mDownloadProgressBar = (ProgressBar) mView.findViewById(R.id.pb_detail_download_progress);
		mBtOpen = (Button) mView.findViewById(R.id.bt_detail_open);
		mBtOpen.setOnClickListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_item_detail,
				container, false);

		mapViews();


		if (mEntry != null) {
			mTvName.setText(String.format(getResources().getString(R.string.detail_name), mEntry.getName()));
			mTvSize.setText(String.format(getResources().getString(R.string.detail_name), LazyAdapter.readableFileSize(mEntry.getSize())));

			FileInfoChecker fileInfoChecker = new FileInfoChecker();

			mStatus = fileInfoChecker.getStatus(mEntry, mEntryDbHandler.getPath(mEntry));
			reloadStatus();

		}


		return mView;
	}
	private void reloadStatus() {
		switch (mStatus) {
		case NOT_EXISTENT:
			mDownloadProgressBar.setVisibility(View.GONE);
			mBtOpen.setVisibility(View.GONE);
			mBtDownload.setVisibility(View.VISIBLE);
			break;
		case OLD_VERSION:
			mDownloadProgressBar.setVisibility(View.GONE);
			mBtOpen.setVisibility(View.VISIBLE);
			mBtDownload.setVisibility(View.VISIBLE);
			
			break;
		case CURRENT:
			mDownloadProgressBar.setVisibility(View.GONE);
			mBtOpen.setVisibility(View.VISIBLE);
			mBtDownload.setVisibility(View.GONE);
			
			break;
		case DOWNLOADING:
			mDownloadProgressBar.setVisibility(View.VISIBLE);
			mBtOpen.setVisibility(View.GONE);
			mBtDownload.setVisibility(View.GONE);
			
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.bt_detail_download:
			getActivity().startService(mDownloadServiceIntent);
			getActivity().getApplicationContext().bindService(mDownloadServiceIntent, mConnection, 0);
			onDownloadAdded();
			break;
		case R.id.bt_detail_open:
			openFile();
			break;
		default:
			break;
		}
	}

	private void openFile() {

		File file = new File(mEntryDbHandler.getPath(mEntry), mEntry.getName());
		
		if(file.exists()){
			Uri uri = Uri.fromFile(file);

			String mime = getMimeType(uri.toString());

			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(uri, mime);
			startActivity(intent); 
		}

	}

	private String getMimeType(String url)
	{
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
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
			onDownloadAdded();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mBound = false;
			
			
		}
	};

	@Override
	public void refreshProgress(FileSystemEntry entry, final Integer value) {

		if(entry.equals(mEntry) && getActivity() != null){
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {

						mDownloadProgressBar.setIndeterminate(false);
						mDownloadProgressBar.setProgress(value);
					
				}
			});
		}
	}

	@Override
	public void onDownloadFinished(FileSystemEntry entry) {

		if(entry.equals(mEntry) && getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mStatus = FileInfoStatus.CURRENT;
						reloadStatus();

				}
			});
		}
	}

	@Override
	public void onDownloadAdded(){
		if(!mBound || !mService.isEntryInQueue(mEntry))
			return;

		if(getActivity() != null){
			
			mStatus = FileInfoStatus.DOWNLOADING;
			reloadStatus();
		}

	}

	@Override
	public void onDownloadFailed(FileSystemEntry entry) {
		
		if(entry.equals(mEntry) && getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mStatus = FileInfoStatus.NOT_EXISTENT;
					reloadStatus();

				}
			});
		}
	}
}
