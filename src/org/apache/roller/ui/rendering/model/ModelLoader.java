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

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.ui.rendering.velocity.deprecated.ContextLoader;
import org.apache.roller.util.Utilities;


/**
 * Loads page models (read-only data access objects which implement Model) 
 * and helpers (which "help" with HTML gen.) needed by page rendering process.
 */
public class ModelLoader {
    
    private static Log log = LogFactory.getLog(ModelLoader.class);
    
    
    /** 
     * Load helpers needed in weblog pages (e.g. calendar, menu).
     */
    public static void loadWeblogHelpers(Map model, Map initData) 
            throws RollerException {
        
        CalendarModel calendarTag = new CalendarModel();
        calendarTag.init(initData);
        model.put(calendarTag.getModelName(), calendarTag);
        
        MenuModel menuTag = new MenuModel();
        menuTag.init(initData);
        model.put(menuTag.getModelName(), menuTag);
    }
    
    
    /**
     * Load generic utility helpers.
     */
    public static void loadUtilityHelpers(Map model, Map initData) 
            throws RollerException {
        
        UtilitiesModel utils = new UtilitiesModel();
        utils.init(initData);
        model.put("utils", utils);
    }
    
    
    /**
     * Load old page models, but only if velocity.pagemodel.classname defined.
     */
    public static void loadOldModels(
            Map model,
            HttpServletRequest  request,
            HttpServletResponse response,
            PageContext pageContext,
            WeblogRequest weblogRequest) throws RollerException {
        
        // Only load old model if it's specified
        String useOldModel = 
            RollerConfig.getProperty("velocity.pagemodel.classname");        
        if (useOldModel != null) { 
            ContextLoader.setupContext(model, request, response, pageContext, weblogRequest);            
        }
    }
    
    
    /**
     * Load set of common weblog models.
     *
     * This is the list of models defined by rendering.weblogPageModels
     */
    public static void loadPageModels(Map model, Map initData)
            throws RollerException {
        
        String weblogModels = 
                RollerConfig.getProperty("rendering.pageRenderModels");
        loadModels(weblogModels, model, initData);
    }
    
    
    /**
     * Load set of common feed models.
     *
     * This is the list of models defined by rendering.feedRendererModels
     */
    public static void loadFeedModels(Map model, Map initData)
            throws RollerException {
        
        String weblogModels = 
                RollerConfig.getProperty("rendering.feedRenderModels");
        loadModels(weblogModels, model, initData);
    }
    
    
    /**
     * Load set of common search models.
     *
     * This is the list of models defined by rendering.searchRendererModels
     */
    public static void loadSearchModels(Map model, Map initData)
            throws RollerException {
        
        String searchModels = 
                RollerConfig.getProperty("rendering.searchRenderModels");
        loadModels(searchModels, model, initData);
    }
    
    
    /**
     * Load set of common site-wide models.
     *
     * This is the list of models defined by rendering.sitePageModels
     */
    public static void loadSiteModels(Map model, Map initData)
            throws RollerException {
        
        String weblogModels = 
                RollerConfig.getProperty("rendering.siteRenderModels");
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
                    Model pageModel = (Model)modelClass.newInstance();
                    pageModel.init(initData);             
                    model.put(pageModel.getModelName(), pageModel);
                } catch (RollerException re) {
                    log.warn("ERROR: initializing a plugin: " + weblogModels[i]);
                } catch (ClassNotFoundException cnfe) {
                    log.warn("ERROR: can't find model: " + weblogModels[i]);
                } catch (InstantiationException ie) {
                    log.warn("ERROR: insantiating model: " + weblogModels[i]);
                } catch (IllegalAccessException iae) {
                    log.warn("ERROR: access exception model: " + weblogModels[i]);
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
                Model pageModel = (Model) modelClass.newInstance();
                pageModel.init(initData);            
                model.put(pageModel.getModelName(), pageModel);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new RollerException("ERROR: can't find model: " + currentModel);
        } catch (InstantiationException ie) {
            throw new RollerException("ERROR: insantiating model: " + currentModel);
        } catch (IllegalAccessException iae) {
            throw new RollerException("ERROR: access exception model: " + currentModel);
        }
    }
    
}
