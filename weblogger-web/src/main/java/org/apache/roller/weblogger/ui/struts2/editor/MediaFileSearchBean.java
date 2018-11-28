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

import java.util.ResourceBundle;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;

/**
 * Bean for holding media file search criteria.
 */
public class MediaFileSearchBean {
    private transient ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");

    public static int PAGE_SIZE = 10;

    // Media file name as search criteria
    String name;

    // Media file type as search criteria
    String type;

    // Type of size filter as search criteria
    String sizeFilterType;

    // Size of file as search criteria
    long size;

    // Size unit
    String sizeUnit;

    // Tags as search criteria
    String tags;

    // Page number of results
    int pageNum = 0;
    
    // Sort option for search results
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

    public String getTypeLabel() {
        return this.bundle.getString(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSizeFilterType() {
        return sizeFilterType;
    }

    public void setSizeFilterType(String sizeFilterType) {
        this.sizeFilterType = sizeFilterType;
    }

    public String getSizeFilterTypeLabel() {
        return this.bundle.getString(sizeFilterType);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSizeUnit() {
        return sizeUnit;
    }

    public String getSizeUnitLabel() {
        return this.bundle.getString(sizeUnit);
    }

    public void setSizeUnit(String sizeUnit) {
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

    /**
     * Copies data from this bean to media file filter object.
     *
     */
    public void copyTo(MediaFileFilter dataHolder) {
        dataHolder.setName(this.name);

        if (!StringUtils.isEmpty(this.type)) {
            MediaFileType filterType = null;
            if ("mediaFileView.audio".equals(this.type)) {
                filterType = MediaFileType.AUDIO;
            } else if ("mediaFileView.video".equals(this.type)) {
                filterType = MediaFileType.VIDEO;
            } else if ("mediaFileView.image".equals(this.type)) {
                filterType = MediaFileType.IMAGE;
            } else if ("mediaFileView.others".equals(this.type)) {
                filterType = MediaFileType.OTHERS;
            } 

            dataHolder.setType(filterType);
        }

        if (this.size > 0) {
            SizeFilterType sftype = SizeFilterType.EQ;
            if ("mediaFileView.gt".equals(this.sizeFilterType)) {
                sftype = SizeFilterType.GT;
            } else if ("mediaFileView.ge".equals(this.sizeFilterType)) {
                sftype = SizeFilterType.GTE;
            } else if ("mediaFileView.eq".equals(this.sizeFilterType)) {
                sftype = SizeFilterType.EQ;
            } else if ("mediaFileView.le".equals(this.sizeFilterType)) {
                sftype = SizeFilterType.LTE;
            } else if ("mediaFileView.lt".equals(this.sizeFilterType)) {
                sftype = SizeFilterType.LT;
            }
            dataHolder.setSizeFilterType(sftype);

            long filterSize = this.size ;
            if ("mediaFileView.kb".equals(this.sizeUnit)) {
                filterSize = this.size * 1024;
            } else if ("mediaFileView.mb".equals(this.sizeUnit)) {
                    filterSize = this.size * 1024 * 1024;
            }
            dataHolder.setSize(filterSize);
        }

        if (!StringUtils.isEmpty(this.tags)) {
            List<String> tagsSet = new ArrayList<String>();
            for (String tag : this.tags.split(" ")) {
                tagsSet.add(tag);
            }
            dataHolder.setTags(tagsSet);
        }

        dataHolder.setStartIndex(pageNum * PAGE_SIZE);

        // set length to fetch to one more than what is required.
        // this would help us determine whether there are more pages
        dataHolder.setLength(PAGE_SIZE + 1);

        MediaFileOrder order;
        switch (this.sortOption) {
            case 0:
                order = MediaFileOrder.NAME;
                break;
            case 1:
                order = MediaFileOrder.DATE_UPLOADED;
                break;
            case 2:
                order = MediaFileOrder.TYPE;
                break;
            default:
                order = null;
        }
        dataHolder.setOrder(order);
    }
}
