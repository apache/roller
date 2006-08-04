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

package org.apache.roller.ui.rendering.pagers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;

/**
 * Provides paging for comments.
 */
public class CommentsPager extends AbstractPager {
    
    private List comments = null;
    protected static Log log =
            LogFactory.getFactory().getInstance(CommentsPager.class);
    
    /** Creates a new instance of CommentPager */
    public CommentsPager(            
            WebsiteData    weblog,             
            Template       weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        super(weblog, weblogPage, locale, sinceDays, page, length);
        getItems();
    }
    
    public List getItems() {
        if (comments == null) {
            List results = new ArrayList();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);
            Date startDate = cal.getTime();
            try {            
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                List entries = wmgr.getComments( 
                    null, null, null, startDate, new Date(), 
                    Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, true, offset, length + 1);
                int count = 0;
                for (Iterator it = entries.iterator(); it.hasNext();) {
                    CommentData comment = (CommentData) it.next();
                    if (count++ < length) {
                        results.add(CommentDataWrapper.wrap(comment));
                    } else {
                        more = true;
                    }                                
                }
            } catch (Exception e) {
                log.error("ERROR: fetching comment list", e);
            }
            comments = results;
        }
        return comments;
    }   
}
