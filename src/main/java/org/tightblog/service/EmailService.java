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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.User;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.UserWeblogRoleDao;
import org.tightblog.dao.WeblogEntryCommentDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(DynamicProperties.class)
public class EmailService {

    private static Logger log = LoggerFactory.getLogger(EmailService.class);

    private UserManager userManager;
    private UserDao userDao;
    private UserWeblogRoleDao userWeblogRoleDao;
    private WeblogManager weblogManager;
    private WeblogEntryCommentDao weblogEntryCommentDao;
    private URLService urlService;
    private JavaMailSender mailSender;
    private SpringTemplateEngine standardTemplateEngine;
    private MessageSource messages;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private DynamicProperties dp;
    private boolean mailEnabled;

    @Autowired
    public EmailService(UserManager userManager, UserDao userDao,
                        UserWeblogRoleDao userWeblogRoleDao, WeblogManager weblogManager,
                        URLService urlService, JavaMailSender mailSender,
                        SpringTemplateEngine standardTemplateEngine, MessageSource messages, DynamicProperties dp,
                        WebloggerPropertiesDao webloggerPropertiesDao,
                        WeblogEntryCommentDao weblogEntryCommentDao,
                        @Value("${mail.enabled:false}") boolean mailEnabled) {
        this.userManager = userManager;
        this.userDao = userDao;
        this.userWeblogRoleDao = userWeblogRoleDao;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.weblogManager = weblogManager;
        this.weblogEntryCommentDao = weblogEntryCommentDao;
        this.urlService = urlService;
        this.mailSender = mailSender;
        this.standardTemplateEngine = standardTemplateEngine;
        this.messages = messages;
        this.dp = dp;
        this.mailEnabled = mailEnabled;
    }

