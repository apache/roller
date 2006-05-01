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
 * CacheFactory.java
 *
 * Created on October 26, 2005, 3:25 PM
 */

package org.apache.roller.presentation.cache;

import java.util.Map;


/**
 * An interface representing a cache factory.  Implementors of this interface
 * are responsible for providing a method to construct cache implementations.
 *
 * In Roller you switch between various caching options by choosing a different
 * cache factory before starting up the application.
 *
 * @author Allen Gilliland
 */
public interface CacheFactory {
    
    public Cache constructCache(Map properties);
    
}
