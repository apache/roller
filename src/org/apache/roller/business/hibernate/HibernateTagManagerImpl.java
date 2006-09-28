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

package org.apache.roller.business.hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.TagManager;
import org.apache.roller.pojos.SiteTagAggregateData;
import org.apache.roller.pojos.TagCloudEntry;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogTagAggregateData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.DateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;


/**
 * Hibernate implementation of the TagManager.
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public class HibernateTagManagerImpl implements TagManager {
    
    private static final long serialVersionUID = -6573148804064466850L;

    private static Log log = LogFactory.getLog(HibernateTagManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
        
    public HibernateTagManagerImpl(HibernatePersistenceStrategy strat) {
        log.debug("Instantiating Hibernate Tag Manager");
        
        this.strategy = strat;
    }

    public List getTags(Date startDate,
        Date endDate,
        WebsiteData website,
        UserData user,
        boolean sortByCount,
        int limit) throws RollerException {
      try {
        List results = new ArrayList();
        
        Session session = ((HibernatePersistenceStrategy) strategy).getSession();
        
        StringBuffer queryString = new StringBuffer();
        queryString.append("select t.name, count(t.name) ");
        queryString.append("from WeblogEntryTagData t ");
        queryString.append("where t.time between ? and ? ");
        if(website != null)
          queryString.append("and t.website.id = '" + website.getId() + "' ");
        if(user != null)
          queryString.append("and t.user.id = '" + user.getId() + "' ");
        queryString.append("group by t.name ");
        queryString.append(sortByCount ? "order by col_1_0_ desc " : "order by t.name ");

        Query query = session.createQuery(queryString.toString());
        query.setTimestamp(0, DateUtil.getStartOfDay(startDate));
        query.setTimestamp(1, DateUtil.getEndOfDay(endDate));
        if(limit > 0)
          query.setMaxResults(limit);
        
        for (Iterator iter = query.list().iterator(); iter.hasNext();) {
          Object[] row = (Object[]) iter.next();
          TagCloudEntry ce = new TagCloudEntry();
          ce.setName((String) row[0]);
          ce.setCount(((Integer)row[1]).intValue());
          results.add(ce);
        }
        
        return results;
        
      } catch (HibernateException e) {
        throw new RollerException(e);
      }

    }

    public void release() {
      // TODO Auto-generated method stub
      
    }

    public Date summarize(Date startDate) throws RollerException {
      
      Session session = ((HibernatePersistenceStrategy) strategy).getSession();
      
      // This queries the db for the last time we'll include in our batch processing
      Query query = session.createQuery("select time from WeblogEntryTagData group by time order by time desc");
      query.setMaxResults(1);
      
      Timestamp thisRun = (Timestamp) query.uniqueResult();
      
      if(thisRun == null || thisRun.compareTo(startDate) == 0)
      {
        // nothing to do
        log.debug("TagManager.summarize() found nothing to do.");
        return null;
      }
                        
      // #### SiteTagAgg ####
      
      List params = new ArrayList(2);
      StringBuffer queryString = new StringBuffer();
      queryString.append("select name, count(name) from WeblogEntryTagData where ");
      if(startDate != null) {
        queryString.append("time > ? and ");
        params.add(startDate);
      }
      queryString.append("time <= ? group by name");
      params.add(thisRun);
      
      query =  session.createQuery(queryString.toString());
      for(int i = 0; i < params.size(); i++)
        query.setParameter(i, params.get(i));
      
      List results = query.list();
      log.debug("TagManager.summarize() found " + results.size() + " tags to summarize.");
      
      for(Iterator it = results.iterator(); it.hasNext(); )
      {
        Object[] row = (Object[]) it.next();
        String tagName = (String) row[0];
        int tagCount = ((Integer)row[1]).intValue();
        
        SiteTagAggregateData siteData = (SiteTagAggregateData) session.createCriteria(SiteTagAggregateData.class).add(Expression.eq("name", tagName)).uniqueResult();
        if(siteData == null) 
            siteData = new SiteTagAggregateData(null, tagName, tagCount);
        else
            siteData.setCount(siteData.getCount()+tagCount);
        
        strategy.store(siteData);
      } 

      // #### WeblogTagAgg ####
      
      params = new ArrayList(2);
      queryString = new StringBuffer();
      queryString.append("select t.website, t.name, count(t.name) from WeblogEntryTagData t where ");
      if(startDate != null) {
        queryString.append("t.time > ? and ");
        params.add(startDate);
      }
      queryString.append("t.time <= ? group by t.website, t.name");
      params.add(thisRun);
      
      query =  session.createQuery(queryString.toString());
      for(int i = 0; i < params.size(); i++)
        query.setParameter(i, params.get(i));
      
      results = query.list();
      log.debug("TagManager.summarize() found " + results.size() + " tags/website to summarize.");
                
      for(Iterator it = results.iterator(); it.hasNext(); )
      {
        Object[] row = (Object[]) it.next();
        WebsiteData website = (WebsiteData) row[0];
        String tagName = (String) row[1];
        int tagCount = ((Integer)row[2]).intValue();
        
        WeblogTagAggregateData weblogData = (WeblogTagAggregateData) session
          .createCriteria(WeblogTagAggregateData.class)
          .add(Expression.eq("name", tagName))
          .add(Expression.eq("website", website))
          .uniqueResult();
        if(weblogData == null) 
          weblogData = new WeblogTagAggregateData(null, website, tagName, tagCount);
        else
          weblogData.setCount(weblogData.getCount()+tagCount);
        
        strategy.store(weblogData);
      } 
        
      session.flush(); 

      return thisRun;
    }
}
