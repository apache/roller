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

package org.apache.roller.ui.core.plugins;

import org.apache.roller.business.WeblogEntryEditor;
import org.apache.roller.util.MessageUtilities;


/**
 * A rich text wysiwyg editor using Xinha.
 */
public class XinhaEditor implements WeblogEntryEditor {
    
    
    public XinhaEditor() {}
    
    
    public String getId() {
        return "XinhaEditor";
    }
    
    public String getName() {
        return MessageUtilities.getString("editor.xinha.name");
    }
    
    public String getJspPage() {
        return "/roller-ui/authoring/editors/editor-xinha.jsp";
    }
    
}
