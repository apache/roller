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
package org.apache.roller.pojos;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a statistical count.
 */
public class StatCount { 
    
    /** Id of the subject of the statistic */
    private String subjectId;
    
    /** Short name of the subject of the statistic */
    private String subjectNameShort;
    
    /** Long name of the subject of the statistic */
    private String subjectNameLong; 
    
    /** I18N key that describes the type of statistic */
    private String typeKey;
    
    /** The statistical count */    
    private long count;
    
    /** Weblog handle of weblog that stat is associated with, or null if none */
    private String weblogHandle = null;

    public StatCount(String subjectId, String subjectNameShort, String subjectNameLong, String typeKey, long count) {
        this.setSubjectId(subjectId);
        this.setSubjectNameShort(subjectNameShort);
        this.setSubjectNameLong(subjectNameLong);
        this.setTypeKey(typeKey);
        this.setCount(count);
    } 
    
    public String getTypeKey() {
        return typeKey;
    }
    
    public void setTypeKey(String typeKey) {
        this.typeKey = typeKey;
    }
    
    public long getCount() {
        return count;
    }
    
    public void setCount(long count) {
        this.count = count;
    }
    
    public String getSubjectId() {
        return subjectId;
    }
    
    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }
    
    public String getSubjectNameShort() {
        return subjectNameShort;
    }
    
    public void setSubjectNameShort(String subjectNameShort) {
        this.subjectNameShort = subjectNameShort;
    }
    
    public String getSubjectNameLong() {
        return subjectNameLong;
    }
    
    public void setSubjectNameLong(String subjectNameLong) {
        this.subjectNameLong = subjectNameLong;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.weblogHandle);
        buf.append(", ").append(this.count);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof StatCount != true) return false;
        StatCount o = (StatCount)other;
        return new EqualsBuilder()
            .append(getSubjectId(), o.getSubjectId()) 
            .append(getTypeKey(), o.getTypeKey()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getSubjectId())
            .append(getTypeKey())
            .toHashCode();
    }
}
