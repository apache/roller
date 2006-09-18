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

import org.apache.roller.business.datamapper.DatamapperQuery;
import org.apache.roller.business.datamapper.DatamapperRemoveQuery;

/**
 *
 */
public class JDOQueryImpl implements DatamapperQuery {

    /** */
    protected PersistenceManager pm;

    /** */
    protected Query q;
    
    /** Creates a new instance of JDOQueryImpl */
    public JDOQueryImpl(PersistenceManager pm,
            Class clazz, String queryName) {
        this.pm = pm;
        q = pm.newQuery(clazz, queryName);
    }

    public Object execute() {
        return q.execute();
    }

    public Object execute(Object param) {
        return q.execute(param);
    }

    public Object execute(Object[] params) {
        return q.execute(params);
    }

    public DatamapperQuery setUnique() {
        q.setUnique(true);
        return this;
    }

    public DatamapperQuery setRange(long fromIncl, long toExcl) {
        q.setRange(fromIncl, toExcl);
        return this;
    }
    
}
