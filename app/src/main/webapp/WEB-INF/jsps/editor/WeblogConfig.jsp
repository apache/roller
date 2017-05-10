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
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular-sanitize.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        deleteLabel: '<fmt:message key="generic.delete"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
        deleteWeblogTmpl: '<fmt:message key="weblogConfig.deleteConfirm"/>'
    };
    // Below populated for weblog update only
    var weblogId = "<c:out value='${weblogId}'/>";
    var homeUrl = "<c:url value='/tb-ui/app/home'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/weblogconfig.js'/>"></script>

<div id="successMessageDiv" class="messages" ng-show="ctrl.showSuccessMessage" ng-cloak>
    <p><fmt:message key="weblogConfig.savedChanges"/></p>
</div>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj.errorMessage" ng-cloak>
    <p>{{ctrl.errorObj.errorMessage}}</p>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item}}</li>
    </ul>
</div>

<c:choose>
    <%-- Create Weblog --%>
    <c:when test="${weblogId == null}">
        <fmt:message var="saveButtonText" key="weblogConfig.create.button.save"/>
        <fmt:message var="subtitlePrompt" key="weblogConfig.create.prompt"/>
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/createWeblog'/>"/>
        <c:url var="refreshUrl" value="/tb-ui/app/createWeblog"/>
    </c:when>
    <%-- Update Weblog --%>
    <c:otherwise>
        <fmt:message var="saveButtonText" key="weblogConfig.button.update"/>
        <fmt:message var="subtitlePrompt" key="weblogConfig.prompt">
            <fmt:param value="${actionWeblog.handle}"/>
        </fmt:message>
        <c:url var="refreshUrl" value="/tb-ui/app/weblogConfig">
            <c:param name="weblogId" value="${param.weblogId}"/>
        </c:url>
    </c:otherwise>
</c:choose>

<input id="refreshURL" type="hidden" value="${refreshURL}"/>

<p class="subtitle">
    ${subtitlePrompt}
</p>

<table class="formtable">

    <tr>
        <td colspan="3"><h2><fmt:message key="weblogConfig.generalSettings"/></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.websiteTitle"/>*</td>
        <td class="field"><input type="text" ng-model="ctrl.weblog.name" size="40" maxlength="255"></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.tagline"/></td>
        <td class="field"><input type="text" ng-model="ctrl.weblog.tagline" size="40" maxlength="255"></td>
        <td class="description"><fmt:message key="weblogConfig.tip.tagline"/></td>
    </tr>

    <tr>
        <td class="label"><label for="handle"><fmt:message key="weblogConfig.handle"/>*</label></td>
        <td class="field">
        <input type="text" ng-model="ctrl.weblog.handle" size="30" maxlength="30"
        <c:choose>
            <c:when test="${weblogId == null}">required></c:when>
            <c:otherwise>readonly></c:otherwise>
        </c:choose>
            <br>
            <span style="text-size:70%">
                <fmt:message key="weblogConfig.weblogUrl"/>:&nbsp;
                {{ctrl.metadata.absoluteSiteURL}}/<span style="color:red">{{ctrl.weblog.handle}}</span>
            </span>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.handle"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.about"/></td>
        <td class="field"><textarea ng-model="ctrl.weblog.about" rows="3" cols="40" maxlength="255"></textarea></td>
        <td class="description"><fmt:message key="weblogConfig.tip.about"/></td>
    </tr>

    <c:if test="${weblogId == null}">
        <tr>
        <td class="label"><label for="theme"><fmt:message key="weblogConfig.theme"/>*</label></td>
        <td class="field">
        <select ng-model="ctrl.weblog.theme" size="1">
            <option ng-repeat="(key, theme) in ctrl.metadata.sharedThemeMap" value="{{key}}">{{theme.name}}</option>
        </select>
        <div style="height:400px">
            <p>{{ctrl.metadata.sharedThemeMap[ctrl.weblog.theme].description}}</p>
            <img ng-src="{{ctrl.metadata.relativeSiteURL}}{{ctrl.metadata.sharedThemeMap[ctrl.weblog.theme].previewPath}}"></img>
        </div>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.theme"/></td>
        </tr>
    </c:if>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.editFormat"/></td>
        <td class="field">
            <select ng-model="ctrl.weblog.editFormat" size="1" required>
                <option ng-repeat="(key, value) in ctrl.metadata.editFormats" value="{{key}}">{{value}}</option>
            </select>
       </td>
       <td class="description"><fmt:message key="weblogConfig.tip.editFormat"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.visible"/></td>
        <td class="field"><input type="checkbox" ng-model="ctrl.weblog.visible"></td>
        <td class="description"><fmt:message key="weblogConfig.tip.visible"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.entriesPerPage"/></td>
        <td class="field"><input type="number" min="1" max="100" step="1" ng-model="ctrl.weblog.entriesPerPage" size="3"></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.locale"/>*</td>
        <td class="field">
            <select ng-model="ctrl.weblog.locale" size="1">
                <option ng-repeat="(key, value) in ctrl.metadata.locales" value="{{key}}">{{value}}</option>
            </select>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.locale"/></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="weblogConfig.timeZone"/>*</td>
        <td class="field">
            <select ng-model="ctrl.weblog.timeZone" size="1">
                <option ng-repeat="(key, value) in ctrl.metadata.timezones" value="{{key}}">{{value}}</option>
            </select>
        </td>
        <td class="description"><fmt:message key="weblogConfig.tip.timezone"/></td>
    </tr>

    <tr ng-if="ctrl.metadata.usersOverrideAnalyticsCode">
        <td class="label"><fmt:message key="weblogConfig.analyticsTrackingCode"/></td>
        <td class="field"><textarea ng-model="ctrl.weblog.analyticsCode" rows="10" cols="70" maxlength="1200"></textarea></td>
        <td class="description"><fmt:message key="weblogConfig.tip.analyticsTrackingCode"/></td>
    </tr>

