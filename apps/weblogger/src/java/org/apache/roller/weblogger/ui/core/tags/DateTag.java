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
package org.apache.roller.weblogger.ui.core.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

/**
 * Struts-based date field tag that wraps Matt Kruze's JavaScript data chooser.
 * @jsp.tag name="Date"
 */
public class DateTag extends TagSupport {
    static final long serialVersionUID = 1485100916981692535L;
    
    // Unique key prefix keeps us from colliding with other tags and user data
    public static final String KEY_PREFIX = "ZZZ_DATETAG_ZZZ";
    
    private String property = null;
    private String dateFormat = null;
    private Boolean readOnly = Boolean.FALSE;
    private String formName = null;
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(DateTag.class);
    
    /**
     * Renders date field by calling a JSP page.
     */
    public int doStartTag() throws JspException {
        
        // Get form name
        ActionMapping mapping =
                (ActionMapping) pageContext.getRequest().getAttribute(
                Globals.MAPPING_KEY);
        if (formName == null) {
            formName = mapping.getName();
        }
        
        // Get value of form field
        Object value =
                RequestUtils.lookup(pageContext, formName, property, null);
        if (value == null)
            value = "";
        
        // put variables into request scope for view page
        pageContext.getRequest().setAttribute(
                KEY_PREFIX + "_formName",
                formName);
        pageContext.getRequest().setAttribute(
                KEY_PREFIX + "_property",
                property);
        pageContext.getRequest().setAttribute(
                KEY_PREFIX + "_dateFormat",
                dateFormat);
        pageContext.getRequest().setAttribute(
                KEY_PREFIX + "_readOnly",
                readOnly);
        pageContext.getRequest().setAttribute(KEY_PREFIX + "_value", value);
        
        // dispatch to view page
        try {
            pageContext.include("/roller-ui/widgets/date.jsp");
        } catch (Exception e) {
            // can't handle this here
            throw new JspException("ERROR including date.jsp");
        }
        
        // Don't evaluate content of tag, just continue processing this page
        return (SKIP_BODY);
    }
    
    /**
     * Date format string to be used.
     *
     * @jsp.attribute required="true" rtexprvalue="true" type="java.lang.String"
     */
    public String getDateFormat() {
        return dateFormat;
    }
    
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Name of form property represented.
     * @jsp.attribute required="true" rtexprvalue="true" type="java.lang.String"
     */
    public String getProperty() {
        return property;
    }
    
    public void setProperty(String property) {
        this.property = property;
    }
    
    /**
     * True if field should be readOnly.
     * @jsp.attribute required="false" rtexprvalue="true" type="java.lang.Boolean"
     */
    public Boolean getReadOnly() {
        return readOnly;
    }
        
    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }
    
    /**
     * Form name, only needed when more than one form on page.
     * @jsp.attribute required="false" rtexprvalue="true" type="java.lang.String"
     */
    public String getFormName() {
        return formName;
    }
        
    public void setFormName(String formName) {
        this.formName = formName;
    }
    
}
