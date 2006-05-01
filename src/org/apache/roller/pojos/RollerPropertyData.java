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
/*
 * RollerConfigProperty.java
 *
 * Created on April 20, 2005, 2:58 PM
 */

package org.apache.roller.pojos;


/**
 * This POJO represents a single property of the roller system.
 *
 * @author Allen Gilliland
 *
 * @ejb:bean name="RollerPropertyData"
 * @hibernate.class lazy="false" table="roller_properties"
 * @hibernate.cache usage="read-write"
 */
public class RollerPropertyData 
    extends org.apache.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    
    static final long serialVersionUID = 6913562779484028899L;
    
    
    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Holds value of property value.
     */
    private String value;

    
    public RollerPropertyData() {}
    
    
    public RollerPropertyData(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    
    public void setData(PersistentObject object)
    {
        if (object instanceof RollerPropertyData)
        {
            RollerPropertyData prop = (RollerPropertyData) object;
            this.name = prop.getName();
            this.value = prop.getValue();
        }
    }
    
    
    public String toString()
    {
        return (this.name + "=" + this.value);
    }
    

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     * @ejb:persistent-field
     * @hibernate.id column="name" generator-class="assigned"
     */
    public String getName() {

        return this.name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     * @ejb:persistent-field
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Getter for property value.
     *
     * @return Value of property value.
     * @ejb:persistent-field
     * @hibernate.property column="value" non-null="false" unique="false"
     */
    public String getValue() {

        return this.value;
    }

    /**
     * Setter for property value.
     *
     * @param value New value of property value.
     * @ejb:persistent-field
     */
    public void setValue(String value) {

        this.value = value;
    }
    
    
    public String getId() {
        // this is only here because it is required by PersistentObject
        return null;
    }
    
    
    public void setId(String id) {
        // do nothing ... only here because the PersistentObject class requires it
    }
}
