package net.etticat.dokabox;

import net.etticat.dokabox.UploadService.BoundUploadServiceListener;
import net.etticat.dokabox.UploadService.LocalUploadBinder;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.models.SharedPrefs;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;



/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends SherlockFragmentActivity implements
		ItemListFragment.Callbacks, SearchView.OnQueryTextListener, BoundUploadServiceListener{

	private static final int PICKFILE_RESULT_CODE = 12412;
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	
	private  Boolean mSearchActive = false;
	private SearchView mSearchView;
	private MenuItem mSearchItem;
	private boolean mTwoPane; 
	private ItemListFragment mItemListFragment;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.item_list, menu); 
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);
		
		if(SharedPrefs.getUsername() == ""){
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		
		if(savedInstanceState == null){
			
			mItemListFragment = new ItemListFragment();		
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, mItemListFragment).commit();
			Bundle extras = getIntent().getExtras();
			
			if (extras != null && extras.containsKey(ItemDetailFragment.ARG_ITEM_ID)) {
				mItemListFragment.setId(extras.getInt(ItemDetailFragment.ARG_ITEM_ID));
			}
		}

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

		}
		
	}
	
	

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(FileSystemEntry item) {
		mSearchItem.collapseActionView();
		
		if(item.getType() == FileSystemEntryType.FOLDER){
			
			ItemListFragment subItemListFragment = new ItemListFragment();
			subItemListFragment.setId(item.getId());
			
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragment_container, subItemListFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack(Integer.toString(item.getId()));
			ft.commit();
			return;
		}
		
		if (mTwoPane) {
			
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putInt(ItemDetailFragment.ARG_ITEM_ID, item.getId());
			
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.getId());
			startActivity(detailIntent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_upload:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("file/*");
				startActivityForResult(intent,PICKFILE_RESULT_CODE);
				break;
			case R.id.action_create_folder:
				mItemListFragment.createFolder();
				break;
			case R.id.search:
				mSearchActive = true;
				onQueryTextChange("-");

		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if(resultCode == RESULT_OK && data != null){
	    	switch (requestCode) {
			case PICKFILE_RESULT_CODE:
				Uri Fpath = data.getData();
				
				Integer parentId = mItemListFragment.getEntryId();
				
				Intent intent = new Intent(this, UploadService.class);
				intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, parentId);
				intent.setData(Fpath);
				startService(intent);
				bindService(intent, mConnection, 0);
				
				break;

			default:
				break;
			}
	    }
		
	    
	    super.onActivityResult(requestCode, resultCode, data);

	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {



		@Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {

			LocalUploadBinder binder = (LocalUploadBinder) service;
            binder.setListener(ItemListActivity.this);
        }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
    };

	@Override
	public boolean onQueryTextSubmit(String query) {

		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) { 
		if(!mSearchActive) 
			return false;
		
		if(!(mItemListFragment instanceof SearchListFragment)){
			
			mItemListFragment = new SearchListFragment();
			
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.replace(R.id.fragment_container, mItemListFragment, "search");
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.addToBackStack("search");
			ft.commit();
		}
		((SearchListFragment) mItemListFragment).setSearchQuery(newText);
		return false;
	}

	@Override
	public void setFragment(ItemListFragment itemListFragment) {
		mSearchActive = (itemListFragment instanceof SearchListFragment);
		this.mItemListFragment = itemListFragment;
		
	}



	@Override
	public Boolean isTwoPane() {
		return mTwoPane;
	}



	@Override
	public void onUploadStarted(FileSystemEntry entry) {
		
		
	}



	@Override
	public Boolean onUploadError(FileSystemEntry entry, String message) {
		return null;
	}




	@Override
	public void onUploadFinished(FileSystemEntry entry, Boolean success) {
		if(mItemListFragment instanceof ItemListFragment && entry.getId() == ((ItemListFragment) mItemListFragment).getEntryId())
			mItemListFragment.refresh();
		
	}
}
