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

import java.util.Date;
import org.apache.roller.weblogger.pojos.ThemeTemplate;


/**
 * Pojo safety wrapper for ThemeTemplate objects.
 */
public final class ThemeTemplateWrapper {
    
    // keep a reference to the wrapped pojo
    private final ThemeTemplate pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private ThemeTemplateWrapper(ThemeTemplate toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static ThemeTemplateWrapper wrap(ThemeTemplate toWrap) {
        if (toWrap != null) {
            return new ThemeTemplateWrapper(toWrap);
        }
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
    
    
    public String getContents() {
        return this.pojo.getContents();
    }
    
    
    public String getLink() {
        return this.pojo.getLink();
    }
    
    
    public Date getLastModified() {
        return this.pojo.getLastModified();
    }
    
    
    public boolean isHidden() {
        return this.pojo.isHidden();
    }
    
    
    public boolean isNavbar() {
        return this.pojo.isNavbar();
    }
    
}
