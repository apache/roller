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

/**
 * @author Mitesh Meswani
 */
public class JPAUpdateQuery {

    protected EntityManager em;

    protected Query q;

    /**
     * Creates a new instance of JPAQueryImpl
     */
    public JPAUpdateQuery(EntityManager em, String queryName) {
        this.em = em;
        q = em.createNamedQuery(queryName);
    }

    public int updateAll() {
        return q.executeUpdate();
    }

    public int updateAll(Object param) {
        q.setParameter(1, param);
        return q.executeUpdate();
    }

    public int updateAll(Object[] params) {
        for (int i = 0; i < params.length ; i++) {
            q.setParameter(i + 1, params[i]);
        }
        return q.executeUpdate();
    }


}
