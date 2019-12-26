package org.tightblog.domain;

import org.tightblog.util.HTMLSanitizer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "weblogger_properties")
public class WebloggerProperties {

    private String id;
    private int databaseVersion;
    private Weblog mainBlog;
    private RegistrationPolicy registrationPolicy;
    private boolean usersCreateBlogs;
    private HTMLSanitizer.Level blogHtmlPolicy;
    private boolean usersCustomizeThemes;
    private String defaultAnalyticsCode;
    private boolean usersOverrideAnalyticsCode;
    private CommentPolicy commentPolicy;
    private SpamPolicy spamPolicy;
    private HTMLSanitizer.Level commentHtmlPolicy;
    private boolean usersCommentNotifications;
    private String globalSpamFilter;
    private int maxFileUploadsSizeMb;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // see application.properties file, tightblog.database.expected.version value for explanation
    @Column(name = "database_version")
    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    @ManyToOne
    @JoinColumn(name = "main_blog_id", nullable = true)
    public Weblog getMainBlog() {
        return mainBlog;
    }

    public void setMainBlog(Weblog mainBlog) {
        this.mainBlog = mainBlog;
    }

    @Column(name = "registration_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public RegistrationPolicy getRegistrationPolicy() {
        return registrationPolicy;
    }

    public void setRegistrationPolicy(RegistrationPolicy registrationPolicy) {
        this.registrationPolicy = registrationPolicy;
    }

    @Column(name = "users_create_blogs")
    public boolean isUsersCreateBlogs() {
        return usersCreateBlogs;
    }

    public void setUsersCreateBlogs(boolean usersCreateBlogs) {
        this.usersCreateBlogs = usersCreateBlogs;
    }

    @Column(name = "blog_html_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public HTMLSanitizer.Level getBlogHtmlPolicy() {
        return blogHtmlPolicy;
    }

    public void setBlogHtmlPolicy(HTMLSanitizer.Level blogHtmlPolicy) {
        this.blogHtmlPolicy = blogHtmlPolicy;
    }

    @Column(name = "users_customize_themes")
    public boolean isUsersCustomizeThemes() {
        return usersCustomizeThemes;
    }

    public void setUsersCustomizeThemes(boolean usersCustomizeThemes) {
        this.usersCustomizeThemes = usersCustomizeThemes;
    }

    @Column(name = "default_analytics_code")
    public String getDefaultAnalyticsCode() {
        return defaultAnalyticsCode;
    }

    public void setDefaultAnalyticsCode(String defaultAnalyticsCode) {
        this.defaultAnalyticsCode = defaultAnalyticsCode;
    }

    @Column(name = "users_override_analytics_code")
    public boolean isUsersOverrideAnalyticsCode() {
        return usersOverrideAnalyticsCode;
    }

    public void setUsersOverrideAnalyticsCode(boolean usersOverrideAnalyticsCode) {
        this.usersOverrideAnalyticsCode = usersOverrideAnalyticsCode;
    }

    @Column(name = "comment_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public CommentPolicy getCommentPolicy() {
        return commentPolicy;
    }

    public void setCommentPolicy(CommentPolicy commentPolicy) {
        this.commentPolicy = commentPolicy;
    }

    @Column(name = "comment_html_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public HTMLSanitizer.Level getCommentHtmlPolicy() {
        return commentHtmlPolicy;
    }

    public void setCommentHtmlPolicy(HTMLSanitizer.Level commentHtmlPolicy) {
        this.commentHtmlPolicy = commentHtmlPolicy;
    }

    @Column(name = "spam_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public SpamPolicy getSpamPolicy() {
        return spamPolicy;
    }

    public void setSpamPolicy(SpamPolicy spamPolicy) {
        this.spamPolicy = spamPolicy;
    }

    @Column(name = "users_comment_notifications")
    public boolean isUsersCommentNotifications() {
        return usersCommentNotifications;
    }

    public void setUsersCommentNotifications(boolean usersCommentNotifications) {
        this.usersCommentNotifications = usersCommentNotifications;
    }

    @Column(name = "comment_spam_filter")
    public String getGlobalSpamFilter() {
        return globalSpamFilter;
    }

    public void setGlobalSpamFilter(String globalSpamFilter) {
        this.globalSpamFilter = globalSpamFilter;
    }

    @Column(name = "max_file_uploads_size_mb")
    public int getMaxFileUploadsSizeMb() {
        return maxFileUploadsSizeMb;
    }

    public void setMaxFileUploadsSizeMb(int maxFileUploadsSizeMb) {
        this.maxFileUploadsSizeMb = maxFileUploadsSizeMb;
    }

    public enum RegistrationPolicy {
        APPROVAL_REQUIRED("globalConfig.registration.approvalRequired"),
        DISABLED("globalConfig.registration.disabled");

        private String description;

        RegistrationPolicy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum CommentPolicy {
        NONE(0, "generic.no"),
        MODERATE_NONPUB(1, "globalConfig.nonPubMustModerate"),
        MODERATE_NONAUTH(2, "globalConfig.nonAuthMustModerate");

        private String label;

        private int level;

        CommentPolicy(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }
    }

    public enum SpamPolicy {
        DONT_CHECK(0, "globalConfig.spam.skipCheck"),
        MARK_SPAM(1, "globalConfig.spam.markAsSpam"),
        NO_EMAIL(2, "globalConfig.spam.noEmailNotification"),
        JUST_DELETE(3, "globalConfig.spam.autoDelete");

        private String label;

        private int level;

        SpamPolicy(int level, String label) {
            this.level = level;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getLevel() {
            return level;
        }
    }

}
