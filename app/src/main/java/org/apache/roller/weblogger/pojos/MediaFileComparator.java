/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.pojos;

import java.util.Comparator;

/**
 * Compares media files based on the type passed.
 * 
 * Implements java.util.Comparator interface so this can be used
 * for comparing and sorting media files.
 *  
 */
public class MediaFileComparator implements Comparator<MediaFile> {
	
	/**
	 * Comparator types that define all possible attributes for comparing media files. 
	 *
	 */
	public enum MediaFileComparatorType {NAME, TYPE, DATE_UPLOADED};

	// Comparator type associated with this instance of media file comparator
	MediaFileComparatorType type;
	
	public MediaFileComparator(MediaFileComparatorType type) {
		this.type = type;
	}

	/**
	 * Compares media files based on the attribute associated with this comparator
	 * 
	 */
	public int compare(MediaFile file1, MediaFile file2) {
		switch (this.type) {
		    case NAME: return file1.getName().compareTo(file2.getName());
		    case TYPE: return file1.getContentType().compareTo(file2.getContentType());
		    // Do last uploaded first comparison by default for date field
		    case DATE_UPLOADED: return file2.getDateUploaded().compareTo(file1.getDateUploaded());  
		    default: return 0;
		}
	}

}
