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

<%-- Media file details: preview, facts and actions first, then the metadata form.
     Links back to the library page the user came from (returnQuery). --%>

<s:if test="mediaFile != null">

<div id="mediaDetails" class="media-details"
     data-copied="<s:text name='mediaFileEdit.copied'/>"
     data-copy-failed="<s:text name='mediaFileEdit.copyFailed'/>"
     data-confirm-delete="<s:text name='mediaFileEdit.confirmDelete'/>"
     data-unsaved="<s:text name='mediaFileEdit.unsavedChanges'/>">

    <p class="media-back">
        <a href="<s:property value='libraryURL'/>">
            <span class="bi bi-arrow-left" aria-hidden="true"></span>
            <s:text name="mediaFileEdit.backToLibrary"/></a>
    </p>

    <div class="media-details-top">

        <div class="media-details-preview">
            <s:if test="mediaFile.imageFile">
                <a href="<s:property value='mediaFile.permalink'/>" target="_blank" rel="noopener">
                    <img src="<s:property value='mediaFile.permalink'/>" alt=""
                         data-fallback-icon="<s:property value='typeIcon(mediaFile.contentType)'/>"/>
                </a>
            </s:if>
            <s:else>
                <span class="bi <s:property value='typeIcon(mediaFile.contentType)'/>" aria-hidden="true"></span>
            </s:else>
        </div>

        <div class="media-details-facts">
            <h3 class="media-details-name"><s:property value="mediaFile.name"/></h3>

            <dl class="media-facts">
                <dt><s:text name="mediaFileView.type"/></dt>
                <dd><s:property value="formatType(mediaFile.contentType, mediaFile.name)"/>
                    <span class="text-body-secondary">&nbsp;(<s:property value="mediaFile.contentType"/>)</span></dd>
                <dt><s:text name="mediaFileView.size"/></dt>
                <dd><s:property value="formatSize(mediaFile.length)"/></dd>
                <s:if test="mediaFile.imageFile && mediaFile.width > 0">
                    <dt><s:text name="mediaFileEdit.dimensions"/></dt>
                    <dd><s:text name="mediaFileEdit.pixels">
                        <s:param value="mediaFile.width"/>
                        <s:param value="mediaFile.height"/>
                    </s:text></dd>
                </s:if>
                <dt><s:text name="mediaFileEdit.uploaded"/></dt>
                <dd><s:date name="mediaFile.dateUploaded" format="yyyy-MM-dd HH:mm"/></dd>
                <dt><s:text name="mediaFileEdit.directory"/></dt>
                <dd><s:property value="mediaFile.directory.name"/></dd>
            </dl>

            <label class="form-label" for="mediaFileURL"><s:text name="mediaFileEdit.permalink"/></label>
            <div class="input-group">
                <input type="text" id="mediaFileURL" class="form-control" readonly
                       value="<s:property value='mediaFile.permalink'/>"/>
                <button type="button" class="btn btn-outline-primary" id="copyURLButton">
                    <span class="bi bi-clipboard" aria-hidden="true"></span>
                    <s:text name="mediaFileEdit.copyURL"/></button>
            </div>
            <p class="media-copy-status" id="copyStatus" role="status"></p>

            <div class="media-details-actions">
                <a class="btn btn-outline-secondary" href="<s:property value='mediaFile.permalink'/>"
                   target="_blank" rel="noopener">
                    <span class="bi bi-box-arrow-up-right" aria-hidden="true"></span>
                    <s:text name="mediaFileEdit.openOriginal"/></a>

                <s:form id="createPostForm" action="entryAddWithMediaFile" theme="simple">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <input type="hidden" name="selectedImage" value="<s:property value='mediaFile.id'/>"/>
                    <button type="submit" class="btn btn-outline-secondary">
                        <span class="bi bi-file-earmark-plus" aria-hidden="true"></span>
                        <s:text name="mediaFileEdit.createEntry"/></button>
                </s:form>

                <s:form id="deleteMediaFileForm" action="mediaFileView!delete" theme="simple">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="mediaFileId"/>
                    <input type="hidden" name="directoryId" value="<s:property value='mediaFile.directory.id'/>"/>
                    <button type="submit" class="btn btn-outline-danger">
                        <span class="bi bi-trash" aria-hidden="true"></span>
                        <s:text name="generic.delete"/></button>
                </s:form>
            </div>
        </div>
    </div>

    <h3 class="media-edit-heading"><s:text name="mediaFileEdit.editDetails"/></h3>

    <s:form id="mediaFileEditForm" action="mediaFileEdit!save" method="POST" theme="bootstrap"
            cssClass="form-vertical media-edit-form">

        <s:hidden name="salt"/>
        <s:hidden name="weblog"/>
        <s:hidden name="mediaFileId"/>
        <s:hidden name="returnQuery"/>
        <s:hidden name="bean.permalink"/>

        <s:textfield name="bean.name" maxlength="255" label="%{getText('generic.name')}"/>

        <s:textarea name="bean.description" rows="2" label="%{getText('generic.description')}"/>

        <s:textfield name="bean.tagsAsString" maxlength="255" label="%{getText('mediaFileEdit.tags')}"/>

        <s:textfield name="bean.copyrightText" maxlength="1023" label="%{getText('mediaFileEdit.copyright')}"/>

        <s:select name="bean.directoryId" list="allDirectories" listKey="id" listValue="name"
                  label="%{getText('mediaFileEdit.directory')}"/>

        <s:checkbox name="bean.sharedForGallery" label="%{getText('mediaFileEdit.includeGallery')}"
                    tooltip="%{getText('mediaFileEdit.includeGalleryHelp')}"/>

        <s:if test="getBooleanProp('mediafile.originalPathEdit.enabled')">
            <details class="media-advanced" <s:if test="bean.originalPath != null && !bean.originalPath.isEmpty()">open</s:if>>
                <summary><s:text name="mediaFileEdit.advanced"/></summary>
                <s:textfield name="bean.originalPath" id="originalPath" maxlength="255"
                             label="%{getText('mediaFileEdit.originalPath')}"/>
                <p class="form-text"><s:text name="mediaFileEdit.originalPathHelp"/></p>
            </details>
        </s:if>

        <div class="media-edit-buttons">
            <s:submit cssClass="btn btn-primary" theme="simple" value="%{getText('generic.save')}"/>
            <a class="btn btn-outline-secondary" href="<s:property value='libraryURL'/>">
                <s:text name="generic.cancel"/></a>
        </div>

    </s:form>

</div>

<script src="<s:url value='/roller-ui/scripts/media-library.js'/>"></script>

</s:if>
<s:else>
    <p><a href="<s:property value='libraryURL'/>"><s:text name="mediaFileEdit.backToLibrary"/></a></p>
</s:else>
