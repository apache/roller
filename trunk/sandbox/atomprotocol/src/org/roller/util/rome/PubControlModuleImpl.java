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

public class PubControlModuleImpl extends ModuleImpl implements PubControlModule {
    private Boolean _draft;

    public PubControlModuleImpl() {
        super(PubControlModule.class,PubControlModule.URI);
    }
    public Boolean getDraft() {
        return _draft;
    }
    public void setDraft(Boolean draft) {
        _draft = draft;
    }
    public Class getInterface() {
        return PubControlModule.class;
    }
    public void copyFrom(Object obj) {
        PubControlModule m = (PubControlModule)obj;
        setDraft(m.getDraft());
    }
}
