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
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<c:url var="mediaFileViewUrl" value="/tb-ui/app/authoring/mediaFileView">
    <c:param name="weblogId" value="${actionWeblog.id}" />
</c:url>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var mediaFileId = "<c:out value='${param.mediaFileId}'/>";
    var directoryId = "<c:out value='${param.directoryId}'/>";
    var mediaViewUrl = "<c:out value='${mediaFileViewUrl}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mediafileedit.js'/>"></script>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj">
  <b>{{ctrl.errorObj.errorMessage}}</b>
  <ul>
     <li ng-repeat="em in ctrl.errorObj.errors">{{em}}</li>
  </ul>
</div>

<c:choose>
    <c:when test="${param.mediaFileId != null}">
        <c:set var="subtitleKey">mediaFileEdit.subtitle</c:set>
        <c:set var="mainAction">mediaFileEdit</c:set>
        <c:set var="pageTip">mediaFileEdit.pagetip</c:set>
        <input id="refreshURL" type="hidden"
            value="<c:url value='/tb-ui/app/authoring/mediaFileEdit'/>?weblogId=<c:out value='${param.weblogId}'/>"\
                "&directoryId=<c:out value='${param.directoryId}'/>&mediaFileId=<c:out value='${param.mediaFileId}'/>"/>
        <div ng-if="ctrl.mediaFileData.imageFile" class="mediaFileThumbnail">
            <a ng-href='{{ctrl.mediaFileData.permalink}}' target="_blank">
                <img align="right" alt="thumbnail" ng-src='{{ctrl.mediaFileData.thumbnailURL}}'
                     title='<fmt:message key="mediaFileEdit.clickToView" />' />
            </a>
        </div>
    </c:when>
    <c:otherwise>
        <c:set var="subtitleKey">mediaFileAdd.title</c:set>
        <c:set var="mainAction">mediaFileAdd</c:set>
        <c:set var="pageTip">mediaFileAdd.pageTip</c:set>
        <input id="refreshURL" type="hidden"
            value="<c:url value='/tb-ui/app/authoring/mediaFileAdd'/>?weblogId=<c:out value='${param.weblogId}'/>"\
                "&directoryId=<c:out value='${param.directoryId}'/>"/>
    </c:otherwise>
</c:choose>

    <p class="subtitle">
        <fmt:message key="${subtitleKey}"/>
    </p>

    <p class="pagetip">
        <fmt:message key="${pageTip}"/>
    </p>

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="name"><fmt:message key="generic.name" /></label>
            </td>
            <td>
                <input id="name" type="text" ng-model="ctrl.mediaFileData.name" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="altText"><fmt:message key="mediaFileEdit.altText"/><tags:help key="mediaFileEdit.altText.tooltip"/></label>
            </td>
            <td>
                <input id="altText" type="text" ng-model="ctrl.mediaFileData.altText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="titleText"><fmt:message key="mediaFileEdit.titleText"/><tags:help key="mediaFileEdit.titleText.tooltip"/></label>
            </td>
            <td>
                <input id="titleText" type="text" ng-model="ctrl.mediaFileData.titleText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="anchor"><fmt:message key="mediaFileEdit.anchor"/><tags:help key="mediaFileEdit.anchor.tooltip"/></label>
            </td>
            <td>
                <input id="anchor" type="text" ng-model="ctrl.mediaFileData.anchor" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="notes"><fmt:message key="generic.notes"/></label>
            </td>
            <td>
                <input id="notes" type="text" ng-model="ctrl.mediaFileData.notes" size="50" maxlength="255" style="width:30%"/>
            </td>
       </tr>
    <c:if test="${param.mediaFileId != null}">
       <tr>
            <td class="entryEditFormLabel">
                <fmt:message key="mediaFileEdit.fileInfo" />
            </td>
            <td>
                <b><fmt:message key="mediaFileEdit.fileType"/></b>: {{ctrl.mediaFileData.contentType}}
                <b><fmt:message key="mediaFileEdit.fileSize"/></b>: {{ctrl.mediaFileData.length}}
                <b><fmt:message key="mediaFileEdit.fileDimensions"/></b>: {{ctrl.mediaFileData.width}} x {{ctrl.mediaFileData.height}} pixels
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="permalink"><fmt:message key="mediaFileEdit.permalink" /></label>
            </td>
            <td>
                <input id="permalink" type="text" size="50" style="width:50%" value='{{ctrl.mediaFileData.permalink}}' readonly />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="directoryId"><fmt:message key="mediaFileEdit.folder" /></label>
            </td>
            <td>
                <input id="directoryId" type="text" size="50" value='{{ctrl.mediaFileData.directory.name}}' readonly />
            </td>
       </tr>
    </c:if>

        <tr>
            <td class="entryEditFormLabel">
                <label for="fileControl"><fmt:message key="mediaFileEdit.fileLocation" /></label>
            </td>
            <td>
                <c:choose>
                    <c:when test="${param.mediaFileId != null}">
                        <input id="fileControl" type="file" file-model="ctrl.myMediaFile" size="30" value=""/>
                    </c:when>
                    <c:otherwise>
                        <input id="fileControl" type="file" file-model="ctrl.myMediaFile" size="30" value="" required/>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </table>

<br />
<div class="control">
    <input type="button" value="<fmt:message key='generic.save'/>" ng-click="ctrl.saveMediaFile()"/>
    <a href="<c:out value='${mediaFileViewUrl}'/>&amp;directoryId={{ctrl.mediaFileData.directory.id}}">
        <input type="button" value="<fmt:message key='generic.cancel'/>"/>
    </a>
</div>
