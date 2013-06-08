/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.business.jpa;

import java.io.IOException; 
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Query;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.OAuthAccessorRecord;
import org.apache.roller.weblogger.pojos.OAuthConsumerRecord;


/**
 * JPA based OAuth manager implementation.
 */
public class JPAOAuthManagerImpl implements OAuthManager {
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    private final OAuthValidator validator;

    /**
     * The logger instance for this class.
     */
    private static Log log = LogFactory
            .getFactory().getInstance(JPAOAuthManagerImpl.class);


    @com.google.inject.Inject
    public JPAOAuthManagerImpl(
            Weblogger roller,
            JPAPersistenceStrategy strategy,
            OAuthValidator validator) {
        this.roller = roller;
        this.strategy = strategy;
        this.validator = validator;
    }
    
    public OAuthServiceProvider getServiceProvider() {
        return new OAuthServiceProvider(
            roller.getUrlStrategy().getOAuthRequestTokenURL(),
            roller.getUrlStrategy().getOAuthAuthorizationURL(),
            roller.getUrlStrategy().getOAuthAccessTokenURL());
    }

    public OAuthValidator getValidator() {
        return validator;
    }

    public OAuthConsumer getConsumer(
            OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {

        OAuthConsumer consumer = null;
        // try to load from local cache if not throw exception
        String consumer_key = requestMessage.getConsumerKey();

        consumer = getConsumerByKey(consumer_key);

        if(consumer == null) {
            OAuthProblemException problem = new OAuthProblemException("token_rejected");
            throw problem;
        }

        return consumer;
    }
    
    /**
     * Get the access token and token secret for the given oauth_token. 
     */
    public OAuthAccessor getAccessor(OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {

        String consumerToken = requestMessage.getToken();
        OAuthAccessor accessor = null;
        if (StringUtils.isNotEmpty(consumerToken)) {
            // caller provided a token, it better be good or else
            accessor = getAccessorByToken(consumerToken);
            if (accessor == null){
                OAuthProblemException problem = new OAuthProblemException("token_expired");
                throw problem;
            }
        }

        String consumerKey = requestMessage.getConsumerKey();
        if (accessor == null && StringUtils.isNotEmpty(consumerKey)) {
            // caller provided contumer key, do we have an accessor yet
            accessor = getAccessorByKey(consumerKey);
        }
        return accessor;
    }

    /**
     * Set the access token 
     */
    public void markAsAuthorized(OAuthAccessor accessor, String userId)
            throws OAuthException {
        try {
            OAuthAccessorRecord record = (OAuthAccessorRecord) strategy.load(
                OAuthAccessorRecord.class, accessor.consumer.consumerKey);
            record.setUserName(userId);
            record.setAuthorized(Boolean.TRUE);
            strategy.store(record);
            
        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR: setting authorization flag", ex);
        }
    }

    /**
     * Generate a fresh request token and secret for a consumer.
     * @throws OAuthException
     */
    public void generateRequestToken(
            OAuthAccessor accessor)
            throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.consumerKey;
        // generate token and secret based on consumer_key

        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        String secret_data = consumer_key + System.nanoTime() + token;
        String secret = DigestUtils.md5Hex(secret_data);

        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;

        // add to the local cache
        addAccessor(accessor);
    }
    
    /**
     * Generate a fresh request token and secret for a consumer.
     * @throws OAuthException
     */
    public void generateAccessToken(OAuthAccessor accessor)
            throws OAuthException {

        try {
            // generate oauth_token and oauth_secret
            // generate token and secret based on consumer_key
            String consumer_key = (String) accessor.consumer.consumerKey;

            OAuthAccessorRecord record = (OAuthAccessorRecord) strategy.load(
                OAuthAccessorRecord.class, accessor.consumer.consumerKey);
            
            // for now use md5 of name + current time as token
            String token_data = consumer_key + System.nanoTime();
            String token = DigestUtils.md5Hex(token_data);

            record.setRequestToken(null);
            record.setAccessToken(token);
            strategy.store(record);

        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR: generating access token", ex);
        }
    }

    public OAuthConsumer addConsumer(String username, String consumerKey) throws OAuthException {

        OAuthConsumerRecord record = new OAuthConsumerRecord();
        record.setConsumerKey(consumerKey);
        record.setUserName(username);
        record.setConsumerSecret(UUID.randomUUID().toString());

        try {
            strategy.store(record);
        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR storing accessor", ex);
        }
        
        OAuthConsumer consumer = new OAuthConsumer(
            null,
            record.getConsumerKey(),
            record.getConsumerSecret(),
            getServiceProvider());

        return consumer;
    }

    public OAuthConsumer addConsumer(String consumerKey) 
            throws OAuthException, WebloggerException {
        if (getConsumer() == null) {
            return addConsumer(null, consumerKey);
        } else {
            throw new OAuthException("ERROR: cannot have more than one site-wide consumer");
        }
    }

    public OAuthConsumer getConsumer() throws WebloggerException {
        OAuthConsumerRecord record = null;
        try {
            Query q = strategy.getNamedQuery("OAuthConsumerRecord.getSiteWideConsumer");
            record = (OAuthConsumerRecord)q.getSingleResult();

        } catch (Throwable ex) {
            log.debug("ERROR fetching site-wide consumer", ex);
        }
        if (record != null) {
            OAuthConsumer consumer = new OAuthConsumer(
                null,
                record.getConsumerKey(),
                record.getConsumerSecret(),
                getServiceProvider());
            return consumer;
        }
        return null;
    }

    public OAuthConsumer getConsumerByUsername(String username) throws WebloggerException {
        OAuthConsumerRecord record = null;
        try {
            Query q = strategy.getNamedQuery("OAuthConsumerRecord.getByUsername");
            q.setParameter(1, username);
            record = (OAuthConsumerRecord)q.getSingleResult();

        } catch (Throwable ex) {
            log.debug("ERROR fetching consumer", ex);
        }
        if (record != null) {
            OAuthConsumer consumer = new OAuthConsumer(
                null,
                record.getConsumerKey(),
                record.getConsumerSecret(),
                getServiceProvider());
            consumer.setProperty("userName", record.getUserName());
            return consumer;
        }
        return null;
    }

    
    //--------------------------------------------- package protected internals

    OAuthConsumer consumerFromRecord(OAuthConsumerRecord record) {
        OAuthConsumer consumer = null;
        if (record != null) {
            consumer = new OAuthConsumer(
                null,
                record.getConsumerKey(),
                record.getConsumerSecret(),
                getServiceProvider());
            if (record.getUserName() != null) {
                consumer.setProperty("userId", record.getUserName());
            }
        }
        return consumer;
    }

    OAuthAccessor accessorFromRecord(OAuthAccessorRecord record) {
        OAuthAccessor accessor = null;
        if (record != null) {
            accessor =
                new OAuthAccessor(getConsumerByKey(record.getConsumerKey()));
            accessor.accessToken = record.getAccessToken();
            accessor.requestToken = record.getRequestToken();
            accessor.tokenSecret = record.getTokenSecret();
            if (record.getAuthorized() != null) {
                accessor.setProperty("authorized", record.getAuthorized());
            }
            if (record.getUserName() != null) {
                accessor.setProperty("userId", record.getUserName());
            }
        }
        return accessor;
    }

    OAuthConsumer getConsumerByKey(String consumerKey) {
        OAuthConsumerRecord record = null;
        try {
            Query q = strategy.getNamedQuery("OAuthConsumerRecord.getByConsumerKey");
            q.setParameter(1, consumerKey);
            record = (OAuthConsumerRecord)q.getSingleResult();

        } catch (Throwable ex) {
            log.debug("ERROR fetching consumer", ex);
        }
        return consumerFromRecord(record);
    }

    void addAccessor(OAuthAccessor accessor) throws OAuthException {

        OAuthAccessorRecord record = new OAuthAccessorRecord();
        record.setConsumerKey(accessor.consumer.consumerKey);
        record.setRequestToken(accessor.requestToken);
        record.setAccessToken(accessor.accessToken);
        record.setTokenSecret(accessor.tokenSecret);
        if (accessor.getProperty("userId") != null) {
            record.setUserName((String)accessor.getProperty("userId"));
        }

        if (record.getCreated() != null) {
            record.setCreated(record.getCreated());
        } else {
            record.setCreated(new Timestamp(new Date().getTime()));
        }
        
        if (record.getUpdated() != null) {
            record.setUpdated(record.getUpdated());
        } else {
            record.setUpdated(record.getCreated());
        }

        if (accessor.getProperty("authorized") != null) {
            record.setAuthorized((Boolean)accessor.getProperty("authorized"));
        }
        try {
            strategy.store(record);
        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR storing accessor", ex);
        }
    }

    OAuthAccessor getAccessorByKey(String consumerKey) {
        OAuthAccessorRecord record = null;
        try {
            Query q = strategy.getNamedQuery("OAuthAccessorRecord.getByKey");
            q.setParameter(1, consumerKey);
            record = (OAuthAccessorRecord)q.getSingleResult();

        } catch (Throwable ex) {
            log.debug("ERROR fetching accessor", ex);
        }
        return accessorFromRecord(record);
    }

    OAuthAccessor getAccessorByToken(String token) {
        OAuthAccessorRecord record = null;
        try {
            Query q = strategy.getNamedQuery("OAuthAccessorRecord.getByToken");
            q.setParameter(1, token);
            record = (OAuthAccessorRecord)q.getSingleResult();

        } catch (Throwable ex) {
            log.debug("ERROR fetching accessor", ex);
        }
        return accessorFromRecord(record);
    }

    void removeConsumer(OAuthConsumer consumer) throws OAuthException {
        try {
            strategy.remove(OAuthConsumerRecord.class, consumer.consumerKey);
        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR removing consumer", ex);
        }
    }

    void removeAccessor(OAuthAccessor accessor) throws OAuthException {
        try {
            strategy.remove(OAuthAccessorRecord.class, accessor.consumer.consumerKey);
        } catch (WebloggerException ex) {
            throw new OAuthException("ERROR removing accessor", ex);
        }
    }
}
