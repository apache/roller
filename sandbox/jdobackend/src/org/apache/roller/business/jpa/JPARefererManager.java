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

import org.apache.roller.business.datamapper.DatamapperRefererManagerImpl;
import org.apache.roller.business.datamapper.DatamapperQuery;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WebsiteData;

import java.util.List;

/**
 * @author Mitesh Meswani
 */
public class JPARefererManager extends DatamapperRefererManagerImpl {
    public JPARefererManager(JPAPersistenceStrategy strategy) {
        super(strategy);
    }

    protected void clearDayHits() throws RollerException {
        JPAUpdateQuery query = ((JPAPersistenceStrategy) strategy).
                newUpdateQuery("RefererData.clearDayHits");
        query.updateAll();
    }

    protected void clearDayHitsByWebsite(WebsiteData website) throws
            RollerException {
        JPAUpdateQuery query = ((JPAPersistenceStrategy) strategy).
                newUpdateQuery("RefererData.clearDayHitsByWebsite");
        query.updateAll(website);    
    }

    protected List getBlackListedReferer(String[] blacklist) throws
            RollerException {
        StringBuffer queryString = getQueryStringForBlackList(blacklist);
        DatamapperQuery query =
                ((JPAPersistenceStrategy) strategy).newDynamicQuery(queryString.toString());
        return (List) query.execute();
    }

    protected List getBlackListedReferer(WebsiteData website, String[] blacklist) 
            throws RollerException {
        StringBuffer queryString = getQueryStringForBlackList(blacklist);
        queryString.append(" AND r.website = ?1 ");
        DatamapperQuery query =
                ((JPAPersistenceStrategy) strategy).newDynamicQuery(queryString.toString());
        return (List) query.execute(website);
    }

    /**
     * Generates a JPQL query of form
     * SELECT r FROM RefererData r WHERE
     *     ( refererUrl like %blacklist[1] ..... OR refererUrl like %blacklist[n])
     * AND (r.excerpt IS NULL OR r.excerpt LIKE '')
     * @param blacklist
     * @return
     */
    private StringBuffer getQueryStringForBlackList(String[] blacklist) {
        assert blacklist.length > 0;
        StringBuffer queryString = new StringBuffer("SELECT r FROM RefererData r WHERE (");
        //Search for any matching entry from blacklist[]
        final String OR = " OR ";
        for (int i = 0; i < blacklist.length; i++) {
            String ignoreWord = blacklist[i];
            //TODO: DataMapper port: original code use "like ignore case" as follows
            // or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            // There is no equivalent for it in JPA
            queryString.append("r.refererUrl like '%").append(ignoreWord.trim()).append("%'").
                    append(OR);
        }
        // Get rid of last OR
        queryString.delete(queryString.length() - OR.length(), queryString.length());
        queryString.append(" ) ");
        queryString.append(" AND (r.excerpt IS NULL OR r.excerpt LIKE '')");
        return queryString;
    }

}