    /**
     * Sends a newly registered user an activation code to confirm the email
     * entered at registration time is one the user has access to.  This step
     * is before the account approval process if the latter has been activated.
     *
     * @param user user to send activation email to.
     */
    public void sendUserActivationEmail(User user) {
        if (!mailEnabled) {
            log.warn("Cannot send user activation email to {} because mail.enabled=false; either enable" +
                    " or have a blog server admin activate account from User Admin page.", user);
            return;
        }

        String activationURL = dp.getAbsoluteUrl() + "/tb-ui/app/login?activationCode=" +
                user.getActivationCode();

        Context ctx = new Context();
        ctx.setVariable("emailType", "UserActivation");
        ctx.setVariable("userName", user.getUserName());
        ctx.setVariable("activationURL", activationURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        String subject = messages.getMessage("user.account.activation.mail.subject", null, Locale.getDefault());
        String[] to = new String[]{user.getEmailAddress()};
        sendMessage(null, to, null, subject, message);
    }

    /**
     * Sends emails to weblogger admins when someone registers for a blog account and the
     * system is configured to require admin approval of new registrants
     *
     * @param user new user account requiring approval before it can be used.
     */
    public void sendRegistrationApprovalRequest(User user) {
        if (!mailEnabled) {
            return;
        }

        String userAdminURL = urlService.getActionURL("/tb-ui/app/admin", "userAdmin",
                null, null);

        // send to blog server admins
        List<User> admins = userDao.findAdmins();
        String[] to = admins.stream().map(User::getEmailAddress).toArray(String[]::new);

        String subject = messages.getMessage("mailMessage.approveRegistrationSubject",
                new Object[] {user.getScreenName()}, Locale.getDefault());

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationApprovalRequest");
        ctx.setVariable("screenName", user.getScreenName());
        ctx.setVariable("emailAddress", user.getEmailAddress());
        ctx.setVariable("userAdminURL", userAdminURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    /**
     * Sends an email to a newly approved user, letting him know he can now log into
     * the weblogger.
     *
     * @param user user whose account was just approved
     */
    public void sendRegistrationApprovedNotice(User user) {
        if (!mailEnabled) {
            return;
        }

        String[] to = new String[]{user.getEmailAddress()};
        String subject = messages.getMessage("mailMessage.registrationApprovedSubject",
                new Object[] {user.getScreenName()}, Locale.getDefault());

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationApprovedNotice");
        ctx.setVariable("screenName", user.getScreenName());
        ctx.setVariable("userName", user.getUserName());
        ctx.setVariable("loginURL", urlService.getLoginURL());
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    /**
     * Sends an email to a newly rejected user, letting him know his account wasn't approved.
     *
     * @param user user whose account was not approved.
     */
    public void sendRegistrationRejectedNotice(User user) {
        if (!mailEnabled) {
            return;
        }

        String[] to = new String[]{user.getEmailAddress()};
        String subject = messages.getMessage("mailMessage.registrationRejectedSubject", null, Locale.getDefault());

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationRejectedNotice");
        ctx.setVariable("screenName", user.getScreenName());
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    /**
     * Sends email to owners and publishers of a blog whenever someone with draft rights
     * submits a blog entry for review.
     *
     * @param entry pending WeblogEntry to review.
     */
    public void sendPendingEntryNotice(WeblogEntry entry) {
        if (!mailEnabled) {
            return;
        }

        String from = entry.getCreator().getEmailAddress();

        // build list of reviewers (website users with at least publish role)
        List<User> weblogUsers = userWeblogRoleDao.findByWeblogAndStatusEnabled(entry.getWeblog());
        List<String> reviewers = new ArrayList<>();
        weblogUsers.forEach(user -> {
            if (userManager.checkWeblogRole(user, entry.getWeblog(), WeblogRole.POST) &&
                    user.getEmailAddress() != null) {
                reviewers.add(user.getEmailAddress());
            }
        });

        String[] to = reviewers.toArray(new String[0]);
        String subject = messages.getMessage("weblogEntry.pendingEntrySubject",
               new Object[] {entry.getWeblog().getName(), entry.getWeblog().getHandle()},
                entry.getWeblog().getLocaleInstance());

        Context ctx = new Context(entry.getWeblog().getLocaleInstance());
        ctx.setVariable("emailType", "PendingEntryNotice");
        ctx.setVariable("entryTitle", entry.getTitle());
        ctx.setVariable("screenName", entry.getCreator().getScreenName());
        String entryEditURL = urlService.getEntryEditURL(entry);
        ctx.setVariable("editURL", entryEditURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(from, to, new String[]{from}, subject, message);
    }

    /**
     * Sends email to owners and publishers of a blog whenever a blog reader leaves a comment
     * that needs to be moderated.
     *
     * @param comment  pending comment to review.
     * @param commentNotes any spam or validation issues found, to be placed within the email.
     */
    public void sendPendingCommentNotice(WeblogEntryComment comment, Map<String, List<String>> commentNotes) {
        if (!mailEnabled || comment.isApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        User user = entry.getCreator();

        Context ctx = new Context(weblog.getLocaleInstance());
        ctx.setVariable("comment", comment);
        String commentURL = urlService.getWeblogEntryCommentsURL(entry);
        ctx.setVariable("commentURL", commentURL);
        ctx.setVariable("messages", commentNotes);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bean.entryId", entry.getId());
        String manageURL = urlService.getActionURL("/tb-ui/app/authoring", "comments", weblog, parameters);
        ctx.setVariable("manageURL", manageURL);

        String homeURL = urlService.getActionURL("/tb-ui/app", "home", null, null);
        ctx.setVariable("homeURL", homeURL);

        String msg = standardTemplateEngine.process("emails/PendingCommentNotice", ctx);

        String subject = messages.getMessage("email.comment.moderate.title", null, weblog.getLocaleInstance());
        subject = String.format("%s: %s", subject, entry.getTitle());

        List<UserWeblogRole> bloggerList = userWeblogRoleDao.findByWeblogAndEmailCommentsTrue(weblog);

        String[] sendToList = bloggerList.stream()
                .map(UserWeblogRole::getUser)
                .map(User::getEmailAddress)
                .collect(Collectors.toList()).toArray(new String[bloggerList.size()]);

        String from = user.getEmailAddress();
        sendMessage(from, sendToList, null, subject, msg);
    }

    /**
     * Sends an email to members of a weblog and prior commenters who selected "notify me" whenever
     * a new comment appears (after approval if moderation required for it.)
     *
     * @param comment new comment to announce
     */
    public void sendNewPublishedCommentNotification(WeblogEntryComment comment) {
        if (!mailEnabled ||
                !webloggerPropertiesDao.findOrNull().isUsersCommentNotifications() ||
                !comment.isApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        User user = entry.getCreator();

        // build list of email addresses to send notification to
        Map<String, String> subscribers = new HashMap<>();

        // Get all the subscribers to this comment thread
        List<WeblogEntryComment> priorComments =
                weblogEntryCommentDao.findByWeblogEntryAndStatusApproved(entry).stream()
                // don't send a routing email to the person who made the comment.
                .filter(pc -> !comment.getEmail().equalsIgnoreCase(pc.getEmail()))
                .collect(Collectors.toList());

        for (WeblogEntryComment priorComment : priorComments) {
            // if user has commented twice, count the most recent notify setting
            if (priorComment.getNotify()) {
                log.info("Add to subscribers list: {}", priorComment.getEmail());
                subscribers.put(priorComment.getEmail().toLowerCase(), priorComment.getId());
            } else {
                // remove user who doesn't want to be notified
                log.info("Remove from subscribers list: {}", priorComment.getEmail());
                subscribers.remove(priorComment.getEmail().toLowerCase());
            }
        }

        String from = user.getEmailAddress();

        String subject = messages.getMessage("email.comment.title", new Object[] {entry.getTitle()},
                weblog.getLocaleInstance());

        // Send notification to blog's members?
        // if email was moderated, blogger(s) already got pending email, good enough.

        boolean commenterIsPublisher =
                userManager.checkWeblogRole(comment.getEmail(), comment.getWeblog(), WeblogRole.POST);

        boolean commenterIsNonPublisherUser = !commenterIsPublisher &&
                userDao.findEnabledByUserName(comment.getEmail()) != null;

        // MODERATE_NONAUTH = some non-blog publishers may have sent non-moderated comments, so send email
        if (commenterIsNonPublisherUser && CommentPolicy.MODERATE_NONAUTH.equals(weblog.getAllowComments())) {
            List<UserWeblogRole> bloggerList = userWeblogRoleDao.findByWeblogAndEmailCommentsTrue(weblog);

            Context ctx = getPublishedCommentNotificationContext(comment, null);
            String msg = standardTemplateEngine.process("emails/NewCommentNotification", ctx);

            String[] bloggerEmailAddrs = bloggerList.stream()
                    .map(UserWeblogRole::getUser)
                    .map(User::getEmailAddress)
                    .collect(Collectors.toList()).toArray(new String[bloggerList.size()]);

            sendMessage(from, bloggerEmailAddrs, null, subject, msg);
        }

        // now send to subscribers (different email each with different unsubscribe link)
        if (subscribers.size() > 0) {
            for (Map.Entry<String, String> subscriber : subscribers.entrySet()) {
                Context ctx = getPublishedCommentNotificationContext(comment, subscriber);
                String msg = standardTemplateEngine.process("emails/NewCommentNotification", ctx);
                sendMessage(from, null, new String[]{subscriber.getKey()}, subject, msg);
            }
        }
    }

    private Context getPublishedCommentNotificationContext(WeblogEntryComment comment, Map.Entry<String, String> subscriber) {
        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();

        Context ctx = new Context(weblog.getLocaleInstance());
        ctx.setVariable("comment", comment);
        String commentURL = urlService.getWeblogEntryCommentsURL(entry);
        ctx.setVariable("commentURL", commentURL);
        if (subscriber != null) {
            ctx.setVariable("unsubscribeURL", urlService.getCommentNotificationUnsubscribeURL(subscriber.getValue()));
        }
        return ctx;
    }

    /**
     * A list of newly moderated and approved comments, informing the commenter
     * that his comment is now on the weblog.
     *
     * @param comments list of comments
     */
    public void sendYourCommentWasApprovedNotifications(List<WeblogEntryComment> comments) {
        if (!mailEnabled || comments == null || comments.size() < 1) {
            return;
        }

        for (WeblogEntryComment comment : comments) {
            // Send email notifications because a new comment has been approved
            sendNewPublishedCommentNotification(comment);
            // Send approval notification to author of approved comment
            sendYourCommentWasApprovedNotification(comment);
        }
    }

    private void sendYourCommentWasApprovedNotification(WeblogEntryComment wec) {
        WeblogEntry entry = wec.getWeblogEntry();

        Context ctx = new Context();
        ctx.setVariable("emailType", "CommentApproved");
        ctx.setVariable("commentURL", urlService.getCommentURL(entry, wec.getTimestamp()));
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        // send message to author of approved comment
        String subject = messages.getMessage("email.comment.commentApproved", null,
                entry.getWeblog().getLocaleInstance());
        sendMessage(null, new String[]{wec.getEmail()}, null, subject, message);
    }

    /**
     * This method is used to send an HTML Message
     *
     * @param from    e-mail address of sender
     * @param to      e-mail address(es) of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     */
    private void sendMessage(String from, String[] to, String[] cc, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // n.b. any default from address is expected to be determined by caller.
            if (!StringUtils.isEmpty(from)) {
                InternetAddress sentFrom = new InternetAddress(from);
                message.setFrom(sentFrom);
                log.debug("e-mail from: {}", sentFrom);
            }

            if (to != null) {
                InternetAddress[] sendTo = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    sendTo[i] = new InternetAddress(to[i]);
                    log.debug("sending e-mail to: {}", to[i]);
                }
                message.setRecipients(Message.RecipientType.TO, sendTo);
            }

            if (cc != null) {
                InternetAddress[] copyTo = new InternetAddress[cc.length];
                for (int i = 0; i < cc.length; i++) {
                    copyTo[i] = new InternetAddress(cc[i]);
                    log.debug("copying e-mail to: {}", cc[i]);
                }
                message.setRecipients(Message.RecipientType.CC, copyTo);
            }

            message.setSubject((subject == null) ? "(no subject)" : subject, "UTF-8");
            message.setContent(content, "text/html; charset=utf-8");
            message.setSentDate(new java.util.Date());

            // First collect all the addresses together.
            boolean bFailedToSome = false;
            SendFailedException sendex = new SendFailedException("Unable to send message to some recipients");

            try {
                // Send to the list of remaining addresses, ignoring the addresses attached to the message
                mailSender.send(message);
            } catch (MailAuthenticationException | MailSendException ex) {
                bFailedToSome = true;
                sendex.setNextException(ex);
            }

            if (bFailedToSome) {
                throw sendex;
            }
        } catch (MessagingException e) {
            log.error("ERROR: Problem sending email with subject {}", subject, e);
        }
    }
}
