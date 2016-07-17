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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
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
        <s:if test="entry.editFormat.name() == 'RICHTEXT'">
            <div id="toolbar_quill" class="toolbar">
                <span class="ql-format-group">
                    <select title="Font" class="ql-font">
                        <option value="sans-serif" selected="">Sans Serif</option>
                        <option value="serif">Serif</option>
                        <option value="monospace">Monospace</option>
                    </select>
                    <select title="Size" class="ql-size">
                        <option value="10px">Small</option>
                        <option value="13px" selected="">Normal</option>
                        <option value="18px">Large</option>
                        <option value="32px">Huge</option>
                    </select>
                </span>
                <span class="ql-format-group">
                    <span title="Bold" class="ql-format-button ql-bold"></span>
                    <span class="ql-format-separator"></span>
                    <span title="Italic" class="ql-format-button ql-italic"></span>
                    <span class="ql-format-separator"></span>
                    <span title="Underline" class="ql-format-button ql-underline"></span>
                    <span class="ql-format-separator"></span>
                    <span title="Strikethrough" class="ql-format-button ql-strike"></span>
                </span>
                <span class="ql-format-group">
                    <span title="List" class="ql-format-button ql-list"></span>
                    <span class="ql-format-separator"></span>
                    <span title="Bullet" class="ql-format-button ql-bullet"></span>
                    <span class="ql-format-separator"></span>
                    <select title="Text Alignment" class="ql-align">
                        <option value="left" label="Left" selected=""></option>
                        <option value="center" label="Center"></option>
                        <option value="right" label="Right"></option>
                        <option value="justify" label="Justify"></option>
                    </select>
                </span>
                <span class="ql-format-group">
                    <span title="Link" class="ql-format-button ql-link"></span>
                </span>
            </div>
            <div id="editor_quill"></div>
            <s:textarea id="edit_content" name="entry.text" cssStyle="display:none" tabindex="5" onBlur="this.value=this.value.trim()"/>
        </s:if>
        <s:else>
            <s:textarea id="edit_content" name="entry.text" cols="75" rows="25" cssStyle="width: 100%" tabindex="5" onBlur="this.value=this.value.trim()"/>
        </s:else>
    </div>
    <h3><s:text name="weblogEdit.summary"/><tags:help key="weblogEdit.summary.tooltip"/></h3>
    <div>
        <s:textarea id="edit_summary" name="entry.summary" cols="75" rows="10" cssStyle="width: 100%" tabindex="6" onBlur="this.value=this.value.trim()"/>
    </div>
    <h3><s:text name="weblogEdit.notes"/><tags:help key="weblogEdit.notes.tooltip"/></h3>
    <div>
        <s:textarea id="edit_notes" name="entry.notes" cols="75" rows="10" cssStyle="width: 100%" tabindex="7" onBlur="this.value=this.value.trim()"/>
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
    $(function() {
        $( "#accordion" ).accordion({
        });
    });
    function onClickAddImage(){
        <s:url var="mediaFileChooser" action="mediaFileChooser">
            <s:param name="weblogId" value="%{actionWeblog.id}" />
        </s:url>
        $( "#mediaFileEditor" ).attr('src','<s:property value="%{mediaFileChooser}" />');
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

    function onSelectMediaFile(name, url, alt, title, anchor, isImage) {
        $("#mediafile_edit_lightbox").dialog("close");
        $("#mediaFileEditor").attr('src','about:blank');
        if (isImage === true) {
            insertMediaFile(
            (anchor ? '<a href="' + anchor + '">' : '') +
            '<img src="' + url + '"' +
            ' alt="' + (alt ? alt : name) + '"' +
             (title ? ' title="' + title + '"' : '') +
             '/>' +
            (anchor ? '</a>' : ''));
        } else {
            insertMediaFile('<a href="' + url + '"' +
             (title ? ' title="' + title + '"' : '') +
            '>' + (alt ? alt : name) + '</a>');
        }
    }
</script>

<s:if test="entry.editFormat.name() == 'RICHTEXT'">
    <script src="//cdn.quilljs.com/0.20.1/quill.js"></script>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
    <link rel="stylesheet" href="//cdn.quilljs.com/0.20.1/quill.snow.css">
    <script>
        var editor = new Quill("#editor_quill", {
          modules: {
            'toolbar': { container: '#toolbar_quill' },
            'link-tooltip': true
          },
          theme: 'snow'
        });
        function retrieveText() {
          // populate hidden text area with data from RTE div
          var html = $("#ql-editor-1").html();
          $("#edit_content").val(html);
        };
        $(function() {
            // load from hidden text area into RTE div
            var textFromServer = $("#edit_content").val();
            $("#ql-editor-1").html(textFromServer);
        });
        function insertMediaFile(anchorTag) {
          var html = $("#ql-editor-1").html();
          html = html + anchorTag;
          $("#ql-editor-1").html(html);
        }
    </script>
</s:if>
<s:else>
    <%-- Plain text editor (raw HTML entry) --%>
    <script>
        function retrieveText() {
        };
        function insertMediaFile(anchorTag) {
            var textAreaElement = document.getElementById('edit_content');

            if (textAreaElement.selectionStart || textAreaElement.selectionStart == '0') {
                var preText = textAreaElement.value.substring(0, textAreaElement.selectionStart);
                var postText = textAreaElement.value.substring(textAreaElement.selectionStart, textAreaElement.value.length);
                textAreaElement.value = preText + anchorTag + postText;
                textAreaElement.selectionStart = preText.length + anchorTag.length;
                textAreaElement.selectionEnd = textAreaElement.selectionStart;
                textAreaElement.focus();
            } else {
                textAreaElement.value += anchorTag;
                textAreaElement.focus();
            }
        }
    </script>
</s:else>
