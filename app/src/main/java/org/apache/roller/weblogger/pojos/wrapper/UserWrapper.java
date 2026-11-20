/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.pojos.wrapper;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;


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
            return getScreenName();
        }
        return this.pojo.getUserName();
    }
    
    
    /**
     * Get the screen name as html escaped text, since users choose their own
     * screen names and templates print them as they are.
     */
    public String getScreenName() {
        return escapeOnce(this.pojo.getScreenName());
    }
    
    
    /**
     * Get the full name as html escaped text.
     */
    public String getFullName() {
        return escapeOnce(this.pojo.getFullName());
    }
    
    
    /**
     * Names are stored as typed when weblog admins are trusted. When they
     * aren't, the sanitizer may encode angle brackets, quotes and
     * non-breaking spaces but leaves ampersands as they are, so decode just
     * those entities before escaping to encode them exactly once. Any other
     * entity, such as a literal {@code &amp;}, was typed that way and is
     * shown as typed. Apostrophes are encoded too, so that a name is also
     * safe inside a single-quoted attribute.
     */
    private static String escapeOnce(String name) {
        if (name != null && WebloggerConfig.getBooleanProperty("weblogAdminsUntrusted")) {
            name = StringUtils.replaceEach(name,
                    new String[] {"&lt;", "&gt;", "&quot;", "&#39;", "&nbsp;"},
                    new String[] {"<", ">", "\"", "'", "\u00A0"});
        }
        return StringUtils.replace(StringEscapeUtils.escapeHtml4(name), "'", "&#39;");
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
