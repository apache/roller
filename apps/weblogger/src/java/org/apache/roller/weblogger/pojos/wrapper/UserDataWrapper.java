/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.pojos.User;


/**
 * Generated wrapper for class: org.apache.roller.weblogger.pojos.User
 */
public class UserDataWrapper {

    // keep a reference to the wrapped pojo
    private User pojo = null;

    // this is private so that we can force the use of the .wrap(pojo) method
    private UserDataWrapper(User toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static UserDataWrapper wrap(User toWrap) {
        if(toWrap != null)
            return new UserDataWrapper(toWrap);

        return null;
    }

    /**
     * This is here for backwards compatability.  We no longer allow the
     * username to be displayed publicly, so screen name is returned instead.
     */
    public java.lang.String getUserName()
    {   
        return this.pojo.getScreenName();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getScreenName()
    {
        return this.pojo.getScreenName();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getFullName()
    {   
        return this.pojo.getFullName();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getEmailAddress()
    {   
        return this.pojo.getEmailAddress();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.util.Date getDateCreated()
    {   
        return this.pojo.getDateCreated();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getLocale()
    {   
        return this.pojo.getLocale();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getTimeZone()
    {   
        return this.pojo.getTimeZone();
    }

    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public org.apache.roller.weblogger.pojos.User getPojo() {
        return this.pojo;
    }

}
