package net.etticat.ettibox;

import java.util.LinkedList;
import java.util.Queue;

import net.etticat.ettibox.dbmodels.EntryDbHandler;
import net.etticat.ettibox.dto.FileSystemEntry;
import net.etticat.ettibox.models.WebServiceConnection;
import net.etticat.ettibox.models.WebServiceConnection.OnUploadProgressHandler;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

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
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;
	private WebServiceConnection webServiceConnection;
	private Handler handler;
	private Boolean isThreadRunning = false;


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
		public void onUploadFinished(FileSystemEntry entry, Boolean success);
	}


	@Override
	public void onCreate() {

		webServiceConnection = new WebServiceConnection();
		uploadQueue = new LinkedList<FileSystemEntry>();

		entryDbConnection = EntryDbHandler.getInstance();
		showNotification();

		handler = new Handler();

		thread = new Thread(new Runnable() {


			@Override
			public void run() {

				while(true){
					actualUploadItem = uploadQueue.poll();
					if(actualUploadItem == null)
						break;

					mBuilder.setContentTitle(actualUploadItem.getName());

					startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());

					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {

						webServiceConnection.uploadFile(actualUploadItem, UploadService.this);
						
						if(mListener != null)
							mListener.onUploadFinished(actualUploadItem, true);
					}

				}
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						stopSelf();
					}
				});
				showEndNotification();

			}

		});
		super.onCreate();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Integer id;
		Uri uri;
		
		id = intent.getExtras().getInt(ItemDetailFragment.ARG_ITEM_ID);
		uri  = intent.getData();

		FileSystemEntry entry = entryDbConnection.getFileSystemEntry(id);
		
		if(entry != null){
			entry.setUri(uri);
			uploadQueue.add(entry);
		}

		if(!isThreadRunning){
			thread.start();
			isThreadRunning = true;
		}
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		running = false;
		super.onDestroy();
	}
	private void showNotification(){
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
		// TODO Notification when Upload is finished

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

	@Override
	public void progressChanged(FileSystemEntry entry, Integer percent) {

		mBuilder.setProgress(100, percent, false);

		startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());	
	}
}
