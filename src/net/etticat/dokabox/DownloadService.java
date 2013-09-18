package net.etticat.dokabox;

import java.util.LinkedList;
import java.util.Queue;

import net.etticat.dokabox.dbmodels.EntryDbHandler;
import net.etticat.dokabox.dto.FileSystemEntry;
import net.etticat.dokabox.models.WebServiceConnection;
import net.etticat.dokabox.models.WebServiceConnection.OnFileTransferProgressHandler;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class DownloadService  extends Service implements OnFileTransferProgressHandler{

	

	private int ONGOING_NOTIFICATION_ID = 1231182;

	private final IBinder mBinder = new LocalBinder();
	private BoundDownloadServiceListener mListener;
	private boolean mBound = false;
	private Thread mThread;
	private EntryDbHandler mEntryDbConnection;
	private NotificationCompat.Builder mBuilder;
	private Notification mNotification;
	private WebServiceConnection mWebServiceConnection;


	private Queue<FileSystemEntry> mDownloadQueue;
	private FileSystemEntry mActualDownloadItem;


	@Override
	public boolean onUnbind(Intent intent) {
		mBound = false;

		// ensures onRebind is called
		return true; 
	}

	@Override
	public void onRebind(Intent intent) {
		mBound = true;
	}

	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {


		public void setListener(BoundDownloadServiceListener listener) {
			mListener = listener;
		}
		DownloadService getService() {
			// Return this instance of LocalService so clients can call public methods
			return DownloadService.this;
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		mBound = true;
		return mBinder;
	}

	public interface BoundDownloadServiceListener {

		public void refreshProgress(FileSystemEntry entry, Integer value);
		public void onDownloadFinished(FileSystemEntry entry);
		public void onDownloadAdded();
		public void onDownloadFailed(FileSystemEntry entry);

	}


	@Override
	public void onCreate() {

		mWebServiceConnection = new WebServiceConnection();
		mDownloadQueue = new LinkedList<FileSystemEntry>();
		mEntryDbConnection = EntryDbHandler.getInstance();
		showNotification();
		super.onCreate();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Integer id;
		try {
			id =  intent.getExtras().getInt(ItemDetailFragment.ARG_ITEM_ID);
		} catch (NumberFormatException e) {
			return super.onStartCommand(intent, flags, startId);
		}

		FileSystemEntry entry = mEntryDbConnection.getFileSystemEntry(id);
		if(entry != null)
			mDownloadQueue.add(mEntryDbConnection.getFileSystemEntry(id));

		if(mBound && mListener != null)
			mListener.onDownloadAdded();
		
		if(mThread == null)
			startDownload();

		return super.onStartCommand(intent, flags, startId);
	}
	private void startDownload(){

		mThread = new Thread(new Runnable() {


			@Override
			public void run() {

				while(true){

					mActualDownloadItem = mDownloadQueue.poll();

					if(mActualDownloadItem == null)
						break;

					mBuilder.setContentTitle(mActualDownloadItem.getName());

					startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());

					String state = Environment.getExternalStorageState();
					if (Environment.MEDIA_MOUNTED.equals(state)) {

						Boolean success = mWebServiceConnection.download(mActualDownloadItem, DownloadService.this, true);

						if(mListener != null){

							if(success)
								mListener.onDownloadFinished(mActualDownloadItem);
							else 
								mListener.onDownloadFailed(mActualDownloadItem);
						}
					}

				}
				DownloadService.this.stopSelf();
				showEndNotification();

			}

		});
		mThread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	private void showNotification(){

		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle(getText(R.string.download_service_notification_title))
		.setContentText(getText(R.string.download_service_notification_message))
		.setTicker(getText(R.string.download_service_notification_ticker_text))
		.setProgress(100, 0, true)
		.setSmallIcon(R.drawable.ic_launcher);

		mNotification = mBuilder.build();

		Intent notificationIntent = new Intent(this, ItemDetailActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		startForeground(ONGOING_NOTIFICATION_ID, mNotification);
	}

	@Override
	public void progressChanged(FileSystemEntry entry, Integer value) {

		mBuilder.setProgress(100, value, false);

		startForeground(ONGOING_NOTIFICATION_ID, mBuilder.build());
		
		if(mBound && mListener != null)
			mListener.refreshProgress(entry, value);

	}

	private void showEndNotification() {
		// TODO Notification when Download is finished

	}
	public Boolean isEntryInQueue(FileSystemEntry entry){
		return mDownloadQueue.contains(entry) || entry.equals(mActualDownloadItem);
	}
}
