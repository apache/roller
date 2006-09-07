/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.apache.roller.business.jdo;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.roller.business.datamapper.DatamapperRemoveQuery;

/**
 *
 */
public class JDORemoveQueryImpl implements DatamapperRemoveQuery {

    /** */
    protected PersistenceManager pm;

    /** */
    protected Query q;
    
    /** Creates a new instance of JDOQueryImpl */
    public JDORemoveQueryImpl(PersistenceManager pm,
            Class clazz, String queryName) {
        this.pm = pm;
        q = pm.newQuery(clazz, queryName);
    }

    public void removeAll() {
        q.deletePersistentAll();
    }

    public void removeAll(Object param) {
        q.deletePersistentAll(new Object[]{param});
    }

    public void removeAll(Object[] params) {
        q.deletePersistentAll(params);
    }

    public DatamapperRemoveQuery setUnique() {
        q.setUnique(true);
        return this;
    }

    public DatamapperRemoveQuery setTypes(Object[] types) {
        return this;
    }

}