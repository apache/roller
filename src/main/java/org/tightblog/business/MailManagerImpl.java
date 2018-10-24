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
package org.tightblog.business;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.tightblog.config.DynamicProperties;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.tightblog.repository.UserRepository;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("mailManager")
@EnableConfigurationProperties(DynamicProperties.class)
public class MailManagerImpl implements MailManager {

    private static Logger log = LoggerFactory.getLogger(MailManagerImpl.class);

    private UserManager userManager;
    private UserRepository userRepository;
    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    private URLStrategy urlStrategy;
    private JavaMailSender mailSender;
    private SpringTemplateEngine standardTemplateEngine;
    private JPAPersistenceStrategy persistenceStrategy;
    private MessageSource messages;
    private DynamicProperties dp;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    @Autowired
    public MailManagerImpl(UserManager userManager, UserRepository userRepository, WeblogManager weblogManager,
                           WeblogEntryManager weblogEntryManager, URLStrategy urlStrategy, JavaMailSender mailSender,
                           SpringTemplateEngine standardTemplateEngine, JPAPersistenceStrategy persistenceStrategy,
                           MessageSource messages, DynamicProperties dp) {
        this.userManager = userManager;
        this.userRepository = userRepository;
        this.weblogManager = weblogManager;
        this.weblogEntryManager = weblogEntryManager;
        this.urlStrategy = urlStrategy;
        this.mailSender = mailSender;
        this.standardTemplateEngine = standardTemplateEngine;
        this.persistenceStrategy = persistenceStrategy;
        this.messages = messages;
        this.dp = dp;
    }

