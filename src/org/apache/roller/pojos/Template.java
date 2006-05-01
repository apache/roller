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
 * Template.java
 *
 * Created on June 27, 2005, 11:59 AM
 */

package org.apache.roller.pojos;

import java.util.Date;


/**
 * The Template interface represents the abstract concept of a single unit
 * of templated or non-rendered content.  For Roller we mainly think of
 * templates as Velocity templates which are meant to be fed into the 
 * Velocity rendering engine.
 *
 * @author Allen Gilliland
 */
public interface Template {
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getName();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDescription();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getContents();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getLink();
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getLastModified();
    
    /*
    public void setId(String id);
    public void setName(String name);
    public void setDescription(String desc);
    public void setContents(String contents);
    public void setLink(String link);
    public void setLastModified(Date date);
    */
}
