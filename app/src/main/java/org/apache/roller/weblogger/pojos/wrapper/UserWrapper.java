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
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.util.HTMLSanitizer;


/**
 * Pojo safety wrapper for User objects.
 */
public final class UserWrapper {
    
    // keep a reference to the wrapped pojo
    private final User pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private UserWrapper(User toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static UserWrapper wrap(User toWrap) {
        if(toWrap != null) {
            return new UserWrapper(toWrap);
        }
        return null;
    }
    
    
    /**
     * This is here for backwards compatibility.  We no longer allow the
     * username to be displayed publicly, so screen name is returned instead.
     */
    public String getUserName() {
        if (WebloggerConfig.getBooleanProperty("user.hideUserNames")) {
            return this.pojo.getScreenName();
        }
        return this.pojo.getUserName();
    }
    
    
    public String getScreenName() {
        return HTMLSanitizer.conditionallySanitize(this.pojo.getScreenName());
    }
    
    
    public String getFullName() {
        return HTMLSanitizer.conditionallySanitize(this.pojo.getFullName());
    }
    
    
    public String getEmailAddress() {
        return this.pojo.getEmailAddress();
    }
    
    
    public Date getDateCreated() {
        return this.pojo.getDateCreated();
    }
    
    
    public String getLocale() {
        return this.pojo.getLocale();
    }
    
    
    public String getTimeZone() {
        return this.pojo.getTimeZone();
    }
    
}
