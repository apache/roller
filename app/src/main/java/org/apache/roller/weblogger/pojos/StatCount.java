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
*
* Source file modified from the original ASF source; all changes made
* are also under Apache License.
*/
package org.apache.roller.weblogger.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Comparator;

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
        this.subjectId = subjectId;
        this.subjectNameShort = subjectNameShort;
        this.subjectNameLong = subjectNameLong;
        this.typeKey = typeKey;
        this.count = count;
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
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getWeblogHandle());
        buf.append(", ").append(getCount());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof StatCount)) {
            return false;
        }
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

    public static Comparator<StatCount> CountComparator = new Comparator<StatCount>() {
        public int compare(StatCount sc1, StatCount sc2) {
            // higher numbers first for counts
            int compVal = Long.valueOf(sc2.getCount()).compareTo(sc1.getCount());

            // still alpha order if tied
            if (compVal == 0) {
                compVal = sc1.getSubjectId().compareTo(sc2.getSubjectId());
                if (compVal == 0) {
                    compVal = sc1.getTypeKey().compareTo(sc2.getTypeKey());
                }
            }
            return compVal;
        }
    };

}
