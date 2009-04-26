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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;

/**
 * Bean for holding media file search criteria.
 */
public class MediaFileSearchBean {
	public static int PAGE_SIZE = 10;

	String name;
	String type;
	int sizeFilterType;
	long size;
	int sizeUnit;
	String tags;
	int pageNum = 0;
	int sortOption;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getSizeFilterType() {
		return sizeFilterType;
	}
	public void setSizeFilterType(int sizeFilterType) {
		this.sizeFilterType = sizeFilterType;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int getSizeUnit() {
		return sizeUnit;
	}
	public void setSizeUnit(int sizeUnit) {
		this.sizeUnit = sizeUnit;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	
    public int getPageNum() {
		return pageNum;
	}
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getSortOption() {
		return sortOption;
	}
	public void setSortOption(int sortOption) {
		this.sortOption = sortOption;
	}

	public void copyTo(MediaFileFilter dataHolder) {
    	dataHolder.setName(this.name);
    	
    	if (!StringUtils.isEmpty(this.type)) {
        	MediaFileType filterType = null;
    		if ("Audio".equals(this.type)) {
    			filterType = MediaFileType.AUDIO;
    		}
    		else if ("Video".equals(this.type)) {
    			filterType = MediaFileType.VIDEO;
    		}
    		else if ("Image".equals(this.type)) {
    			filterType = MediaFileType.IMAGE;
    		}
    		
    		dataHolder.setType(filterType);
    	}
    	
    	if (this.size > 0) {
        	SizeFilterType type;
        	switch(this.sizeFilterType) {
        	case 0: type = SizeFilterType.GT;break;
        	case 1: type = SizeFilterType.GTE;break;
        	case 2: type = SizeFilterType.EQ;break;
        	case 3: type = SizeFilterType.LTE;break;
        	case 4: type = SizeFilterType.LT;break;
        	default: type = null;
        	}
        	dataHolder.setSizeFilterType(type);
        	
        	long filterSize;
        	switch (this.sizeUnit) {
        	case 1: filterSize = this.size * 1024;break;
        	case 2: filterSize = this.size * 1024 * 1024;break;
        	default: filterSize = this.size;
        	}
        	dataHolder.setSize(filterSize);
    	}
    	
    	if (!StringUtils.isEmpty(this.tags)) {
        	List<String> tagsSet = new ArrayList<String>();  
        	for (String tag: this.tags.split(" ")) {
        		tagsSet.add(tag);
        	}
        	dataHolder.setTags(tagsSet);
    	}
    	
    	dataHolder.setStartIndex(pageNum * PAGE_SIZE);
    	/**
    	 * Set length to fetch to one more than what is required.
    	 * This would help us determine whether there are more pages
    	 */
    	dataHolder.setLength(PAGE_SIZE + 1);
    	
    	MediaFileOrder order; 
    	switch(this.sortOption) {
    	case 0: order = MediaFileOrder.NAME;break;
    	case 1: order = MediaFileOrder.DATE_UPLOADED;break;
    	case 2: order = MediaFileOrder.TYPE;break;
    	default: order = null;
    	}
    	dataHolder.setOrder(order);
    }

}
