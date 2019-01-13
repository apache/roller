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

<%-- content

<div class="panel panel-default" id="panel-content">
    <div class="panel-heading">
        <h4 class="panel-title">
            <a data-toggle="collapse" data-target="#collapseContentEditor" href="#">
                <s:text name="weblogEdit.content"/> </a>
        </h4>
    </div>
    <div id="collapseContentEditor" class="panel-collapse collapse in">
        <div class="panel-body">

            <s:textarea id="edit_content" name="bean.text"
                        tabindex="5" rows="18" cssClass="col-sm-12" theme="simple"/>

            <s:if test="editor.id == 'editor-text.jsp'">
                <span>
                    <a href="#" onClick="onClickAddImage();"><s:text name="weblogEdit.insertMediaFile"/></a>
                </span>
            </s:if>
        </div>
    </div>
</div>
--%>

<s:textarea id="edit_content" name="bean.text"
            tabindex="5" rows="18" cssClass="col-sm-12" theme="simple"/>

<%-- content --%>

<s:if test="editor.id == 'editor-text.jsp'">
    <span>
        <a href="#" onClick="onClickAddImage();"><s:text name="weblogEdit.insertMediaFile"/></a>
    </span>
</s:if>

<br/>
<img src="<s:url value='/roller-ui/images/spacer.png' />" alt="spacer" style="min-height: 2em" />

<%-- summary --%>

<div class="panel panel-default" id="panel-summary">
    <div class="panel-heading">

        <h4 class="panel-title">
            <a href="#" class="collapsed"
               data-toggle="collapse" data-target="#collapseSummaryEditor">
                <s:text name="weblogEdit.summary"/>
            </a>
        </h4>

    </div>
    <div id="collapseSummaryEditor" class="panel-collapse collapse">
        <div class="panel-body">

            <s:textarea id="edit_summary" name="bean.summary"
                        tabindex="6" rows="10" cssClass="col-sm-12" theme="simple"/>

        </div>
    </div>
</div>

<%-- ********************************************************************* --%>

<s:if test="editor.id == 'editor-text.jsp'">

    <%-- Media File Insert for plain textarea editor --%>

    <div id="mediafile_edit_lightbox" class="modal fade" role="dialog">

        <div class="modal-dialog modal-lg">

            <div class="modal-content">

                <div class="modal-header">
                    <h4 class="modal-title"><s:text name='weblogEdit.insertMediaFile'/></h4>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="modal-body">
                    <iframe id="mediaFileEditor"
                            style="visibility:inherit"
                            height="600" <%-- pixels, sigh, this is suboptimal--%>
                            width="100%"
                            frameborder="no"
                            scrolling="auto">
                    </iframe>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>

            </div>
        </div>

    </div>

    <script>
        function insertMediaFile(anchorTag) {
            insertAtCursor(document.getElementById('edit_content'), anchorTag);
        }

        function insertAtCursor(textAreaElement, valueForInsertion) {
            if (document.selection) {
                textAreaElement.focus();
                var range = document.selection.createRange();
                range.text = valueForInsertion;
            } else if (textAreaElement.selectionStart || textAreaElement.selectionStart === '0') {
                var preText;
                var postText;
                if (textAreaElement.selectionStart === 0) {
                    preText = '';
                    postText = '';
                } else {
                    preText = textAreaElement.value.substring(
                        0, textAreaElement.selectionStart);
                    postText = textAreaElement.value.substring(
                        textAreaElement.selectionEnd, textAreaElement.value.length);
                }
                textAreaElement.value = preText + valueForInsertion + postText;
                textAreaElement.selectionStart = preText.length + valueForInsertion.length;
                textAreaElement.selectionEnd = textAreaElement.selectionStart;
                textAreaElement.focus();
            } else {
                textAreaElement.value += valueForInsertion;
                textAreaElement.focus();
            }
        }

        function onClickAddImage() {
            <s:url var="mediaFileImageChooser" action="mediaFileImageChooser" namespace="overlay">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            $("#mediaFileEditor").attr('src', '<s:property value="%{mediaFileImageChooser}" />');
            $('#mediafile_edit_lightbox').modal({show: true});
        }

        function onClose() {
            $("#mediaFileEditor").attr('src', 'about:blank');
        }

        function onSelectMediaFile(name, url, isImage) {
            $("#mediafile_edit_lightbox").modal("hide");
            $("#mediaFileEditor").attr('src', 'about:blank');
            if (isImage === "true") {
                insertMediaFile('<a href="' + url + '"><img src="' + url + '?t=true" alt="' + name + '" /></a>');
            } else {
                insertMediaFile('<a href="' + url + '">' + name + '</a>');
            }
        }

    </script>

</s:if>
<s:else>

    <%-- Rich text editor --%>

    <script>
        $(document).ready(function () {
            $('#edit_content').summernote();
        });
    </script>

</s:else>
