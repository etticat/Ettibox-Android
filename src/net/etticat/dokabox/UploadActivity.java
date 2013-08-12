package net.etticat.dokabox;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import net.etticat.dokabox.UploadService.LocalUploadBinder;
import net.etticat.dokabox.UploadService.BoundUploadServiceListener;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.models.WebServiceConnection;
import net.etticat.dokabox.models.WebServiceConnection.OnFileTransferProgressHandler;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

public class UploadActivity extends SherlockFragmentActivity implements OnFileTransferProgressHandler,
	ItemListFragment.Callbacks, BoundUploadServiceListener{ 


	private ItemListFragment currentItemListFragment;
	private Uri fileUri;
	public static final String ARG_UPLOAD_URI = "upload_uri";
	

	private UploadService mService;
	private boolean mBound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		Intent intent = getIntent();
		fileUri = intent.getData();
		
		if(fileUri == null){
			finish();
			return;
		}

		ItemListFragment itemListFragment;
		itemListFragment = new ItemListFragment();		
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, itemListFragment).commit();
		
		currentItemListFragment= itemListFragment;
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.upload, menu);
		return true;
	}

	@Override
	public void progressChanged(FileSystemEntry entry, Integer value) {
		// TODO Auto-generated method stub
		
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
			
			currentItemListFragment = subItemListFragment;
			return;
		}
		
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
		if(currentItemListFragment != null && currentItemListFragment.getEntryId() != 0){
			Integer parentId = currentItemListFragment.getEntryId();
			
			Intent intent = new Intent(this, UploadService.class);
			intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, parentId);
			intent.setData(fileUri);
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
		if(entry.getUri().equals(fileUri)){
			
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
		currentItemListFragment = itemListFragment;
		
	}

}
