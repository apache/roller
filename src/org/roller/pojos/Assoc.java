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
 * Created on Jan 13, 2004
 */

package org.roller.pojos;

import org.roller.RollerException;
import java.io.Serializable;


/**
 * Interface for hierarchical assocations.
 */
public interface Assoc extends Serializable {
    
    public static final String PARENT = "PARENT";
    public static final String GRANDPARENT = "GRANDPARENT";
    
    /** Object that owns this association. */
    public HierarchicalPersistentObject getObject();
    
    public void setObject(HierarchicalPersistentObject hpo);
    
    
    /** Associated object. */
    public HierarchicalPersistentObject getAncestor();
    
    public void setAncestor(HierarchicalPersistentObject hpo);
    
    
    /** Type of relationship, PARENT or GRANDPARENT. */
    public String getRelation();
    
}
