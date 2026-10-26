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

<%-- The media library: a page of files from a folder or a search. The search form is a GET,
     so the page state is in the URL; the selection actions POST with that state in hidden
     fields and come back to the same page. --%>

<s:url var="mediaFileAddURL" escapeAmp="false" action="mediaFileAdd">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
    <s:param name="directoryName" value="%{directoryName}"/>
</s:url>
<s:url var="clearSearchURL" escapeAmp="false" action="mediaFileView">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
    <s:param name="directoryId" value="%{directoryId}"/>
</s:url>

<div id="mediaLibrary" class="media-library"
     data-confirm-delete="<s:text name='mediaFileView.confirmDelete'/>"
     data-confirm-move="<s:text name='mediaFile.move.confirm'/>"
     data-confirm-delete-folder="<s:text name='mediaFile.deleteFolder.confirm'/>"
     data-selected-count="<s:text name='mediaFileView.selectedCount'/>">

<%-- Heading: where we are, and the main action --%>

<div class="media-header">
    <h3 class="media-heading">
        <s:if test="search">
            <span class="bi bi-search" aria-hidden="true"></span>
            <s:text name="mediaFileView.searchTitle"/>
        </s:if>
        <s:else>
            <span class="bi bi-folder2" aria-hidden="true"></span>
            <s:property value="currentDirectory.name"/>
        </s:else>
    </h3>
    <div class="media-folder-picker">
        <label class="form-label" for="folderPicker"><s:text name="mediaFileView.viewFolder"/></label>
        <select id="folderPicker" class="form-select">
            <s:iterator var="folder" value="allDirectories">
                <s:url var="folderURL" escapeAmp="false" action="mediaFileView">
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

<%-- Search: name, type and scope first; size and tags behind Filters --%>

<s:form id="mediaFileSearchForm" name="mediaFileSearchForm" action="mediaFileView" method="get"
        theme="simple" cssClass="media-search" role="search">
    <s:hidden name="weblog"/>
    <s:hidden name="directoryId"/>

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
        <div class="media-search-scope">
            <label class="form-label" for="beanScope"><s:text name="mediaFileView.searchIn"/></label>
            <select id="beanScope" name="bean.scope" class="form-select">
                <option value="folder"><s:text name="mediaFileView.thisFolder" escapeHtml="true"><s:param value="currentDirectory.name"/></s:text></option>
                <option value="all" <s:if test="bean.allFolders">selected</s:if>><s:text name="mediaFileView.allFolders"/></option>
            </select>
        </div>
        <div class="media-search-buttons">
            <button type="button" class="btn btn-outline-secondary" data-bs-toggle="collapse"
                    data-bs-target="#mediaFilters" aria-controls="mediaFilters"
                    aria-expanded="<s:property value='bean.filtersSet'/>">
                <span class="bi bi-sliders" aria-hidden="true"></span>
                <s:text name="mediaFileView.filters"/>
            </button>
            <button type="submit" class="btn btn-primary"><s:text name="generic.search"/></button>
        </div>
    </div>

    <div id="mediaFilters" class="collapse media-filters <s:if test='bean.filtersSet'>show</s:if>">
        <fieldset class="media-filter-size">
            <legend class="form-label"><s:text name="mediaFileView.size"/></legend>
            <s:select id="sizeFilterTypeCombo" name="bean.sizeFilterType" list="sizeFilterTypes"
                      listKey="key" listValue="value" cssClass="form-select"
                      aria-label="%{getText('mediaFileView.sizeComparison')}"/>
            <input type="number" id="beanSize" name="bean.size" min="0" class="form-control"
                   value="<s:if test='bean.size > 0'><s:property value='bean.size'/></s:if>"
                   aria-label="<s:text name='mediaFileView.size'/>"/>
            <s:select id="beanSizeUnit" name="bean.sizeUnit" list="sizeUnits" listKey="key" listValue="value"
                      cssClass="form-select" aria-label="%{getText('mediaFileView.sizeUnit')}"/>
        </fieldset>
        <div class="media-filter-tags">
            <label class="form-label" for="beanTags"><s:text name="mediaFileView.tags"/></label>
            <s:textfield id="beanTags" name="bean.tags" maxlength="50" cssClass="form-control"/>
        </div>
    </div>
</s:form>

<%-- What is shown: criteria, scope and count, then sort order and layout --%>

