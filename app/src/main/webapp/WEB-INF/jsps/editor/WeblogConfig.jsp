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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
  deleteLabel: '<s:text name="generic.delete"/>',
  cancelLabel: '<s:text name="generic.cancel"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/weblogconfig.js'/>"></script>

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
    <p><s:text name="generic.changes.saved"/></p>
  </s:if>
</div>

<input type="hidden" id="menuURL" value="<s:url action='menu'/>"/>
<input type="hidden" id="weblogId" value="<s:property value='%{#parameters.weblogId}'/>"/>

<%-- Create Weblog --%>
<s:if test="weblogId == null">
    <s:text var="saveButtonText" name="weblogSettings.create.button.save"/>
    <s:text var="subtitlePrompt" name="weblogSettings.create.prompt"/>
    <input type="hidden" id="refreshURL" value="<s:url action='createWeblog'/>"/>
</s:if>
<%-- Update Weblog --%>
<s:else>
    <s:text var="saveButtonText" name="weblogSettings.button.update"/>
    <s:text var="subtitlePrompt" name="weblogSettings.prompt">
        <s:param value="actionWeblog.handle"/>
    </s:text>
    <input type="hidden" id="refreshURL" value="<s:url action='weblogConfig'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>
</s:else>

<p class="subtitle">
  <s:text name="%{#subtitlePrompt}"/>
</p>

