package net.etticat.ettibox.models;

import java.io.File;

import net.etticat.ettibox.dto.FileSystemEntry;

public class FileInfoChecker {

	public enum FileInfoStatus{
		CURRENT, OLD_VERSION, NOT_EXISTENT, DOWNLOADING
	}
	
	public FileInfoStatus getStatus(FileSystemEntry entry, File file){
		
		file = new File(file, entry.getName());
		
		if(!file.exists())
			return FileInfoStatus.NOT_EXISTENT;
		
		if(file.length() == entry.getSize()
			&& file.lastModified() == entry.getDownloadedDate().getTime()
			&& entry.getDownloadedAlternationDate().equals(entry.getAlternationDate()))
			return FileInfoStatus.CURRENT;
		
		return FileInfoStatus.OLD_VERSION;
		
		
		
	}
}
