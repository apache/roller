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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

import java.io.Serializable;

/**
 * pojo that will keep the Associativity of Themes for a weblog
 */
public class WeblogThemeAssoc implements Serializable{


    private static final long serialVersionUID = 2145018660522423453L;
    private String id = UUIDGenerator.generateUUID();
    private Weblog weblog = null;
    private String name = null;
    private boolean isCustom = false;
    private String type = null;

    public WeblogThemeAssoc() {
    }

    public WeblogThemeAssoc(Weblog weblog ,String themeId, boolean isCustom , String type ){
       this.weblog = weblog;
       this.name = themeId;
       this.isCustom = isCustom;
       this.type = type;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(getId());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogThemeAssoc != true) return false;
        WeblogThemeAssoc o = (WeblogThemeAssoc) other;
        return new EqualsBuilder()
            .append(getId(), o.getId())
            .append(getWeblog(), o.getWeblog())
            .append(getName(), o.getName())
            .append(getType(), o.getType())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).toHashCode();
    }
}
