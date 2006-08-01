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

package org.apache.roller.ui.rendering.pagers;

import java.util.List;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.URLUtilities;

/**
 * Abstract base for simple pagers.
 */
public abstract class AbstractPager implements Pager {
    
    protected List           users;
    protected WebsiteData    weblog;
    protected WeblogTemplate weblogPage;
    protected String         locale;
    protected int            sinceDays;
    protected int            page;
    protected int            length;
    protected int            offset;
    protected boolean        more = false;   
    
    /** Creates a new instance of AbstractPager */
    public AbstractPager(            
            WebsiteData    weblog,             
            WeblogTemplate weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        
        this.weblog =     weblog;
        this.weblogPage = weblogPage;
        this.locale =     locale;
        this.sinceDays =  sinceDays;
        this.page =       page;
        this.length =     length;

        if(page > 0) {
            this.page = page;
        }
        this.offset = length * page;
    }

    public String getHomeLink() {
        return createURL(page, 0, weblog, weblogPage, locale);
    }

    public String getHomeName() {
        return "Home";
    }

    public String getNextLink() {
        if (page > 0) {
            return createURL(page, -1, weblog, weblogPage, locale);
        }
        return null;
    }

    public String getNextName() {
        if (page > 0) {
            return "Next";
        }
        return null;
    }

    public String getPrevLink() {
        if (more) {        
            return createURL(page, 1, weblog, weblogPage, locale);
        }
        return null;
    }

    public String getPrevName() {
        if (more) {
            return "Previous";
        }
        return null;
    }
    
    protected static String createURL(
        int                page, 
        int                pageAdd, 
        WebsiteData        weblog, 
        WeblogTemplate     weblogPage, 
        String             locale) {

        int pageNum = page + pageAdd;
        String pageLink = (weblogPage != null) ? weblogPage.getLink() : null;
        if (weblogPage != null) {
            return URLUtilities.getWeblogPageURL(
                weblog, locale, pageLink, null, null, null, pageNum, false);
        } 
        return URLUtilities.getWeblogCollectionURL(
                weblog, locale, null, null, pageNum, false);
    }  
}
