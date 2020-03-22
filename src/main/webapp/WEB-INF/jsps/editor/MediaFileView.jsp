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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var directoryId = "<c:out value='${param.directoryId}'/>";
    var msg = {
        confirmDeleteFilesTmpl: "<fmt:message key='mediafileView.confirmDeleteFilesTmpl'/>",
        confirmDeleteFolderTmpl: "<fmt:message key='mediafileView.confirmDeleteFolderTmpl'/>",
        confirmMoveFilesTmpl: "<fmt:message key='mediafileView.confirmMoveFilesTmpl'/>"
    };
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mediafileview.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/mediaFileView'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p class="pagetip">
    <fmt:message key="mediaFileView.rootPageTip" />
</p>

<div id="successMessageDiv" class="alert alert-success" role="alert" ng-show="ctrl.successMessage" ng-cloak>
    {{ctrl.successMessage}}
    <button type="button" class="close" data-ng-click="ctrl.successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul class="list-unstyled">
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>


<div class="control">
    <span style="padding-left:7px">
        <%-- Folder to View combo-box --%>
        <fmt:message key="mediaFileView.viewFolder" />:
        <%-- ng-options: http://preview.tinyurl.com/z8okbq8 --%>
        <select ng-model="ctrl.currentFolderId"
                ng-change="ctrl.loadMediaFiles()"
                ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories"
                size="1" required></select>
    </span>
</div>

<%-- ***************************************************************** --%>

<%-- Media file folder contents --%>

<div width="720px" height="500px" ng-cloak>
    <ul id = "myMenu">
        <li ng-if="ctrl.mediaFiles.length == 0" style="text-align: center;list-style-type:none;">
           <fmt:message key="mediaFileView.noFiles"/>
        </li>

        <li class="align-images" ng-repeat="mediaFile in ctrl.mediaFiles" id="{{mediaFile.id}}">
            <div class="mediaObject">
                <c:url var="editUrl" value="/tb-ui/app/authoring/mediaFileEdit">
                    <c:param name="weblogId" value="${actionWeblog.id}" />
                </c:url>

                <a ng-href="<c:out value='${editUrl}'/>&amp;directoryId={{ctrl.currentFolderId}}&amp;mediaFileId={{mediaFile.id}}">
                    <img ng-if="mediaFile.imageFile"
                         ng-src='{{mediaFile.thumbnailURL}}'
                         alt='{{mediaFile.altText}}'
                         title='{{mediaFile.name}}'>

                    <img ng-if="!mediaFile.imageFile" ng-src='<c:out value="/images/page_white.png" />'
                         alt='{{mediaFile.altText}}'
                         style="padding:40px 50px;">
                </a>
            </div>

            <div class="mediaObjectInfo">
                <input type="checkbox"
                       name="idSelections"
                       ng-model="mediaFile.selected"
                       value="{{mediaFile.id}}">

                {{mediaFile.name | limitTo: 47}}

                <span style="float:right">
                    <input type="image" ng-click="ctrl.copyToClipboard(mediaFile)"
                        src='<c:url value="/images/copy_to_clipboard.png"/>'
                           alt="Copy URL to clipboard" title="Copy URL to clipboard">
                </span>
            </div>
        </li>
    </ul>
</div>

<div style="clear:left;"></div>

<div class="control clearfix" style="margin-top: 15px">
    <span style="padding-left:7px" ng-show="ctrl.mediaFiles.length > 0">

        <input type="button" id="toggleButton"
           value='<fmt:message key="generic.toggle" />'
           ng-click="ctrl.onToggle()"
           />

        <input ng-disabled="!ctrl.filesSelected()" type="button"
           data-toggle="modal" data-target="#deleteFilesModal"
           value='<fmt:message key="mediaFileView.deleteSelected" />'
           />

        <input ng-disabled="!ctrl.filesSelected()" type="button"
           value='<fmt:message key="mediaFileView.moveSelected" />'
           data-toggle="modal" data-target="#moveFilesModal" data-folder-id="{{ctrl.targetFolderId}}"
           ng-show="ctrl.mediaDirectories.length > 1">

        <select id="moveTargetMenu" size="1" required
           ng-model="ctrl.targetFolderId"
           ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories | filter: {id : '!' + ctrl.currentFolderId}"
           ng-show="ctrl.mediaDirectories.length > 1"></select>
    </span>

    <span style="float:right">
        <input type="button" value='<fmt:message key="mediaFileView.deleteFolder" />'
            data-toggle="modal" data-folder-id="{{ctrl.currentFolderId}}" data-target="#deleteFolderModal"
            ng-show="ctrl.mediaDirectories.length > 1" />
    </span>
</div>

<div class="menu-tr sidebarFade">
    <div class="sidebarInner">

        <br>
        <b><fmt:message key="mediaFileView.actions" /></b>
        <br>
        <br>

        <img src='<c:url value="/images/image_add.png"/>' border="0" alt="icon">
        <c:url var="mediaFileAddURL" value="/tb-ui/app/authoring/mediaFileAdd">
            <c:param name="weblogId" value="${actionWeblog.id}" />
        </c:url>
        <a href='${mediaFileAddURL}&directoryId={{ctrl.currentFolderId}}' style='font-weight:bold;'>
            <fmt:message key="mediaFileView.add"/>
        </a>

        <br><br>
        <div>
            <img src='<c:url value="/images/folder_add.png"/>' border="0" alt="icon">
            <fmt:message key="mediaFileView.addFolder" /><br />
            <div style="padding-left:2em; padding-top:1em">
                <fmt:message key="generic.name" />:
                <input type="text" ng-model="ctrl.newFolderName" size="10" maxlength="25"/>
                <input type="button" value='<fmt:message key="mediaFileView.create" />' ng-click="ctrl.addFolder()"
                    ng-disabled="ctrl.newFolderName == ''" />
            </div>
        </div>

        <br><br><br>
    </div>
</div>

<!-- Delete media files modal -->
<div class="modal fade" id="deleteFilesModal" tabindex="-1" role="dialog" aria-labelledby="deleteFilesTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteFilesTitle"><fmt:message key="generic.confirm.delete"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="deleteFilesMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-click="ctrl.deleteFiles()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

<!-- Delete media folder modal -->
<div class="modal fade" id="deleteFolderModal" tabindex="-1" role="dialog" aria-labelledby="deleteFolderTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteFolderTitle"><fmt:message key="generic.confirm.delete"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="deleteFolderMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-click="ctrl.deleteFolder()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>

<!-- Move files modal -->
<div class="modal fade" id="moveFilesModal" tabindex="-1" role="dialog" aria-labelledby="moveFilesTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="moveFilesTitle"><fmt:message key="generic.confirm.move"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="moveFilesMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-warning" ng-click="ctrl.moveFiles()"><fmt:message key='generic.confirm'/></button>
      </div>
    </div>
  </div>
</div>
