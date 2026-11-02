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
     data-confirm-delete-folder="<s:text name='mediaFile.deleteFolder.confirm'/>"
     data-selected-count="<s:text name='mediaFileView.selectedCount'/>">

<%-- Heading: where we are, how many files, and the main action --%>

<div class="media-header">
    <h3 class="media-heading">
        <s:if test="search && bean.allFolders">
            <span class="bi bi-search" aria-hidden="true"></span>
            <s:text name="mediaFileView.allFolders"/>
        </s:if>
        <s:else>
            <span class="bi bi-folder2" aria-hidden="true"></span>
            <s:property value="currentDirectory.name"/>
        </s:else>
        <s:if test="!search && pager.totalCount > 0">
            <span class="media-count"><s:text name="mediaFileView.fileCount"><s:param value="pager.totalCount"/></s:text></span>
        </s:if>
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

<%-- Search: one box with its scope; size and tags behind Filters. The type chips,
     sort and layout below belong to this form through their form attribute. --%>

<s:form id="mediaFileSearchForm" name="mediaFileSearchForm" action="mediaFileView" method="get"
        theme="simple" cssClass="media-search" role="search">
    <s:hidden name="weblog"/>
    <s:hidden name="directoryId"/>

    <div class="media-search-row">
        <div class="input-group media-search-box">
            <span class="input-group-text"><span class="bi bi-search" aria-hidden="true"></span></span>
            <label class="visually-hidden" for="beanName"><s:text name="mediaFileView.nameStartsWith"/></label>
            <s:textfield id="beanName" name="bean.name" maxlength="255" cssClass="form-control"
                         placeholder="%{getText('mediaFileView.searchPlaceholder')}"/>
            <label class="visually-hidden" for="beanScope"><s:text name="mediaFileView.searchIn"/></label>
            <select id="beanScope" name="bean.scope" class="form-select media-search-scope">
                <option value="folder"><s:text name="mediaFileView.thisFolder" escapeHtml="true"><s:param value="currentDirectory.name"/></s:text></option>
                <option value="all" <s:if test="bean.allFolders">selected</s:if>><s:text name="mediaFileView.allFolders"/></option>
            </select>
            <button type="submit" class="btn btn-primary"><s:text name="generic.search"/></button>
        </div>
        <button type="button" class="btn btn-outline-secondary media-filters-button" data-bs-toggle="collapse"
                data-bs-target="#mediaFilters" aria-controls="mediaFilters"
                aria-expanded="<s:property value='bean.filtersSet'/>">
            <span class="bi bi-sliders" aria-hidden="true"></span>
            <s:text name="mediaFileView.filters"/>
            <s:if test="bean.filtersSet"><span class="media-filters-dot" aria-hidden="true"></span></s:if>
        </button>
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

<%-- Files. The bar above them shows type, sort and layout; while files are selected,
     it shows what can be done with them instead. --%>

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" theme="simple">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="directoryId"/>
    <s:hidden name="newDirectoryName"/>
    <input type="hidden" name="mediaFileId" value=""/>
    <input type="hidden" name="selectedDirectory" id="selectedDirectory" value=""/>
    <%-- the page state, so actions return to the same page --%>
    <s:iterator value="bean.toParams()">
        <input type="hidden" name="<s:property value='key'/>" value="<s:property value='value'/>"/>
    </s:iterator>
    <input type="hidden" name="bean.page" value="<s:property value='bean.page'/>"/>

    <s:if test="pager.items.size() > 0 || search">
    <div class="media-bar">

        <div class="media-results-bar">
            <div class="media-type-chips" role="radiogroup" aria-label="<s:text name='mediaFileView.type'/>">
                <s:iterator var="option" value="fileTypes">
                    <input type="radio" class="btn-check" name="bean.type" form="mediaFileSearchForm"
                           id="type-<s:property value='#option.key'/>" value="<s:property value='#option.key'/>"
                           <s:if test="#option.key == bean.type || (#option.key == 'mediaFileView.any' && !bean.typeSet)">checked</s:if>/>
                    <label class="btn btn-sm media-chip" for="type-<s:property value='#option.key'/>">
                        <s:if test="#option.key == 'mediaFileView.any'"><s:text name="mediaFileView.allTypes"/></s:if>
                        <s:else><s:property value="#option.value"/></s:else></label>
                </s:iterator>
            </div>
            <s:if test="pager.items.size() > 0">
            <div class="media-view-options">
                <label class="visually-hidden" for="sortByMenu"><s:text name="mediaFileView.sortBy"/></label>
                <span class="bi bi-sort-down" aria-hidden="true"></span>
                <select id="sortByMenu" name="bean.sortBy" form="mediaFileSearchForm" class="form-select form-select-sm media-sort">
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

        <s:if test="pager.items.size() > 0">
        <div class="media-selection-bar" id="selectionBar" hidden>
            <button type="button" class="btn btn-sm btn-link media-selection-close" id="clearSelectionButton"
                    title="<s:text name='mediaFileView.clearSelection'/>">
                <span class="bi bi-x-lg" aria-hidden="true"></span>
                <span class="visually-hidden"><s:text name="mediaFileView.clearSelection"/></span></button>
            <span class="media-selected-count" id="selectedCount" aria-live="polite"></span>
            <button type="button" class="btn btn-sm btn-link" id="selectPageButton">
                <s:text name="mediaFileView.selectPage"/></button>
            <div class="media-selection-actions">
                <div class="dropdown">
                    <button type="button" class="btn btn-sm btn-outline-primary dropdown-toggle" id="moveButton"
                            data-bs-toggle="dropdown" aria-expanded="false">
                        <span class="bi bi-folder-symlink" aria-hidden="true"></span>
                        <s:text name="mediaFileView.move"/></button>
                    <ul class="dropdown-menu dropdown-menu-end" id="moveTargetMenu">
                        <s:iterator var="folder" value="allDirectories">
                            <li><button type="button" class="dropdown-item" data-folder-id="<s:property value='#folder.id'/>"
                                    <s:if test="!search && #folder.id == currentDirectory.id">disabled</s:if>>
                                <span class="bi bi-folder2" aria-hidden="true"></span>
                                <s:property value="#folder.name"/></button></li>
                        </s:iterator>
                    </ul>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger" id="deleteButton">
                    <span class="bi bi-trash" aria-hidden="true"></span>
                    <s:text name="generic.delete"/></button>
            </div>
        </div>
        </s:if>

    </div>
    </s:if>

    <s:if test="search">
        <div class="media-result-summary" role="status">
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
        </div>
    </s:if>

    <s:if test="pager.items.size() > 0">

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
