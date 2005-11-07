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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;

public class PubControlModuleGenerator implements ModuleGenerator {
    private static final Namespace PUBCONTROL_NS  = 
        Namespace.getNamespace("app", PubControlModule.URI);

    public String getNamespaceUri() {
        return PubControlModule.URI;
    }

    private static final Set NAMESPACES;

    static {
        Set nss = new HashSet();
        nss.add(PUBCONTROL_NS);
        NAMESPACES = Collections.unmodifiableSet(nss);
    }

    public Set getNamespaces() {
        return NAMESPACES;
    }

    public void generate(Module module, Element element) {
        PubControlModule m = (PubControlModule)module;
        if (m.getDraft() != null) {
            String draft = m.getDraft().booleanValue() ? "yes" : "no";
            Element control = new Element("control", PUBCONTROL_NS);
            control.addContent(generateSimpleElement("draft", draft));
            element.addContent(control);
        }
    }

    protected Element generateSimpleElement(String name, String value)  {
        Element element = new Element(name, PUBCONTROL_NS);
        element.addContent(value);
        return element;
    }

}
