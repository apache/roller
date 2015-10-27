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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.themes;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.*;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * The Theme object encapsulates all elements of a single weblog theme. It is
 * used mostly to contain all the templates for a theme, but does contain other
 * theme related attributes such as name, last modified date, etc.
 */
public class SharedTheme implements Theme, Serializable {

    private String id = null;
    private String name = null;
    private String description = null;
    private String author = null;
    private Date lastModified = null;
    private boolean enabled = false;

    private static Log log = LogFactory.getLog(SharedTheme.class);

    // the filesystem directory where we should read this theme from
    private String themeDir = null;

    // the theme preview image path from the shared theme's base folder
    private String previewImagePath = null;

    // we keep templates in a Map for faster lookups by name
    private Map<String, ThemeTemplate> templatesByName = new HashMap<>();

    // we keep templates in a Map for faster lookups by link
    private Map<String, ThemeTemplate> templatesByLink = new HashMap<>();

    // we keep templates in a Map for faster lookups by action
    private Map<ComponentType, ThemeTemplate> templatesByAction = new HashMap<>();

    public SharedTheme(String themeDirPath)
            throws WebloggerException {

        this.themeDir = themeDirPath;

        // load the theme elements and cache 'em
        loadThemeFromDisk();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List<ThemeTemplate> getTemplates() {
        return new ArrayList<>(this.templatesByName.values());
    }

    /**
     * Looup the default template, action = weblog. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getDefaultTemplate() {
        return this.templatesByAction.get(ComponentType.WEBLOG);
    }

    /**
     * Lookup the specified template by name. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) {
        return this.templatesByName.get(name);
    }

    /**
     * Lookup the specified template by link. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByLink(String link) {
        return this.templatesByLink.get(link);
    }

    /**
     * Lookup the specified template by action. Returns null if the template
     * cannot be found.
     */
    public ThemeTemplate getTemplateByAction(ComponentType action) {
        return this.templatesByAction.get(action);
    }

    public String getPreviewImagePath() {
        return previewImagePath;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("\n");

        for (ThemeTemplate template : templatesByName.values()) {
            sb.append(template);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Load all the elements of this theme from disk and cache them.
     */
    private void loadThemeFromDisk() throws WebloggerException {

        log.debug("Parsing theme descriptor for " + this.themeDir);

        ThemeMetadata themeMetadata;
        try {
            // lookup theme descriptor and parse it
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new StreamSource(
                    SharedTheme.class.getResourceAsStream("/themes.xsd")));

            InputStream is = new FileInputStream(this.themeDir + File.separator + "theme.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(ThemeMetadata.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(schema);
            jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    log.error("Theme parsing error: " +
                            event.getMessage() + "; Line #" +
                            event.getLocator().getLineNumber() + "; Column #" +
                            event.getLocator().getColumnNumber());
                    return false;
                }
            });
            themeMetadata = (ThemeMetadata) jaxbUnmarshaller.unmarshal(is);
        } catch (Exception ex) {
            throw new WebloggerException(
                    "Unable to parse theme.xml for theme " + this.themeDir, ex);
        }

        log.debug("Loading Theme " + themeMetadata.getName());

        // use parsed theme descriptor to load Theme data
        setId(themeMetadata.getId());
        setName(themeMetadata.getName());
        if (StringUtils.isNotEmpty(themeMetadata.getDescription())) {
            setDescription(themeMetadata.getDescription());
        } else {
            setDescription(" ");
        }
        setAuthor(themeMetadata.getAuthor());
        setLastModified(null);
        setEnabled(true);

        // load resource representing preview image
        File previewFile = new File(this.themeDir + File.separator
                + themeMetadata.getPreviewImagePath());
        if (!previewFile.exists() || !previewFile.canRead()) {
            log.warn("Couldn't read theme [" + this.getName()
                    + "] preview image file ["
                    + themeMetadata.getPreviewImagePath() + "]");
        } else {
            this.previewImagePath = themeMetadata.getPreviewImagePath();
        }

        // available types with Roller
        List<RenditionType> availableTypesList = new ArrayList<>();
        availableTypesList.add(RenditionType.STANDARD);
        if (themeMetadata.getDualTheme()) {
            availableTypesList.add(RenditionType.MOBILE);
        }

        // create the templates based on the theme descriptor data
        SharedThemeTemplate themeTemplate;
        boolean hasWeblogTemplate = false;
        for (ThemeMetadataTemplate templateMetadata : themeMetadata.getTemplates()) {

            // one and only one template with action "weblog" allowed
            if (ComponentType.WEBLOG.equals(templateMetadata.getAction())) {
                if (hasWeblogTemplate) {
                    throw new WebloggerException("Theme has more than one template with action of 'weblog'");
                } else {
                    hasWeblogTemplate = true;
                }
            }

            // get the template's available renditions
            ThemeMetadataTemplateRendition standardRendition = templateMetadata
                    .getTemplateRenditionTable().get(RenditionType.STANDARD);

            if (standardRendition == null) {
                throw new WebloggerException("Cannot retrieve required standard rendition for template " + templateMetadata.getName());
            } else {
                // Check to make sure standard rendition is retrievable
                File templateFile = new File(this.themeDir + File.separator + standardRendition.getContentsFile());
                String contents = loadTemplateRendition(templateFile);
                if (contents == null) {
                    throw new WebloggerException("Couldn't load template file [" + templateFile + "]");
                }
            }

            if (themeMetadata.getDualTheme()) {
                ThemeMetadataTemplateRendition mobileRendition = templateMetadata
                        .getTemplateRenditionTable().get(RenditionType.MOBILE);

                // cloning the standard template code if no mobile is present
                if (mobileRendition == null) {
                    mobileRendition = new ThemeMetadataTemplateRendition();
                    mobileRendition.setContentsFile(standardRendition.getContentsFile());
                    mobileRendition.setTemplateLanguage(standardRendition.getTemplateLanguage());
                    mobileRendition.setType(RenditionType.MOBILE);
                    templateMetadata.addTemplateRendition(mobileRendition);
                }
            }

            // construct ThemeTemplate representing this file
            themeTemplate = new SharedThemeTemplate(
                    themeMetadata.getId() + ":" + templateMetadata.getName(),
                    templateMetadata.getAction(), templateMetadata.getName(),
                    templateMetadata.getDescription(),
                    templateMetadata.getLink(),
                    templateMetadata.isHidden(), templateMetadata.isNavbar());

            for (RenditionType type : availableTypesList) {
                SharedThemeTemplateRendition templateCode = createRendition(
                        templateMetadata.getTemplateRenditionTable().get(type));

                themeTemplate.addTemplateRendition(templateCode);
            }

            // add it to the theme
            addTemplate(themeTemplate);
        }
        if(!hasWeblogTemplate) {
            throw new WebloggerException("Theme " + themeMetadata.getName() + " has no template with 'weblog' action");
        }
    }

