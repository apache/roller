/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
// import java.net.URL;
// import java.net.MalformedURLException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

// import org.apache.abdera.Abdera;
// import org.apache.abdera.ext.thread.ThreadHelper;
// import org.apache.abdera.model.Entry;
// import org.apache.abdera.model.Feed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryCommentWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ServletResponseAware;

/**
 * Provides export functionality for the author of a weblog.
 */
public final class WeblogExport extends UIAction
        implements ServletResponseAware {

    // Static Variables --------------------------------------------------------
    private static final Log log = LogFactory.getLog(WeblogExport.class);

    private static final Pattern SC_TAG_PATTERN =
            Pattern.compile("(([\\S])(/>))");

    private static final Pattern PRE_TAG_PATTERN =
            Pattern.compile("<pre>[\\s\\S]+?</pre>");

    private static final Pattern NEWLINE_PATTERN =
            Pattern.compile("\\r\\n|\\r|\\n");

    // TODO: Perhaps add enum to manage the different MT constants
    private static final String MT_SECTION_DIVIDER = "-----\n";
    private static final String MT_ENTRY_DIVIDER = "--------\n";

    private static final SimpleDateFormat MT_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private static final SimpleDateFormat ATOM_ID_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    private static final String MT_FORMAT = "mtimport";
    private static final String MT_PLUS_FORMAT = "mtimportplus";
    private static final String ATOM_FORMAT = "atom";

    // private static final Abdera abdera = new Abdera();

    // Instance Variables ------------------------------------------------------
    private Pattern baseUrlPattern;

    private HttpServletResponse response;
    private String baseUrl;
    private String format;

    // Constructors ------------------------------------------------------------
    public WeblogExport() {
        this.actionName = "weblogExport";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogExport.title";

        // Set the default format
        this.format = MT_FORMAT;
    }

    // Public Methods ----------------------------------------------------------
    /**
     * Keeps a reference to the current HTTP servlet response object.
     *
     * @param httpServletResponse The HTTP servlet response.
     */
    public void setServletResponse(HttpServletResponse httpServletResponse) {
        this.response = httpServletResponse;
    }

    /**
     * Sets the base URL to be used when replacing references to resource files.
     *
     * @param baseUrl The desired base URL.
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Get the current format.
     *
     * @return The current format.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the desired export format.
     *
     * @param format The desired export format.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Gets the list of supported export formats.
     *
     * @return A list of string made of of supported export formats.
     */
    public Map<String, String> getFormatOptions() {
        Map<String, String> options;
        options = new LinkedHashMap<String, String>();

        options.put(MT_FORMAT, getText("weblogExport.format.mtimport"));
        options.put(MT_PLUS_FORMAT, getText("weblogExport.format.mtimportplus"));
        // options.put(ATOM_FORMAT, getText("weblogExport.format.atom"));

        return options;
    }

    /**
     * Require the author role before allowing export functionality.
     */
    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }

    /**
     * Simply triggers the display of the export options UI.
     */
    @Override
    public String execute() throws WebloggerException {

        if (!WebloggerConfig.getBooleanProperty("weblog.export.enabled")) {
            throw new WebloggerException("ERROR: export is disabled");
        }

        // We need to gather some more info before we can attempt an export
        return INPUT;
    }

    /**
     * Returns an output stream to the client containing a text file of all
     * entries and comments. This will include draft entries as well.
     *
     * Currently the only file format supported is mtimport.
     */
    public void exportEntries() throws WebloggerException {

        if (!WebloggerConfig.getBooleanProperty("weblog.export.enabled")) {
            throw new WebloggerException("ERROR: export is disabled");
        }

        try {
            WeblogEntryManager wmgr =
                    WebloggerFactory.getWeblogger().getWeblogEntryManager();

            URLStrategy urlStrategy;
            urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();

            List rawEntries;
            rawEntries = wmgr.getWeblogEntries(getActionWeblog(), null, null,
                    null, null, null, null, null, null, null, null, 0, -1);

            List<WeblogEntryWrapper> entries;
            entries = new ArrayList<WeblogEntryWrapper>();

            for (Object entry : rawEntries) {
                entries.add(WeblogEntryWrapper.wrap((WeblogEntry) entry,
                        urlStrategy));
            }

            // Compile the resource URL pattern using the weblog handle
            baseUrlPattern = Pattern.compile(
                    "(<[\\s\\S]+?=[\"'])(http[s]*?://[\\S]+/" +
                            getActionWeblog().getHandle() + "/resource/|/" +
                            getActionWeblog().getHandle() + "/resource/)");

            // Produce the selected output format
            String output;
            output = formatAsMoveableType(entries);
            /*
            if (format.equals(ATOM_FORMAT)) {
                output = formatAsAtom(entries);
            }
            else {
                output = formatAsMoveableType(entries);
            }
            */

            if (!response.isCommitted()) {
                response.reset();

                SimpleDateFormat dateFormat;
                dateFormat = new SimpleDateFormat("MMddyyyy'T'HHmmss");

                StringBuilder fileName;
                fileName = new StringBuilder();
                fileName.append(getActionWeblog().getHandle());
                fileName.append("-entries-");
                fileName.append(dateFormat.format(System.currentTimeMillis()));

                if (format.equals(ATOM_FORMAT)) {
                    fileName.append(".xml");
                }
                else {
                    fileName.append(".txt");
                }

                // Force the browser to download the export file
                response.setContentType(
                        "application/octet-stream; charset=utf-8");
                response.setContentLength(
                        output.getBytes("UTF-8").length);
                response.setHeader(
                        "Content-Disposition", "attachment; filename=\"" +
                                fileName.toString() + "\"");

                ServletOutputStream outputStream;
                outputStream = response.getOutputStream();
                outputStream.print(output);
                outputStream.flush();
                outputStream.close();
            }
        } catch (WebloggerException e) {
            log.error("Error looking up entries: ", e);
        } catch (IOException e) {
            log.error("Error getting output stream: ", e);
        }
    }

    /**
     * Returns an output stream to the client of all uploaded resource files as
     * a ZIP archive.
     */
    public void exportResources() {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("MMddyyyy'T'HHmmss");

        StringBuilder fileName;
        fileName = new StringBuilder();
        fileName.append(getActionWeblog().getHandle());
        fileName.append("-resources-");
        fileName.append(dateFormat.format(System.currentTimeMillis()));
        fileName.append(".zip");

        if (!response.isCommitted()) {
            response.reset();

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + fileName.toString() + "\"");

            try {
                MediaFileManager fmgr =
                        WebloggerFactory.getWeblogger().getMediaFileManager();

                List<MediaFile> resources = new ArrayList<MediaFile>();

                // Load the contents of any sub-directories
                for (MediaFileDirectory mdir : fmgr.getMediaFileDirectories(getActionWeblog())) {
                    loadResources(resources, mdir);
                }

                // Load the files at the root of the specific upload directory
                loadResources(resources, null);

                // Create a buffer for reading the files
                byte[] buffer;
                buffer = new byte[1024];

                ServletOutputStream servletOutput;
                servletOutput = response.getOutputStream();

                ZipOutputStream zipOutput;
                zipOutput = new ZipOutputStream(servletOutput);

                for (MediaFile resource : resources) {
                    InputStream input;
                    input = resource.getInputStream();

                    // Add a new ZIP entry to output stream
                    zipOutput.putNextEntry(new ZipEntry(resource.getPath()));

                    int length;
                    while ((length = input.read(buffer)) > 0) {
                        zipOutput.write(buffer, 0, length);
                    }

                    // Cleanup the entry
                    input.close();
                    zipOutput.closeEntry();
                }

                // Cleanup the output stream
                zipOutput.flush();
                zipOutput.close();
            } catch (Exception e) {
                log.error("Error exporting resources: " + e.getMessage());
            }
        }
    }

    // Private Methods ---------------------------------------------------------
    /**
     * Formats all entries and comments, including draft entries, in the
     * Atom Syndication Format.
     *
     * @param entries A collection of entries to format.
     * @return A String of all entries and comments formatted as Atom
     */
    /*
    private String formatAsAtom(List<WeblogEntryWrapper> entries) {
        Weblog weblog;
        weblog = getActionWeblog();

        String hostname;
        URL absoluteUrl;
        try {
            absoluteUrl = new URL(weblog.getAbsoluteURL());
            hostname = absoluteUrl.getHost();
        }
        catch (MalformedURLException e) {
            log.error("Unable to parse the absolute URL: " + e.getMessage());
            hostname = "unknown";
        }

        // Feed
        StringBuilder feedId;
        feedId = new StringBuilder();

        feedId.append("tag:");
        feedId.append(hostname);
        feedId.append(",");
        feedId.append(ATOM_ID_DATE_FORMAT.format(weblog.getDateCreated()));
        feedId.append(":");
        feedId.append(weblog.getId());

        Feed feed;
        feed = abdera.newFeed();

        feed.setId(feedId.toString());
        feed.setTitle(weblog.getName());

        if (weblog.getDescription() != null &&
                !weblog.getDescription().equals("")) {
            feed.setSubtitle(weblog.getDescription());
        }

        // TODO: Maybe want to add all authors
        feed.addAuthor(weblog.getCreator().getScreenName());

        // TODO: Really need a "self" link, but what to use?
        feed.addLink(weblog.getAbsoluteURL(), "self");
        feed.addLink(weblog.getAbsoluteURL(), "alternate");

        feed.setUpdated(weblog.getLastModified());

        feed.setGenerator("http://roller.apache.org",
                WebloggerFactory.getWeblogger().getVersion(), "Apache Roller");

        // Entries
        for (WeblogEntryWrapper entryWrapper : entries) {
            StringBuilder entryId;
            entryId = new StringBuilder();

            entryId.append("tag:");
            entryId.append(hostname);
            entryId.append(",");
            entryId.append(ATOM_ID_DATE_FORMAT.format(
                    (entryWrapper.getPubTime() != null) ?
                            entryWrapper.getPubTime() :
                            entryWrapper.getUpdateTime()));
            entryId.append(":");
            entryId.append(entryWrapper.getId());

            Entry entry;
            entry = feed.addEntry();

            entry.setId(entryId.toString());
            entry.setTitle(entryWrapper.getTitle());
            entry.addAuthor(entryWrapper.getCreator().getScreenName());
            entry.addLink(entryWrapper.getPermalink(), "alternate");
            entry.setPublished(entryWrapper.getPubTime());
            entry.setUpdated(entryWrapper.getUpdateTime());

            // Category
            entry.addCategory(null, entryWrapper.getCategory().getPath(),
                    entryWrapper.getCategory().getName());

            // Tags
            for (Object tagWrapperObj : entryWrapper.getTags()) {
                WeblogEntryTagWrapper tagWrapper;
                tagWrapper = (WeblogEntryTagWrapper) tagWrapperObj;

                entry.addCategory("http://roller.apache.org/ns/tags/",
                        tagWrapper.getName(), tagWrapper.getName());
            }

            // Enclosure
            String enclosureUrl;
            enclosureUrl = entryWrapper.findEntryAttribute(
                    "att_mediacast_url");

            if (enclosureUrl != null && !enclosureUrl.equals("")) {
                String enclosureType;
                enclosureType = entryWrapper.findEntryAttribute(
                        "att_mediacast_type");

                Long enclosureLength;
                try {
                    enclosureLength = Long.parseLong(
                            entryWrapper.findEntryAttribute(
                                    "att_mediacast_length"));
                }
                catch (NumberFormatException e) {
                    log.error("Unable to parse 'att_mediacast_length': " +
                            e.getMessage());
                    enclosureLength = (long) 0;
                }

                entry.addLink(enclosureUrl, "enclosure", enclosureType, null,
                        null, enclosureLength);
            }

            // Summary
            if (entryWrapper.getSummary() != null &&
                    !entryWrapper.getSummary().equals("")) {
                entry.setSummaryAsHtml(processEntry(
                        entryWrapper.getSummary().trim()));
            }

            // Content
            if (entryWrapper.getText() != null &&
                    !entryWrapper.getText().equals("")) {
                entry.setContentAsHtml(processEntry(
                        entryWrapper.getText().trim()));
            }

            // Comments in reply to the entry
            for (Object commentObj : entryWrapper.getComments()) {
                WeblogEntryCommentWrapper commentEntryWrapper;
                commentEntryWrapper = (WeblogEntryCommentWrapper) commentObj;

                StringBuilder commentEntryId;
                commentEntryId = new StringBuilder();

                commentEntryId.append("tag:");
                commentEntryId.append(hostname);
                commentEntryId.append(",");
                commentEntryId.append(ATOM_ID_DATE_FORMAT.format(
                        commentEntryWrapper.getPostTime()));
                commentEntryId.append(":");
                commentEntryId.append(commentEntryWrapper.getId());

                Entry commentEntry;
                commentEntry = feed.addEntry();

                commentEntry.setId(commentEntryId.toString());
                commentEntry.setTitle("Re: " + entryWrapper.getTitle());

                // Author
                if (commentEntryWrapper.getName() != null &&
                        ! commentEntryWrapper.getName().equals("")) {
                    commentEntry.addAuthor(commentEntryWrapper.getName());
                }
                else {
                    commentEntry.addAuthor("Anonymous");
                }

                commentEntry.addLink(entryWrapper.getPermalink() +
                        "#comment-" + commentEntryWrapper.getTimestamp(),
                        "alternate");
                commentEntry.setPublished(commentEntryWrapper.getPostTime());
                commentEntry.setUpdated(commentEntryWrapper.getPostTime());
                commentEntry.setContentAsHtml(commentEntryWrapper.getContent());

                // The important bit
                ThreadHelper.addInReplyTo(commentEntry, entry);
            }
        }

        return feed.toString();
    }
    */

    /**
     * Formats all entries and comments, including draft entries, in the
     * Moveable Type Import Format (mtimport). This format can be imported
     * into both Moveable Type and WordPress blogging platforms.
     *
     * @param entries A collection of entries to format.
     * @return A String of all entries and comments formatted as mtimport
     */
    private String formatAsMoveableType(List<WeblogEntryWrapper> entries) {
        StringBuilder result;
        result = new StringBuilder();

        for (WeblogEntryWrapper entry : entries) {
            // Author
            result.append("AUTHOR: ");
            result.append(entry.getCreator().getScreenName());
            result.append("\n");

            // Title
            result.append("TITLE: ");
            result.append(entry.getTitle());
            result.append("\n");

            // Date
            result.append("DATE: ");
            if (entry.getStatus().equals(WeblogEntry.PUBLISHED)) {
                result.append(MT_DATE_FORMAT.format(entry.getPubTime()));
            } else {
                result.append(MT_DATE_FORMAT.format(entry.getUpdateTime()));
            }
            result.append("\n");

            // Primary category
            result.append("PRIMARY CATEGORY: ");
            result.append(entry.getCategory().getName());
            result.append("\n");

            // Status
            result.append("STATUS: ");
            if (entry.getStatus().equals(WeblogEntry.PUBLISHED)) {
                result.append("publish");
            } else {
                result.append("draft");
            }
            result.append("\n");

            // Allow comments
            result.append("ALLOW COMMENTS: ");
            if (entry.getAllowComments()) {
                result.append("1");
            } else {
                result.append("0");
            }
            result.append("\n");

            result.append(MT_SECTION_DIVIDER);

            // Body
            // TODO: May want to use transformed text here
            result.append("BODY: \n");
            result.append(processEntry(entry.getText().trim()));
            result.append("\n");

            result.append(MT_SECTION_DIVIDER);

            // Excerpt
            if (entry.getSummary() != null && !entry.getSummary().equals("")) {
                // TODO: May want to use transformed summary here
                result.append("EXCERPT: \n");
                result.append(processEntry(entry.getSummary().trim()));
                result.append("\n");

                result.append(MT_SECTION_DIVIDER);
            }

            for (Object commentObj : entry.getComments()) {
                WeblogEntryCommentWrapper comment;
                comment = (WeblogEntryCommentWrapper) commentObj;
                result.append("COMMENT: \n");

                result.append("AUTHOR: ");
                result.append(comment.getName());
                result.append("\n");

                result.append("EMAIL: ");
                result.append(comment.getEmail());
                result.append("\n");

                result.append("URL: ");
                result.append(comment.getUrl());
                result.append("\n");

                result.append("DATE: ");
                result.append(MT_DATE_FORMAT.format(comment.getPostTime()));
                result.append("\n");

                result.append(comment.getContent());
                result.append("\n");

                result.append(MT_SECTION_DIVIDER);
            }

            result.append(MT_ENTRY_DIVIDER);
        }

        return result.toString();
    }

    /**
     * Performs some pre-processing of entry text. It fixes a problem when
     * WordPress imports a self-closing HTML tag that does not have a space
     * preceding the "/>" characters. It also provides a replacment base URL
     * for all referenced resource files if requested.
     *
     * @param text The entry text to process.
     * @return The resulting String after processing has taken place.
     */
    private String processEntry(String text) {
        String result;
        result = text;

        // Some special processing is needed for mtimport
        if (format.startsWith(MT_FORMAT)) {
            // Fix self closing tags that are missing a space,
            // replaceing <foo bar="foobar"/> with <foo bar="foobar" />
            Matcher badSelfClosingTagMatcher;
            badSelfClosingTagMatcher = SC_TAG_PATTERN.matcher(result);

            result = badSelfClosingTagMatcher.replaceAll("$2 />");

            if (format.equals(MT_PLUS_FORMAT)) {
                // Replace all newlines with spaces leaving "<pre>" blocks
                // alone. WordPress will automatically convert newlines to
                // "<br />" which alters the intended formatting.
                Matcher preTagMatcher;
                preTagMatcher = PRE_TAG_PATTERN.matcher(result);

                StringBuilder replacedNewLines;
                replacedNewLines = new StringBuilder();

                int index;
                index = 0;

                while (preTagMatcher.find()) {
                    replacedNewLines.append(NEWLINE_PATTERN.matcher(
                            result.substring(index, preTagMatcher.start())).
                            replaceAll(" "));
                    replacedNewLines.append(preTagMatcher.group());
                    index = preTagMatcher.end();
                }

                replacedNewLines.append(NEWLINE_PATTERN.matcher(
                            result.substring(index, result.length())).
                            replaceAll(" "));

                result = replacedNewLines.toString();
            }
        }

        // Replace all /weblog-handle/resource/ links with a specified base URL
        if (baseUrl != null && !baseUrl.equals("")) {
            Matcher baseUrlMatcher;
            baseUrlMatcher = baseUrlPattern.matcher(result);

            try {
                result = baseUrlMatcher.replaceAll("$1" + baseUrl);
            } catch (IllegalArgumentException e) {
                log.error("Invalid base URL submitted: " + baseUrl + ": " +
                        e.getMessage());
            }
        }

        return result;
    }

    /**
     * Adds all the non-directory files for the specified path to the provided
     * List.
     *
     * @param mfiles The List in which to add the resource objects.
     * @param mdir The path from which to load. If null, the root path is used.
     */
    private void loadResources(List<MediaFile> mfiles, MediaFileDirectory mdir) {
        try {
            // Load the non-directory files
            mfiles.addAll(mdir.getMediaFiles());
        } catch (Exception e) {
            log.error("Error loading resources: " + e.getMessage());
        }
    }
}
