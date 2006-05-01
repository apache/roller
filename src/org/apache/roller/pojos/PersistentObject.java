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

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.roller.RollerException;


/**
 * Base class for all of Roller's persistent objects.
 */
public abstract class PersistentObject implements Serializable {
    
    
    /**
     * All persistent objects require an identifier.
     */
    public abstract String getId();
    
    
    public abstract void setId( String id );
    
    
    /**
     * Load data based on data from another object.
     */
    public abstract void setData(PersistentObject obj);
    
    
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }
    
    
    // TODO: how efficient is this?
    public String toString() {
        try {
            // this may throw an exception if called by a thread that
            return ToStringBuilder.reflectionToString(
                    this, ToStringStyle.MULTI_LINE_STYLE);
        } catch (Throwable e) {
            // alternative toString() implementation used in case of exception
            return getClass().getName() + ":" + getId();
        }
    }
    
}

