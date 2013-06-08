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
 * IncomingReferrer.java
 *
 * Created on December 20, 2005, 3:39 PM
 */

package org.apache.roller.weblogger.business.referrers;

/**
 * Represents an incoming (unprocessed) referrer.
 *
 * @author Allen Gilliland
 */
public class IncomingReferrer {
    
    private String referrerUrl = null;
    private String requestUrl = null;
    private String weblogHandle = null;
    private String weblogAnchor = null;
    private String weblogDateString = null;
    
    
    public IncomingReferrer() {}

    public String getReferrerUrl() {
        return referrerUrl;
    }

    public void setReferrerUrl(String referrerUrl) {
        this.referrerUrl = referrerUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public void setWeblogAnchor(String weblogAnchor) {
        this.weblogAnchor = weblogAnchor;
    }

    public String getWeblogDateString() {
        return weblogDateString;
    }

    public void setWeblogDateString(String weblogDateString) {
        this.weblogDateString = weblogDateString;
    }
    
}
