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


package org.apache.roller.business.datamapper;

import org.apache.roller.pojos.TagStat;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.business.jpa.JPAUpdateQuery;
import org.apache.roller.RollerException;

import java.util.List;
import java.util.Iterator;

/**
 * @author Mitesh Meswani
 */
public class JPAUserManagerImpl extends DatamapperUserManagerImpl {

    private JPAPersistenceStrategy strategy;
    
    public JPAUserManagerImpl(JPAPersistenceStrategy strat) {
        super(strat);
        this.strategy = strat;
    }

    protected void updateTagAggregates(List tags) throws RollerException {
        for(Iterator iter = tags.iterator(); iter.hasNext();) {
                TagStat stat = (TagStat) iter.next();
                JPAUpdateQuery query = strategy.newUpdateQuery(
                    "WeblogEntryTagAggregateData.updateTotalByName&ampWeblogNull");
                query.updateAll(
                    new Object[] {Integer.valueOf(stat.getCount()), 
                    stat.getName() });
        }
    }
}
