package org.apache.roller.weblogger.pojos;

import java.util.Comparator;

public class MediaFileDirectoryComparator implements Comparator<MediaFileDirectory> {
	
	public enum DirectoryComparatorType {NAME, PATH};
	DirectoryComparatorType type;
	
	public MediaFileDirectoryComparator(DirectoryComparatorType type) {
		this.type = type;
	}

	public int compare(MediaFileDirectory dir1, MediaFileDirectory dir2) {
		switch (this.type) {
		    case NAME: return dir1.getName().compareTo(dir2.getName());
		    case PATH: return dir1.getPath().compareTo(dir2.getPath());
		    default: return 0;
		}
	}

}
