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

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ObjectFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;

import javax.servlet.ServletException;

public class UIBeanFactory {
    private static final Log log = LogFactory.getLog(UIBeanFactory.class);

    public static <T> T getBean(Class<T> beanClass) throws ServletException {
        try {
            ObjectFactory objectFactory = ServletActionContext.getContext()
                .getContainer()
                .getInstance(ObjectFactory.class);
            return (T) objectFactory.buildBean(beanClass, null);
        } catch (Exception e) {
            log.error("Failed to create bean of type " + beanClass.getName(), e);
            throw new ServletException("Failed to create bean of type " + beanClass.getName(), e);
        }
    }
}