    @Override
    public void sendUserActivationEmail(User user) throws MessagingException {
        if (!mailEnabled) {
            return;
        }

        String activationURL = dp.getAbsoluteUrl() + "/tb-ui/app/login?activationCode=" +
                user.getActivationCode();

        Context ctx = new Context();
        ctx.setVariable("emailType", "UserActivation");
        ctx.setVariable("userName", user.getUserName());
        ctx.setVariable("activationURL", activationURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        String subject = messages.getMessage("user.account.activation.mail.subject", null, null);
        String[] to = new String[]{user.getEmailAddress()};
        sendMessage(null, to, null, subject, message);
    }

    @Override
    public void sendRegistrationApprovalRequest(User user) {
        if (!mailEnabled) {
            return;
        }

        String userAdminURL = urlStrategy.getActionURL("userAdmin", "/tb-ui/app/admin",
                null, null);

        // send to blog server admins
        List<User> admins = userRepository.findAdmins();
        List<String> adminEmails = admins.stream().map(User::getEmailAddress).collect(Collectors.toList());
        String[] to = adminEmails.toArray(new String[adminEmails.size()]);

        String subject = messages.getMessage("mailMessage.approveRegistrationSubject",
                new Object[] {user.getScreenName()}, null);

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationApprovalRequest");
        ctx.setVariable("screenName", user.getScreenName());
        ctx.setVariable("emailAddress", user.getEmailAddress());
        ctx.setVariable("userAdminURL", userAdminURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    @Override
    public void sendRegistrationApprovedNotice(User user) {
        if (!mailEnabled) {
            return;
        }

        String[] to = new String[]{user.getEmailAddress()};
        String subject = messages.getMessage("mailMessage.registrationApprovedSubject",
                new Object[] {user.getScreenName()}, null);

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationApprovedNotice");
        ctx.setVariable("screenName", user.getScreenName());
        ctx.setVariable("userName", user.getUserName());
        ctx.setVariable("loginURL", urlStrategy.getLoginURL());
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    @Override
    public void sendRegistrationRejectedNotice(User user) {
        if (!mailEnabled) {
            return;
        }

        String[] to = new String[]{user.getEmailAddress()};
        String subject = messages.getMessage("mailMessage.registrationRejectedSubject", null, null);

        Context ctx = new Context();
        ctx.setVariable("emailType", "RegistrationRejectedNotice");
        ctx.setVariable("screenName", user.getScreenName());
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(null, to, null, subject, message);
    }

    @Override
    public void sendWeblogInvitation(User user, Weblog weblog) {
        if (!mailEnabled) {
            return;
        }

        String from = weblog.getCreator().getEmailAddress();
        String[] to = new String[]{user.getEmailAddress()};
        String subject = messages.getMessage("members.inviteMemberEmailSubject",
                new Object[] {weblog.getName(), weblog.getHandle()}, weblog.getLocaleInstance());

        Context ctx = new Context();
        ctx.setVariable("emailType", "WeblogInvitation");
        ctx.setVariable("weblogName", weblog.getName());
        ctx.setVariable("weblogHandle", weblog.getHandle());
        ctx.setVariable("userName", user.getUserName());
        String loginURL = dp.getAbsoluteUrl() + "/tb-ui/app/home";
        ctx.setVariable("loginURL", loginURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(from, to, new String[]{from}, subject, message);
    }

    @Override
    public void sendPendingEntryNotice(WeblogEntry entry) {
        if (!mailEnabled) {
            return;
        }

        String from = entry.getCreator().getEmailAddress();

        // build list of reviewers (website users with at least publish role)
        List<User> weblogUsers = weblogManager.getWeblogUsers(entry.getWeblog());
        List<String> reviewers = new ArrayList<>();
        weblogUsers.forEach(user -> {
            if (userManager.checkWeblogRole(user, entry.getWeblog(), WeblogRole.POST) &&
                    user.getEmailAddress() != null) {
                reviewers.add(user.getEmailAddress());
            }
        });

        String[] to = reviewers.toArray(new String[reviewers.size()]);
        String subject = messages.getMessage("weblogEntry.pendingEntrySubject",
               new Object[] {entry.getWeblog().getName(), entry.getWeblog().getHandle()},
                entry.getWeblog().getLocaleInstance());

        Context ctx = new Context(entry.getWeblog().getLocaleInstance());
        ctx.setVariable("emailType", "PendingEntryNotice");
        ctx.setVariable("entryTitle", entry.getTitle());
        ctx.setVariable("screenName", entry.getCreator().getScreenName());
        String entryEditURL = urlStrategy.getEntryEditURL(entry);
        ctx.setVariable("editURL", entryEditURL);
        String message = standardTemplateEngine.process("emails/CommonEmailLayout", ctx);

        sendMessage(from, to, new String[]{from}, subject, message);
    }

    @Override
    public void sendPendingCommentNotice(WeblogEntryComment comment, Map<String, List<String>> commentNotes) {
        if (!mailEnabled || comment.isApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        User user = entry.getCreator();

        Context ctx = new Context(weblog.getLocaleInstance());
        ctx.setVariable("comment", comment);
        String commentURL = urlStrategy.getWeblogEntryCommentsURL(entry);
        ctx.setVariable("commentURL", commentURL);
        ctx.setVariable("messages", commentNotes);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bean.entryId", entry.getId());
        String manageURL = urlStrategy.getActionURL("comments", "/tb-ui/app/authoring", weblog, parameters);
        ctx.setVariable("manageURL", manageURL);

        String msg = standardTemplateEngine.process("emails/PendingCommentNotice", ctx);

        String subject = messages.getMessage("email.comment.moderate.title", null, weblog.getLocaleInstance());
        subject += entry.getTitle();

        List<UserWeblogRole> bloggerList = userManager.getWeblogRoles(weblog);

        String[] sendToList = bloggerList.stream()
                .map(UserWeblogRole::getUser)
                .map(User::getEmailAddress)
                .collect(Collectors.toList()).toArray(new String[bloggerList.size()]);

        String from = user.getEmailAddress();

        if (comment.isPending() || weblog.getEmailComments()) {
            sendMessage(from, sendToList, null, subject, msg);
        }
    }

    @Override
    public void sendNewPublishedCommentNotification(WeblogEntryComment comment) {
        if (!mailEnabled ||
                !persistenceStrategy.getWebloggerProperties().isUsersCommentNotifications() ||
                !comment.isApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        User user = entry.getCreator();

        // build list of email addresses to send notification to
        Map<String, String> subscribers = new HashMap<>();

        // Get all the subscribers to this comment thread
        List<WeblogEntryComment> priorComments = weblogEntryManager.getComments(
                CommentSearchCriteria.builder(entry, true, false))
                .stream()
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

        // send message to blog members (same email for everyone)
        if (weblog.getEmailComments() &&
                // if must moderate on, blogger(s) already got pending email, good enough.
                !WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(weblog.getAllowComments())) {
            List<UserWeblogRole> bloggerList = userManager.getWeblogRoles(weblog);

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
        String commentURL = urlStrategy.getWeblogEntryCommentsURL(entry);
        ctx.setVariable("commentURL", commentURL);
        if (subscriber != null) {
            ctx.setVariable("unsubscribeURL", urlStrategy.getCommentNotificationUnsubscribeURL(subscriber.getValue()));
        }
        return ctx;
    }

    @Override
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
        ctx.setVariable("commentURL", urlStrategy.getCommentURL(entry, wec.getTimestamp()));
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
