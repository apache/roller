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
/*
 * WeblogEntrySet.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.apache.roller.weblogger.webservices.adminprotocol.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This class describes a set of weblog entries. 
 * @author jtb
 */
public class WeblogEntrySet extends EntrySet {
    interface Tags {
        String WEBLOGS = "weblogs";
    }      
    
    public WeblogEntrySet(String urlPrefix) {
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }
    
    public WeblogEntrySet(Document d, String urlPrefix) throws UnexpectedRootElementException {
        populate(d, urlPrefix);
    }
    
    public WeblogEntrySet(InputStream stream, String urlPrefix) throws JDOMException, IOException, UnexpectedRootElementException {
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);

        populate(d, urlPrefix);        
    }    
    
    private void populate(Document d, String urlPrefix) throws UnexpectedRootElementException {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.WEBLOGS)) {
            throw new UnexpectedRootElementException("ERROR: Unexpected root element", Tags.WEBLOGS, rootName);
        }
        List<Element> weblogs = root.getChildren(WeblogEntry.Tags.WEBLOG, Service.NAMESPACE);
        if (weblogs != null) {
            List<WeblogEntry> entries = new ArrayList<WeblogEntry>();
            for (Element weblog : weblogs) {
                WeblogEntry entry = new WeblogEntry(weblog, urlPrefix);
                entries.add(entry);
            }
            setEntries(entries.toArray(new Entry[0]));
        }
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }    
       
    public String getType() {
        return Types.WEBLOGS;
    }    
}
