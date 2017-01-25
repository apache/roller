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
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
  deleteLabel: '<fmt:message key="generic.delete"/>',
  cancelLabel: '<fmt:message key="generic.cancel"/>'
};
</script>
<script src="<c:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/weblogconfig.js'/>"></script>

<div id="errorMessageDiv" class="errors" style="display:none">
  <script id="errorMessageTemplate" type="text/x-jsrender">
  <b>{{:errorMessage}}</b>
  <ul>
     {{for errors}}
     <li>{{>#data}}</li>
     {{/for}}
  </ul>
  </script>
</div>

<div id="successMessageDiv" class="messages" style="display:none">
  <s:if test="weblogId != null">
    <p><fmt:message key="generic.changes.saved"/></p>
  </s:if>
</div>

<input type="hidden" id="menuURL" value="<c:url value='/tb-ui/menu.rol'/>"/>
<input type="hidden" id="weblogId" value="<c:out value='${param.weblogId}'/>"/>

<%-- Create Weblog --%>
<s:if test="weblogId == null">
    <fmt:message key="weblogSettings.create.button.save" var="saveButtonText"/>
    <fmt:message key="weblogSettings.create.prompt" var="subtitlePrompt"/>
    <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/createWeblog.rol'/>"/>
</s:if>
<%-- Update Weblog --%>
<s:else>
    <fmt:message key="weblogSettings.button.update" var="saveButtonText"/>
    <fmt:message key="weblogSettings.prompt" var="subtitlePrompt">
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
    <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/authoring/weblogConfig.rol'/>?weblogId=<c:out value='${param.weblogId}'/>"/>
</s:else>

<p class="subtitle">
  ${subtitlePrompt}
</p>

<s:form id="myForm">
<table class="formtable">
    <tbody id="formBody">
      <script id="formTemplate" type="text/x-jsrender">
        <%-- ***** General settings ***** --%>

        <tr id="recordId" data-id="{{:weblogData.id}}">
            <td colspan="3"><h2><fmt:message key="weblogSettings.generalSettings" /></h2></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.websiteTitle"/>*</td>
            <td class="field"><input type="text" data-link="weblogData.name" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.tagline" /></td>
            <td class="field"><input type="text" data-link="weblogData.tagline" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
            <td class="description"><fmt:message key="weblogSettings.tip.tagline" /></td>
        </tr>

        <tr>
            <td class="label"><label for="handle"><fmt:message key="weblogSettings.handle" />*</label></td>
            <td class="field">
                <s:if test="weblogId == null">
                  <input type="text" data-link="weblogData.handle trigger=true" size="30" maxlength="30" onBlur="this.value=this.value.trim()" required><br />
                </s:if>
                <s:else>
                  <%-- handle not changeable --%>
                  <input type="text" data-link="weblogData.handle" size="30" maxlength="30" readonly><br />
                </s:else>
                <span style="text-size:70%">
                    <fmt:message key="weblogSettings.weblogUrl" />:&nbsp;
                    <c:out value="${absoluteSiteURL}" />/<span style="color:red" data-link="weblogData.handle"></span>
                </span>
            </td>
            <td class="description"><fmt:message key="weblogSettings.tip.handle" /></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.about" /></td>
            <td class="field"><textarea data-link="weblogData.about" rows="3" cols="40" maxlength="255" onBlur="this.value=this.value.trim()"></textarea></td>
            <td class="description"><fmt:message key="weblogSettings.tip.about" /></td>
        </tr>

        <s:if test="weblogId == null">
          <tr>
              <td class="label"><label for="theme"><fmt:message key="weblogSettings.theme" />*</label></td>
              <td class="field">
                  <select id="themeSelector" data-link="weblogData.theme" size="1">
                       {{for themeList}}
                           <option value="{{:id}}">{{:name}}</option>
                       {{/for}}
                  </select>
                  <br />
                  <br />
                  <div id="themeDetails" style="height:400px"></div>
              </td>
              <td class="description"><fmt:message key="weblogSettings.tip.theme" /></td>
          </tr>
        </s:if>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.editFormat" /></td>
            <td class="field">
                <select data-link="weblogData.editFormat trigger=true" size="1">
                    <option value="HTML"><fmt:message key="weblogSettings.editFormat.html"/></option>
                    <option value="COMMONMARK"><fmt:message key="weblogSettings.editFormat.commonMark"/></option>
                    <option value="RICHTEXT"><fmt:message key="weblogSettings.editFormat.richText"/></option>
                </select>
           </td>
           <td class="description"><fmt:message key="weblogSettings.tip.editFormat" /></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.visible" /></td>
            <td class="field"><input type="checkbox" data-link="weblogData.visible"></td>
            <td class="description"><fmt:message key="weblogSettings.tip.visible" /></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.entriesPerPage" /></td>
            <td class="field"><input type="number" min="1" max="100" step="1" data-link="weblogData.entriesPerPage" size="3" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.locale"/>*</td>
            <td class="field">
                <s:select data-link="weblogData.locale" size="1" list="localesList" listValue="displayName" required="required" />
            </td>
            <td class="description"><fmt:message key="weblogSettings.tip.locale" /></td>
        </tr>

        <tr>
            <td class="label"><fmt:message key="weblogSettings.timeZone"/>*</td>
            <td class="field">
                <s:select data-link="weblogData.timeZone" size="1" list="timeZonesList" required="required"/>
            </td>
            <td class="description"><fmt:message key="weblogSettings.tip.timezone" /></td>
        </tr>

        <s:if test="isUsersOverrideAnalyticsCode()">
            <tr>
                <td class="label"><fmt:message key="weblogSettings.analyticsTrackingCode" /></td>
                <td class="field"><textarea data-link="weblogData.analyticsCode" rows="10" cols="70" maxlength="1200" onBlur="this.value=this.value.trim()"></textarea></td>
                <td class="description"><fmt:message key="weblogSettings.tip.analyticsTrackingCode" /></td>
            </tr>
        </s:if>

        <%-- ***** Comment settings ***** --%>

        <s:if test="getCommentPolicy() != 'NONE'">
            <tr>
                <td colspan="3"><h2><fmt:message key="weblogSettings.commentSettings" /></h2></td>
            </tr>

            <tr>
                <td class="label"><fmt:message key="weblogSettings.allowComments" /></td>
                <td class="field">
                  <select data-link="weblogData.allowComments">
                     <option value="NONE"><fmt:message key="generic.no"/></option>
                     <option value="MUSTMODERATE"><fmt:message key="weblogSettings.mustModerateComments"/></option>
                     <s:if test="getCommentPolicy() != 'MUSTMODERATE'">
                         <option value="YES"><fmt:message key="weblogSettings.commentsOK"/></option>
                     </s:if>
                  </select>
                </td>
            </tr>

            <s:if test="isUsersCommentNotifications()">
                <tr>
                    <td class="label"><fmt:message key="weblogSettings.emailComments" /></td>
                    <td class="field"><input type="checkbox" data-link="weblogData.emailComments"></td>
                </tr>
            </s:if>

            <tr>
                <td class="label"><fmt:message key="weblogSettings.defaultCommentDays" /></td>
                <td class="field">
                    <s:select data-link="weblogData.defaultCommentDaysString" list="commentDaysList" size="1" listKey="left" listValue="right" />
                </td>
            </tr>

            <s:if test="weblogId != null">
              <tr>
                  <td class="label"><fmt:message key="weblogSettings.applyCommentDefaults" /></td>
                  <td class="field"><input type="checkbox" data-link="weblogData.applyCommentDefaults"></td>
              </tr>
            </s:if>

            <tr>
                <td class="label"><fmt:message key="weblogSettings.ignoreUrls" /></td>
                <td class="field"><textarea data-link="weblogData.blacklist" rows="7" cols="40" onBlur="this.value=this.value.trim()"></textarea></td>
                <td class="description"><fmt:message key="weblogSettings.tip.ignoreUrls" /></td>
            </tr>

        </s:if>

      </script>
    </tbody>
</table>

<br>
<div class="control">
    <s:submit value="%{getText(#saveButtonText)}"/>
    <input type="button" value="<fmt:message key='generic.cancel'/>" onclick="window.location='<c:url value='/tb-ui/menu.rol'/>'" />
</div>

<br>
<br>

  <s:if test="weblogId != null">
    <h2><fmt:message key="weblogSettings.removeWebsiteHeading" /></h2>

    <p>
        <fmt:message key="weblogSettings.removeWebsite" /><br><br>
        <span class="warning">
            <fmt:message key="weblogSettings.removeWebsiteWarning" />
        </span>
    </p>
    <br>
    <s:submit id="delete-link" value="%{getText('weblogSettings.button.remove')}"/>
    <br>
    <br>
    <br>
  </s:if>
</s:form>

<script id="selectedThemeTemplate" type="text/x-jsrender">
    <p id="themeDescription">{{:description}}</p>
    <img id="themeImage" src="<c:out value='${siteURL}'/>{{:previewPath}}"></img>
</script>

<div id="confirm-delete" title="<fmt:message key='websiteRemove.title'/>" style="display:none">
    <fmt:message key="websiteRemove.youSure">
       <fmt:param value="${actionWeblog.name}"/>
    </fmt:message>
    <br/>
    <br/>
    <span class="warning">
        <fmt:message key="weblogSettings.removeWebsiteWarning" />
    </span>
</div>
