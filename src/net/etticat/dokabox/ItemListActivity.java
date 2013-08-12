package net.etticat.dokabox;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.models.SharedPrefs;



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
		ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private SharedPrefs sharedPrefs;
	private boolean mTwoPane; 
	ItemListFragment itemListFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_item_list);
		

		
		sharedPrefs = new SharedPrefs(this);
		
		
		if(sharedPrefs.getUsername() == ""){
			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		
		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			
			//T
			// itemListFragment.setActivateOnItemClick(true);
		}
		
		if(savedInstanceState != null)
			return;

		itemListFragment = new ItemListFragment();		
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.fragment_container, itemListFragment).commit();
		Bundle extras = getIntent().getExtras();
		
		if (extras != null && extras.containsKey(ItemDetailFragment.ARG_ITEM_ID)) {
			itemListFragment.setId(extras.getInt(ItemDetailFragment.ARG_ITEM_ID));
		}



//		TESTING
//		Intent detailIntent = new Intent(this, UploadActivity.class);
//		detailIntent.setData(Uri.parse("/storage/emulated/0/dokabox/24599/Lukas Kamleitner/Bilder/_DSC0072.JPG"));
//		startActivity(detailIntent);
//		TESTING
		
		
		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
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
}