<c:if test="${globalCommentPolicy != 'NONE'}">

    <tr>
    <td colspan="3"><h2><fmt:message key="weblogConfig.commentSettings"/></h2></td>
    </tr>

    <tr>
    <td class="label"><fmt:message key="weblogConfig.allowComments"/></td>
    <td class="field">
        <select ng-model="ctrl.weblog.allowComments" size="1">
            <option ng-repeat="(key, value) in ctrl.metadata.commentOptions" value="{{key}}">{{value}}</option>
        </select>
    </td>
    </tr>

    <tr ng-if="ctrl.metadata.usersCommentNotifications">
        <td class="label"><fmt:message key="weblogConfig.emailComments"/></td>
        <td class="field"><input type="checkbox" ng-model="ctrl.weblog.emailComments"></td>
    </tr>

    <tr>
    <td class="label"><fmt:message key="weblogConfig.defaultCommentDays"/></td>
    <td class="field">
        <select ng-model="ctrl.weblog.defaultCommentDays" size="1">
            <option ng-repeat="(key, value) in ctrl.metadata.commentDayOptions" ng-value="{{key-0}}">{{value}}</option>
        </select>
    </td>
    </tr>

    <tr ng-if="ctrl.weblog.id != null">
        <td class="label"><fmt:message key="weblogConfig.applyCommentDefaults"/></td>
        <td class="field"><input type="checkbox" ng-model="ctrl.weblog.applyCommentDefaults"></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="globalConfig.ignoreUrls"/></td>
        <td class="field"><textarea ng-model="ctrl.weblog.blacklist" rows="7" cols="40"></textarea></td>
        <td class="description"><fmt:message key="globalConfig.tip.ignoreUrls"/></td>
    </tr>

</c:if>

</table>

<br>

<div class="control">
    <input class="buttonBox" type="button" value="${saveButtonText}" ng-click="ctrl.updateWeblog()"/>
    <input class="buttonBox" type="button" value="<fmt:message key='generic.cancel'/>" ng-click="ctrl.cancelChanges()"/>
</div>

<br><br>

<div ng-if="ctrl.weblog.id != null">
    <h2><fmt:message key="weblogConfig.removeWebsiteHeading"/></h2>
    <p>
        <span class="warning">
            <fmt:message key="weblogConfig.removeWebsiteWarning"/>
        </span>
    </p>
    <br>
    <button confirm-delete-dialog="confirm-delete"><fmt:message key="weblogConfig.button.remove"/></button>
    <br><br><br>
</div>

<div id="confirm-delete" title="<fmt:message key='weblogConfig.deleteTitle'/>" style="display:none">
    {{ctrl.deleteWeblogConfirmation}}
    <br/>
    <br/>
    <span class="warning">
        <fmt:message key="weblogConfig.removeWebsiteWarning"/>
    </span>
</div>
