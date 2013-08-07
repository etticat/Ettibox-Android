package net.etticat.dokabox;

import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import net.etticat.dokabox.LoginActivity.UserLoginTask;
import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dto.UserData;
import net.etticat.dokabox.dto.FileSystemEntry.FileSystemEntryType;
import net.etticat.dokabox.dummy.DummyContent;
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
public class ItemListFragment extends ListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(FileSystemEntry item);
	}
	
	private EntryDbHandler entryDbHandler;
	private Integer id = 0;
	private RefreshTask mRefreshTask;
	private SharedPrefs sharedPrefs;
	private LazyAdapter mLazyAdapter;

	public WebServiceConnection webServiceConnection;

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(FileSystemEntry item) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

    	setHasOptionsMenu(true);
    	
		entryDbHandler = new EntryDbHandler(getActivity());
		sharedPrefs = new SharedPrefs(getActivity());
		webServiceConnection = new WebServiceConnection(getActivity());
		
	}
	
	@Override
	public void onResume() {

		refreshListView(entryDbHandler.getEntries(id));
		refresh();
		
		super.onResume();
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.itemlistfragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			refresh();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void refreshListView(List<FileSystemEntry> entries){
		mLazyAdapter = new LazyAdapter(getActivity(), entries);
		setListAdapter(mLazyAdapter);

	}
	public void setId(Integer id) {
		this.id = id;
	}

	public void refresh(){
		if (mRefreshTask != null) {
			return;
		}
		
		mRefreshTask = new RefreshTask();
		mRefreshTask.execute((Void) null);
	}
	

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		
		FileSystemEntry entry = mLazyAdapter.getItem(position);
			mCallbacks.onItemSelected(mLazyAdapter.getItem(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class RefreshTask extends AsyncTask<Void, Void, Boolean> {
		
		String uuid;
		String user;
		String accessToken;
		List<FileSystemEntry> entries;
		
		@Override
		protected void onPreExecute() {
			
			uuid = sharedPrefs.getUuid();
			user = sharedPrefs.getUsername();
			accessToken = sharedPrefs.getAccessToken();
			
			super.onPreExecute(); 
		}
		
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			entries = webServiceConnection.getDirectoryContent(id, true);
			
			if (entries != null) {			
				return true;
			}

			return false;
		}


		@Override
		protected void onPostExecute(final Boolean success) {
			mRefreshTask = null;

			if (success) {
				refreshListView(entries);
				
				entryDbHandler.replaceEntries(entries, id);
				
				
			} else {
				
			}
		}

		@Override
		protected void onCancelled() {
			mRefreshTask = null;
		}
	}
}
