package net.etticat.dokabox.dbmodels;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.etticat.dokabox.dto.FileSystemEntry;

public class EntryDbHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 6;

	// Database Name
	private static final String DATABASE_NAME = "dokabox";

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

	private static final String TAG = "EntryDbHandler";

	public EntryDbHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
				+ KEY_SIZE + " INTEGER" + ")";
		Log.d(TAG, CREATE_CONTACTS_TABLE);
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
	public long replaceEntry(FileSystemEntry entry) {

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

		// Inserting Row
		return db.replace(TABLE_ENTRIES, null, values);
	}
	public FileSystemEntry getFileSystemEntry(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_ENTRIES, new String[]{KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_ALTERNATION_DATE, KEY_PARENT_ID, KEY_ACTIVE, 
				KEY_SYNC_SUBSCRIBED, KEY_SIZE}, KEY_ID + "=?",
				new String[]{String.valueOf(id)}, null, null, null, null);
		if (cursor != null){
			if(cursor.moveToFirst()){
				FileSystemEntry entry = new FileSystemEntry();
				entry.setId(cursor.getInt(0));
				entry.setName(cursor.getString(1));
				entry.setIntegerType(cursor.getInt(2));
				entry.setAlternationDate(new Date(cursor.getInt(3)));
				entry.setParentId(cursor.getInt(4));
				entry.setActive(cursor.getInt(5) == 1);
				entry.setSyncSubscribed(cursor.getInt(6) == 1);
				entry.setSize(cursor.getInt(7));

				// return contact
						return entry;
			}
		}
		return  null;

	}
	/*
	public int updateContact(Contact contact) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_NAME, contact.getName());
		values.put(KEY_PH_NO, JsonConverter.toJson(contact.getNumbers()));
		values.put(KEY_MODIFIED, System.currentTimeMillis());
		values.put(KEY_DELETED, contact.getDeleted());

		// updating row
		return db.update(TABLE_ENTRIES, values, KEY_ID + " = ?",
				new String[] { String.valueOf(contact.getCid()) });
	}
	public long replaceContact(Contact contact) {
		Contact oldContact = getFileSystemEntry(contact.getCid());

		if(oldContact == null)
			return addContact(contact);
		else if(contact != null &&
				contact.getName() != null &&
				(!contact.getName().equals(oldContact.getName()) ||
						contact.getNumbers() == null
						|| !contact.getNumbers().equals(oldContact.getNumbers()))
						|| contact.getDeleted() != oldContact.getDeleted()){

			return updateContact(contact);
		}
		return 0;
	}
	 */
	public List<FileSystemEntry> getEntries(Integer parentId) {
		List<FileSystemEntry> result = new ArrayList<FileSystemEntry>();

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_ENTRIES, new String[] { KEY_ID,
				KEY_NAME, KEY_TYPE, KEY_ALTERNATION_DATE, KEY_PARENT_ID, KEY_ACTIVE, 
				KEY_SYNC_SUBSCRIBED, KEY_SIZE}, KEY_PARENT_ID + "=?",
						new String[] { String.valueOf(parentId) }, null, null, KEY_TYPE + " ASC");
		
		if (cursor != null){
			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				do {
					FileSystemEntry entry = new FileSystemEntry();
					entry.setId(cursor.getInt(0));
					entry.setName(cursor.getString(1));
					entry.setIntegerType(cursor.getInt(2));
					entry.setAlternationDate(new Date(cursor.getInt(3)));
					entry.setParentId(cursor.getInt(4));
					entry.setActive(cursor.getInt(5) == 1);
					entry.setSyncSubscribed(cursor.getInt(6) == 1);
					entry.setSize(cursor.getInt(7));


					// Adding entry to list
					result.add(entry);
				} while (cursor.moveToNext());
			}
		}
		return result;
	}


	public void deleteAll()
	{
		SQLiteDatabase db= this.getWritableDatabase();
		db.delete(TABLE_ENTRIES, null, null);

	}
//
//	public void deleteContact(Contact contact) {
//		contact.setDeleted(true);
//		updateContact(contact);
//	}

	public void replaceEntries(List<FileSystemEntry> entries, Integer id) {
		
		SQLiteDatabase db= this.getWritableDatabase();
		db.delete(TABLE_ENTRIES, KEY_PARENT_ID + "=?", new String[] { ""+id});
		
		for (FileSystemEntry entry : entries) {
			replaceEntry(entry);
		}
		
	}

	public File getPath(FileSystemEntry entry) {
		
		String path = "";
		
		while(entry.getParentId() != 0){
			entry = getFileSystemEntry(entry.getParentId());
			path = entry.getName() + "/" + path;
		}
		path = "/dokabox/" + entry.getId() + "/" + path;

		return new File(
				Environment.getExternalStorageDirectory(), path);
	}

	public FileSystemEntry getRootEntry(FileSystemEntry entry) {
		while(entry.getParentId() != 0){
			entry = getFileSystemEntry(entry.getParentId());
		}
		return entry;
	}

//
//	public List<Integer> getAllContactIds() {
//		List<Integer> result = new ArrayList<Integer>();
//
//		SQLiteDatabase db = this.getReadableDatabase();
//
//		Cursor cursor = db.query(TABLE_ENTRIES, new String[] { KEY_ID, KEY_DELETED }, null, null, null, null, null);
//		if (cursor != null){
//			if (cursor.moveToFirst()) {
//				do {
//					if(cursor.getInt(1) == 0)
//						result.add(cursor.getInt(0));
//				} while (cursor.moveToNext());
//			}
//		}
//		return result;
//	}
}
