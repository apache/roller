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
package org.apache.roller.planet.util.rome;

import com.sun.syndication.feed.CopyFrom;
import com.sun.syndication.feed.module.ModuleImpl;

public class ContentModuleImpl extends ModuleImpl implements ContentModule {
	private static final long serialVersionUID = 1L;
    private String _encoded;

    public ContentModuleImpl() {
        super(ContentModule.class,ContentModule.URI);
    }
	@Override
    public String getEncoded() {
        return _encoded;
    }
	@Override
    public void setEncoded(String encoded) {
        _encoded = encoded;
    }
	@Override
    public Class getInterface() {
        return ContentModule.class;
    }

	@Override
    public void copyFrom(CopyFrom obj) {
        ContentModule sm = (ContentModule) obj;
        setEncoded(sm.getEncoded());
	}
}
