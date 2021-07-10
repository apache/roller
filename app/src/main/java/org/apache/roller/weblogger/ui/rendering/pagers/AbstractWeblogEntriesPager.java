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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.util.I18nMessages;

/**
 * An abstract implementation of a WeblogEntriesPager.
 *
 * This implementation lays out the basic functionality of an entries pager so
 * that subclasses can easily tweak only the few things necessary to handle
 * paging their own way.
 */
public abstract class AbstractWeblogEntriesPager implements WeblogEntriesPager {
    
    // message utils for doing i18n messages
    final I18nMessages messageUtils;
    
    // url strategy for building urls
    final URLStrategy urlStrategy;
    
    final Weblog weblog;
    final String locale;
    final String pageLink;
    final String entryAnchor;
    final String dateString;
    final String catName;
    
    final int offset;
    final int page;
    final int length;
    
    final List<String> tags;
    
    
    public AbstractWeblogEntriesPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List<String>       tags,
            int                page) {
        
        this.urlStrategy = strat;
        
        this.weblog = weblog;
        this.locale = locale;
        this.pageLink = pageLink;
        this.entryAnchor = entryAnchor;
        this.dateString = dateString;
        this.catName = catName;
        
        this.tags = tags != null ? tags : Collections.emptyList();
        
        // make sure offset, length, and page are valid
        int maxLength = WebloggerRuntimeConfig.getIntProperty("site.pages.maxEntries");
        this.length = Math.min(weblog.getEntryDisplayCount(), maxLength);
        
        this.page = Math.max(0, page);
        this.offset = length * page;
        
        // get a message utils instance to handle i18n of messages
        Locale viewLocale = null;
        if(locale != null) {
            String[] langCountry = locale.split("_");
            if(langCountry.length == 1) {
                viewLocale = new Locale(langCountry[0]);
            } else if(langCountry.length == 2) {
                viewLocale = new Locale(langCountry[0], langCountry[1]);
            }
        } else {
            viewLocale = weblog.getLocaleInstance();
        }
        this.messageUtils = I18nMessages.getMessages(viewLocale);
    }
    
    
    public boolean hasMoreEntries() {
        return false;
    }
    
    
    @Override
    public String getHomeLink() {
        return createURL(0, 0, weblog, locale, pageLink, entryAnchor, dateString, catName, tags);
    }
    
    
    @Override
    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.latest.home");
    }
    
    
    @Override
    public String getNextLink() {
        if (hasMoreEntries()) {
            return createURL(page, 1, weblog, locale, pageLink, entryAnchor, dateString, catName, tags);
        }
        return null;
    }
    
    
    @Override
    public String getNextName() {
        if (hasMoreEntries()) {
            return messageUtils.getString("weblogEntriesPager.latest.next");
        }
        return null;
    }
    
    
    @Override
    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, weblog, locale, pageLink, entryAnchor, dateString, catName, tags);
        }
        return null;
    }
    
    
    @Override
    public String getPrevName() {
        if (page > 0) {
            return messageUtils.getString("weblogEntriesPager.latest.prev");
        }
        return null;
    }
    
    
    @Override
    public String getNextCollectionLink() {
        return null;
    }
    
    
    @Override
    public String getNextCollectionName() {
        return null;
    }
    
    
    @Override
    public String getPrevCollectionLink() {
        return null;
    }
    
    
    @Override
    public String getPrevCollectionName() {
        return null;
    }
    
    
    /**
     * Parse data as either 6-char or 8-char format.
     */
    protected Date parseDate(String dateString) {
        Date ret = null;
        SimpleDateFormat char8DateFormat = DateUtil.get8charDateFormat();
        SimpleDateFormat char6DateFormat = DateUtil.get6charDateFormat();
        Calendar cal = Calendar.getInstance(
                weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
        if (   dateString!=null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
        	char8DateFormat.setCalendar(cal);
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) {
                ret = today;
            }
        }
        if (   dateString!=null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString) ) {
        	char6DateFormat.setCalendar(cal);
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) {
                ret = today;
            }
        }
        return ret;
    }
    
    
    /**
     * Return today based on current blog's timezone/locale.
     */
    protected Date getToday() {
        Calendar todayCal = Calendar.getInstance(weblog.getTimeZoneInstance(), weblog.getLocaleInstance());
        todayCal.setTime(new Date());
        return todayCal.getTime();
    }
    
    
    /**
     * Create URL that encodes pager state using most appropriate forms of URL.
     * @param pageAdd To be added to page number, or 0 for no page number
     */
    protected String createURL(
            int                page,
            int                pageAdd,
            Weblog             website,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catName,
            List<String>       tags) {
        
        int pageNum = page + pageAdd;
        
        if (pageLink != null) {
            return urlStrategy.getWeblogPageURL(website, locale, pageLink, entryAnchor, catName, dateString, tags, pageNum, false);
        } else if (entryAnchor != null) {
            return urlStrategy.getWeblogEntryURL(website, locale, entryAnchor, true);
        }
        
        return urlStrategy.getWeblogCollectionURL(website, locale, catName, dateString, tags, pageNum, false);
    }
    
}
