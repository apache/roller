/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file for details.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import com.opensymphony.xwork2.ObjectFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.core.RollerSessionManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class UIBeanFactory extends ObjectFactory {
    private static final Log log = LogFactory.getLog(UIBeanFactory.class);

    @Override
    public Object buildBean(Class clazz, Map<String, Object> extraContext) throws Exception {
        if (clazz == RollerSession.class) {
            return createRollerSession(extraContext);
        }
        return super.buildBean(clazz, extraContext);
    }

    private RollerSession createRollerSession(Map<String, Object> extraContext) {
        HttpServletRequest request = (HttpServletRequest) extraContext.get("request");
        return new RollerSession(new RollerSessionManager(), request);
    }

    public static <T> T getBean(Class<T> beanClass) throws ServletException {
        return getBean(beanClass, null);
    }

    public static <T> T getBean(Class<T> beanClass, HttpServletRequest request) throws ServletException {
        try {
            Map<String, Object> context = new HashMap<>();
            if (request != null) {
                context.put("request", request);
            }
            return (T) new UIBeanFactory().buildBean(beanClass, context);
        } catch (Exception e) {
            String msg = String.format("Failed to create bean of type %s: %s",
                beanClass.getName(), e.getMessage());
            log.error(msg, e);
            throw new ServletException(msg, e);
        }
    }
}