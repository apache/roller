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

package org.apache.roller.planet;

import org.apache.roller.RollerException;


/**
 * A base exception class for Roller Planet.
 */
public class PlanetException extends RollerException {
    
    
    public PlanetException() {
        super();
    }
    
    
    /**
     * Construct PlanetException with message string.
     *
     * @param s Error message string.
     */
    public PlanetException(String s) {
        super(s);
    }
    
    
    /**
     * Construct PlanetException, wrapping existing throwable.
     *
     * @param s Error message
     * @param t Existing connection to wrap.
     */
    public PlanetException(String s, Throwable t) {
        super(s, t);
    }
    
    
    /**
     * Construct PlanetException, wrapping existing throwable.
     *
     * @param t Existing exception to be wrapped.
     */
    public PlanetException(Throwable t) {
        super(t);
    }
    
}
