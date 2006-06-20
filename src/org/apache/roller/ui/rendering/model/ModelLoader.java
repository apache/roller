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
 * Model loading code, shared by rendering servlets.
 */
public class ModelLoader {
    
    private static Log log = LogFactory.getLog(ModelLoader.class);   
    
    /** 
     * Load page models needed for rendering a weblog page.
     */
    public static void loadPageModels(
        WebsiteData weblog,
        PageContext pageContext,
        Map         map) throws RollerException { 
        
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse)pageContext.getRequest();

        // Only load old model if it's specified
        String useOldModel = 
            RollerConfig.getProperty("velocity.pagemodel.classname");        
        if (useOldModel != null) { 
            ContextLoader.setupContext(map, request, response);            
        }
        
        // Weblogs pages get the weblog page models
        String modelsString = 
            RollerConfig.getProperty("rendering.weblogPageModels");
        loadConfiguredPageModels(modelsString, request, map);
        
        // Weblog pages get utilities, calendar and editor-menu
        
        UtilitiesPageHelper utils = new UtilitiesPageHelper();
        map.put("utils", utils);
        
        CalendarPageHelper calendarTag = new CalendarPageHelper();
        calendarTag.init(pageContext);
        map.put("calendarTag", calendarTag);
        
        EditorMenuPageHelper menuTag = new EditorMenuPageHelper();
        menuTag.init(pageContext);
        map.put("menuTag", menuTag);
        
        // Weblog pages get weblog's additional custom models too
        if (weblog != null) {
            loadWeblogPageModels(weblog, request, map);
        }
    }
            
    /** 
     * Load page models needed for rendering a feed.
     */
    public static void loadFeedModels( 
        WebsiteData         weblog,
        HttpServletRequest  request,  
        HttpServletResponse response, 
        Map                 map) throws RollerException { 
        
        // TODO: remove this for Roller 3.0
        String useOldModel = 
            RollerConfig.getProperty("velocity.pagemodel.classname");        
        if (useOldModel != null) { 
            ContextLoader.setupContext(map, request, response);            
        }
        
        // Feeds get the weblog specific page model
        String modelsString = RollerConfig.getProperty("rendering.weblogPageModels");
        
        // Unless the weblog is the frontpage weblog w/aggregated feeds
        String frontPageHandle = 
            RollerConfig.getProperty("velocity.pagemodel.classname");
        boolean frontPageAggregated = 
            RollerConfig.getBooleanProperty("frontpage.weblog.aggregatedFeeds");
        if (weblog.getHandle().equals(frontPageHandle) && frontPageAggregated) {
            modelsString = RollerConfig.getProperty("rendering.weblogPageModels");
        }
        loadConfiguredPageModels(modelsString, request, map);

        // Feeds get utilities, but that's all
        UtilitiesPageHelper utils = new UtilitiesPageHelper();
        map.put("utils", utils);
        
        // Feeds get weblog's additional custom models too
        if (weblog != null) {
            loadWeblogPageModels(weblog, request, map);
        }
    }
    
    /**
     * Load comma-separated list of configured page models and if any of the
     * models fail to load, throws an exception.
     */
    private static void loadConfiguredPageModels(
            String modelsString, 
            HttpServletRequest request, 
            Map map) throws RollerException {
        String currentModel = null;
        try { // if we can't load a configued page models, then bail out
            String[] models = Utilities.stringToStringArray(modelsString, ",");
            for (int i=0; i<models.length; i++) {
                currentModel = models[i];
                Class modelClass = Class.forName(currentModel);
                PageModel pageModel = (PageModel)modelClass.newInstance();
                Map args = new HashMap();
                args.put("request", request);
                pageModel.init(args);            
                map.put(pageModel.getModelName(), pageModel);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new RollerException("ERROR: can't find page model: " + currentModel);
        } catch (InstantiationException ie) {
            throw new RollerException("ERROR: insantiating page model: " + currentModel);
        } catch (IllegalAccessException iae) {
            throw new RollerException("ERROR: access exception page model: " + currentModel);
        }
    }
    
    /**
     * Load comma-separated list of page models and does not fail if one of the 
     * models fails to load.
     */
    private static void loadWeblogPageModels(
            WebsiteData weblog, 
            HttpServletRequest request, 
            Map map) {
        if (weblog.getPageModels() != null) {
            String[] weblogModels = 
                Utilities.stringToStringArray(weblog.getPageModels(), ",");
            for (int i=0; i<weblogModels.length; i++) {
                try { // don't die just because of one bad custom model
                    Class modelClass = Class.forName(weblogModels[i]);
                    PageModel pageModel = (PageModel)modelClass.newInstance();
                    Map args = new HashMap();
                    args.put("request", request);
                    pageModel.init(args);             
                    map.put(pageModel.getModelName(), pageModel);
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
}
