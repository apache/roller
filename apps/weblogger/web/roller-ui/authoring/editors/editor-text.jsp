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
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<style>
    a:link, a:visited, a:hover, a:active     { text-decoration:underline; }
    body        {
        margin:0;
        padding:0;
        text-align:left;
    }
    h1          {
        font-size:20px;
        font-weight:bold;
    }
    .yui-overlay {
        position:fixed;
        background: #ffffff;
        z-index: 112;
        color:#000000;
        border: 4px solid #525252;
        text-align:left;
        top: 50%;
        left: 50%;
    }
</style>

<script type="text/javascript">
<!--
    YAHOO.namespace("mediaFileEditor");

    function init() {

        if (getCookie("editorSize1") != null) {
            document.getElementById('entry_bean_text').rows = getCookie("editorSize1");
        }
        if (getCookie("editorSize") != null) {
            document.getElementById('entry_bean_summary').rows = getCookie("editorSize");
        }

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

    function onClose(textForInsertion)
    {
        document.getElementById('overlay').style.display = 'none';
        document.getElementById('overlay_img').style.visibility = 'hidden';
        if (textForInsertion && textForInsertion.length > 0) {
            insertAtCursor(document.getElementById('entry_bean_text'), textForInsertion);
        }
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

    function onClickAddImage(){
        <s:url id="mediaFileImageChooser" action="mediaFileImageChooser" namespace="overlay">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
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
        insertAtCursor(document.getElementById('entry_bean_text'),
            '<a href="' + url + '"><img src="' + url + '?t=true" alt="' + name+ '"></img></a>');
    }
-->
</script>

<script type="text/javascript">
    <!--
    function editorCleanup() {
        // no-op
    }
    function changeSize(e, num) {
        a = e.rows + num;
        if (a > 0) e.rows = a;
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
        setCookie("editorSize",e.rows,expires);
    }
    function changeSize1(e, num) {
        a = e.rows + num;
        if (a > 0) e.rows = a;
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
        setCookie("editorSize1",e.rows,expires);
    }
    // -->
</script>


<%-- ===================================================================== --%>

<p class="toplabel">
    <span style="float:left;"><s:text name="weblogEdit.content" /></span>
    <span style="font-weight:normal;float:right;">
        <a href="#" onClick="onClickAddImage();">Add Image</a>&nbsp;
    </span>
</p>

<s:textarea name="bean.text" cols="75" rows="25" cssStyle="width: 100%" tabindex="5"/>
<table style="width:100%"><tr><td align="right">
    <!-- Add buttons to make this textarea taller or shorter -->
    <input type="button" name="taller" value=" &darr; "
           onclick="changeSize1(document.getElementById('entry_bean_text'), 5)" />
    <input type="button" name="shorter" value=" &uarr; "
           onclick="changeSize1(document.getElementById('entry_bean_text'), -5)" />
</td></tr></table>


<%-- ===================================================================== --%>

<p class="toplabel"><s:text name="weblogEdit.summary" /></p>

<s:textarea name="bean.summary" cols="75" rows="5" cssStyle="width: 100%" tabindex="6"/>

<table style="width:100%"><tr><td align="right">
    <!-- Add buttons to make this textarea taller or shorter -->
    <input type="button" name="taller" value=" &darr; "
           onclick="changeSize(document.getElementById('entry_bean_summary'), 5)" />
    <input type="button" name="shorter" value=" &uarr; "
           onclick="changeSize(document.getElementById('entry_bean_summary'), -5)" />
</td></tr></table>


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