<div class="media-results-bar">
<div class="media-result-summary" role="status">
    <s:if test="search">
        <s:text name="mediaFileView.matchCount"><s:param value="pager.totalCount"/></s:text>
        <ul class="media-criteria">
            <s:if test="bean.name != null && !bean.name.trim().isEmpty()">
                <li><s:text name="mediaFileView.criterionName" escapeHtml="true"><s:param value="bean.name.trim()"/></s:text></li>
            </s:if>
            <s:if test="bean.typeSet">
                <li><s:text name="mediaFileView.type"/>: <s:property value="bean.typeLabel"/></li>
            </s:if>
            <s:if test="bean.size > 0">
                <li><s:text name="mediaFileView.size"/> <s:property value="bean.sizeFilterTypeLabel"/>
                    <s:property value="bean.size"/> <s:property value="bean.sizeUnitLabel"/></li>
            </s:if>
            <s:if test="bean.tags != null && !bean.tags.trim().isEmpty()">
                <li><s:text name="mediaFileView.tags"/>: <s:property value="bean.tags.trim()"/></li>
            </s:if>
            <li>
                <s:if test="bean.allFolders"><s:text name="mediaFileView.allFolders"/></s:if>
                <s:else><s:text name="mediaFileView.thisFolder" escapeHtml="true"><s:param value="currentDirectory.name"/></s:text></s:else>
            </li>
        </ul>
        <a href="<s:property value='%{clearSearchURL}'/>"><s:text name="mediaFileView.clearSearch"/></a>
    </s:if>
    <s:elseif test="pager.totalCount > 0">
        <s:text name="mediaFileView.fileCount"><s:param value="pager.totalCount"/></s:text>
    </s:elseif>
</div>
<s:if test="pager.items.size() > 0">
    <div class="media-toolbar-group media-view-options">
        <label for="sortByMenu"><s:text name="mediaFileView.sortBy"/></label>
        <%-- belongs to the GET search form, so sorting keeps the criteria --%>
        <select id="sortByMenu" name="bean.sortBy" form="mediaFileSearchForm" class="form-select form-select-sm">
            <s:iterator var="option" value="sortOptions">
                <option value="<s:property value='#option.key'/>"
                        <s:if test="#option.key == bean.sortBy">selected</s:if>><s:property value="#option.value"/></option>
            </s:iterator>
        </select>
        <div class="btn-group btn-group-sm" role="group" aria-label="<s:text name='mediaFileView.layout'/>">
            <button type="button" class="btn btn-outline-secondary" data-media-view="grid" aria-pressed="true"
                    title="<s:text name='mediaFileView.gridView'/>">
                <span class="bi bi-grid-3x3-gap" aria-hidden="true"></span>
                <span class="visually-hidden"><s:text name="mediaFileView.gridView"/></span></button>
            <button type="button" class="btn btn-outline-secondary" data-media-view="list" aria-pressed="false"
                    title="<s:text name='mediaFileView.listView'/>">
                <span class="bi bi-list-ul" aria-hidden="true"></span>
                <span class="visually-hidden"><s:text name="mediaFileView.listView"/></span></button>
        </div>
    </div>
</s:if>
</div>

<%-- Files, with the selection toolbar above them --%>

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" theme="simple">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="directoryId"/>
    <s:hidden name="newDirectoryName"/>
    <input type="hidden" name="mediaFileId" value=""/>
    <%-- the page state, so actions return to the same page --%>
    <s:iterator value="bean.toParams()">
        <input type="hidden" name="<s:property value='key'/>" value="<s:property value='value'/>"/>
    </s:iterator>
    <input type="hidden" name="bean.page" value="<s:property value='bean.page'/>"/>

    <s:if test="pager.items.size() > 0">

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
        </div>

        <s:include value="/WEB-INF/jsps/editor/Pager.jsp"/>

        <ul class="media-grid" id="mediaGrid">
            <li class="media-list-header" aria-hidden="true">
                <span></span><span></span>
                <span><s:text name="generic.name"/></span>
                <span><s:text name="mediaFileView.type"/></span>
                <span><s:text name="mediaFileView.size"/></span>
                <span><s:text name="mediaFileEdit.uploaded"/></span>
            </li>
            <s:iterator var="mediaFile" value="pager.items">
                <s:include value="/WEB-INF/jsps/editor/MediaFileCard.jsp"/>
            </s:iterator>
        </ul>

        <s:include value="/WEB-INF/jsps/editor/Pager.jsp"/>

    </s:if>
    <s:elseif test="search">
        <div class="media-empty">
            <span class="bi bi-search" aria-hidden="true"></span>
            <p><s:text name="mediaFileView.noResults"/></p>
            <a class="btn btn-outline-primary" href="<s:property value='%{clearSearchURL}'/>">
                <s:text name="mediaFileView.clearSearch"/></a>
        </div>
    </s:elseif>
    <s:elseif test="!hasActionErrors()">
        <div class="media-empty">
            <span class="bi bi-images" aria-hidden="true"></span>
            <p><s:text name="mediaFileView.noFiles"/></p>
            <a class="btn btn-primary" href="<s:property value='%{mediaFileAddURL}'/>">
                <s:text name="mediaFileView.upload"/></a>
        </div>
    </s:elseif>

    <s:if test="!search && currentDirectory.name != 'default'">
        <div class="media-folder-actions">
            <button type="button" class="btn btn-link text-danger" id="deleteFolderButton">
                <s:text name="mediaFileView.deleteFolder"/></button>
        </div>
    </s:if>

</s:form>

</div>

<span id="mediaFileUrls" hidden
      data-delete-selected-url="<s:url action='mediaFileView!deleteSelected'/>"
      data-move-selected-url="<s:url action='mediaFileView!moveSelected'/>"
      data-delete-folder-url="<s:url action='mediaFileView!deleteFolder'/>"
      data-create-folder-url="<s:url action='mediaFileView!createNewDirectory'/>"></span>

<script src="<s:url value='/roller-ui/scripts/media-library.js'/>"></script>
