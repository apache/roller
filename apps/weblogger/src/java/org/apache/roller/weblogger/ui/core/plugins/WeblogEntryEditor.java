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

package org.apache.roller.weblogger.ui.core.plugins;

/**
 * Represents an editor for a WeblogEntry.
 */
public interface WeblogEntryEditor {
    
    /**
     * The unique identifier for this editor.
     *
     * It is important that each editor have a unique identifier.  The id for 
     * the editor is how it will be tracked and associated with each weblog 
     * that is using it, so having 2 editors with the same id would cause some
     * nasty problems.  It is also preferable if the id not be a full class
     * name because then if you ever want to refactor the location of the class
     * then you have a problem, so just pick a simple unique name.
     *
     * @return The unique identifier for this WeblogEntryEditor.
     */
    public String getId();
    
    
    /**
     * The display name for the editor, as seen by users.
     *
     * This is the name that users will see on the editors option list, so
     * pick something that's easy for users to remember and recognize.
     *
     * It is also a good idea for the name to be internationalized if possible.
     *
     * @return The display name of this WeblogEntryEditor.
     */
    public String getName();
    
    
    /**
     * The location of the jsp page inside the webapp that renders this editor.
     *
     * Example: /roller-ui/authoring/editors/editor-text.jsp
     *
     * @return The location of the jsp for this editor.
     */
    public String getJspPage();

}
