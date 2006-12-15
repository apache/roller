
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

package org.apache.roller.business.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.NoResultException;

import org.apache.roller.business.datamapper.DatamapperQuery;

/**
 *
 */
public abstract class JPAQueryImpl implements DatamapperQuery {

    /** */
    protected EntityManager em;

    /** */
    protected Query q;

    private boolean singleResult = false;

    /**
     * Creates a new instance of JPAQueryImpl
     */
    protected JPAQueryImpl(EntityManager em, String arg) {
        this.em = em;
        q = createQuery(arg);
    }

    /**
     * Create a Query for this instance.
     * @param arg Can be the name of a named query or the actual query string
     * for a dynamic query.
     * @return Query object
     */
    protected abstract Query createQuery(String arg);

    public Object execute() {
        return executeQuery();
    }

    public Object execute(Object param) {
        q.setParameter(1, param);
        return executeQuery();
    }

    public Object execute(Object[] params) {
        for (int i = 0; i < params.length ; i++) {
            q.setParameter(i + 1, params[i]);
        }
        return executeQuery();
    }

    public DatamapperQuery setUnique() {
        singleResult = true;
        return this;
    }

    public DatamapperQuery setTypes(Object[] types) {
        //TODO: Craig, a more natural fit is to pass types also with execute
        return this;
    }

    public DatamapperQuery setRange(long fromIncl, long toExcl) {
        //TODO: JPA takes these as int :(.
        q.setFirstResult((int) fromIncl);
        q.setMaxResults((int) toExcl);
        return this;
    }

    /**
     * Helper that calls q.getSingleResult vs. q.getResultList depending on
     * value of singleResult.
     */
    private Object executeQuery() {
        Object result = null;
        if (singleResult) {
            try {
                result = q.getSingleResult();
            } catch (NoResultException e) {
                // Roller code expects a null for this condition
                result = null;
            }
        } else {
            result = q.getResultList();
        }
        return result;
    }

}
