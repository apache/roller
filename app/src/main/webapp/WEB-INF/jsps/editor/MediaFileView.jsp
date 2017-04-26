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
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var directoryId = "<c:out value='${param.directoryId}'/>";
    var msg = {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        deleteLabel: '<fmt:message key="generic.delete"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
        fileDeleteSuccess: '<fmt:message key="mediaFileView.delete.success"/>',
        folderDeleteSuccess: '<fmt:message key="mediaFileView.deleteFolder.success"/>',
        fileMoveSuccess: '<fmt:message key="mediaFileView.move.success"/>',
        fileMoveError: '<fmt:message key="mediaFileView.move.errors"/>'
    };
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mediafileview.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/mediaFileView'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<%-- Subtitle and folder path --%>

<p class="subtitle">
    <fmt:message key="mediaFileView.subtitle" >
       <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

<p class="pagetip">
    <fmt:message key="mediaFileView.rootPageTip" />
</p>

<div class="messages" ng-show="ctrl.successMessage" ng-cloak>
    <p>{{ctrl.successMessage}}</p>
</div>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorMessage" ng-cloak>
   <b>{{ctrl.errorMessage}}</b>
</div>

<div class="control">
    <span style="padding-left:7px">
        <%-- Folder to View combo-box --%>
        <fmt:message key="mediaFileView.viewFolder" />:
        <%-- ng-options: http://preview.tinyurl.com/z8okbq8 --%>
        <select ng-model="ctrl.directoryToView"
                ng-change="ctrl.loadMediaFiles()"
                ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories"
                size="1" required></select>
    </span>
</div>

<%-- ***************************************************************** --%>

<%-- Media file folder contents --%>

<div width="720px" height="500px">
    <ul id = "myMenu">
        <li ng-if="ctrl.mediaFiles.length == 0" style="text-align: center;list-style-type:none;">
           <fmt:message key="mediaFileView.noFiles"/>
        </li>

        <li class="align-images" ng-repeat="mediaFile in ctrl.mediaFiles" id="{{mediaFile.id}}">
            <div class="mediaObject">
                <c:url var="editUrl" value="/tb-ui/app/authoring/mediaFileEdit">
                    <c:param name="weblogId" value="${actionWeblog.id}" />
                </c:url>

                <a ng-href="<c:out value='${editUrl}'/>&amp;directoryId={{ctrl.directoryToView}}&amp;mediaFileId={{mediaFile.id}}">
                    <img ng-if="mediaFile.imageFile"
                         ng-src='{{mediaFile.thumbnailURL}}'
                         alt='{{mediaFile.altText}}'
                         title='{{mediaFile.name}}'>

                    <c:url var="mediaFileURL" value="/images/page_white.png"/>
                    <img ng-if="!mediaFile.imageFile" ng-src='<c:out value="${mediaFileURL}" />'
                         alt='{{mediaFile.altText}}'
                         style="padding:40px 50px;">
                </a>
            </div>

            <div class="mediaObjectInfo">
                <input type="checkbox"
                       ng-model="mediaFile.selected"
                       value="{{mediaFile.id}}">

                {{mediaFile.name | limitTo: 47}}
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

        <input type="button" delete-files-dialog="confirm-delete-files"
           value='<fmt:message key="mediaFileView.deleteSelected" />'
           />

        <input type="button" move-files-dialog="confirm-move-file"
           value='<fmt:message key="mediaFileView.moveSelected" />'
           ng-show="ctrl.mediaDirectories.length > 1">

        <select id="moveTargetMenu" size="1" required
           ng-model="ctrl.directoryToMoveTo"
           ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories"
           ng-show="ctrl.mediaDirectories.length > 1"></select>
    </span>

    <span style="float:right">
        <input type="button" value='<fmt:message key="mediaFileView.deleteFolder" />'
            delete-folder-dialog="confirm-delete-folder" ng-show="ctrl.mediaDirectories.length > 1" />
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
        <a href='${mediaFileAddURL}&directoryId={{ctrl.directoryToView}}' style='font-weight:bold;'>
            <fmt:message key="mediaFileView.add"/>
        </a>

        <br><br>
        <div>
            <img src='<c:url value="/images/folder_add.png"/>' border="0" alt="icon">
            <fmt:message key="mediaFileView.addDirectory" /><br />
            <div style="padding-left:2em; padding-top:1em">
                <fmt:message key="mediaFileView.directoryName" />
                <input type="text" id="newDirectoryNameField" ng-model="ctrl.newDirectoryName" size="10" maxlength="25"/>
                <input type="button" id="newDirectoryButton"
                    value='<fmt:message key="mediaFileView.create" />' ng-click="ctrl.createNewDirectory()"
                    ng-disabled="ctrl.newDirectoryName == ''" />
            </div>
        </div>

        <br><br><br>
    </div>
</div>

<div id="confirm-delete-files" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key='mediaFileView.delete.confirm' /></p>
</div>

<div id="confirm-delete-folder" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key='mediaFileView.deleteFolder.confirm' /></p>
</div>

<div id="confirm-move-file" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key='mediaFileView.move.confirm' /></p>
</div>

<br>
<br>
<br>
