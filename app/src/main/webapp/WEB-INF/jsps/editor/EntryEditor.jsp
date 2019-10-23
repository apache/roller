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

<%-- content --%>
<s:textarea id="edit_content" name="bean.text"
            tabindex="5" rows="18" cssClass="col-sm-12" theme="simple"/>

<a href="#" onClick="onClickMediaFileInsert();"><s:text name="weblogEdit.insertMediaFile"/></a><br/>
<img src="<s:url value='/roller-ui/images/spacer.png' />" alt="spacer" style="min-height: 2em"/>

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

    <s:if test="editor.id == 'editor-text.jsp'">

    <%-- Plain text editor functions --%>

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
    // Added event listener to confirm once the editor content is changed
    $("#edit_content").one("change", function() {
        var confirmFunction = function(event) {
            // Chrome requires returnValue to be set and original event is found as originalEvent
            // see https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onbeforeunload#Example
            if (event.originalEvent)
                event.originalEvent.returnValue = "Are you sure you want to leave?";
            return "Are you sure you want to leave?";
        }
        $(window).on("beforeunload", confirmFunction);

        // Remove it if it is form submit
        $(this.form).on('submit', function() {
            $(window).off("beforeunload", confirmFunction);
        });
    });

    </s:if>
    <s:else>

    <%-- Rich text editor functions --%>

    $(document).ready(function () {
        $('#edit_content').summernote({
                toolbar: [
                    // [groupName, [list of button]]
                    ['style', ['bold', 'italic', 'underline', 'clear']],
                    ['font', ['strikethrough', 'superscript', 'subscript']],
                    ['fontsize', ['fontsize']],
                    ['color', ['color']],
                    ['para', ['ul', 'ol', 'paragraph']],
                    ['height', ['height']],
                    ['misc', ['codeview']],
                    ['insert', ['link']]
                ],
                height: 400
            }
        );
        // Added event listener to confirm once the editor content is changed
        $('#edit_content').on('summernote.change', function(we, contents, $editable) {
            var confirmFunction = function(event) {
                // Chrome requires returnValue to be set and original event is found as originalEvent
                // see https://developer.mozilla.org/en-US/docs/Web/API/WindowEventHandlers/onbeforeunload#Example
                if (event.originalEvent)
                    event.originalEvent.returnValue = "Are you sure you want to leave?";
                return "Are you sure you want to leave?";
            }
            $(window).on("beforeunload", confirmFunction);

            // Remove it if it is form submit
            $(this.form).on('submit', function() {
                $(window).off("beforeunload", confirmFunction);
            });
        });
    });

    function insertMediaFile(toInsert) {
        $('#edit_content').summernote("pasteHTML", toInsert);
    }

    </s:else>

    <%-- Common functions --%>

    function onClickMediaFileInsert() {
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
