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

package org.apache.roller.weblogger.pojos;

import org.apache.roller.weblogger.WebloggerException;

import java.util.Date;


/**
 * The Template interface represents the abstract concept of a single unit
 * of templated or non-rendered content.  For Roller we mainly think of
 * templates as Velocity templates which are meant to be fed into the
 * Velocity rendering engine.
 */
public interface Template {
    
    /**
     * The unique identifier for this Template.
     */
    String getId();
    
    
    /**
     * A simple name for this Template.
     */
    String getName();
    
    
    /**
     * A description of the contents of this Template.
     */
    String getDescription();
    
    
    /**
     * The last time the template was modified.
     */
    Date getLastModified();
    
    
    /**
     * The templating language used by this template.
     */

    String getTemplateLanguage();

    /**
     * Set the template language.This is used by template code object to assign
     * correct template language for different template content types
     */
    void setTemplateLanguage(String templateLanguage);
    
    
    /**
     * Content-type of output or null if none defined.
     */
    String getOutputContentType();

    /**
     *
     * type of the template , eg: standard , mobile etc.
     */
    //TODO need to remove this type from weblog template as one template acts in different types

    @Deprecated
    //Moved to templateCode
    String getType();

    /**
     *
     * get the Template rendition object for the given type.
     */
    TemplateRendition getTemplateRendition(String type) throws WebloggerException;
    
}
