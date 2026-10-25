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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
import org.apache.roller.weblogger.pojos.MediaFileFilter.SizeFilterType;
import org.apache.roller.weblogger.pojos.MediaFileType;

/**
 * What the media library shows: search criteria, scope, sort and page. Folder
 * browsing is the same query with no criteria, so one bean serves both.
 */
public class MediaFileSearchBean {
    private transient ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");

    public static final int PAGE_SIZE = 48;

    public static final String SORT_NEWEST = "newest";
    public static final String SORT_OLDEST = "oldest";
    public static final String SORT_NAME = "name";
    public static final String SORT_TYPE = "type";

    public static final String SCOPE_FOLDER = "folder";
    public static final String SCOPE_ALL = "all";

    private static final String ANY_TYPE = "mediaFileView.any";

    // Media file name as search criteria; matches names that start with it
    private String name;

    // Media file type as search criteria
    private String type;

    // Type of size filter as search criteria
    private String sizeFilterType;

    // Size of file as search criteria
    private long size;

    // Size unit
    private String sizeUnit;

    // Tags as search criteria
    private String tags;

    // Page number of results, from 0
    private int page = 0;

    // Sort order, one of the SORT_ values
    private String sortBy = SORT_NEWEST;

    // Search the current folder or all folders, one of the SCOPE_ values
    private String scope = SCOPE_FOLDER;

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

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(0, page);
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = Arrays.asList(SORT_NEWEST, SORT_OLDEST, SORT_NAME, SORT_TYPE).contains(sortBy)
                ? sortBy : SORT_NEWEST;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = SCOPE_ALL.equals(scope) ? SCOPE_ALL : SCOPE_FOLDER;
    }

    public boolean isAllFolders() {
        return SCOPE_ALL.equals(scope);
    }

    public boolean isTypeSet() {
        return StringUtils.isNotEmpty(type) && !ANY_TYPE.equals(type);
    }

    /** True when any search criterion is set; otherwise the library browses a folder. */
    public boolean isSearch() {
        return StringUtils.isNotBlank(name) || StringUtils.isNotBlank(tags)
                || isTypeSet() || size > 0;
    }

    /** True when a criterion behind the Filters button is set, so the filters start open. */
    public boolean isFiltersSet() {
        return StringUtils.isNotBlank(tags) || size > 0;
    }

    /**
     * The library state as request parameters, without the page: criteria
     * that are set, sort and scope. Values are not encoded.
     */
    public Map<String, String> toParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(name)) {
            params.put("bean.name", name.trim());
        }
        if (isTypeSet()) {
            params.put("bean.type", type);
        }
        if (size > 0) {
            params.put("bean.sizeFilterType", StringUtils.defaultString(sizeFilterType));
            params.put("bean.size", Long.toString(size));
            params.put("bean.sizeUnit", StringUtils.defaultString(sizeUnit));
        }
        if (StringUtils.isNotBlank(tags)) {
            params.put("bean.tags", tags.trim());
        }
        if (isSearch() && isAllFolders()) {
            params.put("bean.scope", SCOPE_ALL);
        }
        if (!SORT_NEWEST.equals(sortBy)) {
            params.put("bean.sortBy", sortBy);
        }
        return params;
    }

    /**
     * Copies data from this bean to media file filter object. The folder is
     * set by the caller, since it depends on the scope.
     */
    public void copyTo(MediaFileFilter dataHolder) {
        dataHolder.setName(StringUtils.trimToNull(this.name));

        if (isTypeSet()) {
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

            long filterSize = this.size;
            if ("mediaFileView.kb".equals(this.sizeUnit)) {
                filterSize = this.size * RollerConstants.ONE_KB_IN_BYTES;
            } else if ("mediaFileView.mb".equals(this.sizeUnit)) {
                filterSize = this.size * RollerConstants.ONE_MB_IN_BYTES;
            }
            dataHolder.setSize(filterSize);
        }

        if (StringUtils.isNotBlank(this.tags)) {
            dataHolder.setTags(Arrays.asList(StringUtils.split(this.tags.trim())));
        }

        dataHolder.setStartIndex(page * PAGE_SIZE);
        dataHolder.setLength(PAGE_SIZE);

        switch (sortBy) {
            case SORT_OLDEST:
                dataHolder.setOrder(MediaFileOrder.DATE_UPLOADED);
                break;
            case SORT_NAME:
                dataHolder.setOrder(MediaFileOrder.NAME);
                break;
            case SORT_TYPE:
                dataHolder.setOrder(MediaFileOrder.TYPE);
                break;
            default:
                dataHolder.setOrder(MediaFileOrder.DATE_UPLOADED_DESC);
        }
    }
}
