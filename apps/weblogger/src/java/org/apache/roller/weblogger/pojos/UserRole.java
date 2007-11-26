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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Role bean.
 */
public class UserRole implements Serializable {
    
    public static final long serialVersionUID = -4254083071697970972L;
    
    private String id = UUIDGenerator.generateUUID();
    private String userName;
    private String role;
    
    
    public UserRole() {
    }
    
    public UserRole(String username, String role) {
        this.userName = username;
        this.role = role;
    }
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    
    public String getUserName() {
        return this.userName;
    }
    
    public void setUserName( String userName ) {
        this.userName = userName;
    }        
    
    
    public String getRole() {
        return this.role;
    }
    
    public void setRole( String role ) {
        this.role = role;
    }
    
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.userName);
        buf.append(", ").append(this.role);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof UserRole != true) return false;
        UserRole o = (UserRole)other;
        return new EqualsBuilder()
        .append(getRole(), o.getRole())
        .append(getUserName(), o.getUserName())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).append(getRole()).toHashCode();
    }
    
}
