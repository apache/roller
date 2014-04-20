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
import java.util.List;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;


/**
 * Pojo safety wrapper for WeblogBookmarkFolder object.
 */
public final class WeblogBookmarkFolderWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogBookmarkFolder pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogBookmarkFolderWrapper(WeblogBookmarkFolder toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static WeblogBookmarkFolderWrapper wrap(WeblogBookmarkFolder toWrap) {
        if (toWrap != null) {
            return new WeblogBookmarkFolderWrapper(toWrap);
        }
        return null;
    }

    public String getId() {
        return this.pojo.getId();
    }

    public String getName() {
        return this.pojo.getName();
    }

    public List<WeblogBookmarkWrapper> getBookmarks() {
        // iterate through and wrap
        List<WeblogBookmarkWrapper> wrappedCollection = new ArrayList<WeblogBookmarkWrapper>(this.pojo.getBookmarks().size());
        for (WeblogBookmark bookmark : this.pojo.getBookmarks()) {
            wrappedCollection.add(WeblogBookmarkWrapper.wrap(bookmark));
        }
        return wrappedCollection;
    }    

}
