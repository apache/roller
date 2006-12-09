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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import org.apache.roller.business.datamapper.DatamapperWeblogManagerImpl;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogEntryTagAggregateData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.RollerException;

/**
 * @author Markus Fuchs
 * @author Mitesh Meswani
 */
public class JPAWeblogManagerImpl extends DatamapperWeblogManagerImpl {

    public JPAWeblogManagerImpl(JPAPersistenceStrategy strategy) {
        super(strategy);
    }

    /**
     * @inheritDoc
     */
    public List getWeblogEntries(
            WebsiteData website,
            UserData    user,
            Date        startDate,
            Date        endDate,
            String      catName,
            List        tags,
            String      status,
            String      sortby,
            String      locale,
            int         offset,
            int         length) throws RollerException {
        
        WeblogCategoryData cat = null;
        if (StringUtils.isNotEmpty(catName) && website != null) {
            cat = getWeblogCategoryByPath(website, catName);
            if (cat == null) catName = null;
        }
        if (catName != null && catName.trim().equals("/")) {
            catName = null;
        }
                
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT e FROM WeblogEntryData e WHERE ");

        if (website != null) {
            params.add(++size, website.getId());
            queryString.append("website.id = ?").append(size);                
        } else {
            params.add(++size, Boolean.TRUE);                
            queryString.append("website.enabled = ?").append(size);                
        }
            
        if (user != null) {
            params.add(++size, user.getId());
            queryString.append(" AND creator.id = ?").append(size);
        }

        if (startDate != null) {
            params.add(++size, startDate);
            queryString.append(" AND pubTime >= ?").append(size);
        }
            
        if (endDate != null) {
            params.add(++size, endDate);                
            queryString.append(" AND pubTime <= ?").append(size);
        }
            
        if (cat != null && website != null) {
            params.add(++size, cat.getId());                
            queryString.append(" AND category.id = ?").append(size);
        }
            
        if (tags != null && tags.size() > 0) {
          for(int i = 0; i < tags.size(); i++) {
              params.add(++size, tags.get(i));
              queryString.append(" AND tags.name = ?").append(size);
          }
        }
            
        if (status != null) {
            params.add(++size, status);
            queryString.append(" AND status = ?").append(size);
        }
            
        if (locale != null) {
            params.add(++size, locale + '%');
            queryString.append(" AND locale like ?").append(size);
        }
            
        if (sortby != null && sortby.equals("updateTime")) {
            queryString.append("ORDER BY updateTime DESC");
        } else {
            queryString.append("ORDER BY pubTime DESC");
        }
            
        JPADynamicQueryImpl query = (JPADynamicQueryImpl) 
            ((JPAPersistenceStrategy) strategy)
            .newDynamicQuery(queryString.toString());
            
        if (offset != 0 || length != -1) {
            if (length == -1) {
                length = Integer.MAX_VALUE - offset;
            }
            query.setRange(offset, length);
        }
                        
        return (List) query.execute(params.toArray());
    }

