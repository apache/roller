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

package org.apache.roller.ui.rendering.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.UserDataWrapper;
import org.apache.roller.util.URLUtilities;

/**
 * Paging for users.
 */
public class UsersPager extends AbstractPager {
    private List users;    
    private String letter = null;
    protected static Log log =
            LogFactory.getFactory().getInstance(UsersPager.class);
    
    /** Creates a new instance of CommentPager */
    public UsersPager(            
            WebsiteData    weblog,             
            WeblogTemplate weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        super(weblog, weblogPage, locale, sinceDays, page, length);
        getItems();
    }
    
    /** Creates a new instance of CommentPager */
    public UsersPager(   
            String letter,
            WebsiteData    weblog,             
            WeblogTemplate weblogPage,
            String         locale,
            int            sinceDays,
            int            page,
            int            length) {
        super(weblog, weblogPage, locale, sinceDays, page, length);
        this.letter = letter;
        getItems();
    }
    
    public List getItems() {
        if (users == null) {
            List results = new ArrayList();
            try {            
                Roller roller = RollerFactory.getRoller();
                UserManager umgr = roller.getUserManager();
                List rawUsers = null;
                if (letter == null) {
                    rawUsers = umgr.getUsers(offset, length + 1);
                } else {
                    rawUsers = umgr.getUsersByLetter(letter.charAt(0), offset, length);
                }
                int count = 0;
                for (Iterator it = rawUsers.iterator(); it.hasNext();) {
                    UserData user = (UserData) it.next();
                    if (count++ < length) {
                        results.add(UserDataWrapper.wrap(user));
                    } else {
                        more = true;
                    }                    
                }
            } catch (Exception e) {
                log.error("ERROR: fetching user list", e);
            }
            users = results;
        }
        return users;
    }   
}
