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

package org.apache.roller.weblogger.business.themes;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * The parser for theme xml descriptors.
 *
 * This class unmarshalls a theme descriptor into a set of objects.
 */
public class ThemeMetadataParser {

    /**
     * Unmarshall the given input stream into our defined
     * set of Java objects.
     **/
    public ThemeMetadata unmarshall(InputStream instream)
        throws ThemeParsingException, IOException, JDOMException {
        
        if(instream == null) {
            throw new IOException("InputStream is null!");
        }
        
        ThemeMetadata theme = new ThemeMetadata();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(instream);
        
        // start at root and get theme id, name, description and author
        Element root = doc.getRootElement();
        theme.setId(root.getChildText("id"));
        theme.setName(root.getChildText("name"));
        theme.setDescription(root.getChildText("description"));
        theme.setAuthor(root.getChildText("author"));

        // dual-theme (standard & mobile) or one-theme-fits-all?
        theme.setDualTheme("true".equalsIgnoreCase(root.getChildText("dualTheme")));

        // if either id or name is null then throw a parsing exception
        if(StringUtils.isEmpty(theme.getId()) || StringUtils.isEmpty(theme.getName())) {
            throw new ThemeParsingException("'id' and 'name' are required theme elements");
        }
        
        // now grab the preview image path
        Element previewImage = root.getChild("preview-image");
        if (previewImage != null) {
            theme.setPreviewImage(previewImage.getAttributeValue("path"));
        } else {
            throw new ThemeParsingException("No preview image specified");
        }
        
        // grab the stylesheet if it exists
        Element stylesheet = root.getChild("stylesheet");
        if (stylesheet != null) {
            theme.setStylesheet(elementToStylesheet(stylesheet));

        }
        
        // now grab the static resources
        List<Element> resources = root.getChildren("resource");
        for (Element resource : resources) {
            theme.addResource(resource.getAttributeValue("path"));
        }
        
        // now grab the templates
        boolean weblogActionTemplate = false;
        List<Element> templates = root.getChildren("template");
        for (Element template : templates) {

            ThemeMetadataTemplate tmpl = elementToTemplateMetadata(template);
            theme.addTemplate(tmpl);

            if(ComponentType.WEBLOG.equals(tmpl.getAction())) {
                weblogActionTemplate = true;
            }
        }
        
        // make sure all required elements are present and values are valid
        // check that there is a template with action='weblog'
        if(!weblogActionTemplate) {
            throw new ThemeParsingException("did not find a template of action = 'weblog'");
        }
        
        return theme;
    }
    
    
	private ThemeMetadataTemplate elementToTemplateMetadata(Element element)
			throws ThemeParsingException {

		ThemeMetadataTemplate template = new ThemeMetadataTemplate();

		template.setName(element.getChildText("name"));
		template.setDescription(element.getChildText("description"));
		template.setLink(element.getChildText("link"));
		template.setContentType(element.getChildText("contentType"));
        String actionString = element.getAttributeValue("action");
        if (StringUtils.isEmpty(actionString)) {
            throw new ThemeParsingException("Template must contain an 'action' element");
        } else {
            try {
                template.setAction(ComponentType.valueOf(actionString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ThemeParsingException("Unknown template action value '" + actionString + "'");
            }
        }

		//parsing template code segment
		List<Element> renditionList = element.getChildren("rendition");

		for (Element renditionElement : renditionList) {
			ThemeMetadataTemplateRendition rendition = new ThemeMetadataTemplateRendition();
            String renditionValue = renditionElement.getAttributeValue("type");
            if (renditionValue != null) {
                try {
                    rendition.setType(RenditionType.valueOf(renditionValue.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ThemeParsingException("Invalid rendition type " + renditionValue + " found.");
                }
            } else {
                // default to standard if type not provided (most templates don't have multiple renditions anyway)
                rendition.setType(RenditionType.STANDARD);
            }
            String templateString = renditionElement.getChildText("templateLanguage");
            if (StringUtils.isEmpty(templateString)) {
                throw new ThemeParsingException("rendition must contain a 'templateLanguage' element");
            } else {
                try {
                    rendition.setTemplateLang(TemplateLanguage.valueOf(templateString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ThemeParsingException("Unknown templateLanguage value '" + templateString + "'");
                }
            }
			rendition.setContentsFile(renditionElement.getChildText("contentsFile"));
            if (StringUtils.isEmpty(rendition.getContentsFile())) {
                throw new ThemeParsingException("Rendition must contain a 'contentsFile' element");
            }
			template.addTemplateRendition(rendition);
		}

		String navbar = element.getChildText("navbar");
		if ("true".equalsIgnoreCase(navbar)) {
			template.setNavbar(true);
		}

		String hidden = element.getChildText("hidden");
		if ("true".equalsIgnoreCase(hidden)) {
			template.setHidden(true);
		}

		// validate template
		if (StringUtils.isEmpty(template.getName())) {
			throw new ThemeParsingException("templates must contain a 'name' element");
		}

		return template;
	}
    
    
    private ThemeMetadataTemplate elementToStylesheet(Element element) 
            throws ThemeParsingException {
        
        ThemeMetadataTemplate template = new ThemeMetadataTemplate();
        
        template.setName(element.getChildText("name"));
        template.setDescription(element.getChildText("description"));
        template.setLink(element.getChildText("link"));
        template.setContentType(element.getChildText("contentType"));
        template.setAction(ComponentType.STYLESHEET);

        // parsing rendition segment
        List<Element> renditionList = element.getChildren("rendition");
        for (Element renditionElement : renditionList) {
            ThemeMetadataTemplateRendition rendition = new ThemeMetadataTemplateRendition();
            String renditionValue = renditionElement.getAttributeValue("type");
            if (renditionValue != null) {
                try {
                    rendition.setType(RenditionType.valueOf(renditionValue.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ThemeParsingException("Invalid rendition type " + renditionValue + " found.");
                }
            } else {
                // default to standard if type not provided (most templates don't have multiple renditions anyway)
                rendition.setType(RenditionType.STANDARD);
            }
            String templateString = renditionElement.getChildText("templateLanguage");
            if (StringUtils.isEmpty(templateString)) {
                throw new ThemeParsingException("rendition must contain a 'templateLanguage' element");
            } else {
                try {
                    rendition.setTemplateLang(TemplateLanguage.valueOf(templateString.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ThemeParsingException("Unknown templateLanguage value '" + templateString + "'");
                }
            }
            rendition.setContentsFile(renditionElement.getChildText("contentsFile"));
            if (StringUtils.isEmpty(rendition.getContentsFile())) {
                throw new ThemeParsingException("stylesheet must contain a 'contentsFile' element");
            }
            template.addTemplateRendition(rendition);
        }

        // validate template
        if(StringUtils.isEmpty(template.getName())) {
            throw new ThemeParsingException("stylesheet must contain a 'name' element");
        }
        if(StringUtils.isEmpty(template.getLink())) {
            throw new ThemeParsingException("stylesheet must contain a 'link' element");
        }

        return template;
    }
    
}
