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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;


/**
 * Stores data for an OAuth accessor
 */
public class OAuthAccessorRecord implements Serializable {
    private String consumerKey;
    private String requestToken;
    private String accessToken;
    private String tokenSecret;
    private Timestamp created;
    private Timestamp updated;
    private Boolean authorized;
    private String userName;

    public OAuthAccessorRecord() {
    }

    /**
     * @return the consumerKey
     */
    public String getConsumerKey() {
        return consumerKey;
    }

    /**
     * @param consumerKey the consumerKey to set
     */
    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    /**
     * @return the requestToken
     */
    public String getRequestToken() {
        return requestToken;
    }

    /**
     * @param requestToken the requestToken to set
     */
    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return the tokenSecret
     */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * @param tokenSecret the tokenSecret to set
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    /**
     * @return the created
     */
    public Timestamp getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Timestamp created) {
        this.created = created;
    }

    /**
     * @return the updated
     */
    public Timestamp getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(this.getConsumerKey());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof OAuthAccessorRecord)) {
            return false;
        }
        OAuthAccessorRecord o = (OAuthAccessorRecord)other;
        return new EqualsBuilder()
            .append(getConsumerKey(), o.getConsumerKey())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getConsumerKey()).toHashCode();
    }

    /**
     * @return the authorized
     */
    public Boolean getAuthorized() {
        return authorized;
    }

    /**
     * @param authorized the authorized to set
     */
    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

}
