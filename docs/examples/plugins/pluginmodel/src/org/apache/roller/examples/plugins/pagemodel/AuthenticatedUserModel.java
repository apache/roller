/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.examples.plugins.pagemodel;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;
import org.apache.roller.weblogger.ui.rendering.model.Model;


public class AuthenticatedUserModel implements Model {
    private static Log log = LogFactory.getLog(AuthenticatedUserModel.class); 
    private HttpServletRequest request = null;
    
    public String getModelName() {
        return "authenticated";
    }

    public void init(Map params) throws WebloggerException {
    	PageContext context = (PageContext)params.get("pageContext");
    	this.request = (HttpServletRequest) context.getRequest();
    }
    
    public UserWrapper getUser() {
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses != null && rses.getAuthenticatedUser() != null) {
                return UserWrapper.wrap(rses.getAuthenticatedUser());
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return null;
    }
}
