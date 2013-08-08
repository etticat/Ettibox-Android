package net.etticat.dokabox;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ClipData.Item;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.dbmodels.EntryDbHandler;

public class DownloadService  extends Service{

	public static final int MSG_SAY_HELLO = 234;
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();
	// Random number generator
	Boolean running = true;
	private int ONGOING_NOTIFICATION_ID = 1231182;
	BoundServiceListener mListener;
	public boolean bound = false;
	private Thread thread;
	private FileSystemEntry mEntry;
	private EntryDbHandler entryDbConnection;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;


	private final int TIMEOUT_CONNECTION = 5000;//5sec
	private final int TIMEOUT_SOCKET = 30000;//30sec
	
	private Queue<FileSystemEntry> downloadQueue;

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
	public class LocalBinder extends Binder {


		public void setListener(BoundServiceListener listener) {
			mListener = listener;
		}
		DownloadService getService() {
			// Return this instance of LocalService so clients can call public methods
			return DownloadService.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		bound = true;
		return mBinder;
	}

	public interface BoundServiceListener {

		public void refreshProgress(Integer value);
	}


	@Override
	public void onCreate() {

		downloadQueue = new LinkedList<FileSystemEntry>();

		entryDbConnection = new EntryDbHandler(getApplicationContext());
		//mEntry = entryDbConnection();
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
					FileSystemEntry entry = downloadQueue.poll();
					if(entry == null)
						break;
					
					Download("", "");

				}

			}
		});
		thread.start();

		super.onCreate();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Context context = getApplicationContext();
		CharSequence text = "Downloads pending: " +  downloadQueue.size();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();

		Integer id;
		try {
			id = Integer.valueOf(intent
					.getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
		} catch (NumberFormatException e) {
			return super.onStartCommand(intent, flags, startId);
		}

		FileSystemEntry entry = entryDbConnection.getFileSystemEntry(id);
		if(entry != null)
			downloadQueue.add(entryDbConnection.getFileSystemEntry(id));

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
		mBuilder.setContentTitle(getText(R.string.download_service_notification_title))
		.setContentText(getText(R.string.download_service_notification_message))
		.setTicker(getText(R.string.download_service_notification_ticker_text))
		.setProgress(100, 0, true)
		.setSmallIcon(R.drawable.ic_launcher);


		mNotification = mBuilder.build();

		Intent notificationIntent = new Intent(this, ItemDetailActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


		startForeground(ONGOING_NOTIFICATION_ID, mNotification);
	}

	private void Download(String downloadUrl, String filename){


		URL url;
		try {
			url = new URL(downloadUrl);


			//Open a connection to that URL.
			URLConnection ucon = url.openConnection();
	
			//this timeout affects how long it takes for the app to realize there's a connection problem
			ucon.setReadTimeout(TIMEOUT_CONNECTION);
			ucon.setConnectTimeout(TIMEOUT_SOCKET);
	
	
			//Define InputStreams to read from the URLConnection.
			// uses 3KB download buffer
			InputStream is = ucon.getInputStream();
			BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
			FileOutputStream outStream = new FileOutputStream(filename);
			byte[] buff = new byte[5 * 1024];
	
			//Read bytes (and store them) until there is nothing more to read(-1)
			int totalLength = 123;
			
			int restLength;
			while ((restLength = inStream.read(buff)) != -1)
			{
				outStream.write(buff,0,restLength);
				
				Integer percent = 100-(100*restLength/totalLength);
				mBuilder.setProgress(100, percent, false);
				startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());
				if(bound)
					mListener.refreshProgress(percent);
				
				
				
			}
	
			//clean up
			outStream.flush();
			outStream.close();
			inStream.close();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
