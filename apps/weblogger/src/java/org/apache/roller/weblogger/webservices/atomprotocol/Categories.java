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
package org.apache.roller.weblogger.webservices.atomprotocol;

import com.sun.syndication.feed.atom.Category;
import java.util.ArrayList;
import java.util.List;


/** 
 * Categories object may contain Category objects 
 *//*
	 appInlineCategories =
 element app:categories {
     attribute fixed { "yes" | "no" }?,
     attribute scheme { atomURI }?,
     (atomCategory*)
 }
 */
public class Categories {
    private List categories = new ArrayList(); // of Category objects
    private String scheme = null;
    private boolean fixed = false;
    
    /** Add category list of those specified*/
    public void addCategory(Category cat) {
        categories.add(cat);
    }
    
    /** Iterate over Category objects */
    public List getCategories() {
        return categories;
    }

    /** True if clients MUST use one of the categories specified */
    public boolean isFixed() {
        return fixed;
    }

    /** True if clients MUST use one of the categories specified */
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /** Category URI scheme to use for Categories without a scheme */
    public String getScheme() {
        return scheme;
    }

    /** Category URI scheme to use for Categories without a scheme */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

}
