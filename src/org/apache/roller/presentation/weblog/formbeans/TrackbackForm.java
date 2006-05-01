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
/*
 * Created on Apr 14, 2003
 */
package org.apache.roller.presentation.weblog.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * Data to be sent in a Trackback.
 * @author David M Johnson
 * @struts.form name="trackbackForm"
 */
public class TrackbackForm extends ActionForm
{
    private String mTrackbackURL = null;
    private String mEntryId = null;
    
    /**
     * 
     */
    public TrackbackForm()
    {
        super();
    }

    /**
     * @return
     */
    public String getEntryId()
    {
        return mEntryId;
    }

    /**
     * @return
     */
    public String getTrackbackURL()
    {
        return mTrackbackURL;
    }

    /**
     * @param string
     */
    public void setEntryId(String string)
    {
        mEntryId = string;
    }

    /**
     * @param string
     */
    public void setTrackbackURL(String string)
    {
        mTrackbackURL = string;
    }

}
