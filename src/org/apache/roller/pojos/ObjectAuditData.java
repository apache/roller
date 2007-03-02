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

import java.util.Date;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Records change that a user has made to an object.
 * @ejb:bean name="ObjectAuditData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="roller_audit_log"  
 *
 * @author Dave Johnson
 */
public class ObjectAuditData
{
    private String id = UUIDGenerator.generateUUID();          // primary key
    private String userId;      // user that made change
    private String objectId;    // id of associated object, if any
    private String objectClass; // name of associated object class (e.g. WeblogEntryData)
    private String comment;     // description of change
    private Date changeTime;    // time that change was made
    
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
     * @ejb:persistent-field
     * @hibernate.property column="change_time" non-null="true" unique="false"
     */    
    public Date getChangeTime()
    {
        return changeTime;
    }
    /** @ejb:persistent-field */
    public void setChangeTime(Date changeTime)
    {
        this.changeTime = changeTime;
    }
    /**
     * @ejb:persistent-field
     * @hibernate.property column="comment_text" non-null="true" unique="false"
     */
    public String getComment()
    {
        return comment;
    }
    /** @ejb:persistent-field */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    /**
     * @ejb:persistent-field
     * @hibernate.property column="object_class" non-null="true" unique="false"
     */
    public String getObjectClass()
    {
        return objectClass;
    }
    /** @ejb:persistent-field */
    public void setObjectClass(String objectClass)
    {
        this.objectClass = objectClass;
    }
    /**
     * @ejb:persistent-field
     * @hibernate.property column="object_id" non-null="true" unique="false"
     */
    public String getObjectId()
    {
        return objectId;
    }
    /** @ejb:persistent-field */
    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }
    /**
     * @ejb:persistent-field
     * @hibernate.property column="user_id" non-null="true" unique="false"
     */
    public String getUserId()
    {
        return userId;
    }
    /** @ejb:persistent-field */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof ObjectAuditData != true) return false;
        ObjectAuditData o = (ObjectAuditData)other;
        return new EqualsBuilder()
            .append(getId(), o.getId()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getId())
            .toHashCode();
    }
}
