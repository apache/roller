/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.business;

import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;

import javax.mail.MessagingException;
import java.util.List;
import java.util.Map;

public interface MailManager {

    /**
     * Sends email to owners and publishers of a blog whenever someone with draft rights
     * submits a blog entry for review.
     *
     * @param entry pending WeblogEntry to review.
     */
    void sendPendingEntryNotice(WeblogEntry entry);

    /**
     * Sends emails to weblogger admins when someone registers for a blog account and the
     * system is configured to require admin approval of new registrants
     *
     * @param user new user account requiring approval before it can be used.
     */
    void sendRegistrationApprovalRequest(User user);

    /**
     * Sends an email to a newly approved user, letting him know he can now log into
     * the weblogger.
     *
     * @param user user whose account was just approved
     */
    void sendRegistrationApprovedNotice(User user);

    /**
     * Sends an email to a newly rejected user, letting him know his account wasn't approved.
     *
     * @param user user whose account was not approved.
     */
    void sendRegistrationRejectedNotice(User user);

    /**
     * Send a user an invitation to join a weblog
     *
     * @param user   user being invited
     * @param weblog weblog being invited to.
     */
    void sendWeblogInvitation(User user, Weblog weblog);

    /**
     * Sends a newly registered user an activation code to confirm the email
     * entered at registration time is one the user has access to.  This step
     * is before the account approval process if the latter has been activated.
     *
     * @param user user to send activation email to.
     */
    void sendUserActivationEmail(User user) throws MessagingException;

    /**
     * Sends email to owners and publishers of a blog whenever a blog reader leaves a comment
     * that needs to be moderated.
     *
     * @param comment  pending comment to review.
     * @param messages any spam or validation issues found, to be placed within the email.
     */
    void sendPendingCommentNotice(WeblogEntryComment comment, Map<String, List<String>> messages);

    /**
     * A list of newly moderated and approved comments, informing the commenter
     * that his comment is now on the weblog.
     *
     * @param comments list of comments
     */
    void sendYourCommentWasApprovedNotifications(List<WeblogEntryComment> comments);

    /**
     * Sends an email to members of a weblog and prior commenters who selected "notify me" whenever
     * a new comment appears (after approval if moderation required for it.)
     *
     * @param comment new comment to announce
     */
    void sendNewCommentNotification(WeblogEntryComment comment);

}
