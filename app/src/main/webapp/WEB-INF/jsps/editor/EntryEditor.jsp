<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%-- This page is designed to be included in EntryEdit.jsp --%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>


<%-- ********************************************************************* --%>

<%-- content --%>
<s:if test="editor.id != 'editor-text.jsp'">
<div id="edit_content_html_notice" class="alert alert-info" role="status" style="display: none">
    <s:text name="weblogEdit.richEditorShowsHtml"/>
</div>
</s:if>
<s:textarea id="edit_content" name="bean.text"
            tabindex="5" rows="18" cssClass="col-sm-12" theme="simple"/>

<a href="#" onClick="onClickMediaFileInsert();"><s:text name="weblogEdit.insertMediaFile"/></a><br/>
<img src="<s:url value='/roller-ui/images/spacer.png' />" alt="spacer" style="min-height: 2em"/>

<%-- summary --%>

<div class="accordion-item" id="panel-summary">
    <h2 class="accordion-header">
        <button type="button" class="accordion-button collapsed"
                data-bs-toggle="collapse" data-bs-target="#collapseSummaryEditor"
                aria-expanded="false" aria-controls="collapseSummaryEditor">
            <s:text name="weblogEdit.summary"/>
        </button>
    </h2>
    <div id="collapseSummaryEditor" class="accordion-collapse collapse">
        <div class="accordion-body">

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
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
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
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Close</button>
            </div>

        </div>
    </div>

</div>

<s:if test="editor.id != 'editor-text.jsp'">
<script src="<s:url value='/roller-ui/scripts/rich-editor-content.js' />"></script>
</s:if>

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

    // Entries with HTML that Summernote would run or drop open in the textarea
    // as HTML; see rich-editor-content.js. Summernote starts the editor on any
    // call, so every call checks this first.
    var richEditorStarted = false;

    $(document).ready(function () {
        if (RollerRichEditor.hasActiveHtml(document.getElementById('edit_content').value)) {
            $('#edit_content_html_notice').show();
            $("#edit_content").one("change", warnBeforeLeaving);
            return;
        }
        richEditorStarted = true;
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
                height: 400,
                callbacks: {
                    onPaste: function (e) {
                        // Summernote inserts a pasted image file itself, but it
                        // does not stop the browser from also pasting the image's
                        // HTML, so an image copied from a web page appeared twice.
                        // Use the same clipboard item Summernote picks.
                        var clipboard = (e.originalEvent || e).clipboardData;
                        if (!clipboard || !clipboard.items || !clipboard.items.length) {
                            return;
                        }
                        var items = clipboard.items;
                        var item = items.length > 1 ? items[1] : items[0];
                        if (item.kind === 'file' && item.type.indexOf('image/') !== -1) {
                            e.preventDefault();
                        }
                    }
                }
            }
        );
        // Added event listener to confirm once the editor content is changed
        $('#edit_content').on('summernote.change', warnBeforeLeaving);
    });

    function warnBeforeLeaving() {
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
    }

    function insertMediaFile(toInsert) {
        if (richEditorStarted) {
            $('#edit_content').summernote("pasteHTML", toInsert);
        } else {
            // at the cursor, or at the end if the textarea hasn't had one
            var textArea = document.getElementById('edit_content');
            var start = textArea.selectionStart;
            if (start === 0 && textArea.selectionEnd === 0) {
                start = textArea.selectionEnd = textArea.value.length;
            }
            textArea.value = textArea.value.substring(0, start) + toInsert
                + textArea.value.substring(textArea.selectionEnd);
            textArea.selectionStart = textArea.selectionEnd = start + toInsert.length;
            textArea.focus();
            $(textArea).trigger("change");
        }
    }

    </s:else>

    <%-- Common functions --%>

    function onClickMediaFileInsert() {
        <s:url var="mediaFileImageChooser" action="mediaFileImageChooser" namespace="overlay">
        <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        $("#mediaFileEditor").attr('src', '<s:property value="%{mediaFileImageChooser}" escapeHtml="false" escapeJavaScript="true" />');
        $('#mediafile_edit_lightbox').modal('show');
    }

    function onClose() {
        $("#mediaFileEditor").attr('src', 'about:blank');
    }

    // Called by the media chooser. The markup is built with DOM APIs, so names,
    // descriptions and URLs are escaped. The alt text is the file's description,
    // or its name when it has none.
    function onSelectMediaFile(name, url, isImage, alt) {
        $("#mediafile_edit_lightbox").modal("hide");
        $("#mediaFileEditor").attr('src', 'about:blank');
        var link = $('<a>').attr('href', url);
        if (isImage === "true") {
            link.append($('<img>').attr({src: url + '?t=true', alt: alt || name}));
        } else {
            link.text(name);
        }
        insertMediaFile(link.prop('outerHTML'));
    }

</script>
