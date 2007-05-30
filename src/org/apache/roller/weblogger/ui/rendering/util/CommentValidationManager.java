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

package org.apache.roller.weblogger.ui.rendering.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.ui.rendering.servlets.CommentServlet;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Responsible for loading validators and using them to validate comments.
 */
public class CommentValidationManager {
    private static Log     log = LogFactory.getLog(CommentValidationManager.class);
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");
    private List           validators = new ArrayList();
    
    public CommentValidationManager() {
        
        // instantiate the validators that are configured
        try {
            String vals = RollerConfig.getProperty("comment.validator.classnames");
            String[] valsarray = Utilities.stringToStringArray(vals, ",");
            for(int i=0; i < valsarray.length; i++) {
                try {
                    Class valClass = Class.forName(valsarray[i]);
                    CommentValidator val = (CommentValidator)valClass.newInstance();
                    validators.add(val);
                    log.info("Configured CommentValidator: " + val.getName() + " / " + valClass.getName());
                } catch (ClassNotFoundException cnfe) {
                    log.warn("Error finding comment validator: " + valsarray[i]);
                } catch (InstantiationException ie) {
                    log.warn("Error insantiating comment validator: " + valsarray[i]);
                } catch (IllegalAccessException iae) {
                    log.warn("Error accessing comment validator: " + valsarray[i]);
                }
            }
                        
        } catch (Exception e) {
            log.error("Error instantiating comment validators");
        }
        log.info("Configured " + validators.size() + " CommentValidators");
    }
    
    /**
     * Add validator to those managed by this manager (testing purposes).
     */
    public void addCommentValidator(CommentValidator val) {
        validators.add(val);
    }
    
    /**
     * Return total number of validators (for teasting purposes).
     */
    public int getValidatorCount() {
        return validators.size();
    }
    
    /**
     * @param comment Comment to be validated
     * @param messages Messages object to which errors will be added
     * @return Number indicating confidence that comment is valid (100 meaning 100%)
     */
    public int validateComment(WeblogEntryComment comment, RollerMessages messages) {
        int total = 0;
        if (validators.size() > 0) {
            for (Iterator it = validators.iterator(); it.hasNext();) {
                CommentValidator val = (CommentValidator) it.next();
                log.debug("Invoking comment validator "+val.getName());
                total += val.validate(comment, messages);
            }
            total = total / validators.size();
        } else {
            // When no validators: consider all comments valid
            total = 100;
        }
        return total;
    }
    
}
