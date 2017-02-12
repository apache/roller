package org.apache.roller.weblogger.pojos;

import org.apache.roller.weblogger.util.HTMLSanitizer;

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
    private int newsfeedItemsPage;
    private String defaultAnalyticsCode;
    private boolean usersOverrideAnalyticsCode;
    private GlobalCommentPolicy commentPolicy;
    private HTMLSanitizer.Level commentHtmlPolicy;
    private boolean autodeleteSpam;
    private boolean usersCommentNotifications;
    private String commentSpamFilter;
    private boolean usersUploadMediaFiles;
    private String allowedFileExtensions;
    private String disallowedFileExtensions;
    private int maxFileSizeMb;
    private int maxFileUploadsSizeMb;

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // see tightblog.properties file, tightblog.database.expected.version value for explanation
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

    @Column(name = "newsfeed_items_page")
    public int getNewsfeedItemsPage() {
        return newsfeedItemsPage;
    }

    public void setNewsfeedItemsPage(int newsfeedItemsPage) {
        this.newsfeedItemsPage = newsfeedItemsPage;
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
    public GlobalCommentPolicy getCommentPolicy() {
        return commentPolicy;
    }

    public void setCommentPolicy(GlobalCommentPolicy commentPolicy) {
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

    @Column(name = "autodelete_spam")
    public boolean isAutodeleteSpam() {
        return autodeleteSpam;
    }

    public void setAutodeleteSpam(boolean autodeleteSpam) {
        this.autodeleteSpam = autodeleteSpam;
    }

    @Column(name = "users_comment_notifications")
    public boolean isUsersCommentNotifications() {
        return usersCommentNotifications;
    }

    public void setUsersCommentNotifications(boolean usersCommentNotifications) {
        this.usersCommentNotifications = usersCommentNotifications;
    }

    @Column(name = "comment_spam_filter")
    public String getCommentSpamFilter() {
        return commentSpamFilter;
    }

    public void setCommentSpamFilter(String commentSpamFilter) {
        this.commentSpamFilter = commentSpamFilter;
    }

    @Column(name = "users_upload_media_files")
    public boolean isUsersUploadMediaFiles() {
        return usersUploadMediaFiles;
    }

    public void setUsersUploadMediaFiles(boolean usersUploadMediaFiles) {
        this.usersUploadMediaFiles = usersUploadMediaFiles;
    }

    @Column(name = "allowed_file_extensions")
    public String getAllowedFileExtensions() {
        return allowedFileExtensions;
    }

    public void setAllowedFileExtensions(String allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    @Column(name = "disallowed_file_extensions")
    public String getDisallowedFileExtensions() {
        return disallowedFileExtensions;
    }

    public void setDisallowedFileExtensions(String disallowedFileExtensions) {
        this.disallowedFileExtensions = disallowedFileExtensions;
    }

    @Column(name = "max_file_size_mb")
    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public void setMaxFileSizeMb(int maxFileSizeMb) {
        this.maxFileSizeMb = maxFileSizeMb;
    }

    @Column(name = "max_file_uploads_size_mb")
    public int getMaxFileUploadsSizeMb() {
        return maxFileUploadsSizeMb;
    }

    public void setMaxFileUploadsSizeMb(int maxFileUploadsSizeMb) {
        this.maxFileUploadsSizeMb = maxFileUploadsSizeMb;
    }

    public enum RegistrationPolicy {
        EMAIL("globalConfig.registration.email"),
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

    public enum GlobalCommentPolicy {
        NONE(0, "generic.no", "generic.no"),
        MUSTMODERATE(1, "globalConfig.mustModerateComments", "weblogSettings.mustModerateComments"),
        YES(2, "globalConfig.commentsOK", "weblogSettings.commentsOK");

        private String siteDescription;

        private String weblogDescription;

        private int level;

        GlobalCommentPolicy(int level, String siteDescription, String weblogDescription) {
            this.level = level;
            this.siteDescription = siteDescription;
            this.weblogDescription = weblogDescription;
        }

        public String getWeblogDescription() {
            return weblogDescription;
        }

        public String getSiteDescription() {
            return siteDescription;
        }

        public int getLevel() {
            return level;
        }
    }
}
