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

import java.util.List;
import org.apache.roller.RollerException;


/**
 * Abstract base class for hierarchical persistent objects. Provides generic
 * implementations of save and remove that know how to handle parents, children,
 * and descendents.
 */
public abstract class HierarchicalPersistentObject extends PersistentObject {
    
    
    HierarchicalPersistentObject mNewParent = null;
    
    /** Create an association between object and ancestor. */
    public abstract Assoc createAssoc(
            HierarchicalPersistentObject object,
            HierarchicalPersistentObject ancestor,
            String relation ) throws RollerException;
    
    /** Name of association class which must implement Assoc. */
    public abstract Class getAssocClass();
    
    /** Name of object propery in association class */
    public abstract String getObjectPropertyName();
    
    /** Name of ancestor propery in association class */
    public abstract String getAncestorPropertyName();
    
    /** Set new parent - invalidates getPath() until object is saved(). */
    public abstract void setParent(HierarchicalPersistentObject parent);
    
    public abstract Assoc getParentAssoc() throws RollerException;
    
    public abstract List getChildAssocs() throws RollerException;
    
    public abstract List getAllDescendentAssocs() throws RollerException;
    
    public abstract List getAncestorAssocs() throws RollerException;
    
    /** Returns true if this object is in use and should not be deleted */
    public abstract boolean isInUse() throws RollerException;
    
    /** Should be needed only be manager objects */
    public HierarchicalPersistentObject getNewParent() {
        return mNewParent;
    }
    
}
