/*
 * Copyright 2004 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.roller.util.rome;

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
                if ("yes".equals(draftElem.getText())) m.setDraft(Boolean.TRUE); 
                if ("no".equals(draftElem.getText())) m.setDraft(Boolean.FALSE);                
            }
        }
        return m.getDraft()!=null ? m : null;
    }
}

