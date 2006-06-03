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

import com.sun.syndication.feed.module.ModuleImpl;

public class ContentModuleImpl extends ModuleImpl implements ContentModule {
    private String _encoded;

    public ContentModuleImpl() {
        super(ContentModule.class,ContentModule.URI);
    }
    public String getEncoded() {
        return _encoded;
    }
    public void setEncoded(String encoded) {
        _encoded = encoded;
    }
    public Class getInterface() {
        return ContentModule.class;
    }
    public void copyFrom(Object obj) {
        ContentModule sm = (ContentModule) obj;
        setEncoded(sm.getEncoded());
    }

}
