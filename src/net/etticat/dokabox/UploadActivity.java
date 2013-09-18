package net.etticat.dokabox;

import net.etticat.dokabox.UploadService.BoundUploadServiceListener;
import net.etticat.dokabox.UploadService.LocalUploadBinder;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.models.WebServiceConnection.OnFileTransferProgressHandler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class UploadActivity extends SherlockFragmentActivity implements OnFileTransferProgressHandler,
	ItemListFragment.Callbacks, BoundUploadServiceListener{ 


	private ItemListFragment mCurrentItemListFragment;
	private Uri mFileUri;
	public static final String ARG_UPLOAD_URI = "upload_uri";
	
	private boolean mBound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		Intent intent = getIntent();
		mFileUri = intent.getData();
		
		if(mFileUri == null){
			finish();
			return;
		}

		ItemListFragment itemListFragment;
		itemListFragment = new ItemListFragment();		
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, itemListFragment).commit();
		
		mCurrentItemListFragment= itemListFragment;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.upload, menu);
		return true;
	}

	@Override
	public void progressChanged(FileSystemEntry entry, Integer value) {
		
	}

	@Override
	public void onItemSelected(FileSystemEntry item) {
		if(item.getType() == FileSystemEntryType.FOLDER){
			
			ItemListFragment subItemListFragment = new ItemListFragment();
			subItemListFragment.setId(item.getId());
			
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragment_container, subItemListFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(""+item.getId());
			ft.commit();
			
			mCurrentItemListFragment = subItemListFragment;
			return;
		}
		// If User clicks a file => nothing happenes
		
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_upload:
				startUpload();
				break;
			}
		return super.onMenuItemSelected(featureId, item);
	}

	private void startUpload() {
		if(mCurrentItemListFragment != null && mCurrentItemListFragment.getEntryId() != 0){
			Integer parentId = mCurrentItemListFragment.getEntryId();
			
			Intent intent = new Intent(this, UploadService.class);
			intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, parentId);
			intent.setData(mFileUri);
			startService(intent);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
		
	}
	

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {

			LocalUploadBinder binder = (LocalUploadBinder) service;
            binder.setListener(UploadActivity.this);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

	@Override
	protected void onStop() {
        if (mBound) {
        	unbindService(mConnection);
            mBound = false;
        }
    	super.onStop();
	}

	@Override
	public void onUploadStarted(FileSystemEntry entry) {
		finish();
	}

	@Override
	public Boolean onUploadError(FileSystemEntry entry, String message) {
		if(entry.getUri().equals(mFileUri)){
			
			Toast toast = new Toast(this);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.setText(message);
			toast.show();
			return true;
		}
		return false;
		
	}

	@Override
	public void setFragment(ItemListFragment itemListFragment) {
		mCurrentItemListFragment = itemListFragment;
		
	}

	@Override
	public Boolean isTwoPane() {
		return false;
	}

	@Override
	public void onUploadFinished(FileSystemEntry entry, Boolean success) {		
	}

}
