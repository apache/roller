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

<p class="subtitle">
   <s:text name="websiteSettings.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  
   
<s:form action="weblogConfig!save" theme="bootstrap" cssClass="form-horizontal">
	<s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />

    <%-- ***** General settings ***** --%>
    
    <s:text name="websiteSettings.generalSettings" />

        <s:textfield label="%{getText('websiteSettings.websiteTitle')}"
                     name="bean.name" size="30" maxlength="40" />

        <s:textfield label="%{getText('generic.tagline')}"
                     name="bean.tagline" size="30" maxlength="255" />

        <s:textfield label="%{getText('websiteSettings.icon')}"
                     name="bean.icon" size="30" maxlength="40" />

        <s:textarea label="%{getText('websiteSettings.about')}"
                     name="bean.about" rows="3" cols="40 "/>

        <s:textfield label="%{getText('websiteSettings.emailAddress')}"
                     name="bean.emailAddress" size="30" maxlength="40" />

        <s:text name="websiteSettings.editor" />
        <s:select name="bean.editorPage" size="1" list="editorsList" listKey="id" listValue="getText(name)" />

        <td class="label"><s:text name="websiteSettings.active" /></td>
        <td class="field"><s:checkbox name="bean.active" /></td>
        <td class="description"></td>

        <s:textfield label="%{getText('')}"
                     name="" size="30" maxlength="40" />

        <td class="label"><s:text name="websiteSettings.entryDisplayCount" /></td>
        <td class="field"><s:textfield name="bean.entryDisplayCount" size="4"/></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <s:textfield label="%{getText('')}"
                     name="" size="30" maxlength="40" />

        <s:textfield label="%{getText('')}"
                     name="" size="30" maxlength="40" />

    <%-- ***** Language/i18n settings ***** --%>

        <td colspan="3"><h2><s:text name="websiteSettings.languageSettings" /></h2></td>

        <td class="label"><s:text name="websiteSettings.enableMultiLang" /></td>
        <td class="field"><s:checkbox name="bean.enableMultiLang" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="websiteSettings.showAllLangs" /></td>
        <td class="field"><s:checkbox name="bean.showAllLangs" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="createWebsite.locale" />
        <td class="field">
            <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
        </td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="createWebsite.timeZone" />
        <td class="field">
            <s:select name="bean.timeZone" size="1" list="timeZonesList" />
        </td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

    
    <%-- ***** Comment settings ***** --%>
    
        <td colspan="3"><h2><s:text name="websiteSettings.commentSettings" /></h2></td>

        <td class="label"><s:text name="websiteSettings.allowComments" /></td>
        <td class="field"><s:checkbox name="bean.allowComments" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

    <s:if test="!getBooleanProp('users.moderation.required')">
        <td class="label"><s:text name="websiteSettings.moderateComments" /></td>
        <td class="field"><s:checkbox name="bean.moderateComments" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>
    </s:if>
    
    <s:if test="getBooleanProp('users.comments.emailnotify')">
            <td class="label"><s:text name="websiteSettings.emailComments" /></td>
            <td class="field"><s:checkbox name="bean.emailComments"/></td>
            <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>
    </s:if>

    <%-- ***** Default entry comment settings ***** --%>

        <td colspan="3"><h2><s:text name="websiteSettings.defaultCommentSettings" /></h2></td>

        <td class="label"><s:text name="websiteSettings.defaultAllowComments" /></td>
        <td class="field"><s:checkbox name="bean.defaultAllowComments" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="websiteSettings.defaultCommentDays" /></td>
        <td class="field">
            <s:select name="bean.defaultCommentDays" list="commentDaysList" size="1" listKey="key" listValue="value" />
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="websiteSettings.applyCommentDefaults" /></td>
        <td class="field"><s:checkbox name="bean.applyCommentDefaults" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

    <%-- ***** Blogger API setting settings ***** --%>
    
        <td colspan="3"><h2><s:text name="websiteSettings.bloggerApi" /></h2></td>

        <td class="label"><s:text name="websiteSettings.enableBloggerApi" /></td>
        <td class="field"><s:checkbox name="bean.enableBloggerApi" /></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

        <td class="label"><s:text name="websiteSettings.bloggerApiCategory" /></td>
        <td class="field">
            <s:select name="bean.bloggerCategoryId" list="weblogCategories" size="1" listKey="id" listValue="name" />
        </td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>

    <%-- ***** Plugins "formatting" settings ***** --%>

        <td colspan="3"><h2><s:text name="websiteSettings.formatting" /></h2></td>

    <s:if test="!pluginsList.isEmpty">
            <td class="label"><s:text name="websiteSettings.label1" /> <br /><s:text name="websiteSettings.label2" /></td>
            <td class="field">
                <s:checkboxlist theme="roller" list="pluginsList" name="bean.defaultPluginsArray" listKey="name" listValue="name" />
            
            </td>
            <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>
    </s:if>
    <s:else>
        <s:hidden name="defaultPlugins" />
    </s:else>


    <%-- ***** Spam prevention settings ***** --%>
    
        <td colspan="3"><h2><s:text name="websiteSettings.spamPrevention" /></h2></td>

        <td class="label"><s:text name="websiteSettings.ignoreUrls" /></td>
        <td class="field"><s:textarea name="bean.blacklist" rows="7" cols="40"/></td>
        <td class="description"><%-- <s:text name="websiteSettings.tip." /> --%></td>


    <%-- ***** Web analytics settings ***** --%>

    <s:if test="getBooleanProp('analytics.code.override.allowed')">
            <td colspan="3"><h2><s:text name="configForm.webAnalytics" /></h2></td>

            <td class="label"><s:text name="websiteSettings.analyticsTrackingCode" /></td>
            <td class="field"><s:textarea name="bean.analyticsCode" rows="10" cols="70"/></td>
    </s:if>

</table>

<br />
<div class="control">
    <s:submit value="%{getText('websiteSettings.button.update')}" />
</div>
        
<br />
<br />

</s:form>


<s:form action="weblogRemove" cssClass="form-horizontal">
	<s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />
    
    <h2><s:text name="websiteSettings.removeWebsiteHeading" /></h2>
    
    <p>
        <s:text name="websiteSettings.removeWebsite" /><br/><br/>
        <span class="warning">
            <s:text name="websiteSettings.removeWebsiteWarning" />
        </span>
    </p>
    
    <br />
    
    <s:submit value="%{getText('websiteSettings.button.remove')}" />
    
    <br />
    <br />    
    <br />
    
</s:form>
