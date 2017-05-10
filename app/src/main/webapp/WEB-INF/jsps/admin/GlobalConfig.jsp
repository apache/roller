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

<p class="subtitle"><fmt:message key="globalConfig.subtitle" /></p>
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/globalconfig.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/admin/globalConfig'/>"/>

<p><fmt:message key="globalConfig.prompt" /></p>

<div id="successMessageDiv" class="messages" ng-show="ctrl.saveResponseMessage" ng-cloak>
    <p>{{ctrl.saveResponseMessage}}</p>
</div>

<table class="formtable">

    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.siteSettings" /></h2></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="globalConfig.frontpageWeblogHandle" /></td>
        <td class="field">
            <select ng-model="ctrl.webloggerProps.mainBlog.id" size="1">
                <option ng-repeat="(key, value) in ctrl.metadata.weblogList" value="{{key}}">{{value}}</option>
                <option value=""><fmt:message key="globalConfig.none" /></option>
            </select>
        </td>
        <td class="description"><fmt:message key="globalConfig.tip.frontpageWeblogHandle"/></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="globalConfig.requiredRegistrationProcess" /></td>
        <td class="field">
             <select ng-model="ctrl.webloggerProps.registrationPolicy" size="1" required>
                 <option ng-repeat="(key, value) in ctrl.metadata.registrationOptions" value="{{key}}">{{value}}</option>
             </select>
        </td>
        <td class="description"><fmt:message key="globalConfig.tip.requiredRegistrationProcess"/></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.newUsersCreateBlogs" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCreateBlogs"></td>
            <td class="description"><fmt:message key="globalConfig.tip.newUsersCreateBlogs"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.weblogSettings" /></h2></td>
    </tr>
    <tr>
           <td class="label"><fmt:message key="globalConfig.weblogSettings" /></td>
           <td class="field">
               <select ng-model="ctrl.webloggerProps.blogHtmlPolicy" size="1" required>
                   <option ng-repeat="(key, value) in ctrl.metadata.blogHtmlLevels" value="{{key}}">{{value}}</option>
               </select>
           </td>
           <td class="description"><fmt:message key="globalConfig.tip.htmlWhitelistLevel"/></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.allowCustomTheme" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCustomizeThemes"></td>
            <td class="description"><fmt:message key="globalConfig.tip.allowCustomTheme"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.newsfeedMaxEntries" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.newsfeedItemsPage" size='35'></td>
            <td class="description"><fmt:message key="globalConfig.tip.newsfeedMaxEntries"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.defaultAnalyticsTrackingCode" /></td>
            <td class="field"><textarea rows="10" cols="70" ng-model="ctrl.webloggerProps.defaultAnalyticsCode"></textarea></td>
            <td class="description"><fmt:message key="globalConfig.tip.defaultAnalyticsTrackingCode"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.allowAnalyticsCodeOverride" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersOverrideAnalyticsCode"></td>
            <td class="description"><fmt:message key="globalConfig.tip.allowAnalyticsCodeOverride"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.commentSettings" /></h2></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.enableComments" /></td>
            <td class="field">
                <select ng-model="ctrl.webloggerProps.commentPolicy" size="1" required>
                    <option ng-repeat="(key, value) in ctrl.metadata.commentOptions" value="{{key}}">{{value}}</option>
                </select>
            </td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.commentHtmlWhitelistLevel" /></td>
            <td class="field">
                <select ng-model="ctrl.webloggerProps.commentHtmlPolicy" size="1" required>
                    <option ng-repeat="(key, value) in ctrl.metadata.commentHtmlLevels" value="{{key}}">{{value}}</option>
                </select>
            </td>
            <td class="description"><fmt:message key="globalConfig.tip.commentHtmlWhitelistLevel"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.ignoreSpamComments" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.autodeleteSpam"></td>
            <td class="description"><fmt:message key="globalConfig.tip.ignoreSpamComments"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.emailComments" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCommentNotifications"></td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.ignoreUrls" /></td>
            <td class="field"><textarea rows="7" cols="80" ng-model="ctrl.webloggerProps.commentSpamFilter"></textarea></td>
            <td class="description"><fmt:message key="globalConfig.tip.ignoreUrls"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="globalConfig.fileUploadSettings" /></h2></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.enableFileUploads" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersUploadMediaFiles"></td>
            <td class="description"><fmt:message key="globalConfig.tip.enableFileUploads"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.allowedExtensions" /></td>
            <td class="field"><input type="text" ng-model="ctrl.webloggerProps.allowedFileExtensions" size='35'></td>
            <td class="description"><fmt:message key="globalConfig.tip.allowedExtensions"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.forbiddenExtensions" /></td>
            <td class="field"><input type="text" ng-model="ctrl.webloggerProps.disallowedFileExtensions" size='35'></td>
            <td class="description"><fmt:message key="globalConfig.tip.forbiddenExtensions"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.maxFileSize" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.maxFileSizeMb" size='35'></td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="globalConfig.maxDirSize" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.maxFileUploadsSizeMb" size='35'></td>
            <td class="description"><fmt:message key="globalConfig.tip.maxDirSize"/></td>
        </tr>

</table>

<div class="control">
    <input class="buttonBox" type="button" value="<fmt:message key='generic.save'/>" ng-click="ctrl.updateProperties()"/>
</div>
