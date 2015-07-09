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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Stores data for an OAuth consumer key and secret.
 * There can be up to one for the whole site and up to one per user.
 */
@Entity
@Table(name="roller_oauthconsumer")
@NamedQueries({
        @NamedQuery(name="OAuthConsumerRecord.getByConsumerKey",
                query="SELECT p FROM OAuthConsumerRecord p WHERE p.consumerKey = ?1"),
        @NamedQuery(name="OAuthConsumerRecord.getByUsername",
                query="SELECT p FROM OAuthConsumerRecord p WHERE p.userName = ?1"),
        @NamedQuery(name="OAuthConsumerRecord.getSiteWideConsumer",
                query="SELECT p FROM OAuthConsumerRecord p WHERE p.userName IS NULL")
})
public class OAuthConsumerRecord implements Serializable {
    private String consumerKey;
    private String consumerSecret;
    private String userName;

    public OAuthConsumerRecord() {
    }

    @Id
    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    @Basic(optional = false)
    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return "{" + this.getConsumerKey() + "}";
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof OAuthConsumerRecord)) {
            return false;
        }
        OAuthConsumerRecord o = (OAuthConsumerRecord)other;
        return new EqualsBuilder()
            .append(getConsumerKey(), o.getConsumerKey())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getConsumerKey()).toHashCode();
    }

}
