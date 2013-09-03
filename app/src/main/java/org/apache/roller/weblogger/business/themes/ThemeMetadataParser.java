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

import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
        
        // start at root and get theme id, name, and author
        Element root = doc.getRootElement();
        theme.setId(root.getChildText("id"));
        theme.setName(root.getChildText("name"));
        theme.setAuthor(root.getChildText("author"));
        theme.setType(root.getChildText("type"));
        
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
        List resources = root.getChildren("resource");
        Iterator resourcesIter = resources.iterator();
        while (resourcesIter.hasNext()) {
            Element resource = (Element) resourcesIter.next();
            theme.addResource(resource.getAttributeValue("path"));
        }
        
        // now grab the templates
        boolean weblogActionTemplate = false;
        List templates = root.getChildren("template");
        Iterator templatesIter = templates.iterator();
        while (templatesIter.hasNext()) {
            Element template = (Element) templatesIter.next();
            ThemeMetadataTemplate tmpl = elementToTemplateMetadata(template);

            theme.addTemplate(tmpl);
            
            if(WeblogTemplate.ACTION_WEBLOG.equals(tmpl.getAction())) {
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

		template.setAction(element.getAttributeValue("action"));
		template.setName(element.getChildText("name"));
		template.setDescription(element.getChildText("description"));
		template.setLink(element.getChildText("link"));
		template.setTemplateLanguage(element.getChildText("templateLanguage"));
		template.setContentType(element.getChildText("contentType"));
		template.setContentsFile(element.getChildText("contentsFile"));

		//parsing tempaltecode segment
		List templateCodeList = element.getChildren("templateCode");
		Iterator templCodeitr = templateCodeList.iterator();

		boolean roller50format = false;
		while (templCodeitr.hasNext()) {
			Element templateCodeElement = (Element) templCodeitr.next();

			ThemeMetadataTemplateCode templateCode = new ThemeMetadataTemplateCode();
			templateCode.setTemplateLang(templateCodeElement.getChildText("templateLanguage"));
			templateCode.setContentsFile(templateCodeElement.getChildText("contentsFile"));
			templateCode.setContentType(templateCodeElement.getChildText("contentType"));
			templateCode.setType(templateCodeElement.getChildText("type"));

			// validating template code
			if (StringUtils.isEmpty(templateCode.getContentsFile())) {
				throw new ThemeParsingException("templateCode must contain a 'contentsFile' element");
			}
			if (StringUtils.isEmpty(templateCode.getTemplateLang())) {
				throw new ThemeParsingException("templateCode must contain a 'templateLanguage' element");
			}
			if (StringUtils.isEmpty(templateCode.getType())) {
				throw new ThemeParsingException("templateCode must contain a 'type' element");
			}
			template.addTemplateCode(templateCode.getType(), templateCode);

			// if theme has type, then it's roller50format
			roller50format = true;
		}

		// hack to ensure old format themes still work
		if (!roller50format) {
			ThemeMetadataTemplateCode templateCode = new ThemeMetadataTemplateCode();
			templateCode.setTemplateLang(template.getTemplateLanguage());
			templateCode.setContentsFile(template.getContentsFile());
			templateCode.setContentType(template.getContentType());
			templateCode.setType("standard");
			template.addTemplateCode("standard", templateCode);
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
		if (StringUtils.isEmpty(template.getAction())) {
			throw new ThemeParsingException("templates must contain an 'action' attribute");
		}
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

        // parsing templatecode segment
         List templateCodeList = element.getChildren("templateCode");
        Iterator templCodeitr = templateCodeList.iterator();

		boolean roller50format = false;
        while (templCodeitr.hasNext()){
            Element templateCodeElement = (Element) templCodeitr.next();

            ThemeMetadataTemplateCode templateCode = new ThemeMetadataTemplateCode();
            templateCode.setTemplateLang(templateCodeElement.getChildText("templateLanguage"));
            templateCode.setContentsFile(templateCodeElement.getChildText("contentsFile"));
            templateCode.setContentType(templateCodeElement.getChildText("contentType"));
            templateCode.setType(templateCodeElement.getChildText("type"));

            //validating stylesheet template code.
            if (StringUtils.isEmpty(templateCode.getContentsFile())) {
                throw new ThemeParsingException("stylesheet must contain a 'contentsFile' element");
            }
            if (StringUtils.isEmpty(templateCode.getTemplateLang())) {
                throw new ThemeParsingException("stylesheet must contain a 'templateLanguage' element");
            }
            if (StringUtils.isEmpty(templateCode.getType())) {
				throw new ThemeParsingException("templateCode must contain a 'type' element");
            }
            template.addTemplateCode(templateCode.getType(), templateCode);

			// if theme has type, then it's roller50format
			roller50format = true;
        }
        
		// hack to ensure old format themes still work
   		if (!roller50format) {
			ThemeMetadataTemplateCode templateCode = new ThemeMetadataTemplateCode();
			templateCode.setTemplateLang(template.getTemplateLanguage());
			templateCode.setContentsFile(template.getContentsFile());
			templateCode.setContentType(template.getContentType());
			templateCode.setType("standard");
			template.addTemplateCode("standard", templateCode);
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
