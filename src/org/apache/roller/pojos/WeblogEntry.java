/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.pojos;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

/**
 * Common weblog entry interface supported by both WeblogEntryData and 
 * PlanetEntryData, meant primarily as documentation -- to help Roller template
 * author's write code that works with both blog and planet entries.
 */
public interface WeblogEntry {    

    /** 
     * Returns raw title of entry, which you should treat as escaped HTML 
     * content, but since titles are not required by all feed formats so this 
     * method may return null.
     */
    public String getTitle();
        
    /**
     * Returns the fully qualified permalink for the entry.
     */
    public String getPermaLink();
    
    /**
     * Returns published time for entry, will not be null (both planet and 
     * Roller itself require a published timestamp).
     */
    public Timestamp getPubTime();
            
    /**
     * Returns updated time for entry, will not be null (planet entries do
     * not alays have update times).
     */
    public Timestamp getUpdateTime();

    /** 
     * Returns the summary of the entry, but since summary is not a required 
     * field, this may be null.
     */
    public String getSummary();
    
    /** 
     * Returns the entry content, which you should treat as escaped HTML content.
     */
    public String getText();

    /** 
     * Returns the entry's category object but since feed formats do require
     * category information, this may be null. If there are multiple categories
     * then the first category found is returned.
     */
    public WeblogCategory getCategory();
    
    /** 
     * Returns collection of WeblogCategoryData objects for entry.
     */
    public List getCategories();
    
    /**
     * Returns user object of author who created post but since titles are not 
     * required by all feed formats so this method may return null.
     */
    public User getCreator();
    
    /**
     * Returns website object representing site from which entry originated.
     */
    public Website getWebsite();
   
    
    public interface User { 
        /** User name of user */
        public String getUserName();
    }
    
    public interface WeblogCategory {     
        /** Name of category */
        public String getName();
    }
    
    public interface Website {     
        /** URL of website */
        public String getUrl();    
        /** Name of website */
        public String getName();
    }
}


