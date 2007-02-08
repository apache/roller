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

package org.apache.roller.planet.config.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * The parser for the rollerRuntimeConfigDefs.xml file.
 * This class uses jdom to unmarshall the xml into a series of java objects.
 */
public class RuntimeConfigDefsParser {
    
    /** Creates a new instance of RuntimeConfigDefsParser */
    public RuntimeConfigDefsParser() {}
    
    
    /**
     * Unmarshall the given input stream into our defined
     * set of Java objects.
     **/
    public RuntimeConfigDefs unmarshall(InputStream instream)
            throws IOException, JDOMException {
        
        if(instream == null)
            throw new IOException("InputStream is null!");
        
        RuntimeConfigDefs configs = new RuntimeConfigDefs();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(instream);
        
        Element root = doc.getRootElement();
        List configdefs = root.getChildren("config-def");
        Iterator iter = configdefs.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            configs.addConfigDef(this.elementToConfigDef(e));
        }
        
        return configs;
    }
    
    
    private ConfigDef elementToConfigDef(Element element) {
        
        ConfigDef configdef = new ConfigDef();
        
        configdef.setName(element.getAttributeValue("name"));
        
        List displaygroups = element.getChildren("display-group");
        Iterator iter = displaygroups.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            configdef.addDisplayGroup(this.elementToDisplayGroup(e));
        }
        
        return configdef;
    }
    
    
    private DisplayGroup elementToDisplayGroup(Element element) {
        
        DisplayGroup displaygroup = new DisplayGroup();
        
        displaygroup.setName(element.getAttributeValue("name"));
        displaygroup.setKey(element.getAttributeValue("key"));
        
        List displaygroups = element.getChildren("property-def");
        Iterator iter = displaygroups.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            displaygroup.addPropertyDef(this.elementToPropertyDef(e));
        }
        
        return displaygroup;
    }
    
    
    private PropertyDef elementToPropertyDef(Element element) {
        
        PropertyDef prop = new PropertyDef();
        
        prop.setName(element.getAttributeValue("name"));
        prop.setKey(element.getAttributeValue("key"));
        prop.setType(element.getChildText("type"));
        prop.setDefaultValue(element.getChildText("default-value"));
        
        // optional elements
        if(element.getChild("rows") != null)
            prop.setRows(element.getChildText("rows"));
        
        if(element.getChild("cols") != null)
            prop.setCols(element.getChildText("cols"));
        
        return prop;
    }
    
}
