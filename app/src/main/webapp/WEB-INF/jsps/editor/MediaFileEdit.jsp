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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<script src="<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<s:property value='actionWeblog.id'/>";
    var mediaFileId = "<s:property value='%{#parameters.mediaFileId}'/>";
    var directoryId = "<s:property value='%{#parameters.directoryId}'/>";
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/mediafileedit.js'/>"></script>

<div ng-app="mediaFileEditApp" ng-controller="MediaFileEditController as ctrl">

    <s:if test="actionName == 'mediaFileEdit'">
        <s:set var="subtitleKey">mediaFileEdit.subtitle</s:set>
        <s:set var="mainAction">mediaFileEdit</s:set>
        <s:set var="pageTip">mediaFileEdit.pagetip</s:set>
        <div ng-if="ctrl.mediaFileData.imageFile" class="mediaFileThumbnail">
            <a ng-href='{{ctrl.mediaFileData.permalink}}' target="_blank">
                <img align="right" alt="thumbnail" ng-src='{{ctrl.mediaFileData.thumbnailURL}}'
                     title='<s:text name="mediaFileEdit.clickToView" />' />
            </a>
        </div>
    </s:if>
    <s:else>
        <s:set var="subtitleKey">mediaFileAdd.title</s:set>
        <s:set var="mainAction">mediaFileAdd</s:set>
        <s:set var="pageTip">mediaFileAdd.pageTip</s:set>
    </s:else>


    <p class="subtitle">
        <s:text name="%{#subtitleKey}"/> {{ctrl.mediaFileData.name}}
    </p>

    <p class="pagetip">
        <s:text name="%{#pageTip}"/>
    </p>

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="name"><s:text name="generic.name" /></label>
            </td>
            <td>
                <input id="name" type="text" ng-model="ctrl.mediaFileData.name" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="altText"><s:text name="mediaFileAdd.altText"/><tags:help key="mediaFileAdd.altText.tooltip"/></label>
            </td>
            <td>
                <input id="altText" type="text" ng-model="ctrl.mediaFileData.altText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="titleText"><s:text name="mediaFileAdd.titleText"/><tags:help key="mediaFileAdd.titleText.tooltip"/></label>
            </td>
            <td>
                <input id="titleText" type="text" ng-model="ctrl.mediaFileData.titleText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="anchor"><s:text name="mediaFileAdd.anchor"/><tags:help key="mediaFileAdd.anchor.tooltip"/></label>
            </td>
            <td>
                <input id="anchor" type="text" ng-model="ctrl.mediaFileData.anchor" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="notes"><s:text name="generic.notes"/></label>
            </td>
            <td>
                <input id="notes" type="text" ng-model="ctrl.mediaFileData.notes" size="50" maxlength="255" style="width:30%"/>
            </td>
       </tr>
<s:if test="actionName == 'mediaFileEdit'">
       <tr>
            <td class="entryEditFormLabel">
                <s:text name="mediaFileEdit.fileInfo" />
            </td>
            <td>
                <b><s:text name="mediaFileEdit.fileType"/></b>: {{ctrl.mediaFileData.contentType}}
                <b><s:text name="mediaFileEdit.fileSize"/></b>: {{ctrl.mediaFileData.length}}
                <b><s:text name="mediaFileEdit.fileDimensions"/></b>: {{ctrl.mediaFileData.width}} x {{ctrl.mediaFileData.height}} pixels
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="permalink"><s:text name="mediaFileEdit.permalink" /></label>
            </td>
            <td>
                <a href='{{ctrl.mediaFileData.permalink}}' target="_blank"
                   title='<s:text name="mediaFileEdit.linkTitle" />'>
                   <s:url var="linkIconURL" value="/images/link.png"/>
                   <img border="0" src='<s:property value="%{linkIconURL}" />'
                       style="padding:2px 2px;" alt="link" />
                </a>
                <input id="permalink" type="text" size="50" style="width:90%" value='{{ctrl.mediaFileData.permalink}}' readonly />
            </td>
       </tr>
</s:if>
       <tr>
            <td class="entryEditFormLabel">
                <label for="directoryId"><s:text name="mediaFileEdit.folder" /></label>
            </td>
            <td>
                <select id="directoryId" ng-model="ctrl.directoryId"
                        ng-options="dir.id as dir.name for dir in ctrl.mediaDirectories"
                        size="1" required></select>
            </td>
       </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="fileControl"><s:text name="mediaFileEdit.fileLocation" /></label>
            </td>
            <td>
                <div id="fileControldiv" class="miscControl">
                    <s:if test="actionName == 'mediaFileEdit'">
                        <input id="fileControl" type="file" file-model="ctrl.myMediaFile" size="30" value=""/>
                    </s:if>
                    <s:else>
                        <input id="fileControl" type="file" file-model="ctrl.myMediaFile" size="30" value="" required/>
                    </s:else>
                </div>
            </td>
        </tr>
    </table>

    <br />
    <div class="control">
        <button ng-click="ctrl.saveMediaFile()" value=
        <s:if test="actionName == 'mediaFileEdit'">
             "<s:text name='generic.save'/>">
        </s:if>
        <s:else>
             "<s:text name='mediaFileAdd.upload'/>">
        </s:else>
        <br><br>
        <form>
            <!--sec:csrfInput/-->
            <s:hidden name="weblogId" />
            <s:hidden name="directoryId" />
            <s:submit id="cancelbtn" value="%{getText('generic.cancel')}" action="mediaFileView" />
        </form>
    </div>

</div>
