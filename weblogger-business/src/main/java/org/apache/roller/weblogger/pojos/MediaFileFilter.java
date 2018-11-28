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

import java.util.List;

/**
 * Represents the search criteria for media files.
 *
 */
public class MediaFileFilter {
	
	/**
	 * Enumeration of the ways in which size can be used to filter media files. 
	 *
	 */
	public enum SizeFilterType {GT, GTE, EQ, LT, LTE};

	/**
	 * Enumeration of possible sort orders for media files. 
	 *
	 */
	public enum MediaFileOrder {NAME, DATE_UPLOADED, TYPE};

	// Search criteria - name
	String name;

	// Search criteria - media file type
	MediaFileType type;

	// Search criteria - media file size in bytes
	long size;

	// Search criteria - way in which media file size should be applied (greater than, less than etc)
	SizeFilterType sizeFilterType;
	
	// Search criteria - list of tags
	List<String> tags;
	
	// sort order for search results
	MediaFileOrder order;
	
	/**
	 * Indicates the starting index in the complete result set 
	 * from which results should be returned.  This is always applied
	 * along with the length attribute below.
	 * A value of -1 means that the complete result set should be
	 * returned. length will be ignored in this case.
	 */
	int startIndex = -1;
	
	/**
	 * Number of results to be returned starting from startIndex.
	 */
	int length;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MediaFileType getType() {
		return type;
	}

	public void setType(MediaFileType type) {
		this.type = type;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public SizeFilterType getSizeFilterType() {
		return sizeFilterType;
	}

	public void setSizeFilterType(SizeFilterType sizeFilterType) {
		this.sizeFilterType = sizeFilterType;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public MediaFileOrder getOrder() {
		return order;
	}

	public void setOrder(MediaFileOrder order) {
		this.order = order;
	}

}
