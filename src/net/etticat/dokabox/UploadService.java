package net.etticat.dokabox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData.Item;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.models.WebServiceConnection;
import net.etticat.dokabox.models.WebServiceConnection.OnFileTransferProgressHandler;
import net.etticat.dokabox.models.WebServiceConnection.OnUploadProgressHandler;

public class UploadService  extends Service implements OnUploadProgressHandler{

	// Binder given to clients
	private final IBinder mBinder = new LocalUploadBinder();
	// Random number generator
	Boolean running = true;
	private int ONGOING_NOTIFICATION_ID = 1232282;
	BoundUploadServiceListener mListener;
	public boolean bound = false;
	private Thread thread;
	private EntryDbHandler entryDbConnection;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;
	private WebServiceConnection webServiceConnection;


	private Queue<FileSystemEntry> uploadQueue;
	private FileSystemEntry actualUploadItem;

	int count = 0;

	@Override
	public boolean onUnbind(Intent intent) {
		bound = false;
		return true; // ensures onRebind is called
	}

	@Override
	public void onRebind(Intent intent) {
		bound = true;
	}

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalUploadBinder extends Binder {


		public void setListener(BoundUploadServiceListener listener) {
			mListener = listener;
		}
		UploadService getService() {
			// Return this instance of LocalService so clients can call public methods
			return UploadService.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		bound = true;
		return mBinder;
	}

	public interface BoundUploadServiceListener {
		public void onUploadStarted(FileSystemEntry entry);
		public Boolean onUploadError(FileSystemEntry entry, String message);
	}


	@Override
	public void onCreate() {

		webServiceConnection = new WebServiceConnection(getApplicationContext());
		uploadQueue = new LinkedList<FileSystemEntry>();

		entryDbConnection = new EntryDbHandler(getApplicationContext());
		showNotification();

		thread = new Thread(new Runnable() {


			@Override
			public void run() {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				while(true){
					actualUploadItem = uploadQueue.poll();
					if(actualUploadItem == null)
						break;

					mBuilder.setContentTitle(actualUploadItem.getName());

					startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());

					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {

						webServiceConnection.uploadFile(actualUploadItem, UploadService.this);
					}

				}
				UploadService.this.stopSelf();
				showEndNotification();

			}

		});
		thread.start();

		super.onCreate();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Integer id;
		Uri uri;
		
		id = intent.getExtras().getInt(ItemDetailFragment.ARG_ITEM_ID);
		uri  = intent.getData();

		FileSystemEntry entry = entryDbConnection.getFileSystemEntry(id);
		entry.setUri(uri);

		if(entry != null)
			uploadQueue.add(entry);

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		running = false;
		super.onDestroy();
	}
	private void showNotification(){

		mNotificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getText(R.string.upload_service_notification_title))
		.setContentText(getText(R.string.upload_service_notification_message))
		.setTicker(getText(R.string.upload_service_notification_ticker_text))
		.setProgress(100, 0, true)
		.setSmallIcon(R.drawable.av_upload);


		mNotification = mBuilder.build();

//		Intent notificationIntent = new Intent(this, ItemDetailActivity.class);
//		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


		startForeground(ONGOING_NOTIFICATION_ID, mNotification);
	}

	private void showEndNotification() {
		// TODO Notification when Download is finished

	}

	@Override
	public void onUploadStart() {
		if(bound && mListener != null)
			mListener.onUploadStarted(actualUploadItem);
		
	}

	@Override
	public void onUploadError(String message) {
		if(bound && mListener != null)
			mListener.onUploadError(actualUploadItem, message);
		
	}
}
