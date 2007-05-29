/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.pojos; 

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Represents a user's permissions within a website.
 *
 * @ejb:bean name="WeblogPermission"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="roller_user_permissions"
 * @hibernate.cache usage="read-write"
 *
 * @author Dave Johnson
 */
public class WeblogPermission
{
    private String      id = UUIDGenerator.generateUUID();
    private Weblog website = null;
    private User    user = null;
    private boolean     pending = true;
    public static short LIMITED = 0x00; // 0000 
    public static short AUTHOR  = 0x01; // 0001
    public static short ADMIN   = 0x03; // 0011 
    private short       permissionMask = LIMITED;
    
    /** Creates a new instance of PermissionsData */
    public WeblogPermission() 
    {
    }

    /**
     * Check for specific permission.
     */
    public boolean has(short priv)
    {
        return (getPermissionMask() & priv) == priv;
    }
    /**
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *     generator-class="assigned"  
     */
    public String getId() 
    {
        return id;
    }
    /** @ejb:persistent-field */
    public void setId(String id) 
    {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    /** 
     * @hibernate.many-to-one column="website_id" cascade="none" not-null="false"
     */
    public Weblog getWebsite() 
    {
        return website;
    }
    public void setWebsite(Weblog website) 
    {
        this.website = website;
    }
    /** 
     * @hibernate.many-to-one column="user_id" cascade="none" not-null="false"
     */
    public User getUser() 
    {
        return user;
    }
    public void setUser(User user) 
    {
        this.user = user;
    }
    /**
     * Bit mask that encodes user's permissions in website.
     * @ejb:persistent-field
     * @hibernate.property column="permission_mask" non-null="true" unique="false"
     */
    public short getPermissionMask() 
    {
        return permissionMask;
    }
    /** @ejb:persistent-field */
    public void setPermissionMask(short permissionMask) 
    {
        this.permissionMask = permissionMask;
    }
    /**
     * True if user has been invited to join site but has not yet accepted.
     * And false if user is member of website.
     * @ejb:persistent-field
     * @hibernate.property column="pending" non-null="true" unique="false"
     */
    public boolean isPending() 
    {
        return pending;
    }
    /** @ejb:persistent-field */
    public void setPending(boolean pending) 
    {
        this.pending = pending;
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.permissionMask);
        buf.append(", ").append(this.pending);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogPermission != true) return false;
        WeblogPermission o = (WeblogPermission)other;
        return new EqualsBuilder()
            .append(user, o.user) 
            .append(website, o.website) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(user)
            .append(website)
            .toHashCode();
    }

}
