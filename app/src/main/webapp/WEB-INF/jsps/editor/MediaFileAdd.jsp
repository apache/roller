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

<%-- Upload media files. One file picker (or drag and drop) fills a list; the shared metadata
     comes after it. With JavaScript (media-upload.js) the files go in one request whose per-file
     results come back as JSON, so failed files can be retried alone. Without it, the form posts
     normally and the success page lists the results. --%>

<s:url var="mediaFileViewURL" escapeAmp="false" action="mediaFileView">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
    <s:param name="directoryName" value="%{directoryName}"/>
</s:url>

<p class="media-back">
    <a href="<s:property value='%{mediaFileViewURL}'/>">
        <span class="bi bi-arrow-left" aria-hidden="true"></span>
        <s:text name="mediaFileEdit.backToLibrary"/></a>
</p>

<s:if test="!uploadsEnabled">
    <div class="alert alert-warning"><s:text name="mediaFileAdd.uploadsDisabled"/></div>
</s:if>
<s:else>

<s:url var="doneURL" escapeAmp="false" action="mediaFileAdd!done">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
</s:url>
<s:url var="folderNewestURL" escapeAmp="false" action="mediaFileView">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
</s:url>

<s:form id="uploadForm" action="mediaFileAdd!save" method="POST" enctype="multipart/form-data"
        theme="simple" cssClass="media-upload">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="directoryName"/>

    <div id="mediaUpload" hidden
         data-max-files="<s:property value='maxFiles'/>"
         data-max-file-mb="<s:property value='maxFileMB'/>"
         data-done-url="<s:property value='%{doneURL}'/>"
         data-folder-url="<s:property value='%{folderNewestURL}'/>"
         data-status-queued="<s:text name='mediaFileAdd.statusQueued'/>"
         data-status-uploading="<s:text name='mediaFileAdd.statusUploading'/>"
         data-status-uploaded="<s:text name='mediaFileAdd.statusUploaded'/>"
         data-status-failed="<s:text name='mediaFileAdd.statusFailed'/>"
         data-status-unknown="<s:text name='mediaFileAdd.statusUnknown'/>"
         data-too-large="<s:text name='mediaFileAdd.tooLarge'/>"
         data-too-many="<s:text name='mediaFileAdd.tooManyFiles'/>"
         data-remove="<s:text name='mediaFileAdd.remove'/>"
         data-selected-count="<s:text name='mediaFileAdd.selectedCount'/>"
         data-uploading="<s:text name='mediaFileAdd.uploading'/>"
         data-some-failed="<s:text name='mediaFileAdd.someFailed'/>"
         data-retry="<s:text name='mediaFileAdd.retry'/>"
         data-unknown-outcome="<s:text name='mediaFileAdd.unknownOutcome'/>"
         data-server-error="<s:text name='mediaFileAdd.serverError'/>"></div>

    <div class="media-upload-folder">
        <label class="form-label" for="uploadFolder"><s:text name="mediaFileAdd.folder"/></label>
        <s:select id="uploadFolder" name="bean.directoryId" list="allDirectories" listKey="id" listValue="name"
                  cssClass="form-select"/>
    </div>

    <div id="dropZone" class="media-drop-zone">
        <span class="bi bi-cloud-arrow-up" aria-hidden="true"></span>
        <label class="form-label" for="fileInput"><s:text name="mediaFileAdd.dropHere"/></label>
        <input type="file" id="fileInput" name="uploadedFiles" multiple class="form-control"
               aria-describedby="uploadLimits"/>
        <p class="form-text" id="uploadLimits">
            <s:text name="mediaFileAdd.limits">
                <s:param value="maxFiles"/>
                <s:param value="maxFileMB"/>
                <s:param value="maxDirMB"/>
            </s:text>
        </p>
    </div>

    <ul id="fileList" class="media-upload-list" aria-label="<s:text name='mediaFileAdd.fileListLabel'/>"></ul>

    <details class="media-upload-shared">
        <summary><s:text name="mediaFileAdd.shared"/></summary>
        <p class="form-text"><s:text name="mediaFileAdd.sharedHelp"/></p>

        <label class="form-label" for="uploadDescription"><s:text name="generic.description"/></label>
        <s:textarea id="uploadDescription" name="bean.description" rows="2" cssClass="form-control"/>

        <label class="form-label" for="uploadTags"><s:text name="mediaFileAdd.tags"/></label>
        <s:textfield id="uploadTags" name="bean.tagsAsString" maxlength="255" cssClass="form-control"/>

        <label class="form-label" for="uploadCopyright"><s:text name="mediaFileAdd.copyright"/></label>
        <s:textarea id="uploadCopyright" name="bean.copyrightText" rows="2" cssClass="form-control"/>

        <div class="form-check">
            <s:checkbox id="uploadGallery" name="bean.sharedForGallery" cssClass="form-check-input"/>
            <label class="form-check-label" for="uploadGallery"><s:text name="mediaFileAdd.includeGallery"/></label>
            <p class="form-text"><s:text name="mediaFileEdit.includeGalleryHelp"/></p>
        </div>
    </details>

    <div class="media-upload-buttons">
        <button type="submit" id="uploadButton" class="btn btn-primary">
            <span class="bi bi-upload" aria-hidden="true"></span>
            <span id="uploadButtonLabel"><s:text name="mediaFileAdd.upload"/></span></button>
        <a class="btn btn-outline-secondary" href="<s:property value='%{mediaFileViewURL}'/>">
            <s:text name="generic.cancel"/></a>
        <a class="btn btn-outline-success" id="continueLink" hidden href="#">
            <s:text name="mediaFileAdd.continue"/></a>
    </div>

    <div id="uploadStatus" class="media-upload-status" role="status" aria-live="polite"></div>

</s:form>

<script src="<s:url value='/roller-ui/scripts/media-upload.js'/>"></script>

</s:else>
