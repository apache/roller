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
package org.apache.roller.webservices.atomprotocol;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;

public class PubControlModuleParser implements ModuleParser {

    public String getNamespaceUri() {
        return PubControlModule.URI;
    }

    public Namespace getContentNamespace() {
        return Namespace.getNamespace(PubControlModule.URI);
    }
    public Module parse(Element elem) {
        boolean foundSomething = false;
        PubControlModule m = new PubControlModuleImpl();
        Element e = elem.getChild("control", getContentNamespace());
        if (e != null) {
            Element draftElem = e.getChild("draft", getContentNamespace());
            if (draftElem != null) {
                if ("yes".equals(draftElem.getText())) m.setDraft(true); 
                if ("no".equals(draftElem.getText())) m.setDraft(false);                
            }
        }
        return m;
    }
}

