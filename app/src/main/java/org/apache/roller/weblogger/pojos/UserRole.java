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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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
    
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getUserName());
        buf.append(", ").append(getRole());
        buf.append("}");
        return buf.toString();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserRole)) {
            return false;
        }
        UserRole o = (UserRole)other;
        return new EqualsBuilder()
        .append(getRole(), o.getRole())
        .append(getUserName(), o.getUserName())
        .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).append(getRole()).toHashCode();
    }
    
}