    /**
     * @inheritDoc
     */
    public List getComments(
            WebsiteData     website,
            WeblogEntryData entry,
            String          searchString,
            Date            startDate,
            Date            endDate,
            Boolean         pending,
            Boolean         approved,
            Boolean         spam,
            boolean         reverseChrono,
            int             offset,
            int             length) throws RollerException {
            
        List params = new ArrayList();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        queryString.append("SELECT c FROM CommentData c WHERE ");

        if (entry != null) {
            params.add(++size, entry);
            queryString.append("c.weblogEntry = ?").append(size);
        } else if (website != null) {
            params.add(++size, website);
            queryString.append("c.weblogEntry.website = ?").append(size);
        }
            
        if (searchString != null) {
            params.add(++size, "%" + searchString + "%");
            queryString.append(" AND (url LIKE ?").append(size).
                        append(" OR content LIKE ?").append(size).append(")");
        }
            
        if (startDate != null) {
            params.add(++size, startDate);
            queryString.append("c.postTime >= ?").append(size);
        }
            
        if (endDate != null) {
            params.add(++size, endDate);
            queryString.append("c.postTime =< ?").append(size);
        }
            
        if (pending != null) {
            params.add(++size, pending);
            queryString.append("c.pending = ?").append(size);
        }
            
        if (approved != null) {
            params.add(++size, approved);
            queryString.append("c.approved = ?").append(size);
        }
            
        if (spam != null) {
            params.add(++size, spam);
            queryString.append("c.spam = ?").append(size);
        }
            
        if (reverseChrono) {
            queryString.append(" ORDER BY c.postTime DESC");
        } else {
            queryString.append(" ORDER BY c.postTime ASC");
        }
            
        JPADynamicQueryImpl query = (JPADynamicQueryImpl) 
            ((JPAPersistenceStrategy) strategy)
            .newDynamicQuery(queryString.toString());
            
        if (length != -1) {
            query.setRange(offset, length);
        }

        List comments = (List) query.execute(params.toArray());
        if (offset==0 || comments.size() < offset) {
            return comments;
        }
        List range = new ArrayList();
        for (int i=offset; i<comments.size(); i++) {
            range.add(comments.get(i));
        }
        return range;
    }

    /**
     * @inheritDoc
     */
    public int removeMatchingComments(
            WebsiteData     website,
            WeblogEntryData entry,
            String  searchString,
            Date    startDate,
            Date    endDate,
            Boolean pending,
            Boolean approved,
            Boolean spam) throws RollerException {

        // TODO dynamic bulk delete query
        /* I'd MUCH rather use a bulk delete, but MySQL says "General error,  
           message from server: "You can't specify target table 'roller_comment' 
           for update in FROM clause"

            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
         
            // Can't use Criteria API to do bulk delete, so we build string     
            StringBuffer queryString = new StringBuffer();
            ArrayList params = new ArrayList();
         
            // Can't use join in a bulk delete query, but can use a sub-query      
            queryString.append(
                "delete CommentData cmt where cmt.id in "
              + "(select c.id from CommentData as c where ");
                
            if (entry != null) {
                queryString.append("c.weblogEntry.anchor = ? and c.weblogEntry.website.handle = ? ");
                params.add(entry.getAnchor());
                params.add(entry.getWebsite().getHandle());
            } else if (website != null) {
                queryString.append("c.weblogEntry.website.handle = ? ");
                params.add(website.getHandle());
            } 
            
            if (searchString != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("(c.url like ? or c.content like ?) ");
                searchString = '%' + searchString + '%';
                params.add(searchString);
                params.add(searchString);
            }
            
            if (startDate != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.postTime > ? ");
                params.add(startDate);
            }
            
            if (endDate != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.postTime < ? ");
                params.add(endDate);
            }
            
            if (pending != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.pending = ? ");
                params.add(pending);
            }
            
            if (approved != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.approved = ? ");
                params.add(approved);
            }
            
            if (spam != null) {
                if (!queryString.toString().trim().endsWith("where")) {
                    queryString.append("and ");
                }
                queryString.append("c.spam = ? ");
                params.add(spam);
            }
            queryString.append(")");
            
            Query query = session.createQuery(queryString.toString());
            for(int i = 0; i < params.size(); i++) {
              query.setParameter(i, params.get(i));
            }
            return query.executeUpdate(); 
         */
        
        return super.removeMatchingComments(
                website,
                entry,
                searchString,
                startDate,
                endDate,
                pending,
                approved,
                spam);
    }

    /**
     * @inheritDoc
     */
    public void applyCommentDefaultsToEntries(WebsiteData website)
            throws RollerException {
        if (log.isDebugEnabled()) {
            log.debug("applyCommentDefaults");
        }

        // TODO: Non-standard JPA bulk update, using parameter values in set clause
        ((JPAPersistenceStrategy) strategy).newUpdateQuery(
            "WeblogEntryData.updateAllowComments&amp;CommentDaysByWebsite")
            .updateAll(new Object[] {
                website.getDefaultAllowComments(),
                new Integer(website.getDefaultCommentDays()),
                website
                });
    }

