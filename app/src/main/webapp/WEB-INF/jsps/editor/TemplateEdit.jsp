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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var templateId = "<c:out value='${param.templateId}'/>";
    var templateName = "<c:out value='${param.templateName}'/>";
    var weblogUrl = "<c:out value='${actionWeblog.absoluteURL}'/>";
    var msg = {
        deleteLabel: "<fmt:message key='generic.delete'/>",
        cancelLabel: "<fmt:message key='generic.cancel'/>"
    };
    var templatesUrl = "<c:url value='/tb-ui/app/templates'/>";
</script>
<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/templateedit.js'/>"></script>

<c:url var="refreshUrl" value="/tb-ui/app/authoring/templateedit">
    <c:param name="weblogId" value="${param.weblogId}"/>
    <c:param name="templateId" value="${param.templateId}"/>
    <c:param name="templateName" value="${param.templateName}"/>
</c:url>
<input id="refreshURL" type="hidden" value="${refreshURL}"/>

<div id="successMessageDiv" class="messages" ng-show="ctrl.showSuccessMessage" ng-cloak>
    <p><fmt:message key="generic.changes.saved"/> ({{ctrl.templateData.lastModified | date:'short'}})</p>
</div>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj">
  <b>{{ctrl.errorObj.errorMessage}}</b>
  <ul>
     <li ng-repeat="em in ctrl.errorObj.errors">{{em}}</li>
  </ul>
</div>

<p class="subtitle">
   <fmt:message key="templateEdit.subtitle"/>
</p>

<p class="pagetip"><fmt:message key="templateEdit.tip" /></p>

<table cellspacing="5">
    <tr>
        <td class="label"><fmt:message key="generic.name"/>&nbsp;</td>
        <td class="field">
            <input id="name" type="text" ng-model="ctrl.templateData.name" size="50" maxlength="255" style="background: #e5e5e5" ng-readonly="ctrl.templateData.derivation != 'Blog-Only'"/>
        </td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="templateEdit.role" />&nbsp;</td>
        <td class="field">
             <span>{{ctrl.templateData.role.readableName}}</span>
        </td>
    </tr>

    <tr ng-if="ctrl.templateData.role.accessibleViaUrl">
        <td class="label" valign="top"><fmt:message key="templateEdit.path" />&nbsp;</td>
        <td class="field">
            <input id="path" type="text" ng-model="ctrl.templateData.relativePath" size="50" maxlength="255"/>
            <br/>
            <c:out value="${actionWeblog.absoluteURL}" />page/<span id="linkPreview" style="color:red">{{ctrl.templateData.relativePath}}</span>
            <span ng-if="ctrl.lastSavedRelativePath != null">
                [<a id="launchLink" ng-click="ctrl.launchPage()"><fmt:message key="templateEdit.launch" /></a>]
            </span>
        </td>
    </tr>

    <tr ng-if="!ctrl.template.role.singleton">
        <td class="label" valign="top" style="padding-top: 4px">
            <fmt:message key="generic.description"/>&nbsp;
        </td>
        <td class="field">
            <textarea id="description" type="text" ng-model="ctrl.templateData.description" cols="50" rows="2"></textarea>
        </td>
    </tr>

</table>

<div data-template-tabs>
    <ul>
        <li><a href="#tabStandard"><em>Standard</em></a></li>
        <li ng-show="ctrl.templateData.contentsMobile != null">
            <a href="#tabMobile"><em>Mobile</em></a>
        </li>
    </ul>
    <div>
        <div id="tabStandard">
            <textarea ng-model="ctrl.templateData.contentsStandard" rows="30" style="width:100%"></textarea>
        </div>
        <div id="tabMobile" ng-show="ctrl.templateData.contentsMobile != null">
            <textarea ng-model="ctrl.templateData.contentsMobile" rows="30" style="width:100%"></textarea>
        </div>
    </div>
</div>

<c:url var="templatesUrl" value="/tb-ui/app/authoring/templates">
    <c:param name="weblogId" value="${param.weblogId}" />
</c:url>

<table style="width:100%">
    <tr>
        <td>
            <input ng-click="ctrl.saveTemplate()" type="button" value="<fmt:message key='generic.save'/>">
            <input type="button" value="<fmt:message key='generic.cancel'/>" onclick="window.location='${templatesUrl}'">
        </td>
    </tr>
</table>
