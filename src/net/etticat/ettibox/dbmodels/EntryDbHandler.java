package net.etticat.ettibox.dbmodels;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.etticat.ettibox.dto.FileSystemEntry;
import net.etticat.ettibox.models.ContextProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class EntryDbHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 7;

	// Database Name
	private static final String DATABASE_NAME = "ettibox";

	// Contacts table name
	private static final String TABLE_ENTRIES = "fileSystemEntries";

	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "name";
	private static final String KEY_TYPE = "type";
	private static final String KEY_ALTERNATION_DATE = "alternationDate";
	private static final String KEY_PARENT_ID = "parentId";
	private static final String KEY_ACTIVE = "active";
	private static final String KEY_SYNC_SUBSCRIBED = "syncSubscribed";
	private static final String KEY_SIZE = "size";
	private static final String KEY_DOWNLOAD_DATE = "downloadedDate";
	private static final String KEY_DOWNLOAD_ALTERNATION_DATE = "downloadedAlternationDate";

	private EntryDbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
    private static EntryDbHandler instance = null;

    public static EntryDbHandler getInstance() {
            if (instance == null) {
                  if (instance == null) {
                        instance = new EntryDbHandler(ContextProvider.getContext());
                  }
            }
            return instance;
    }

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_ENTRIES + "("
				+ KEY_ID + " INTEGER PRIMARY KEY,"
				+ KEY_NAME + " TEXT,"
				+ KEY_TYPE + " INTEGER,"
				+ KEY_ALTERNATION_DATE + " INTEGER,"
				+ KEY_PARENT_ID + " INTEGER,"
				+ KEY_ACTIVE + " INTEGER,"
				+ KEY_SYNC_SUBSCRIBED + " INTEGER,"
				+ KEY_SIZE + " INTEGER, " 
				+ KEY_DOWNLOAD_DATE + " INTEGER, " 
				+ KEY_DOWNLOAD_ALTERNATION_DATE + " INTEGER )";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);

		// Create tables again
		onCreate(db);
	}
	// Adding new contact

	public FileSystemEntry getFileSystemEntry(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_ENTRIES, new String[]{KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_ALTERNATION_DATE, KEY_PARENT_ID, KEY_ACTIVE, 
				KEY_SYNC_SUBSCRIBED, KEY_SIZE, KEY_DOWNLOAD_DATE, KEY_DOWNLOAD_ALTERNATION_DATE}, KEY_ID + "=?",
				new String[]{String.valueOf(id)}, null, null, null, null);
		if (cursor != null){
			if(cursor.moveToFirst()){
				FileSystemEntry entry = new FileSystemEntry();
				entry.setId(cursor.getInt(0));
				entry.setName(cursor.getString(1));
				entry.setIntegerType(cursor.getInt(2));
				entry.setAlternationDate(new Date(cursor.getLong(3)));
				entry.setParentId(cursor.getInt(4));
				entry.setActive(cursor.getInt(5) == 1);
				entry.setSyncSubscribed(cursor.getInt(6) == 1);
				entry.setSize(cursor.getInt(7));
				entry.setDownloadedDate(new Date(cursor.getLong(8)));
				entry.setDownloadedAlternationDate(new Date(cursor.getLong(9)));

				return entry;
			}
		}
		return  null;

	}

	public List<FileSystemEntry> getEntries(Integer parentId) {
		List<FileSystemEntry> result = new ArrayList<FileSystemEntry>();

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_ENTRIES, new String[] { KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_ALTERNATION_DATE, KEY_PARENT_ID, KEY_ACTIVE, 
				KEY_SYNC_SUBSCRIBED, KEY_SIZE, KEY_DOWNLOAD_DATE, KEY_DOWNLOAD_ALTERNATION_DATE}, KEY_PARENT_ID + "=?",
				new String[] { String.valueOf(parentId) }, null, null, KEY_TYPE + " ASC");

		if (cursor != null){
			if (cursor.moveToFirst()) {
				do {
					FileSystemEntry entry = new FileSystemEntry();
					entry.setId(cursor.getInt(0));
					entry.setName(cursor.getString(1));
					entry.setIntegerType(cursor.getInt(2));
					entry.setAlternationDate(new Date(cursor.getLong(3)));
					entry.setParentId(cursor.getInt(4));
					entry.setActive(cursor.getInt(5) == 1);
					entry.setSyncSubscribed(cursor.getInt(6) == 1);
					entry.setSize(cursor.getInt(7));
					entry.setDownloadedDate(new Date(cursor.getLong(8)));
					entry.setDownloadedAlternationDate(new Date(cursor.getLong(9)));


					// Adding entry to list
					result.add(entry);
				} while (cursor.moveToNext());
			}
		}
		return result;
	}
	public long replaceEnastry(FileSystemEntry entry) {

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, entry.getId()); 
		values.put(KEY_NAME, entry.getName()); 
		values.put(KEY_TYPE, entry.getIntegerType());
		values.put(KEY_ALTERNATION_DATE, entry.getAlternationDate().getTime()); 
		values.put(KEY_PARENT_ID, entry.getParentId()); 
		values.put(KEY_ACTIVE, entry.getActive()); 
		values.put(KEY_SYNC_SUBSCRIBED, entry.getSyncSubscribed()); 
		values.put(KEY_SIZE, entry.getSize()); 
		if(entry.getDownloadedAlternationDate() != null)
			values.put(KEY_DOWNLOAD_ALTERNATION_DATE, entry.getDownloadedAlternationDate().getTime()); 
		if(entry.getDownloadedDate() != null)
			values.put(KEY_DOWNLOAD_DATE, entry.getDownloadedDate().getTime()); 
			
		

		// Inserting Row
		return db.replace(TABLE_ENTRIES, null, values);
	}
    public int updateEntry(FileSystemEntry entry) {
        SQLiteDatabase db = this.getWritableDatabase();
     
        ContentValues values = new ContentValues();
		values.put(KEY_ID, entry.getId()); 
		values.put(KEY_NAME, entry.getName()); 
		values.put(KEY_TYPE, entry.getIntegerType());
		values.put(KEY_ALTERNATION_DATE, entry.getAlternationDate().getTime()); 
		values.put(KEY_PARENT_ID, entry.getParentId()); 
		values.put(KEY_ACTIVE, entry.getActive()); 
		values.put(KEY_SYNC_SUBSCRIBED, entry.getSyncSubscribed()); 
		values.put(KEY_SIZE, entry.getSize()); 
		if(entry.getDownloadedAlternationDate() != null)
			values.put(KEY_DOWNLOAD_ALTERNATION_DATE, entry.getDownloadedAlternationDate().getTime()); 
		if(entry.getDownloadedDate() != null)
			values.put(KEY_DOWNLOAD_DATE, entry.getDownloadedDate().getTime()); 
     
        // updating row
        return db.update(TABLE_ENTRIES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(entry.getId()) });
    }
    public long replaceEntry(FileSystemEntry entry) {
    	FileSystemEntry oldContact = getFileSystemEntry(entry.getId());

        if(oldContact == null)
            return addEntry(entry);
        
        return updateEntry(entry);
    }
	public long addEntry(FileSystemEntry entry) {

	    SQLiteDatabase db = this.getWritableDatabase();
	    ContentValues values = new ContentValues();
		values.put(KEY_ID, entry.getId()); 
		values.put(KEY_NAME, entry.getName()); 
		values.put(KEY_TYPE, entry.getIntegerType());
		values.put(KEY_ALTERNATION_DATE, entry.getAlternationDate().getTime()); 
		values.put(KEY_PARENT_ID, entry.getParentId()); 
		values.put(KEY_ACTIVE, entry.getActive()); 
		values.put(KEY_SYNC_SUBSCRIBED, entry.getSyncSubscribed()); 
		values.put(KEY_SIZE, entry.getSize()); 
		if(entry.getDownloadedAlternationDate() != null)
			values.put(KEY_DOWNLOAD_ALTERNATION_DATE, entry.getDownloadedAlternationDate().getTime()); 
		if(entry.getDownloadedDate() != null)
			values.put(KEY_DOWNLOAD_DATE, entry.getDownloadedDate().getTime()); 
	 
	    // Inserting Row
	    return db.insert(TABLE_ENTRIES, null, values);
	}
	public void deleteEntry(FileSystemEntry entry)
	{
		SQLiteDatabase db= this.getWritableDatabase();
		db.delete(TABLE_ENTRIES, KEY_ID + "=?", new String[] { ""+entry.getId()});
	}
	public void deleteAll()
	{
		SQLiteDatabase db= this.getWritableDatabase();
		db.delete(TABLE_ENTRIES, null, null);
		
	}

	public void replaceEntries(List<FileSystemEntry> entries, Integer id) {

		List<FileSystemEntry> oldEntries = getEntries(id);

		for (FileSystemEntry entry : entries) {
			oldEntries.remove(entry);
			replaceEntry(entry);
		}
		
		for(FileSystemEntry oldEntry : oldEntries){
			deleteEntry(oldEntry);
		}

	}

	public File getPath(FileSystemEntry entry) {

		String path = "";

		while(entry.getParentId() != 0){
			entry = getFileSystemEntry(entry.getParentId());
			path = entry.getName() + "/" + path;
		}
		path = "/ettibox/" + entry.getId() + "/" + path;

		return new File(
				Environment.getExternalStorageDirectory(), path);
	}

	public FileSystemEntry getRootEntry(FileSystemEntry entry) {
		while(entry.getParentId() != 0){
			entry = getFileSystemEntry(entry.getParentId());
		}
		return entry;
	}

	public List<FileSystemEntry> getEntriesBySearchQuery(String searchQuery) {
		List<FileSystemEntry> result = new ArrayList<FileSystemEntry>();

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_ENTRIES, new String[] { KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_ALTERNATION_DATE, KEY_PARENT_ID, KEY_ACTIVE, 
				KEY_SYNC_SUBSCRIBED, KEY_SIZE, KEY_DOWNLOAD_DATE, KEY_DOWNLOAD_ALTERNATION_DATE}, KEY_NAME + " LIKE ?",
				new String[] { "%" + searchQuery + "%" }, null, null, KEY_TYPE + " ASC");

		if (cursor != null){
			if (cursor.moveToFirst()) {
				do {
					FileSystemEntry entry = new FileSystemEntry();
					entry.setId(cursor.getInt(0));
					entry.setName(cursor.getString(1));
					entry.setIntegerType(cursor.getInt(2));
					entry.setAlternationDate(new Date(cursor.getLong(3)));
					entry.setParentId(cursor.getInt(4));
					entry.setActive(cursor.getInt(5) == 1);
					entry.setSyncSubscribed(cursor.getInt(6) == 1);
					entry.setSize(cursor.getInt(7));
					entry.setDownloadedDate(new Date(cursor.getLong(8)));
					entry.setDownloadedAlternationDate(new Date(cursor.getLong(9)));


					// Adding entry to list
					result.add(entry);
				} while (cursor.moveToNext());
			}
		}
		return result;
	}

}
