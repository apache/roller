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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<s:property value='actionWeblog.id'/>";
    var directoryId = "<s:property value='%{#parameters.directoryId}'/>";
    var msg = {
        confirmLabel: '<s:text name="generic.confirm"/>',
        deleteLabel: '<s:text name="generic.delete"/>',
        cancelLabel: '<s:text name="generic.cancel"/>'
    };
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/mediafileview.js'/>"></script>

<input id="refreshURL" type="hidden" value="<s:url action='mediaFileView'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>

<%-- Subtitle and folder path --%>

<p class="subtitle">
    <s:text name="mediaFileView.subtitle" >
       <s:param value="actionWeblog.handle"/>
    </s:text>
</p>

<p class="pagetip">
    <s:text name="mediaFileView.rootPageTip" />
</p>

<div id="ngapp-div" ng-app="mediaFileViewApp" ng-controller="MediaFileViewController as ctrl">

    <div id="errorMessageDiv" class="errors" ng-show="ctrl.errorMsg">
       <b>{{ctrl.errorMsg}}</b>
    </div>

    <div class="control">
        <span style="padding-left:7px">
            <%-- Folder to View combo-box --%>
            <s:text name="mediaFileView.viewFolder" />:
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
               <s:text name="mediaFileView.noFiles"/>
            </li>

            <li class="align-images" ng-repeat="mediaFile in ctrl.mediaFiles" id="{{mediaFile.id}}">
                <div class="mediaObject">
                    <s:url var="editUrl" action="mediaFileEdit">
                        <s:param name="weblogId" value="%{actionWeblog.id}" />
                    </s:url>

                    <a ng-href="<s:property value='%{editUrl}'/>&amp;mediaFileId={{mediaFile.id}}">
                        <img ng-if="mediaFile.imageFile"
                             ng-src='{{mediaFile.thumbnailURL}}'
                             alt='{{mediaFile.altText}}'
                             title='{{mediaFile.name}}'>

                        <s:url var="mediaFileURL" value="/images/page_white.png"/>
                        <img ng-if="!mediaFile.imageFile" ng-src='<s:property value="%{mediaFileURL}" />'
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
               value='<s:text name="generic.toggle" />'
               ng-click="ctrl.onToggle()"
               />

            <input type="button" class="delete-file-link"
               value='<s:text name="mediaFileView.deleteSelected" />'
               />

            <input type="button" class="move-file-link"
               value='<s:text name="mediaFileView.moveSelected" />'
               ng-show="ctrl.mediaDirectories.length > 1">

            <select id="moveTargetMenu" size="1" required
               ng-model="ctrl.directoryToMoveTo"
               ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories"
               ng-show="ctrl.mediaDirectories.length > 1"></select>
        </span>

        <span style="float:right">
            <input type="button"
               value='<s:text name="mediaFileView.deleteFolder" />' class="delete-folder-link"
               ng-show="ctrl.mediaDirectories.length > 1" />
        </span>
    </div>

    <div class="menu-tr sidebarFade">
        <div class="sidebarInner">

            <br>
            <b><s:text name="mediaFileSidebar.actions" /></b>
            <br>
            <br>

            <img src='<s:url value="/images/image_add.png"/>' border="0" alt="icon">
            <s:url var="mediaFileAddURL" action="mediaFileAdd">
                <s:param name="weblogId" value="%{actionWeblog.id}" />
            </s:url>
            <s:a href='%{mediaFileAddURL}&directoryId={{ctrl.directoryToView}}' style='font-weight:bold;'>
                <s:text name="mediaFileSidebar.add" />
            </s:a>

            <br><br>
            <div>
                <img src='<s:url value="/images/folder_add.png"/>' border="0" alt="icon">
                <s:text name="mediaFileView.addDirectory" /><br />
                <div style="padding-left:2em; padding-top:1em">
                    <s:text name="mediaFileView.directoryName" />
                    <input type="text" id="newDirectoryNameField" ng-model="ctrl.newDirectoryName" size="10" maxlength="25"/>
                    <input type="button" id="newDirectoryButton"
                        value='<s:text name="mediaFileView.create" />' ng-click="ctrl.createNewDirectory()"
                        ng-disabled="ctrl.newDirectoryName == ''" />
                </div>
            </div>

            <br><br><br>
        </div>
    </div>
</div>

<div id="confirm-delete-file" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.delete.confirm' /></p>
</div>

<div id="confirm-delete-folder" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.deleteFolder.confirm' /></p>
</div>

<div id="confirm-move-file" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.move.confirm' /></p>
</div>

<br>
<br>
<br>
