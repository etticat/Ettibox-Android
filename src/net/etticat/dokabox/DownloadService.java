package net.etticat.dokabox;

import java.util.Date;
import java.util.Random;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.ClipData.Item;
import android.os.Binder;
import android.os.IBinder;
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

    	
    	entryDbConnection = new EntryDbHandler(getApplicationContext());
    	//mEntry = entryDbConnection();
    	showNotification();
    	
    	thread = new Thread(new Runnable() {
    		

    		@Override
    		public void run() {
    			for(int i = 0; i<100; i++){

    				
    				if(bound)
    					mListener.refreshProgress(i);
    				try {
    					Thread.sleep(600 );
    				} catch (InterruptedException e) {
    					e.printStackTrace();
    				}
    				
    			}
    			
    		}
    		
        });
    	thread.start();
    	
    	super.onCreate();
	}

	@Override
	public void onDestroy() {
		running = false;
		super.onDestroy();
	}
	private void showNotification(){
    	Notification notification = new Notification(R.drawable.ic_launcher, getText(R.string.download_service_notification_ticker_text),
    	        System.currentTimeMillis());
    	Intent notificationIntent = new Intent(this, ItemDetailActivity.class);
    	notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	
    	String title;
    	if(mEntry != null)
    		title = mEntry.getName();
    	else title = (String) getText(R.string.download_service_notification_title);
    	
    	notification.setLatestEventInfo(this, title,
    	        getText(R.string.download_service_notification_message), pendingIntent);
    	startForeground(ONGOING_NOTIFICATION_ID , notification);
	}

}
