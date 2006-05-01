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
 * PropertiesManager.java
 *
 * Created on April 21, 2005, 10:34 AM
 */

package org.roller.model;

import java.util.Map;
import org.roller.RollerException;
import org.roller.pojos.RollerPropertyData;


/**
 * Manages global properties for Roller.
 */
public interface PropertiesManager {
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
    
    /** 
     * Save a single property 
     */
    public void saveProperty(RollerPropertyData property) throws RollerException;
    
    
    /** 
     * Save a list of properties 
     */
    public void saveProperties(Map properties) throws RollerException;
    
    
    /** 
     * Retrieve a single property by name 
     */
    public RollerPropertyData getProperty(String name) throws RollerException;
    
    
    /** 
     * Retrieve a list of all properties 
     */
    public Map getProperties() throws RollerException;
    
}
