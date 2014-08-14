<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%-- This page is designed to be included in EntryEdit.jsp --%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%-- ********************************************************************* --%>
<%-- Text editors --%>

<p class="toplabel">

</p>

<div id="accordion">
    <h3>
        <s:text name="weblogEdit.content" />
        <span style="font-weight:normal;float:right;">
            <a href="#" onClick="onClickAddImage();"><s:text name="weblogEdit.insertMediaFile" /></a>
        </span>
    </h3>
    <div>
        <s:textarea id="edit_content" name="bean.text" cols="75" rows="25" cssStyle="width: 100%" tabindex="5"/>
    </div>
    <h3><s:text name="weblogEdit.summary"/><tags:help key="weblogEdit.summary.tooltip"/></h3>
    <div>
        <s:textarea id="edit_summary" name="bean.summary" cols="75" rows="10" cssStyle="width: 100%" tabindex="6"/>
    </div>
</div>


<%-- ********************************************************************* --%>
<%-- Lightbox for popping up image chooser --%>

<div id="mediafile_edit_lightbox" title="<s:text name='weblogEdit.insertMediaFile'/>" style="display:none">
    <iframe id="mediaFileEditor"
            style="visibility:inherit"
            height="100%"
            width="100%"
            frameborder="no"
            scrolling="auto">
    </iframe>
</div>

<%-- ********************************************************************* --%>
<%-- Editor event handling, on close, on add image, etc. --%>

<script>
    function onClickAddImage(){
        <s:url var="mediaFileImageChooser" action="mediaFileImageChooser" namespace="overlay">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        $( "#mediaFileEditor" ).attr('src','<s:property value="%{mediaFileImageChooser}" />');
        $(function() {
            $("#mediafile_edit_lightbox").dialog({
                modal  : true,
                width  : 600,
                height : 600
            });
        });
    }

    function onClose() {
        $("#mediaFileEditor").attr('src','about:blank');
    }

    function onSelectMediaFile(name, url, isImage) {
        $("#mediafile_edit_lightbox").dialog("close");
        $("#mediaFileEditor").attr('src','about:blank');
        if (isImage == "true") {
            insertMediaFile('<a href="' + url + '"><img src="' + url + '?t=true" alt="' + name+ '"></img></a>');
        } else {
            insertMediaFile('<a href="' + url + '">' + name + '</a>');
        }
    }
</script>

<s:if test="editor.id == 'editor-text.jsp'">
    <%-- Plain text editor (raw HTML entry) --%>

    <script>
        $(function() {
            $( "#accordion" ).accordion({
            });
        });
        function insertMediaFile(anchorTag) {
            insertAtCursor(document.getElementById('edit_content'), anchorTag);
        }
        function insertAtCursor(textAreaElement, valueForInsertion) {
            if (document.selection) {
                textAreaElement.focus();
                var range = document.selection.createRange();
                range.text = valueForInsertion;
            }
            else if (textAreaElement.selectionStart || textAreaElement.selectionStart == '0') {
                var preText;
                var postText;
                if (textAreaElement.selectionStart == 0) {
                    preText = '';
                    postText = '';
                }
                else {
                    preText = textAreaElement.value.substring(0, textAreaElement.selectionStart);
                    postText = textAreaElement.value.substring(textAreaElement.selectionEnd, textAreaElement.value.length);
                }
                textAreaElement.value =  preText + valueForInsertion + postText;
                textAreaElement.selectionStart = preText.length + valueForInsertion.length;
                textAreaElement.selectionEnd = textAreaElement.selectionStart;
                textAreaElement.focus();
            } else {
                textAreaElement.value += valueForInsertion;
                textAreaElement.focus();
            }
        }
    </script>
</s:if>
<s:else>
    <%-- Rich text editor (Xinha, see: http://trac.xinha.org/wiki/NewbieGuide) --%>

    <s:url var="xinhaHome" value="/roller-ui/authoring/editors/xinha-0.96.1"></s:url>
    <script>
        // (preferably absolute) URL (including trailing slash) where Xinha is installed
        _editor_url  = '<s:property value="xinhaHome" />';
        _editor_lang = "en";        // And the language we need to use in the editor.
        _editor_skin = "blue-look"; // If you want use a skin, add the name (of the folder) here
    </script>
    <script src="<s:property value="xinhaHome" />/XinhaCore.js"></script>

    <script>
        $(function() {
            $( "#accordion" ).accordion({
                activate: function( event, ui ) {
                   <%-- Xinha summary editor needs a one-time init as it is
                        not visible upon window opening (http://tinyurl.com/mn97j5l) --%>
                   if (!summary_editor_initialized) {
                       xinha_editors.edit_summary.sizeEditor();
                       summary_editor_initialized = true;
                   }
                }
            });
        });

        function insertMediaFile(anchorTag) {
            xinha_editors.edit_content.insertHTML(anchorTag);
        }

        summary_editor_initialized = false;
        xinha_editors = null;
        xinha_init    = null;
        xinha_config  = null;
        xinha_plugins = null;

        xinha_init = xinha_init ? xinha_init : function() {

            xinha_editors = xinha_editors ? xinha_editors : [
                'edit_content', 'edit_summary'
            ];

            xinha_plugins = xinha_plugins ? xinha_plugins :[];
            if(!Xinha.loadPlugins(xinha_plugins, xinha_init)) return;

            xinha_config = xinha_config ? xinha_config() : new Xinha.Config();
            xinha_config.pageStyleSheets = [ _editor_url + "examples/full_example.css" ];
            xinha_config.toolbar =
                [
                ["popupeditor"],
                ["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough"],
                ["separator","forecolor","hilitecolor","textindicator"],
                ["separator","subscript","superscript"],
                ["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],
                ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
                ["separator","inserthorizontalrule","createlink","insertimage","inserttable"],
                ["linebreak","separator","undo","redo","selectall","print"], (Xinha.is_gecko ? [] : ["cut","copy","paste","overwrite","saveas"]),
                ["separator","killword","clearfonts","removeformat","toggleborders","splitblock","lefttoright", "righttoleft"],
                ["separator","htmlmode","showhelp","about"]
            ];

            // turn off Xinha's URL stripping default. Blog entries need absolute URLs,
            // otherwise links will be broken in RSS/Atom feeds.
            xinha_config.stripBaseHref = false;

            xinha_editors   = Xinha.makeEditors(xinha_editors, xinha_config, xinha_plugins);

            Xinha.startEditors(xinha_editors);
        }

        Xinha._addEvent(window,'load', xinha_init);
    </script>
</s:else>
