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
    <s:text name="websiteSettings.subtitle">
        <s:param value="actionWeblog.handle"/>
    </s:text>
</p>

<s:form action="weblogConfig!save" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog" value="%{actionWeblog.handle}"/>

    <%-- ***** General settings ***** --%>

    <h3><s:text name="websiteSettings.generalSettings"/></h3>

    <s:textfield label="%{getText('websiteSettings.websiteTitle')}"
                 name="bean.name" size="30" maxlength="40"/>

    <s:textfield label="%{getText('generic.tagline')}"
                 name="bean.tagline" size="30" maxlength="255"/>

    <s:textfield label="%{getText('websiteSettings.icon')}"
                 name="bean.icon" size="30" maxlength="40"/>

    <s:textarea label="%{getText('websiteSettings.about')}"
                name="bean.about" rows="3" cols="40 "/>

    <s:textfield label="%{getText('websiteSettings.emailAddress')}"
                 name="bean.emailAddress" size="30" maxlength="40"/>

    <s:select name="bean.editorPage" label="%{getText('websiteSettings.editor')}"
              list="editorsList" listKey="id" listValue="getText(name)"/>

    <s:textfield type="number" label="%{getText('websiteSettings.entryDisplayCount')}"
                 name="bean.entryDisplayCount" size="4"/>

    <s:checkbox label="%{getText('websiteSettings.active')}"
                name="bean.active" size="30" maxlength="40"/>

    <%-- ***** Language/i18n settings ***** --%>

    <h3><s:text name="websiteSettings.languageSettings"/></h3>

    <s:select name="bean.locale" list="localesList" listValue="displayName"
              label="%{getText('createWebsite.locale')}"/>

    <s:select name="bean.timeZone" list="timeZonesList"
              label="%{getText('createWebsite.timezone')}"/>

    <s:checkbox name="bean.enableMultiLang"
                label="%{getText('websiteSettings.enableMultiLang')}"/>

    <s:checkbox name="bean.showAllLangs"
                label="%{getText('websiteSettings.showAllLangs')}"/>

    <%-- ***** Comment settings ***** --%>

    <h3><s:text name="websiteSettings.commentSettings"/></h3>

    <s:checkbox name="bean.allowComments"
                label="%{getText('websiteSettings.allowComments')}"/>

    <s:if test="getBooleanProp('users.comments.emailnotify')">
        <s:checkbox name="bean.emailComments"
                    label="%{getText('websiteSettings.emailComments')}"/>
    </s:if>

    <s:if test="!getBooleanProp('users.moderation.required')">
        <s:checkbox name="bean.moderateComments"
                    label="%{getText('websiteSettings.moderateComments')}"/>
    </s:if>

    <%-- ***** Default entry comment settings ***** --%>

    <h3><s:text name="websiteSettings.defaultCommentSettings"/></h3>

    <s:select name="bean.defaultCommentDays" label="%{getText('websiteSettings.applyCommentDefaults')}"
              list="commentDaysList" listKey="key" listValue="value"/>

    <s:checkbox name="bean.defaultAllowComments"
                label="%{getText('websiteSettings.defaultAllowComments')}"/>

    <s:checkbox name="bean.applyCommentDefaults"
                label="%{getText('websiteSettings.applyCommentDefaults')}"/>

    <%-- ***** Blogger API setting settings ***** --%>

    <h3><s:text name="websiteSettings.bloggerApi"/></h3>

    <s:select name="bean.bloggerCategoryId" label="%{getText('websiteSettings.bloggerApiCategory')}"
              list="weblogCategories" listKey="id" listValue="name"/>

    <s:checkbox name="bean.enableBloggerApi"
                label="%{getText('websiteSettings.enableBloggerApi')}"/>

    <%-- ***** Plugins "formatting" settings ***** --%>

    <h3><s:text name="websiteSettings.formatting"/></h3>

    <s:if test="!pluginsList.isEmpty">

        <s:checkboxlist list="pluginsList" label="%{getText('websiteSettings.label1')}"
                        name="bean.defaultPluginsArray" listKey="name" listValue="name"/>

    </s:if>
    <s:else>
        <s:hidden name="defaultPlugins"/>
    </s:else>

    <%-- ***** Spam prevention settings ***** --%>

    <h3><s:text name="websiteSettings.spamPrevention"/></h3>

    <s:textarea name="bean.bannedwordslist" rows="7" cols="40"
                label="%{getText('websiteSettings.analyticsTrackingCode')}"/>

    <%-- ***** Web analytics settings ***** --%>

    <s:if test="getBooleanProp('analytics.code.override.allowed') && !weblogAdminsUntrusted">
        <h3><s:text name="configForm.webAnalytics"/></h3>

        <s:textarea name="bean.analyticsCode" rows="10" cols="70"
                    label="%{getText('websiteSettings.analyticsTrackingCode')}"/>
    </s:if>

    <div class="control" style="margin-bottom:5em">
        <s:submit cssClass="btn btn-success" value="%{getText('websiteSettings.button.update')}"/>
    </div>

</s:form>


<s:form action="weblogRemove" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog" value="%{actionWeblog.handle}"/>

    <h3><s:text name="websiteSettings.removeWebsiteHeading"/></h3>
    <s:text name="websiteSettings.removeWebsite"/><br/><br/>
    <div class="alert alert-danger" role="alert">
        <s:text name="websiteSettings.removeWebsiteWarning"/>
    </div>
    <s:submit cssClass="btn btn-danger" value="%{getText('websiteSettings.button.remove')}"/>

</s:form>
