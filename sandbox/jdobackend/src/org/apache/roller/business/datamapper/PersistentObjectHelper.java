/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.business.datamapper;

import org.apache.roller.pojos.PersistentObject;

/**
 * @author Mitesh Meswani
 */
public class PersistentObjectHelper {

    /**
     * Returns whether given object is already persistent. An object is 
     * persistent if and only if its id field is intialized.
     * Ideally, all callers of this method should be modified so that a call
     * to this method is not needed. This requires that Roller code is 
     * changed to distinguish between new, managed, and detached objects.
     * @param obj
     * @return true if object is already persistent; false otherwise
     * @see org.apache.roller.business.jpa.JPAPersistenceStrategy#store(org.apache.roller.pojos.PersistentObject)
     * for a description of changes needed
     */
    public static boolean isObjectPersistent(PersistentObject obj) {
        return !( obj.getId() == null || obj.getId().trim().equals("") );
    }
}
