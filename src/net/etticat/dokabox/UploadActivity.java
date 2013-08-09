package net.etticat.dokabox;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.models.WebServiceConnection;
import net.etticat.dokabox.models.WebServiceConnection.OnFileTransferProgressHandler;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

public class UploadActivity extends SherlockFragmentActivity implements OnFileTransferProgressHandler,
	ItemListFragment.Callbacks{ 


	private ItemListFragment itemListFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);
		
		Intent intent = getIntent();
		final Uri asd = intent.getData();
		Bundle bundle = intent.getExtras();

		final FileSystemEntry entry = new FileSystemEntry();
		entry.setId(24596);
		
//		final WebServiceConnection webServiceConnection = new WebServiceConnection(this);
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				webServiceConnection.uploadFile(asd, entry, UploadActivity.this);
//			}
//		}).start();

		itemListFragment = new ItemListFragment();		
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_container, itemListFragment).commit();
		
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
		// TODO Auto-generated method stub
		
	}

}
