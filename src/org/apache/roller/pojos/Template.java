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

package org.apache.roller.pojos;

import java.util.Date;


/**
 * The Template interface represents the abstract concept of a single unit
 * of templated or non-rendered content.  For Roller we mainly think of
 * templates as Velocity templates which are meant to be fed into the
 * Velocity rendering engine.
 */
public interface Template {
    
    public static final String ACTION_WEBLOG = "weblog";
    public static final String ACTION_PERMALINK = "permalink";
    public static final String ACTION_SEARCH = "search";
    public static final String ACTION_CUSTOM = "custom";
    
    
    /**
     * The unique identifier for this Template.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getId();
    
    /**
     * The action this template is defined for.
     */
    public String getAction();
    
    /**
     * A simple name for this Template.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getName();
    
    /**
     * A description of the contents of this Template.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDescription();
    
    /**
     * The contents or body of the Template.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getContents();
    
    /**
     * The url link value for this Template.  If this template is not
     * private this is the url that it can be accessed at.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getLink();
    
    /**
     * The last time the template was modified.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getLastModified();
    
    /**
     * Is the Template hidden?  A hidden template cannot be accessed directly.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    
    public boolean isHidden();
    
    /**
     * Is the Template to be included in the navbar?
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean isNavbar();
    
    /**
     * The templating language used by this template.
     */
    public String getTemplateLanguage();
    
    /**
     * The decorator Template to apply.  This returns null if no decorator
     * should be applied.
     */
    public Template getDecorator();
    
    /**
     * Content-type of output or null if none defined.
     */
    public String getOutputContentType();
    
}
