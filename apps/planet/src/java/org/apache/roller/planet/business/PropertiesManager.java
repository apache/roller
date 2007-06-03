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

package org.apache.roller.planet.business;

import java.util.Map;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.pojos.PropertyData;


/**
 * Manages global runtime properties.
 */
public interface PropertiesManager extends Manager {
    
    /** 
     * Save a single property 
     */
    public void saveProperty(PropertyData property) throws PlanetException;
    
    
    /** 
     * Save a list of properties 
     */
    public void saveProperties(Map properties) throws PlanetException;
    
    
    /** 
     * Retrieve a single property by name 
     */
    public PropertyData getProperty(String name) throws PlanetException;
    
    
    /** 
     * Retrieve a list of all properties 
     */
    public Map getProperties() throws PlanetException;
    
}