<s:form id="myForm">
<table class="formtable">
    <tbody id="formBody">
      <script id="formTemplate" type="text/x-jsrender">
        <%-- ***** General settings ***** --%>

        <tr id="recordId" data-id="{{:weblogData.id}}">
            <td colspan="3"><h2><s:text name="weblogSettings.generalSettings" /></h2></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.websiteTitle"/>*</td>
            <td class="field"><input type="text" data-link="weblogData.name" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.tagline" /></td>
            <td class="field"><input type="text" data-link="weblogData.tagline" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
            <td class="description"><s:text name="weblogSettings.tip.tagline" /></td>
        </tr>

        <tr>
            <td class="label"><label for="handle"><s:text name="weblogSettings.handle" />*</label></td>
            <td class="field">
                <s:if test="weblogId == null">
                  <input type="text" data-link="weblogData.handle trigger=true" size="30" maxlength="30" onBlur="this.value=this.value.trim()" required><br />
                </s:if>
                <s:else>
                  <%-- handle not changeable --%>
                  <input type="text" data-link="weblogData.handle" size="30" maxlength="30" readonly><br />
                </s:else>
                <span style="text-size:70%">
                    <s:text name="weblogSettings.weblogUrl" />:&nbsp;
                    <s:property value="absoluteSiteURL" />/<span style="color:red" data-link="weblogData.handle"></span>
                </span>
            </td>
            <td class="description"><s:text name="weblogSettings.tip.handle" /></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.about" /></td>
            <td class="field"><textarea data-link="weblogData.about" rows="3" cols="40" maxlength="255" onBlur="this.value=this.value.trim()"></textarea></td>
            <td class="description"><s:text name="weblogSettings.tip.about" /></td>
        </tr>

        <s:if test="weblogId == null">
          <tr>
              <td class="label"><label for="theme"><s:text name="weblogSettings.theme" />*</label></td>
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
              <td class="description"><s:text name="weblogSettings.tip.theme" /></td>
          </tr>
        </s:if>

        <tr>
            <td class="label"><s:text name="weblogSettings.editor" /></td>
            <td class="field">
                <select data-link="weblogData.editorPage trigger=true" size="1">
                    <option value="editor-text.jsp"><s:text name="editor.text.name"/></option>
                    <option value="editor-rte.jsp"><s:text name="editor.rte.name"/></option>
                </select>
           </td>
           <td class="description"><s:text name="weblogSettings.tip.editor" /></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.visible" /></td>
            <td class="field"><input type="checkbox" data-link="weblogData.visible"></td>
            <td class="description"><s:text name="weblogSettings.tip.visible" /></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.entriesPerPage" /></td>
            <td class="field"><input type="number" min="1" max="100" step="1" data-link="weblogData.entriesPerPage" size="3" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.locale"/>*</td>
            <td class="field">
                <s:select data-link="weblogData.locale" size="1" list="localesList" listValue="displayName" required="required" />
            </td>
            <td class="description"><s:text name="weblogSettings.tip.locale" /></td>
        </tr>

        <tr>
            <td class="label"><s:text name="weblogSettings.timeZone"/>*</td>
            <td class="field">
                <s:select data-link="weblogData.timeZone" size="1" list="timeZonesList" required="required"/>
            </td>
            <td class="description"><s:text name="weblogSettings.tip.timezone" /></td>
        </tr>

        <s:if test="getBooleanProp('analytics.code.override.allowed')">
            <tr>
                <td class="label"><s:text name="weblogSettings.analyticsTrackingCode" /></td>
                <td class="field"><textarea data-link="weblogData.analyticsCode" rows="10" cols="70" maxlength="1200" onBlur="this.value=this.value.trim()"></textarea></td>
                <td class="description"><s:text name="weblogSettings.tip.analyticsTrackingCode" /></td>
            </tr>
        </s:if>

        <%-- ***** Comment settings ***** --%>

        <s:if test="getProp('users.comments.enabled') != 'NONE'">
            <tr>
                <td colspan="3"><h2><s:text name="weblogSettings.commentSettings" /></h2></td>
            </tr>

            <tr>
                <td class="label"><s:text name="weblogSettings.allowComments" /></td>
                <td class="field">
                  <select data-link="weblogData.allowComments">
                     <option value="NONE"><s:text name="generic.no"/></option>
                     <option value="MUSTMODERATE"><s:text name="weblogSettings.mustModerateComments"/></option>
                     <s:if test="getProp('users.comments.enabled') != 'MUSTMODERATE'">
                         <option value="YES"><s:text name="weblogSettings.commentsOK"/></option>
                     </s:if>
                  </select>
                </td>
            </tr>

            <s:if test="getBooleanProp('users.comments.emailnotify')">
                <tr>
                    <td class="label"><s:text name="weblogSettings.emailComments" /></td>
                    <td class="field"><input type="checkbox" data-link="weblogData.emailComments"></td>
                </tr>
            </s:if>

            <tr>
                <td class="label"><s:text name="weblogSettings.defaultCommentDays" /></td>
                <td class="field">
                    <s:select data-link="weblogData.defaultCommentDaysString" list="commentDaysList" size="1" listKey="left" listValue="right" />
                </td>
            </tr>

            <s:if test="weblogId != null">
              <tr>
                  <td class="label"><s:text name="weblogSettings.applyCommentDefaults" /></td>
                  <td class="field"><input type="checkbox" data-link="weblogData.applyCommentDefaults"></td>
              </tr>
            </s:if>

            <tr>
                <td class="label"><s:text name="weblogSettings.ignoreUrls" /></td>
                <td class="field"><textarea data-link="weblogData.blacklist" rows="7" cols="40" onBlur="this.value=this.value.trim()"></textarea></td>
                <td class="description"><s:text name="weblogSettings.tip.ignoreUrls" /></td>
            </tr>

        </s:if>

        <%-- ***** Plugins "formatting" settings ***** --%>
        <s:if test="!weblogEntryPlugins.isEmpty">
            <tr>
                <td colspan="3"><h2><s:text name="weblogSettings.formatting" /></h2></td>
            </tr>

            <tr>
                <td class="label"><s:text name="weblogSettings.label1" /> <br /><s:text name="weblogSettings.label2" /></td>
                <td class="field">
                    <s:checkboxlist theme="strutsoverride" list="weblogEntryPlugins" listKey="name" listValue="name" data-link="weblogData.defaultPluginsArray"/>
                </td>
            </tr>
        </s:if>

      </script>
    </tbody>
</table>

<br>
<div class="control">
    <s:submit value="%{getText(#saveButtonText)}"/>
    <input type="button" value="<s:text name='generic.cancel'/>" onclick="window.location='<s:url action='menu'/>'" />
</div>

<br>
<br>

  <s:if test="weblogId != null">
    <h2><s:text name="weblogSettings.removeWebsiteHeading" /></h2>

    <p>
        <s:text name="weblogSettings.removeWebsite" /><br><br>
        <span class="warning">
            <s:text name="weblogSettings.removeWebsiteWarning" />
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
    <img id="themeImage" src="<s:property value='siteURL'/>{{:previewPath}}"></img>
</script>

<div id="confirm-delete" title="<s:text name='websiteRemove.title'/>" style="display:none">
    <s:text name="websiteRemove.youSure">
        <s:param value="actionWeblog.name" />
    </s:text>
    <br/>
    <br/>
    <span class="warning">
        <s:text name="weblogSettings.removeWebsiteWarning" />
    </span>
</div>
