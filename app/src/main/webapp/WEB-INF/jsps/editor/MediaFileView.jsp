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

<s:form id="createPostForm" action='entryAddWithMediaFile'>
    <s:hidden name="salt"/>
    <input type="hidden" name="weblog" value="<s:property value='actionWeblog.handle' />"/>
    <input type="hidden" name="selectedImage" id="selectedImage"/>
    <input type="hidden" name="type" id="type"/>
</s:form>

<s:url var="mediaFileAddURL" action="mediaFileAdd">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
    <s:param name="directoryName" value="%{directoryName}"/>
</s:url>

<div id="mediaLibrary" class="media-library"
     data-confirm-delete="<s:text name='mediaFileView.confirmDelete'/>"
     data-confirm-move="<s:text name='mediaFile.move.confirm'/>"
     data-confirm-delete-folder="<s:text name='mediaFile.deleteFolder.confirm'/>"
     data-selected-count="<s:text name='mediaFileView.selectedCount'/>">

<%-- Heading: where we are, and the main action --%>

<div class="media-header">
    <div>
        <s:if test="pager">
            <h3 class="media-heading"><s:text name="mediaFileView.searchTitle"/></h3>
        </s:if>
        <s:else>
            <h3 class="media-heading">
                <span class="bi bi-folder2" aria-hidden="true"></span>
                <s:property value="currentDirectory.name"/>
            </h3>
        </s:else>
    </div>
    <div class="media-folder-picker">
        <label class="form-label" for="folderPicker"><s:text name="mediaFileView.viewFolder"/></label>
        <select id="folderPicker" class="form-select">
            <s:iterator var="folder" value="allDirectories">
                <s:url var="folderURL" action="mediaFileView">
                    <s:param name="weblog" value="%{actionWeblog.handle}"/>
                    <s:param name="directoryId" value="%{#folder.id}"/>
                </s:url>
                <option value="<s:property value='%{folderURL}'/>"
                        <s:if test="#folder.id == currentDirectory.id">selected</s:if>><s:property value="#folder.name"/></option>
            </s:iterator>
        </select>
    </div>
    <a class="btn btn-primary" href="<s:property value='%{mediaFileAddURL}'/>">
        <span class="bi bi-upload" aria-hidden="true"></span>
        <s:text name="mediaFileView.upload"/>
    </a>
</div>

<%-- Search: name and type first; size and tags behind Filters --%>

<s:form id="mediaFileSearchForm" name="mediaFileSearchForm" action="mediaFileView!search"
        theme="simple" cssClass="media-search" role="search">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <%-- set by the result pager buttons; a new search starts at page 0 --%>
    <input type="hidden" id="beanPageNum" name="bean.pageNum" value="0"/>

    <div class="media-search-row">
        <div class="media-search-name">
            <label class="form-label" for="beanName"><s:text name="mediaFileView.nameStartsWith"/></label>
            <s:textfield id="beanName" name="bean.name" maxlength="255" cssClass="form-control"/>
        </div>
        <div class="media-search-type">
            <label class="form-label" for="beanType"><s:text name="mediaFileView.type"/></label>
            <s:select id="beanType" name="bean.type" list="fileTypes" listKey="key" listValue="value"
                      cssClass="form-select"/>
        </div>
        <div class="media-search-buttons">
            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="collapse"
                    data-bs-target="#mediaFilters" aria-expanded="false" aria-controls="mediaFilters">
                <span class="bi bi-sliders" aria-hidden="true"></span>
                <s:text name="mediaFileView.filters"/>
            </button>
            <s:submit id="searchButton" cssClass="btn btn-primary" value="%{getText('generic.search')}"/>
            <s:if test="pager">
                <s:url var="mediaFileViewURL" action="mediaFileView">
                    <s:param name="weblog" value="%{actionWeblog.handle}"/>
                </s:url>
                <a class="btn btn-outline-secondary" href="<s:property value='%{mediaFileViewURL}'/>">
                    <s:text name="mediaFileView.clearSearch"/></a>
            </s:if>
        </div>
    </div>

    <div id="mediaFilters" class="collapse media-filters">
        <fieldset class="media-filter-size">
            <legend class="form-label"><s:text name="mediaFileView.size"/></legend>
            <s:select id="sizeFilterTypeCombo" name="bean.sizeFilterType" list="sizeFilterTypes"
                      listKey="key" listValue="value" cssClass="form-select"
                      aria-label="%{getText('mediaFileView.sizeComparison')}"/>
            <s:textfield id="beanSize" name="bean.size" maxlength="10" cssClass="form-control"
                         aria-label="%{getText('mediaFileView.size')}"/>
            <s:select id="beanSizeUnit" name="bean.sizeUnit" list="sizeUnits" listKey="key" listValue="value"
                      cssClass="form-select" aria-label="%{getText('mediaFileView.sizeUnit')}"/>
        </fieldset>
        <div class="media-filter-tags">
            <label class="form-label" for="beanTags"><s:text name="mediaFileView.tags"/></label>
            <s:textfield id="beanTags" name="bean.tags" maxlength="50" cssClass="form-control"/>
        </div>
    </div>
