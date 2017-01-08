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

<p class="subtitle"><s:text name="configForm.subtitle" /></p>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
</script>

<script src="<s:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/globalconfig.js'/>"></script>

<input id="refreshURL" type="hidden" value="<s:url action='globalConfig'/>"/>

<p><fmt:message key="configForm.prompt" /></p>

<div id="successMessageDiv" class="messages" ng-show="ctrl.saveResponseMessage" ng-cloak>
    <p>{{ctrl.saveResponseMessage}}</p>
</div>

<table class="formtable">

    <tr>
        <td colspan="3"><h2><fmt:message key="configForm.siteSettings" /></h2></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="configForm.frontpageWeblogHandle" /></td>
        <td class="field">
            <select ng-model="ctrl.webloggerProps.mainBlog.id" size="1" required>
                <option ng-repeat="(key, value) in ctrl.metadata.weblogList" value="{{key}}">{{value}}</option>
                <option value=""><fmt:message key="configForm.none" /></option>
            </select>
        </td>
        <td class="description"><fmt:message key="configForm.tip.frontpageWeblogHandle"/></td>
    </tr>
    <tr>
        <td class="label"><fmt:message key="configForm.requiredRegistrationProcess" /></td>
        <td class="field">
             <select ng-model="ctrl.webloggerProps.registrationPolicy" size="1" required>
                 <option ng-repeat="(key, value) in ctrl.metadata.registrationOptions" value="{{key}}">{{value}}</option>
             </select>
        </td>
        <td class="description"><fmt:message key="configForm.tip.requiredRegistrationProcess"/></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.newUsersCreateBlogs" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCreateBlogs"></td>
            <td class="description"><fmt:message key="configForm.tip.newUsersCreateBlogs"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="configForm.weblogSettings" /></h2></td>
    </tr>
    <tr>
           <td class="label"><fmt:message key="configForm.weblogSettings" /></td>
           <td class="field">
               <select ng-model="ctrl.webloggerProps.blogHtmlPolicy" size="1" required>
                   <option ng-repeat="(key, value) in ctrl.metadata.blogHtmlLevels" value="{{key}}">{{value}}</option>
               </select>
           </td>
           <td class="description"><fmt:message key="configForm.tip.htmlWhitelistLevel"/></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.allowCustomTheme" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCustomizeThemes"></td>
            <td class="description"><fmt:message key="configForm.tip.allowCustomTheme"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.newsfeedMaxEntries" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.newsfeedItemsPage" size='35'></td>
            <td class="description"><fmt:message key="configForm.tip.newsfeedMaxEntries"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.defaultAnalyticsTrackingCode" /></td>
            <td class="field"><textarea rows="10" cols="70" ng-model="ctrl.webloggerProps.defaultAnalyticsCode"></textarea></td>
            <td class="description"><fmt:message key="configForm.tip.defaultAnalyticsTrackingCode"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.allowAnalyticsCodeOverride" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersOverrideAnalyticsCode"></td>
            <td class="description"><fmt:message key="configForm.tip.allowAnalyticsCodeOverride"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="configForm.commentSettings" /></h2></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.enableComments" /></td>
            <td class="field">
                <select ng-model="ctrl.webloggerProps.commentPolicy" size="1" required>
                    <option ng-repeat="(key, value) in ctrl.metadata.commentOptions" value="{{key}}">{{value}}</option>
                </select>
            </td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.commentHtmlWhitelistLevel" /></td>
            <td class="field">
                <select ng-model="ctrl.webloggerProps.commentHtmlPolicy" size="1" required>
                    <option ng-repeat="(key, value) in ctrl.metadata.commentHtmlLevels" value="{{key}}">{{value}}</option>
                </select>
            </td>
            <td class="description"><fmt:message key="configForm.tip.commentHtmlWhitelistLevel"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.ignoreSpamComments" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.autodeleteSpam"></td>
            <td class="description"><fmt:message key="configForm.tip.ignoreSpamComments"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.emailComments" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersCommentNotifications"></td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="weblogSettings.ignoreUrls" /></td>
            <td class="field"><textarea rows="7" cols="80" ng-model="ctrl.webloggerProps.commentSpamFilter"></textarea></td>
            <td class="description"><fmt:message key="weblogSettings.tip.ignoreUrls"/></td>
        </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
    </tr>
    <tr>
        <td colspan="3"><h2><fmt:message key="configForm.fileUploadSettings" /></h2></td>
    </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.enableFileUploads" /></td>
            <td class="field"><input type="checkbox" ng-model="ctrl.webloggerProps.usersUploadMediaFiles"></td>
            <td class="description"><fmt:message key="configForm.tip.enableFileUploads"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.allowedExtensions" /></td>
            <td class="field"><input type="text" ng-model="ctrl.webloggerProps.allowedFileExtensions" size='35'></td>
            <td class="description"><fmt:message key="configForm.tip.allowedExtensions"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.forbiddenExtensions" /></td>
            <td class="field"><input type="text" ng-model="ctrl.webloggerProps.disallowedFileExtensions" size='35'></td>
            <td class="description"><fmt:message key="configForm.tip.forbiddenExtensions"/></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.maxFileSize" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.maxFileSizeMb" size='35'></td>
            <td class="description"></td>
        </tr>
        <tr>
            <td class="label"><fmt:message key="configForm.maxDirSize" /></td>
            <td class="field"><input type="number" ng-model="ctrl.webloggerProps.maxFileUploadsSizeMb" size='35'></td>
            <td class="description"><fmt:message key="configForm.tip.maxDirSize"/></td>
        </tr>

</table>

<div class="control">
    <input class="buttonBox" type="button" value="<fmt:message key='generic.save'/>" ng-click="ctrl.updateProperties()"/>
</div>
