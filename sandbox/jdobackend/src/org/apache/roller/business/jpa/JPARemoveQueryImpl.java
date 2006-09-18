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


package org.apache.roller.business.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.roller.business.datamapper.DatamapperRemoveQuery;

/**
 *
 */
public class JPARemoveQueryImpl implements DatamapperRemoveQuery {

    /** */
    protected EntityManager em;

    /** */
    protected Query q;

    /**
     * Creates a new instance of JPAQueryImpl
     */
    public JPARemoveQueryImpl(EntityManager em, Class clazz, String queryName) {
        this.em = em;
        q = em.createNamedQuery(queryName);
    }

    public void removeAll() {
        q.executeUpdate();
    }

    public void removeAll(Object param) {
        q.setParameter(1, param);
        q.executeUpdate();
    }

    public void removeAll(Object[] params) {
        for (int i = 0; i < params.length ; i++) {
            q.setParameter(i + 1, params[i]);
        }
        q.executeUpdate();
    }

    public DatamapperRemoveQuery setUnique() {
        // TODO - Craig, what does setUnique mean on a RemoveQuery?
        //q.setUnique(true);
        return this;
    }

    public DatamapperRemoveQuery setTypes(Object[] types) {
        return this;
    }

}