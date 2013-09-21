package net.etticat.ettibox;

import java.util.List;

import net.etticat.ettibox.dbmodels.EntryDbHandler;
import net.etticat.ettibox.dto.FileSystemEntry;
import net.etticat.ettibox.models.LazyAdapter;
import net.etticat.ettibox.models.SharedPrefs;
import net.etticat.ettibox.models.WebServiceConnection;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * A list fragment representing a list of Items. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ItemDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ItemListFragment extends SherlockListFragment {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final String STATE_FOLDER_ID = "folder_id";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = mDummyCallbacks;

	protected List<FileSystemEntry> entries;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	protected EntryDbHandler mEntryDbHandler;
	private Integer mId = 0;
	private RefreshTask mRefreshTask;
	private LazyAdapter mLazyAdapter;

	protected PullToRefreshListView mPullToRefreshListView;

	public WebServiceConnection webServiceConnection;
	
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
		public void setFragment(ItemListFragment itemListFragment);
		public Boolean isTwoPane();
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks mDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(FileSystemEntry item) {
		}

		@Override
		public void setFragment(ItemListFragment itemListFragment) {			
		}

		@Override
		public Boolean isTwoPane() {
			return false;
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

		mEntryDbHandler = EntryDbHandler.getInstance();

		webServiceConnection = new WebServiceConnection();
	}

	@Override
	public void onResume() {

		mCallbacks.setFragment(this);
		entries = mEntryDbHandler.getEntries(mId);
		refreshListView();
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

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = inflater.inflate(R.layout.fragment_list, container, false);
		ListView lv = (ListView) layout.findViewById(R.id.list);

		mPullToRefreshListView = new PullToRefreshListView(getActivity());
		mPullToRefreshListView.setLayoutParams(lv.getLayoutParams());

		mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>()
				{
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				refresh();
			}
				});


		return mPullToRefreshListView;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		menu.setHeaderTitle(mLazyAdapter.getItem(info.position-1).getName());

		getActivity().getMenuInflater().inflate(R.menu.item_list_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {


		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		FileSystemEntry entry = mLazyAdapter.getItem(info.position-1);

		switch (item.getItemId()) {
		case R.id.action_create_folder:
			createFolder();

			break;
		case R.id.action_delete:
			new DeleteTask().execute(entry);

			break;

		default:
			break;
		}

		return true;
	}

	protected void refreshListView(){
		Context activity = getActivity();
		if(activity == null)
			return;
		mLazyAdapter = new LazyAdapter(getActivity(), entries);

		setListAdapter(mLazyAdapter);

	}

	public Integer getEntryId() {
		return mId;
	}

	public void setId(Integer id) {
		this.mId = id;
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
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FOLDER_ID)) 
			mId = savedInstanceState.getInt(STATE_FOLDER_ID);


		if(mCallbacks.isTwoPane()){
			setActivateOnItemClick(true);
		}

		registerForContextMenu(getListView());
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

		// Probably useless TODO check
		mCallbacks.setFragment(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = mDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.

		mCallbacks.onItemSelected(mLazyAdapter.getItem(position-1));
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
		outState.putInt(STATE_FOLDER_ID, mId);

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

		@Override
		protected void onPreExecute() {

			uuid = SharedPrefs.getUuid();
			user = SharedPrefs.getUsername();
			accessToken = SharedPrefs.getAccessToken();

			super.onPreExecute(); 
		}


		@Override
		protected Boolean doInBackground(Void... params) {

			entries = webServiceConnection.getDirectoryContent(mId, true);

			return (entries != null);
		}


		@Override
		protected void onPostExecute(final Boolean success) {
			mRefreshTask = null;

			if (success) {
				refreshListView();
				mEntryDbHandler.replaceEntries(entries, mId);

			} 
			mPullToRefreshListView.onRefreshComplete();
		}

		@Override
		protected void onCancelled() {
			mRefreshTask = null;
		}
	}


	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class DeleteTask extends AsyncTask<FileSystemEntry, Void, Boolean> {

		String uuid;
		String user;
		String accessToken;

		@Override
		protected void onPreExecute() {

			uuid = SharedPrefs.getUuid();
			user = SharedPrefs.getUsername();
			accessToken = SharedPrefs.getAccessToken();

			super.onPreExecute(); 
		}


		@Override
		protected Boolean doInBackground(FileSystemEntry... params) {
			return webServiceConnection.deleteItem(params[0].getId(), true);
		}


		@Override
		protected void onPostExecute(Boolean success) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			refresh();
		}
	}
	


	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class CreateFolderTask extends AsyncTask<String, Void, Boolean> {

		String uuid;
		String user;
		String accessToken;

		@Override
		protected void onPreExecute() {

			uuid = SharedPrefs.getUuid();
			user = SharedPrefs.getUsername();
			accessToken = SharedPrefs.getAccessToken();

			super.onPreExecute(); 
		}


		@Override
		protected Boolean doInBackground(String... params) {
			return webServiceConnection.createFolder(mId, params[0], true);
		}


		@Override
		protected void onPostExecute(final Boolean success) {
			refresh();
		}

	}

	public void createFolder() {
		if(getActivity()  == null) 
			return;
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Create Folder");
		alert.setMessage("Folder Name:");

		final EditText input = new EditText(getActivity());

		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				new CreateFolderTask().execute(value);
			}
		});
		alert.setNegativeButton("Cancel", null);

		alert.show();


	}

}