</s:form>

<s:if test="pager">
    <p class="media-result-summary" role="status">
        <s:if test="pager.items.size() > 0">
            <s:text name="mediaFileView.matchingResults">
                <s:param value="pager.totalCount"/>
            </s:text>
        </s:if>
        <s:else>
            <s:text name="mediaFileView.noResults"/>
        </s:else>
    </p>
</s:if>

<%-- Files, with the selection toolbar above them --%>

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" theme="simple">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="directoryId"/>
    <s:hidden name="newDirectoryName"/>
    <input type="hidden" name="mediaFileId" value=""/>

    <s:if test="(!pager && childFiles.size() > 0) || (pager && pager.items.size() > 0)">

        <div class="media-toolbar">
            <div class="media-toolbar-group">
                <button type="button" class="btn btn-outline-secondary btn-sm" id="selectPageButton">
                    <s:text name="mediaFileView.selectPage"/></button>
                <button type="button" class="btn btn-outline-secondary btn-sm" id="clearSelectionButton" disabled>
                    <s:text name="mediaFileView.clearSelection"/></button>
                <span class="media-selected-count" id="selectedCount" aria-live="polite"></span>
            </div>
            <div class="media-toolbar-group">
                <label class="visually-hidden" for="moveTargetMenu"><s:text name="mediaFileView.moveTo"/></label>
                <s:select id="moveTargetMenu" name="selectedDirectory" list="allDirectories" listKey="id"
                          listValue="name" cssClass="form-select form-select-sm" disabled="true"/>
                <button type="button" class="btn btn-outline-primary btn-sm" id="moveButton" disabled>
                    <span class="bi bi-folder-symlink" aria-hidden="true"></span>
                    <s:text name="mediaFileView.move"/></button>
                <button type="button" class="btn btn-outline-danger btn-sm" id="deleteButton" disabled>
                    <span class="bi bi-trash" aria-hidden="true"></span>
                    <s:text name="generic.delete"/></button>
            </div>
            <s:if test="!pager">
                <div class="media-toolbar-group ms-auto">
                    <label for="sortByMenu"><s:text name="mediaFileView.sortBy"/></label>
                    <s:select id="sortByMenu" name="sortBy" list="sortOptions" listKey="key" listValue="value"
                              cssClass="form-select form-select-sm"/>
                </div>
            </s:if>
        </div>

        <s:if test="pager">
            <s:include value="/WEB-INF/jsps/editor/MediaFilePager.jsp"/>
        </s:if>

        <ul class="media-grid" id="mediaGrid">
            <s:if test="pager">
                <s:iterator var="mediaFile" value="pager.items">
                    <s:include value="/WEB-INF/jsps/editor/MediaFileCard.jsp"/>
                </s:iterator>
            </s:if>
            <s:else>
                <s:iterator var="mediaFile" value="childFiles">
                    <s:include value="/WEB-INF/jsps/editor/MediaFileCard.jsp"/>
                </s:iterator>
            </s:else>
        </ul>

        <s:if test="pager">
            <s:include value="/WEB-INF/jsps/editor/MediaFilePager.jsp"/>
        </s:if>

    </s:if>
    <s:elseif test="!pager">
        <div class="media-empty">
            <span class="bi bi-images" aria-hidden="true"></span>
            <p><s:text name="mediaFileView.noFiles"/></p>
            <a class="btn btn-primary" href="<s:property value='%{mediaFileAddURL}'/>">
                <s:text name="mediaFileView.upload"/></a>
        </div>
    </s:elseif>

    <s:if test="currentDirectory.name != 'default' && !pager">
        <div class="media-folder-actions">
            <button type="button" class="btn btn-link text-danger" id="deleteFolderButton">
                <s:text name="mediaFileView.deleteFolder"/></button>
        </div>
    </s:if>

</s:form>

</div>

<%-- ================================================================================================ --%>

<%-- view image modal --%>

<div id="mediafile_edit_lightbox" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="edit-subtitle">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3 class="modal-title fs-5" id="edit-subtitle"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal"
                        aria-label="<s:text name='generic.close'/>"></button>
            </div>

            <div class="modal-body">
                <iframe id="mediaFileEditor" title="<s:text name='mediaFileEdit.subtitle'/>"
                        style="visibility:inherit; width:100%; height:70vh; border:0"></iframe>
            </div>

        </div>
    </div>

</div>

<s:url var="mediaFileEditURL" action="mediaFileEdit">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>
<span id="mediaFileUrls" hidden
      data-edit-url="<s:property value='%{mediaFileEditURL}'/>"
      data-delete-selected-url="<s:url action='mediaFileView!deleteSelected'/>"
      data-move-selected-url="<s:url action='mediaFileView!moveSelected'/>"
      data-delete-folder-url="<s:url action='mediaFileView!deleteFolder'/>"
      data-create-folder-url="<s:url action='mediaFileView!createNewDirectory'/>"></span>

<script src="<s:url value='/roller-ui/scripts/media-library.js'/>"></script>
