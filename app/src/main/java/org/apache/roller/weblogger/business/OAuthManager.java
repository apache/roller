/*
 * Copyright 2007 AOL, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.business;

import java.io.IOException;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.OAuthValidator;
import org.apache.roller.weblogger.WebloggerException;


/**
 * Management of and access to configured and persistent OAuth objects.
 */
public interface OAuthManager {

    /**
     * Get validator to be used to OAuth validate messages.
     */
    OAuthServiceProvider getServiceProvider();

    /**
     * Get validator to be used to OAuth validate messages.
     */
    OAuthValidator getValidator();

    /**
     * Get the site-wide consumer.
     */
    OAuthConsumer getConsumer()
            throws WebloggerException;

    /**
     * Get consumer corresponding to request.
     */
    OAuthConsumer getConsumerByUsername(String username)
            throws WebloggerException;

    /**
     * Get consumer corresponding to request.
     */
    OAuthConsumer getConsumer(
            OAuthMessage requestMessage)
            throws IOException, OAuthProblemException;

    /**
     * Add a site-wide consumer provided a key, there can only be one.
     */
    OAuthConsumer addConsumer(String consumerKey)
            throws OAuthException, WebloggerException;

    /**
     * Store a new consumer for specified user, each user can have only one.
     */
    OAuthConsumer addConsumer(String username, String consumerKey)
            throws OAuthException;

    /**
     * Get the access token and token secret for the given request.
     * If token is provided, it better be good; otherwise exception.
     * If consumer key is provided and no accessor exists, will return null.
     *
     * @param requestMessage Request with token or consumer key
     * @return Accessor or null if consumer key does not have a token yet
     * @throws OAuthProblemException If provided token is bad
     * @throws java.io.IOException on IO error
     */
    OAuthAccessor getAccessor(OAuthMessage requestMessage)
            throws IOException, OAuthProblemException;

    /**
     * Set the access token
     *
     * @deprecated Records approval against the consumer key alone, without
     *             naming the request token being approved and without
     *             requiring that it is still pending. Use
     *             {@link #authorizeRequestToken(String, String, String)},
     *             which does both in one statement. No longer called from
     *             Roller; retained for callers outside the project.
     */
    @Deprecated
    void markAsAuthorized(OAuthAccessor accessor, String userId)
            throws OAuthException;

    /**
     * Record a user's approval of one pending request token.
     *
     * <p>The whole transition happens in a single conditional statement: the
     * record is claimed only if it still matches the consumer key and the
     * exact request token and has not yet been exchanged for an access token.
     * A pending token already approved by the same user is accepted as an
     * idempotent retry; a different user cannot claim it. The transition stays
     * in one statement, without a read-then-write window.
     *
     * @param consumerKey  key of the consumer the token was issued to
     * @param requestToken the pending request token being approved
     * @param userName     the approving user
     * @return true if this call performed the approval or confirmed the same
     *         user's prior approval; false if the record did not match, belongs
     *         to another user, or was already exchanged. Callers should not
     *         distinguish these cases to the client.
     * @throws OAuthException on persistence failure
     */
    boolean authorizeRequestToken(String consumerKey, String requestToken, String userName)
            throws OAuthException;

    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    void generateRequestToken(
            OAuthAccessor accessor)
            throws OAuthException;
    
    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    void generateAccessToken(OAuthAccessor accessor)
            throws OAuthException;
}
