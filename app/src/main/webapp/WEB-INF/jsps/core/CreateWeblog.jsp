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
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
  deleteLabel: '<s:text name="generic.delete"/>',
  cancelLabel: '<s:text name="generic.cancel"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/createweblog.js'/>"></script>

<div id="errorMessageDiv" style="color:red;display:none">
  <script id="errorMessageTemplate" type="text/x-jsrender">
  <b>{{:errorMessage}}</b>
  <ul>
     {{for errors}}
     <li>{{>#data}}</li>
     {{/for}}
  </ul>
  </script>
</div>

<input type="hidden" id="refreshURL" value="<s:url action='createWeblog'/>"/>
<input type="hidden" id="menuURL" value="<s:url action='menu'/>"/>

<p class="subtitle"><s:text name="createWebsite.prompt" /></p>
<br />

<s:form id="myForm" action="createWeblog">
    <table class="formtable">
      <tbody id="formBody">
        <script id="formTemplate" type="text/x-jsrender">
          <tr id="recordId" data-id="{{:id}}">
              <td class="label"><label for="name"><s:text name="generic.name"/></label></td>
              <td class="field"><input type="text" data-link="newWeblog.name" size="30" maxlength="30" onBlur="this.value=this.value.trim()" required></td>
              <td class="description"><s:text name="createWebsite.tip.name" /></td>
          </tr>

          <tr>
              <td class="label"><label for="description"><s:text name="generic.tagline"/></label></td>
              <td class="field"><input type="text" data-link="newWeblog.tagline" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
              <td class="description"><s:text name="createWebsite.tip.description" /></td>
          </tr>

          <tr>
              <td class="label"><label for="handle"><s:text name="createWebsite.handle" /></label></td>
              <td class="field">
                  <input type="text" data-link="newWeblog.handle trigger=true" size="30" maxlength="30" onBlur="this.value=this.value.trim()" required><br />
                  <span style="text-size:70%">
                      <s:text name="createWebsite.weblogUrl" />:&nbsp;
                      <s:property value="absoluteSiteURL" />/<span style="color:red" data-link="newWeblog.handle"></span>
                  </span>
              </td>
              <td class="description"><s:text name="createWebsite.tip.handle" /></td>
          </tr>

          <tr>
              <td class="label"><label for="locale"><s:text name="createWebsite.locale" /></label></td>
              <td class="field">
                 <s:select data-link="newWeblog.locale" size="1" list="localesList" listValue="displayName" required=""/>
              </td>
              <td class="description"><s:text name="createWebsite.tip.locale" /></td>
          </tr>

          <tr>
              <td class="label"><label for="timeZone"><s:text name="createWebsite.timeZone" /></label></td>
              <td class="field">
                 <s:select data-link="newWeblog.timeZone" size="1" list="timeZonesList" required=""/>
              </td>
              <td class="description"><s:text name="createWebsite.tip.timezone" /></td>
          </tr>

          <tr>
              <td class="label"><label for="theme"><s:text name="createWebsite.theme" /></label></td>
              <td class="field">
                  <select id="themeSelector" data-link="newWeblog.theme" size="1">
                       {{for themeList}}
                           <option value="{{:id}}">{{:name}}</option>
                       {{/for}}
                  </select>
                  <br />
                  <br />
                  <div id="themeDetails" style="height:400px"></div>
              </td>
              <td class="description"><s:text name="createWebsite.tip.theme" /></td>
          </tr>
          </script>
        </tbody>
    </table>

    <script id="selectedThemeTemplate" type="text/x-jsrender">
        <p id="themeDescription">{{:description}}</p>
        <img id="themeImage" src="<s:property value='siteURL'/>{{:previewPath}}"></img>
    </script>

    <br />

    <s:submit value="%{getText('createWebsite.button.save')}" />
    <input type="button" value="<s:text name="generic.cancel"/>" onclick="window.location='<s:url action="menu"/>'" />

</s:form>
