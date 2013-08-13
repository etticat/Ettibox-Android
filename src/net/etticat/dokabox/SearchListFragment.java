package net.etticat.dokabox;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import net.etticat.dokabox.ItemListFragment.RefreshTask;
import net.etticat.dokabox.LoginActivity.UserLoginTask;
import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.UserData;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.models.LazyAdapter;
import net.etticat.dokabox.models.SharedPrefs;
import net.etticat.dokabox.models.WebServiceConnection;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SearchListFragment extends ItemListFragment {
	
	private RefreshSearchQuery refreshSearchQuery;

	private Boolean running = false;
	private String searchQuery = null;
	
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
		refresh();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		refreshSearchQuery = new RefreshSearchQuery();
	}

	@Override
	public void refresh() {
		if(refreshSearchQuery != null)
			refreshSearchQuery.cancel(true);
		
		if(searchQuery != null){
			refreshSearchQuery = new RefreshSearchQuery();
			refreshSearchQuery.execute((Void) null);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class RefreshSearchQuery extends AsyncTask<Void, Void, Boolean> {
	
		
		@Override
		protected void onPreExecute() {
			running = true;
			
			super.onPreExecute(); 
		}
		
		
		@Override
		protected Boolean doInBackground(Void... params) {
			

			if(entryDbHandler == null) return false ;
			entries = entryDbHandler.getEntriesBySearchQuery(searchQuery);
			return entries != null;
		}


		@Override
		protected void onPostExecute(final Boolean success) {

			if (success) {
				refreshListView();
				
			} 
			mPullToRefreshListView.onRefreshComplete();
			running = false;
		}

		@Override
		protected void onCancelled() {
			running = false;
		}
	}
	

}
