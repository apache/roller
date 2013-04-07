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
 * Compares media file directories based on name or path.
 * 
 * Implements java.util.Comparator interface so this can be used
 * for comparing and sorting directories.
 *  
 */
public class MediaFileDirectoryComparator implements Comparator<MediaFileDirectory> {
	
	/**
	 * Comparator types that define all possible attributes for comparing media file directories. 
	 *
	 */
	public enum DirectoryComparatorType {NAME, PATH};
	
	// Comparator type associated with this instance of media directory comparator
	DirectoryComparatorType type;
	
	public MediaFileDirectoryComparator(DirectoryComparatorType type) {
		this.type = type;
	}

	/**
	 * Compares directories based on the attribute associated with this comparator
	 * 
	 */
	public int compare(MediaFileDirectory dir1, MediaFileDirectory dir2) {
		switch (this.type) {
		    case NAME: return dir1.getName().compareTo(dir2.getName());
		    case PATH: return dir1.getPath().compareTo(dir2.getPath());
		    default: return 0;
		}
	}

}
