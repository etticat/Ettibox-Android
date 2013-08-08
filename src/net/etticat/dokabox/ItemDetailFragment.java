package net.etticat.dokabox;

import java.text.DecimalFormat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.CheckBox;
import android.widget.TextView;
import net.etticat.dokabox.dto.FileSystemEntry;


import net.etticat.dokabox.dbmodels.EntryDbHandler;
/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
public class ItemDetailFragment extends Fragment {
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
	
	private View mView;

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
	}

	private void mapViews() {
		tv_name = (TextView) mView.findViewById(R.id.tv_detail_name);
		tv_size = (TextView) mView.findViewById(R.id.tv_detail_size);
		cb_syncSubscription = (CheckBox) mView.findViewById(R.id.cb_detail_sync_subscription);
		
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
	

}
