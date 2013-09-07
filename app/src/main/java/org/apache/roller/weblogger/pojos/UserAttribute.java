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
 * Name and value pair associated with a user object.
 * @author Tatyana Tokareva
 */
public class UserAttribute implements Serializable {
    
    public static final long serialVersionUID = -6354583200913127874L;    
    
    private String id = UUIDGenerator.generateUUID();
    private String userName;
    private String attrName;
    private String attrValue;
    
    
    public static enum Attributes {
        OPENID_URL("openid.url");
        
        private Attributes(String name) {
            this.name = name;
        }        
        private final String name;

        @Override
        public String toString() {
            return name;
        }     
        public String get() {
            return name;
        }
    }

    public UserAttribute() {
        // required for JPA
    }

    public UserAttribute(String userName, String attrName, String attrValue) {
        this.userName = userName;
        this.attrName = attrName;
        this.attrValue = attrValue;
    }        
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userId) {
        this.userName = userId;
    }
    
    public String getName() {
        return attrName;
    }

    public void setName(String attrName) {
        this.attrName = attrName;
    }

    public String getValue() {
        return attrValue;
    }

    public void setValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getUserName());
        buf.append(", ").append(getName());
        buf.append(", ").append(getValue());
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof UserRole)) {
            return false;
        }
        UserAttribute o = (UserAttribute)other;
        return new EqualsBuilder()
            .append(getUserName(), o.getUserName())
            .append(getName(), o.getName())
            .append(getValue(), o.getValue())        
            .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).append(getName()).append(getValue()).toHashCode();
    }
}
