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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.RuntimeConfigDefs;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.RollbackException;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action which handles editing of global configuration.
 */
public class GlobalConfig extends UIAction implements ParameterAware, ServletRequestAware {

    private static Logger log = LoggerFactory.getLogger(GlobalConfig.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    // the request parameters
    private Map<String, String[]> params = Collections.emptyMap();

    // map of config properties
    private Map<String, RuntimeConfigProperty> properties = Collections.emptyMap();

    // the runtime config def used to populate the display
    private RuntimeConfigDefs globalConfigDef = null;

    // work around checkbox issue in cases where user inadvertently does a
    // GET on the GlobalConfig!save URL and thus sets all checkboxes to false
    private String httpMethod = "GET";

    // weblogs for frontpage blog chooser
    private Collection<Weblog> weblogs;

    public GlobalConfig() {
        this.actionName = "globalConfig";
        this.desiredMenu = "admin";
        this.pageTitle = "configForm.title";
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    /**
     * Prepare action by loading runtime properties map.
     */
    @Override
    public void prepare() {
        // just grab our properties map and make it available to the action
        setProperties(propertiesManager.getProperties());
        setWeblogs(weblogManager.getWeblogs(true, 0, -1));
        globalConfigDef = propertiesManager.getRuntimeConfigDefs();
    }

    /**
     * Display global properties editor form.
     */
    @Override
    public String execute() {
        return SUCCESS;
    }

    /**
     * Save global properties.
     */
    public String save() {
        if (!"POST".equals(httpMethod)) {
            return ERROR;
        }

        // only set values for properties that are already defined
        RuntimeConfigProperty updProp;
        String incomingProp;
        for (String propName : getProperties().keySet()) {
            updProp = getProperties().get(propName);
            incomingProp = this.getParameter(updProp.getName());

            log.debug("Checking property [{}]", propName);
            log.debug("Request value is [{}]", incomingProp);

            // some special treatment for booleans
            // this is a bit hacky since we are assuming that any prop
            // with a value of "true" or "false" is meant to be a boolean
            // it may not always be the case, but we should be okay for now
            // null check below needed w/Oracle
            if (updProp.getValue() != null && (updProp.getValue().equals("true") ||
                    updProp.getValue().equals("false"))) {

                if (incomingProp == null || !incomingProp.equals("on")) {
                    incomingProp = "false";
                } else {
                    incomingProp = "true";
                }
            }

            // only work on props that were submitted with the request
            if (incomingProp != null) {
                log.debug("Setting new value for [{}]", propName);

                // NOTE: the old way had some locale sensitive way to do this??
                updProp.setValue(incomingProp.trim());
            }
        }

        try {
            // save 'em and flush
            propertiesManager.saveProperties(getProperties());
            persistenceStrategy.flush();
            // notify user of our success
            addMessage("generic.changes.saved");
        } catch (RollbackException ex) {
            log.error("Error saving roller properties", ex);
            addError("generic.error.check.logs");
        }

        return SUCCESS;
    }

    public void setParameters(Map<String, String[]> parameters) {
        this.params = parameters;

        if (log.isDebugEnabled()) {
            log.debug("Parameter map:");

            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                log.debug(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // convenience method for getting a single parameter as a String
    private String getParameter(String key) {
        String[] p = this.params.get(key);
        if (p != null && p.length > 0) {
            return p[0];
        }
        return null;
    }

    public List<Pair<String, String>> getRegistrationOptions() {
        List<Pair<String, String>> opts;
        opts = Arrays.stream(RuntimeConfigDefs.RegistrationOption.values())
                .map(r -> Pair.of(r.name(), r.getDescription()))
                .collect(Collectors.toList());
        return opts;
    }

    public List<Pair<String, String>> getCommentOptions() {
        List<Pair<String, String>> opts;
        opts = Arrays.stream(RuntimeConfigDefs.CommentOption.values())
                .map(r -> Pair.of(r.name(), r.getSiteDescription()))
                .collect(Collectors.toList());
        return opts;
    }

    public List<Pair<String, String>> getHTMLSanitizingLevels() {
        List<Pair<String, String>> opts;
        opts = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> (!r.equals(HTMLSanitizer.Level.NONE)))
                .map(r -> Pair.of(r.name(), r.getDescription()))
                .collect(Collectors.toList());
        return opts;
    }

    public List<Pair<String, String>> getCommentHTMLSanitizingLevels() {
        List<Pair<String, String>> opts;
        opts = Arrays.stream(HTMLSanitizer.Level.values())
                .filter(r -> (!r.equals(HTMLSanitizer.Level.NONE)))
                .filter(r -> (r.getSanitizingLevel() <= HTMLSanitizer.Level.BASIC_IMAGES.getSanitizingLevel()))
                .map(r -> Pair.of(r.name(), r.getDescription()))
                .collect(Collectors.toList());
        return opts;
    }

    public Map<String, RuntimeConfigProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, RuntimeConfigProperty> properties) {
        this.properties = properties;
    }

    public RuntimeConfigDefs getGlobalConfigDef() {
        return globalConfigDef;
    }

    public void setGlobalConfigDef(RuntimeConfigDefs def) {
        this.globalConfigDef = def;
    }

    public void setServletRequest(HttpServletRequest req) {
        httpMethod = req.getMethod();
    }

    public Collection<Weblog> getWeblogs() {
        return weblogs;
    }

    public void setWeblogs(Collection<Weblog> weblogs) {
        this.weblogs = weblogs;
    }
}
