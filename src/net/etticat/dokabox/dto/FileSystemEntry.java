package net.etticat.dokabox.dto;

import java.util.Date;

import android.net.Uri;

public class FileSystemEntry {

	public enum FileSystemEntryType {
		FOLDER, FILE
	}

	private int id;
	private String name;
	private FileSystemEntryType type;
	private Date alternationDate;
	private Date downloadedAlternationDate;
	private Date downloadedDate;
	public Date getDownloadedAlternationDate() {
		return downloadedAlternationDate;
	}
	public void setDownloadedAlternationDate(Date downloadedAlternationDate) {
		this.downloadedAlternationDate = downloadedAlternationDate;
	}
	public Date getDownloadedDate() {
		return downloadedDate;
	}
	public void setDownloadedDate(Date downloadedDate) {
		this.downloadedDate = downloadedDate;
	}
	private int parentId;
	private Boolean active;
	private Boolean syncSubscribed;
	private Uri uri;
	
	
	
	public Uri getUri() {
		return uri;
	}
	public void setUri(Uri uri) {
		this.uri = uri;
	}
	private long size;
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public FileSystemEntryType getType() {
		return type;
	}
	public void setType(FileSystemEntryType type) {
		this.type = type;
	}
	public Integer getIntegerType() {
		switch (type) {
		case FILE:
			return 1;
		default:
			return 0;
		}
	}
	public void setIntegerType(Integer type) {
		switch (type) {
		case 1:
			this.type = FileSystemEntryType.FILE;
			break;

		default:
			this.type = FileSystemEntryType.FOLDER;
			break;
		}
	}
	public Date getAlternationDate() {
		return alternationDate;
	}
	public void setAlternationDate(Date alternationDate) {
		this.alternationDate = alternationDate;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	public Boolean getSyncSubscribed() {
		return syncSubscribed;
	}
	public void setSyncSubscribed(Boolean syncSubscribed) {
		this.syncSubscribed = syncSubscribed;
	}
	@Override
	public boolean equals(Object o) {
		return 
			o instanceof FileSystemEntry 
			&& o != null 
			&& ((FileSystemEntry) o).getId() == this.getId();
	}
	
}