    /**
     * Load a single template file as a string, returns null if can't read file.
     */
    private String loadTemplateRendition(File templateFile) {
        // Continue reading theme even if problem encountered with one file
        if (!templateFile.exists() && !templateFile.canRead()) {
            return null;
        }

        char[] chars;
        int length;
        try {
            chars = new char[(int) templateFile.length()];
            FileInputStream stream = new FileInputStream(templateFile);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            length = reader.read(chars);
        } catch (Exception noprob) {
            log.error("Exception reading theme [" + this.getName()
                    + "] template file [" + templateFile + "]");
            if (log.isDebugEnabled()) {
                log.debug(noprob);
            }
            return null;
        }

        return new String(chars, 0, length);
    }

    /**
     * Set the value for a given template name.
     */
    private void addTemplate(ThemeTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getLink(), template);
        if (!ComponentType.CUSTOM.equals(template.getAction())) {
            this.templatesByAction.put(template.getAction(), template);
        }
    }

    private SharedThemeTemplateRendition createRendition(
            ThemeMetadataTemplateRendition templateCodeMetadata) {
        SharedThemeTemplateRendition templateRendition = new SharedThemeTemplateRendition();

        // construct File object from path
        File templateFile = new File(this.themeDir + File.separator
                + templateCodeMetadata.getContentsFile());

        // read stylesheet contents
        String contents = loadTemplateRendition(templateFile);
        if (contents == null) {
            // if we don't have any contents then load no string
            contents = "";
            log.error("Couldn't load stylesheet theme [" + this.getName()
                    + "] template file [" + templateFile + "]");
        }

        templateRendition.setTemplate(contents);
        templateRendition.setTemplateLanguage(templateCodeMetadata.getTemplateLanguage());
        templateRendition.setType(templateCodeMetadata.getType());
        templateRendition.setLastModified(new Date(templateFile.lastModified()));

        return templateRendition;
    }

    public int compareTo(Theme other) {
        return getName().compareTo(other.getName());
    }
}
