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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.velocity.deprecated.ContextLoader;
import org.apache.roller.util.Utilities;


/**
 * Loads page models (read-only data access objects which implement PageModel) 
 * and helpers (which "help" with HTML gen.) needed by page rendering process.
 */
public class ModelLoader {
    
    private static Log log = LogFactory.getLog(ModelLoader.class);
    
    
    /** 
     * Load helpers needed in weblog pages (e.g. calendar, menu).
     */
    public static void loadWeblogHelpers(PageContext pageContext, Map model) {
        
        CalendarHelper calendarTag = new CalendarHelper(pageContext);
        model.put("calendarTag", calendarTag);
        
        EditorMenuHelper menuTag = new EditorMenuHelper(pageContext);
        model.put("menuTag", menuTag);
    }
    
    
    /**
     * Load generic utility helpers.
     */
    public static void loadUtilityHelpers(Map model) {
        UtilitiesHelper utils = new UtilitiesHelper();
        model.put("utils", utils);
    }
    
    
    /**
     * Load weblog entry plugin helpers.
     */
    public static void loadPluginHelpers(WebsiteData weblog, Map model) 
        throws RollerException {
        WeblogEntryPluginsHelper plugins = new WeblogEntryPluginsHelper(weblog, model);
        model.put("plugins", plugins);
    }
    
    
    /**
     * Load old page models, but only if velocity.pagemodel.classname defined.
     */
    public static void loadOldModels(
        HttpServletResponse response, 
        HttpServletRequest  request,
        Map model) throws RollerException {

        // Only load old model if it's specified
        String useOldModel = 
            RollerConfig.getProperty("velocity.pagemodel.classname");        
        if (useOldModel != null) { 
            ContextLoader.setupContext(model, request, response);            
        }
    }
    
    
    /**
     * Load set of common weblog models.
     *
     * This is the list of models defined by rendering.weblogPageModels
     */
    public static void loadWeblogModels(Map model, Map initData)
            throws RollerException {
        
        String weblogModels = 
                RollerConfig.getProperty("rendering.weblogPageModels");
        loadModels(weblogModels, model, initData);
    }
    
    
    /**
     * Load set of common site-wide models.
     *
     * This is the list of models defined by rendering.sitePageModels
     */
    public static void loadSiteModels(Map model, Map initData)
            throws RollerException {
        
        String weblogModels = 
                RollerConfig.getProperty("rendering.sitePageModels");
        loadModels(weblogModels, model, initData);
    }
    
    
    /**
     * Load set of custom models allowed for the given weblog.
     *
     * Does not fail if there is a problem with one of the models.
     */
    public static void loadCustomModels(
            WebsiteData weblog, 
            Map model,
            Map initData) {
        
        if (weblog.getPageModels() != null) {
            String[] weblogModels = 
                Utilities.stringToStringArray(weblog.getPageModels(), ",");
            for (int i=0; i<weblogModels.length; i++) {
                try { // don't die just because of one bad custom model
                    Class modelClass = Class.forName(weblogModels[i]);
                    PageModel pageModel = (PageModel)modelClass.newInstance();
                    pageModel.init(initData);             
                    model.put(pageModel.getModelName(), pageModel);
                } catch (RollerException re) {
                    log.warn("ERROR: initializing a plugin: " + weblogModels[i]);
                } catch (ClassNotFoundException cnfe) {
                    log.warn("ERROR: can't find page model: " + weblogModels[i]);
                } catch (InstantiationException ie) {
                    log.warn("ERROR: insantiating page model: " + weblogModels[i]);
                } catch (IllegalAccessException iae) {
                    log.warn("ERROR: access exception page model: " + weblogModels[i]);
                }
            }
        }     
    }
    
    
    /**
     * Convenience method to load a comma-separated list of page models.
     * If any of the models fail to load, throws an exception.
     */
    private static void loadModels(
            String modelsString,
            Map model,
            Map initData) throws RollerException {
        
        String currentModel = null;
        try { // if we can't load a configued page models, then bail out
            String[] models = Utilities.stringToStringArray(modelsString, ",");
            for (int i=0; i<models.length; i++) {
                currentModel = models[i];
                Class modelClass = Class.forName(currentModel);
                PageModel pageModel = (PageModel) modelClass.newInstance();
                pageModel.init(initData);            
                model.put(pageModel.getModelName(), pageModel);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new RollerException("ERROR: can't find page model: " + currentModel);
        } catch (InstantiationException ie) {
            throw new RollerException("ERROR: insantiating page model: " + currentModel);
        } catch (IllegalAccessException iae) {
            throw new RollerException("ERROR: access exception page model: " + currentModel);
        }
    }
    
}
