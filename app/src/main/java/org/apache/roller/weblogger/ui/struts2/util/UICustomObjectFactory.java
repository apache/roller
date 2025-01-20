/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ObjectFactory;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.core.RollerSessionManager;
import org.apache.roller.weblogger.ui.core.SessionManager;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class UICustomObjectFactory extends ObjectFactory {

@Override
public Object buildBean(Class clazz, Map<String, Object> extraContext) throws Exception {
    if (clazz == RollerSession.class) {
        HttpServletRequest request = ServletActionContext.getRequest();
        SessionManager sessionManager = new RollerSessionManager();
        return new RollerSession(sessionManager, request);
    }
    return super.buildBean(clazz, extraContext);
}
}