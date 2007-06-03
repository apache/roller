/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.pojos.wrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Pojo safety wrapper for WeblogCategory objects.
 */
public class WeblogCategoryDataWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogCategory pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogCategoryDataWrapper(WeblogCategory toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogCategoryDataWrapper wrap(WeblogCategory toWrap) {
        if(toWrap != null)
            return new WeblogCategoryDataWrapper(toWrap);
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public String getName() {
        return this.pojo.getName();
    }
    
    
    public String getDescription() {
        return this.pojo.getDescription();
    }
    
    
    public String getImage() {
        return this.pojo.getImage();
    }
    
    
    public String getPath() {
        return this.pojo.getPath();
    }
    
    
    public WebsiteDataWrapper getWebsite() {
        return WebsiteDataWrapper.wrap(this.pojo.getWebsite());
    }
    
    
    public WeblogCategoryDataWrapper getParent() {
        return WeblogCategoryDataWrapper.wrap(this.pojo.getParent());
    }
    
    
    public List getWeblogCategories() {
        Set initialCollection = this.pojo.getWeblogCategories();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, WeblogCategoryDataWrapper.wrap((WeblogCategory) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public List retrieveWeblogEntries(boolean subcats)
            throws WebloggerException {
        
        List initialCollection = this.pojo.retrieveWeblogEntries(subcats);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogEntryDataWrapper.wrap((WeblogEntry) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    // TODO: this method doesn't work and propably doesn't need to be here anyways?
    public boolean descendentOf(WeblogCategory ancestor) {
        return this.pojo.descendentOf(ancestor);
    }
    
    
    public boolean isInUse() {
        return this.pojo.isInUse();
    }
    
}
