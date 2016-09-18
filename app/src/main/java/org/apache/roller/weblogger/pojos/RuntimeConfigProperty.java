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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * This POJO represents a single property of the blog server
 */
@Entity
@Table(name = "weblogger_properties")
@NamedQueries({
        @NamedQuery(name = "RuntimeConfigProperty.getAll",
                query = "SELECT r FROM RuntimeConfigProperty r")
})
public class RuntimeConfigProperty {

    private String name;
    private String value;

    public RuntimeConfigProperty() {
    }

    public RuntimeConfigProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    @Id
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property value.
     *
     * @return Value of property value.
     */
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return (getName() + "=" + getValue());
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RuntimeConfigProperty)) {
            return false;
        }
        RuntimeConfigProperty o = (RuntimeConfigProperty) other;
        return new EqualsBuilder()
                .append(getName(), o.getName())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .toHashCode();
    }

}
