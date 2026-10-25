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

<%-- Media library sidebar: the weblog's folders. Roller folders are flat, so this is a plain list. --%>

<nav class="media-folders" aria-labelledby="mediaFoldersHeading">

    <div class="media-folders-header">
        <h3 id="mediaFoldersHeading"><s:text name="mediaFileView.folders"/></h3>
        <button type="button" class="btn btn-outline-primary btn-sm" data-bs-toggle="collapse"
                data-bs-target="#newFolderForm" aria-expanded="false" aria-controls="newFolderForm">
            <span class="bi bi-folder-plus" aria-hidden="true"></span>
            <s:text name="mediaFileView.newFolder"/>
        </button>
    </div>

    <div id="newFolderForm" class="collapse media-new-folder">
        <label class="form-label" for="newDirectoryName"><s:text name="mediaFileView.directoryName"/></label>
        <div class="input-group input-group-sm">
            <input type="text" id="newDirectoryName" class="form-control" maxlength="25"/>
            <button type="button" id="newDirectoryButton" class="btn btn-primary" disabled>
                <s:text name="mediaFileView.create"/></button>
        </div>
    </div>

    <ul class="media-folder-list">
        <s:iterator var="folder" value="allDirectories">
            <s:url var="folderURL" action="mediaFileView">
                <s:param name="weblog" value="%{actionWeblog.handle}"/>
                <s:param name="directoryId" value="%{#folder.id}"/>
            </s:url>
            <li>
                <a href="<s:property value='%{folderURL}'/>"
                   <s:if test="!pager && #folder.id == currentDirectory.id">class="active" aria-current="page"</s:if>>
                    <span class="bi bi-folder2" aria-hidden="true"></span>
                    <span class="media-folder-name"><s:property value="#folder.name"/></span>
                </a>
            </li>
        </s:iterator>
    </ul>

</nav>
