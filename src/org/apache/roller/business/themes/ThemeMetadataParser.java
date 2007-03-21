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

package org.apache.roller.business.themes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


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
        throws IOException, JDOMException {
        
        if(instream == null)
            throw new IOException("InputStream is null!");
        
        ThemeMetadata theme = new ThemeMetadata();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(instream);
        
        // start at root and get theme id, name, and author
        Element root = doc.getRootElement();
        theme.setId(root.getChildText("id"));
        theme.setName(root.getChildText("name"));
        theme.setAuthor(root.getChildText("author"));
        
        // now grab the preview image path
        Element previewImage = root.getChild("preview-image");
        theme.setPreviewImage(previewImage.getAttributeValue("path"));
        
        // now grab the static resources
        List resources = root.getChildren("resource");
        Iterator resourcesIter = resources.iterator();
        while (resourcesIter.hasNext()) {
            Element resource = (Element) resourcesIter.next();
            theme.addResource(resource.getAttributeValue("path"));
        }
        
        // now grab the templates
        List templates = root.getChildren("template");
        Iterator templatesIter = templates.iterator();
        while (templatesIter.hasNext()) {
            Element template = (Element) templatesIter.next();
            theme.addTemplate(this.elementToTemplateMetadata(template));
        }
        
        // TODO: validation
        // make sure all required elements are present and values are valid
        
        return theme;
    }
    
    
    private ThemeMetadataTemplate elementToTemplateMetadata(Element element) {
        
        ThemeMetadataTemplate template = new ThemeMetadataTemplate();
        
        template.setAction(element.getAttributeValue("action"));
        template.setName(element.getChildText("name"));
        template.setDescription(element.getChildText("description"));
        template.setLink(element.getChildText("link"));
        template.setTemplateLanguage(element.getChildText("templateLanguage"));
        template.setContentType(element.getChildText("contentType"));
        template.setContentsFile(element.getChildText("contentsFile"));
        
        String navbar = element.getChildText("navbar");
        if("true".equalsIgnoreCase(navbar)) {
            template.setNavbar(true);
        }
        
        String hidden = element.getChildText("hidden");
        if("true".equalsIgnoreCase(hidden)) {
            template.setHidden(true);
        }
        
        return template;
    }
    
}