    /**
     * @inheritDoc
     */
    public boolean getTagComboExists(List tags, WebsiteData weblog) {

        List results = null;

        if(tags == null) {
            return false;
        }
        
        // TODO: Non-standard JPA query, passing List as argument
        try {
            // are we checking a specific weblog, or site-wide?
            if (weblog != null)
                results = (List) strategy.newQuery(
                    WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData.getNameByNameIn&Website")
                    .execute(new Object[] {tags, weblog});
            else
                results = (List) strategy.newQuery(
                    WeblogEntryTagAggregateData.class, 
                    "WeblogEntryTagAggregateData
                    .getNameByNameIn&WebsiteNull")
                    .execute(tags);
        } catch (RollerException re) {
            throw new RuntimeException(re);
        }

        return (results != null && results.size() == tags.size());
    }

    /**
     * @inheritDoc
     */
    public void updateTagCount(String name, WebsiteData website, int amount)
            throws RollerException {
        if(amount == 0) {
            throw new RollerException("Tag increment amount cannot be zero.");
        }

        if(website == null) {
            throw new RollerException("Website cannot be NULL.");
        }

        // The reason why add order lastUsed desc is to make sure we keep picking the most recent
        // one in the case where we have multiple rows (clustered environment)
        // eventually that second entry will have a very low total (most likely 1) and
        // won't matter
        WeblogEntryTagAggregateData weblogTagData = 
            (WeblogEntryTagAggregateData)
            strategy.newQuery(WeblogEntryTagAggregateData.class,
                "WeblogEntryTagAggregateData.getByName&WebsiteOrderByLastUsedDesc")
            .setRange(0,1)
            .setUnique()
            .execute(new Object[] {name, website});

        WeblogEntryTagAggregateData siteTagData = 
            (WeblogEntryTagAggregateData)
            strategy.newQuery(WeblogEntryTagAggregateData.class,
                "WeblogEntryTagAggregateData.getByName&WebsiteNullOrderByLastUsedDesc")
            .setRange(0,1)
            .setUnique()
            .execute(new Object[] {name, website});

        Timestamp lastUsed = new Timestamp((new Date()).getTime());

        // create it only if we are going to need it.
        if(weblogTagData == null && amount > 0) {
            weblogTagData = new WeblogEntryTagAggregateData(
                null, website, name, amount);
            weblogTagData.setLastUsed(lastUsed);
            strategy.store(weblogTagData);
        } else if(weblogTagData != null) {
            ((JPAPersistenceStrategy) strategy).newUpdateQuery(
                "WeblogEntryTagAggregateData.updateAddToTotalByName&Weblog")
                .updateAll(new Object[] {
                    new Long(amount),
                    weblogTagData.getName(),
                    website
                    });
        }

        // create it only if we are going to need it.        
        if(siteTagData == null && amount > 0) {
            siteTagData = new WeblogEntryTagAggregateData(null, null, name, amount);
            siteTagData.setLastUsed(lastUsed);
            strategy.store(siteTagData);
        } else if(siteTagData != null) {
            ((JPAPersistenceStrategy) strategy).newUpdateQuery(
                "WeblogEntryTagAggregateData.updateAddToTotalByName&WeblogNull")
                .updateAll(new Object[] {
                    new Long(amount),
                    weblogTagData.getName(),
                    website
                    });
        }

        // delete all bad counts
        strategy.newRemoveQuery(WeblogEntryTagAggregateData.class, 
            "WeblogEntryTagAggregateData.removeByTotalLessEqual")
            .removeAll(new Integer(0));
    }

    /**
     * @inheritDoc
     */
    public void resetAllHitCounts() throws RollerException {

        ((JPAPersistenceStrategy) strategy).newUpdateQuery(
                "HitCountData.updateDailyHitCountZero").updateAll();
    }

}
