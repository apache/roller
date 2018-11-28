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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>


<%-- URLs used by this page --%>

<s:url var="xinhaHome" value="/roller-ui/authoring/editors/xinha-0.95"></s:url>

<s:url var="mediaFileImageChooser" action="mediaFileImageChooser" namespace="overlay">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>


<script type="text/javascript">
    // (preferably absolute) URL (including trailing slash) where Xinha is installed
    _editor_url  = '<s:property value="xinhaHome" />';
    _editor_lang = "en";        // And the language we need to use in the editor.
    _editor_skin = "blue-look"; // If you want use a skin, add the name (of the folder) here
</script>
<script type="text/javascript" src="<s:property value="xinhaHome" />/XinhaCore.js"></script>


<%-- ********************************************************************* --%>
<%-- Xinha config (see also: http://trac.xinha.org/wiki/NewbieGuide) --%>

<script type="text/javascript">
    xinha_editors = null;
    xinha_init    = null;
    xinha_config  = null;
    xinha_plugins = null;

    xinha_init = xinha_init ? xinha_init : function() {

        xinha_editors = xinha_editors ? xinha_editors : [
            'xe_content', 'xe_summary'
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
        xinha_editors.xe_content.config.height = '300px';
        xinha_editors.xe_summary.config.height = '200px';

        Xinha.startEditors(xinha_editors);
    }

    Xinha._addEvent(window,'load', xinha_init);
</script>


<%-- ********************************************************************* --%>
<%-- Editor event handling, on close, on add image, etc. --%>

<script type="text/javascript">

    YAHOO.namespace("mediaFileEditor");

    function init() {

        YAHOO.mediaFileEditor.lightbox = new YAHOO.widget.Panel(
        "mediafile_edit_lightbox", {
            modal:    true,
            width:   "600px",
            height:  "600px",
            visible: false,
            fixedcenter: true,
            constraintoviewport: true
        }
    );
        YAHOO.mediaFileEditor.lightbox.render(document.body);
    }
    YAHOO.util.Event.addListener(window, "load", init);

    function onClickAddImage(){
        $("#mediaFileEditor").attr('src','<s:property value="%{mediaFileImageChooser}" />');
        YAHOO.mediaFileEditor.lightbox.show();
    }

    function onClose() {
        $("#mediaFileEditor").attr('src','about:blank');
        YAHOO.mediaFileEditor.lightbox.hide();
    }

    function onSelectImage(name, url) {
        $("#mediaFileEditor").attr('src','about:blank');
        YAHOO.mediaFileEditor.lightbox.hide();

        xinha_editors.xe_content.insertHTML(
        '<a href="' + url + '"><img src="' + url + '?t=true" alt="' + name+ '"></img></a>');
    }
</script>

<script type="text/javascript">
    function editorCleanup() {
        document.getElementById('xe_content').value = xinha_editors.xe_content.getHTML().trim();
        document.getElementById('xe_summary').value = xinha_editors.xe_summary.getHTML().trim();
    }
</script>


<%-- ********************************************************************* --%>
<%-- Text editors --%>

<p class="toplabel"
    <span style="font-weight:normal;float:right;">
        <a href="#" onClick="onClickAddImage();">Add Image</a>&nbsp;
    </span>
</p>

<b><s:text name="weblogEdit.content" /></b><br />
<s:textarea id="xe_content" name="bean.text" rows="25" cols="50" cssStyle="width: 100%" />


<b><s:text name="weblogEdit.summary" /></b><br />
<s:textarea id="xe_summary" name="bean.summary" rows="10" cols="50" cssStyle="width: 100%" />


<%-- ********************************************************************* --%>
<%-- Lightbox for popping up image chooser --%>

<div id="mediafile_edit_lightbox" style="visibility:hidden">
    <div class="hd"><s:text name="mediaFileChooser.popupTitle" /></div>
    <div class="bd">
        <iframe id="mediaFileEditor"
                style="visibility:inherit"
                height="100%"
                width="100%"
                frameborder="no"
                scrolling="auto">
        </iframe>
    </div>
    <div class="ft"></div>
</div>
