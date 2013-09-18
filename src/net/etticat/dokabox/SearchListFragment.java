package net.etticat.dokabox;

import android.os.AsyncTask;
import android.os.Bundle;

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
			
			super.onPreExecute(); 
		}
		
		
		@Override
		protected Boolean doInBackground(Void... params) {
			

			if(mEntryDbHandler == null) return false ;
			entries = mEntryDbHandler.getEntriesBySearchQuery(searchQuery);
			return entries != null;
		}


		@Override
		protected void onPostExecute(final Boolean success) {

			if (success) {
				refreshListView();
				
			} 
			mPullToRefreshListView.onRefreshComplete();
		}
	}
}
