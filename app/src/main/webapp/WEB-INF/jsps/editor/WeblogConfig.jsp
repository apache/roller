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

<input type="hidden" id="refreshURL" value="<s:url action='weblogConfig'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>
<input type="hidden" id="menuURL" value="<s:url action='menu'/>"/>
<input type="hidden" id="weblogId" value="<s:property value='%{#parameters.weblogId}'/>"/>

<p class="subtitle">
   <s:text name="websiteSettings.subtitle" >
       <s:param value="actionWeblog.handle"/>
   </s:text>
</p>

<s:form id="myForm" action="weblogConfig">
<table class="formtableNoDesc">
    <tbody id="formBody">
      <script id="formTemplate" type="text/x-jsrender">
        <%-- ***** General settings ***** --%>

        <tr id="recordId" data-id="{{:id}}">
            <td colspan="3"><h2><s:text name="websiteSettings.generalSettings" /></h2></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.websiteTitle"/></td>
            <td class="field"><input type="text" data-link="name" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="generic.tagline" /></td>
            <td class="field"><input type="text" data-link="tagline" size="40" maxlength="255" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.about" /></td>
            <td class="field"><textarea data-link="about" rows="3" cols="40" maxlength="255" onBlur="this.value=this.value.trim()"></textarea></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.editor" /></td>
            <td class="field">
                <select data-link="editorPage trigger=true" size="1">
                    <option value="editor-text.jsp"><s:text name="editor.text.name"/></option>
                    <option value="editor-xinha.jsp"><s:text name="editor.xinha.name"/></option>
                </select>
           </td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.visible" /></td>
            <td class="field"><input type="checkbox" data-link="visible"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="websiteSettings.entriesPerPage" /></td>
            <td class="field"><input type="number" min="1" max="100" step="1" data-link="entriesPerPage" size="3" onBlur="this.value=this.value.trim()"></td>
        </tr>

        <tr>
            <td class="label"><s:text name="createWebsite.locale" /></td>
            <td class="field">
                <s:select data-link="locale" size="1" list="localesList" listValue="displayName" />
            </td>
        </tr>

        <tr>
            <td class="label"><s:text name="createWebsite.timeZone" /></td>
            <td class="field">
                <s:select data-link="timeZone" size="1" list="timeZonesList" />
            </td>
        </tr>


        <%-- ***** Comment settings ***** --%>

        <s:if test="getBooleanProp('users.comments.enabled')">
            <tr>
                <td colspan="3"><h2><s:text name="websiteSettings.commentSettings" /></h2></td>
            </tr>

            <tr>
                <td class="label"><s:text name="websiteSettings.allowComments" /></td>
                <td class="field"><input type="checkbox" data-link="allowComments"></td>
            </tr>

            <s:if test="!getBooleanProp('users.moderation.required')">
                <tr>
                    <td class="label"><s:text name="websiteSettings.approveComments" /></td>
                    <td class="field"><input type="checkbox" data-link="approveComments"></td>
                </tr>
            </s:if>

            <s:if test="getBooleanProp('users.comments.emailnotify')">
                <tr>
                    <td class="label"><s:text name="websiteSettings.emailComments" /></td>
                    <td class="field"><input type="checkbox" data-link="emailComments"></td>
                </tr>
            </s:if>

            <tr>
                <td class="label"><s:text name="websiteSettings.defaultCommentDays" /></td>
                <td class="field">
                    <s:select data-link="defaultCommentDaysString" list="commentDaysList" size="1" listKey="left" listValue="right" />
                </td>
            </tr>

            <tr>
                <td class="label"><s:text name="websiteSettings.applyCommentDefaults" /></td>
                <td class="field"><input type="checkbox" data-link="applyCommentDefaults"></td>
            </tr>

            <tr>
                <td class="label"><s:text name="websiteSettings.ignoreUrls" /></td>
                <td class="field"><textarea data-link="blacklist" rows="7" cols="40" onBlur="this.value=this.value.trim()"></textarea></td>
            </tr>

        </s:if>

        <%-- ***** Plugins "formatting" settings ***** --%>
        <s:if test="!weblogEntryPlugins.isEmpty">
            <tr>
                <td colspan="3"><h2><s:text name="websiteSettings.formatting" /></h2></td>
            </tr>

            <tr>
                <td class="label"><s:text name="websiteSettings.label1" /> <br /><s:text name="websiteSettings.label2" /></td>
                <td class="field">
                    <s:checkboxlist theme="strutsoverride" list="weblogEntryPlugins" listKey="name" listValue="name" data-link="defaultPluginsArray"/>
                </td>
            </tr>
        </s:if>

        <%-- ***** Web analytics settings ***** --%>

        <s:if test="getBooleanProp('analytics.code.override.allowed')">
            <tr>
                <td colspan="3"><h2><s:text name="configForm.webAnalytics" /></h2></td>
            </tr>

            <tr>
                <td class="label"><s:text name="websiteSettings.analyticsTrackingCode" /></td>
                <td class="field"><textarea data-link="analyticsCode" rows="10" cols="70" maxlength="1200" onBlur="this.value=this.value.trim()"></textarea></td>
            </tr>
        </s:if>
      </script>
    </tbody>
</table>

<br />
<div class="control">
    <s:submit value="%{getText('websiteSettings.button.update')}" action="weblogConfig"/>
</div>

<br />
<br />

<h2><s:text name="websiteSettings.removeWebsiteHeading" /></h2>

<p>
    <s:text name="websiteSettings.removeWebsite" /><br/><br/>
    <span class="warning">
        <s:text name="websiteSettings.removeWebsiteWarning" />
    </span>
</p>

<br />

<s:submit value="%{getText('websiteSettings.button.remove')}" id="delete-link"/>
    <br />
    <br />
    <br />
</s:form>

<div id="confirm-delete" title="<s:text name='websiteRemove.title'/>" style="display:none">
    <s:text name="websiteRemove.youSure">
        <s:param value="actionWeblog.name" />
    </s:text>
    <br/>
    <br/>
    <span class="warning">
        <s:text name="websiteSettings.removeWebsiteWarning" />
    </span>
</div>
