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

package org.apache.roller.scripting;

import java.util.Arrays;
import java.util.List;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.Template;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererFactory;
import org.apache.roller.util.Utilities;

/**
 * RendererFactory that calls BSF scripting language.
 */
public class BSFRendererFactory implements RendererFactory {   
    private static String[] names = {"groovy", "jruby", "javascript"};
    private static final List bsfLanguages; 
    static {
        String value = RollerConfig.getProperty("bsfrenderer.languageNames");
        if (value != null) {
            names = Utilities.stringToStringArray(value,",");            
        } 
        bsfLanguages = Arrays.asList(names);
    }        
    public Renderer getRenderer(Template template) {        
        Renderer renderer = null;
        if(template.getTemplateLanguage() == null || template.getId() == null) {
            return null;
        }        
        if(bsfLanguages.contains(template.getTemplateLanguage())) {             
            try {
               renderer = new BSFRenderer(template);
            } catch(Exception ex) {
                return null;
            }                        
        }      
        return renderer;
    }    
}
