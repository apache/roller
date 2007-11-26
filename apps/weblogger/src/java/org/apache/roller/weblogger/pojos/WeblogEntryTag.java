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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Tag bean.
 * @author Elias Torres
 */
public class WeblogEntryTag implements Serializable {
    private static Log log = LogFactory.getLog(WeblogEntryTag.class);    
    
    private static final long serialVersionUID = -2602052289337573384L;
    
    private String id = UUIDGenerator.generateUUID();
    private Weblog website = null;
    private WeblogEntry weblogEntry = null;
    private String userName = null;
    private String name = null;
    private Timestamp time = null;
    
    
    public WeblogEntryTag() {
    }
    
    public WeblogEntryTag(
            String id,
            Weblog website,
            WeblogEntry weblogEntry,
            User user, 
            String name,
            Timestamp time) {
        //this.id = id;
        this.website = website;
        this.weblogEntry = weblogEntry;
        this.userName = user.getUserName();
        this.name = name;
        this.time = time;
    }
    
    //------------------------------------------------------- Simple properties
    
    /**
     * Unique ID and primary key.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * ID of website that this tag refers to.
     */
    public Weblog getWeblog() {
        return this.website;
    }
    
    public void setWeblog(Weblog website) {
        this.website = website;
    }
    
    
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    public void setWeblogEntry(WeblogEntry data) {
        weblogEntry = data;
    }
    
    
    public User getUser() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(getCreatorUserName());
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: " + getCreatorUserName(), e);
        }
        return null;
    }
    
    public String getCreatorUserName() {
        return userName;
    }

    public void setCreatorUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Tag value.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    
    public java.sql.Timestamp getTime() {
        return this.time;
    }
    
    public void setTime(java.sql.Timestamp tagTime) {
        this.time = tagTime;
    }
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.time);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogEntryTag != true) return false;
        WeblogEntryTag o = (WeblogEntryTag)other;
        return new EqualsBuilder()
        .append(getName(), o.getName())
        .append(getWeblogEntry(), o.getWeblogEntry())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getName())
        .append(getWeblogEntry())
        .toHashCode();
    }

}
