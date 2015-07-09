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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Stores data for an OAuth accessor
 */
@Entity
@Table(name="roller_oauthaccessor")
@NamedQueries({
        @NamedQuery(name="OAuthAccessorRecord.getByKey",
                query="SELECT p FROM OAuthAccessorRecord p WHERE p.consumerKey = ?1"),
        @NamedQuery(name="OAuthAccessorRecord.getByToken",
                query="SELECT p FROM OAuthAccessorRecord p WHERE p.requestToken = ?1 OR p.accessToken = ?1")
})
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

    @Id
    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public void setRequestToken(String requestToken) {
        this.requestToken = requestToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    @Basic(optional=false)
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Basic(optional=false)
    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    public Boolean getAuthorized() {
        return authorized;
    }

    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

}
