package org.apache.roller.weblogger.pojos;

import java.util.Comparator;

public class MediaFileComparator implements Comparator<MediaFile> {
	
	public enum MediaFileComparatorType {NAME, TYPE, DATE_UPLOADED};
	MediaFileComparatorType type;
	
	public MediaFileComparator(MediaFileComparatorType type) {
		this.type = type;
	}

	public int compare(MediaFile file1, MediaFile file2) {
		switch (this.type) {
		    case NAME: return file1.getName().compareTo(file2.getName());
		    case TYPE: return file1.getContentType().compareTo(file2.getContentType());
		    // Do descending comparison by default for date field
		    case DATE_UPLOADED: return file2.getDateUploaded().compareTo(file1.getDateUploaded());  
		    default: return 0;
		}
	}

}
