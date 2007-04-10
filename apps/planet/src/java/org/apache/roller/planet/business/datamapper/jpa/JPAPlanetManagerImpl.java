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

package org.apache.roller.planet.business.datamapper.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.datamapper.jpa.JPADynamicQueryImpl;
import org.apache.roller.business.datamapper.jpa.JPAPersistenceStrategy;
import org.apache.roller.planet.business.datamapper.DatamapperPlanetManagerImpl;
import org.apache.roller.planet.pojos.PlanetGroupData;

/**
 * Methods that need to go straight-to-JPA.
 */
public class JPAPlanetManagerImpl extends DatamapperPlanetManagerImpl {
    
    private static Log log = LogFactory.getLog(
        JPAPlanetManagerImpl.class);
    
    public JPAPlanetManagerImpl(DatamapperPersistenceStrategy strategy) {
        super(strategy);
    }
    
    public List getEntries(List groups, Date startDate, Date endDate, int offset, int len) throws RollerException {
        StringBuffer queryString = new StringBuffer();
                
        if(groups == null || groups.size() == 0) {
            throw new RollerException("groups cannot be null or empty");
        }
        
        List ret = null;
        try {
            long startTime = System.currentTimeMillis();
            
            StringBuffer sb = new StringBuffer();
            List params = new ArrayList();
            int size = 0;
            sb.append("SELECT e FROM PlanetEntryData e ");
            sb.append("JOIN e.subscription.groups g ");
            
            sb.append("WHERE (");
            for (int i=0; i<groups.size(); i++) {
                if (i > 0) sb.append(" OR ");
                PlanetGroupData group = (PlanetGroupData)groups.get(i);
                params.add(size++, group);            
                sb.append(" g = ?").append(size);
            }
            sb.append(") ");
            
            if (startDate != null) {
                params.add(size++, startDate);
                sb.append("AND e.pubTime > ?").append(size);
            }
            if (endDate != null) {
                params.add(size++, endDate);
                sb.append("AND e.pubTime < :?").append(size);
            }
            sb.append("ORDER BY e.pubTime DESC");
            
            JPADynamicQueryImpl query = (JPADynamicQueryImpl)
                ((JPAPersistenceStrategy) strategy)
                .newDynamicQuery(queryString.toString());

            if (offset != 0 || len != -1) {
                if (len == -1) {
                    len = Integer.MAX_VALUE - offset;
                }
                query.setRange(offset, len);
            }
            
            ret = (List) query.execute(params.toArray());
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Generated aggregation in "
                    +((endTime-startTime)/1000.0)+" seconds");
            
            return ret;
            
        } catch (Throwable e) {
            throw new RollerException(e);
        }
               
    }
    
}
