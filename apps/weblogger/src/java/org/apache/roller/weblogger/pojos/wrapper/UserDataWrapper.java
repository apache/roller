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
import org.apache.roller.weblogger.pojos.User;


/**
 * Pojo safety wrapper for User objects.
 */
public class UserDataWrapper {
    
    // keep a reference to the wrapped pojo
    private final User pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private UserDataWrapper(User toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static UserDataWrapper wrap(User toWrap) {
        if(toWrap != null)
            return new UserDataWrapper(toWrap);
        
        return null;
    }
    
    
    /**
     * This is here for backwards compatability.  We no longer allow the
     * username to be displayed publicly, so screen name is returned instead.
     */
    public String getUserName() {
        return this.pojo.getScreenName();
    }
    
    
    public String getScreenName() {
        return this.pojo.getScreenName();
    }
    
    
    public String getFullName() {
        return this.pojo.getFullName();
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
