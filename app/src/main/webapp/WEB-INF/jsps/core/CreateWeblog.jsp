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
<script src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script src="<s:url value='/webjars/angular/1.2.29/angular.min.js' />"></script>

<script>
function handlePreview(handle) {
    previewSpan = document.getElementById("handlePreview");
    var n1 = previewSpan.childNodes[0];
    var n2 = document.createTextNode(handle.value);
    if (handle.value == null) {
	    previewSpan.appendChild(n2);
    } else {
	    previewSpan.replaceChild(n2, n1);
    }
}
</script>

<p class="subtitle"><s:text name="createWebsite.prompt" /></p>

<br />

<s:form action="createWeblog!save">
<s:hidden name="salt" />

<table class="formtable">

<tr>
    <td class="label"><label for="name" /><s:text name="generic.name" /></label></td>
    <td class="field"><s:textfield name="bean.name" size="30" maxlength="30" /></td>
    <td class="description"><s:text name="createWebsite.tip.name" /></td>
</tr>

<tr>
        <td class="label"><label for="description" /><s:text name="generic.tagline" /></td>
    <td class="field"><s:textfield name="bean.description" size="40" maxlength="255" /></td>
    <td class="description"><s:text name="createWebsite.tip.description" /></td>
</tr>

<tr>
    <td class="label"><label for="handle" /><s:text name="createWebsite.handle" /></label></td>
    <td class="field">
        <s:textfield name="bean.handle" size="30" maxlength="30" onkeyup="handlePreview(this)" /><br />
        <span style="text-size:70%">
            <s:text name="createWebsite.weblogUrl" />:&nbsp;
            <s:property value="absoluteSiteURL" />/<span id="handlePreview" style="color:red"><s:if test="bean.handle != null"><s:property value="bean.handle"/></s:if><s:else>handle</s:else></span>
        </span>
    </td>
    <td class="description"><s:text name="createWebsite.tip.handle" /></td>
</tr>

<tr>
    <td class="label"><label for="emailAddress" /><s:text name="createWebsite.emailAddress" /></label></td>
    <td class="field"><s:textfield name="bean.emailAddress" size="40" maxlength="50" /></td>
    <td class="description"><s:text name="createWebsite.tip.email" /></td>
</tr>

<tr>
    <td class="label"><label for="locale" /><s:text name="createWebsite.locale" /></label></td>
    <td class="field">
       <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
    </td>
    <td class="description"><s:text name="createWebsite.tip.locale" /></td>
</tr>

<tr>
    <td class="label"><label for="timeZone" /><s:text name="createWebsite.timeZone" /></label></td>
    <td class="field">
       <s:select name="bean.timeZone" size="1" list="timeZonesList" />
    </td>
    <td class="description"><s:text name="createWebsite.tip.timezone" /></td>
</tr>

<tr>
    <td class="label"><label for="theme" /><s:text name="createWebsite.theme" /></label></td>
    <td class="field" ng-app="themeSelectModule" ng-controller="themeController">
        <select id="themeSelector" name="bean.theme" size="1"
        ng-model="selectedTheme" ng-options="theme as theme.name for theme in themes track by theme.id"></select>
        <br />
        <br />
        <p>{{ selectedTheme.description }}</p>
        <br />
        <img ng-src="<s:property value='siteURL'/>{{ selectedTheme.previewPath }}"/>
    </td>
    <td class="description"><s:text name="createWebsite.tip.theme" /></td>
</tr>

</table>

<br />

<s:submit value="%{getText('createWebsite.button.save')}" />
<input type="button" value="<s:text name="generic.cancel"/>" onclick="window.location='<s:url action="menu"/>'" />

</s:form>

<script>
    document.forms[0].elements[0].focus();

    angular.module('themeSelectModule', [])
        .controller('themeController', ['$scope', function($scope) {
            $.ajax({ url: "<s:property value='siteURL' />/roller-ui/authoring/themedata", async:false,
                success: function(data) { $scope.themes = data; }
            });
            $scope.selectedTheme = $scope.themes[0];
    }]);
</script>

