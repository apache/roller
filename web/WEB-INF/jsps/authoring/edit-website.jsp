<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.apache.roller.ui.authoring.struts.actions.WebsiteFormAction" %>
<%
WebsiteFormAction.WebsitePageModel model = 
    (WebsiteFormAction.WebsitePageModel)request.getAttribute("model");
%>
<p class="subtitle">
   <fmt:message key="websiteSettings.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
   
<html:form action="/roller-ui/authoring/website" method="post">
    <html:hidden property="method" value="update"/></input>

    <html:hidden property="id"/></input>
    <html:hidden property="defaultPageId" /></input>
    <html:hidden property="weblogDayPageId" /></input>
    <html:hidden property="handle"/></input>
    <html:hidden property="editorTheme"/></input>
    <html:hidden property="enabled"/></input>
    <html:hidden property="defaultCategoryId"/></input>

<table class="formtableNoDesc">

    <%-- ***** General settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.generalSettings" /></h2></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.websiteTitle" />
        <td class="field"><html:text property="name" size="40"/></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.websiteDescription" /></td>
        <td class="field"><html:textarea property="description" rows="3" cols="40"/></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.emailAddress" />
        <td class="field"><html:text property="emailAddress" size="40"/></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.editor" /></td>
        <td class="field">
            <html:select property="editorPage" size="1">
                <html:options name="editorPagesList" />
            </html:select></p>
       </td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.active" /></td>
        <td class="field"><html:checkbox property="active" /></input></td>
        <td class="description"></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.entryDisplayCount" /></td>
        <td class="field"><html:text property="entryDisplayCount" size="4"/></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    
    <%-- ***** Language/i18n settings ***** --%>
    
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.languageSettings" /></h2></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.enableMultiLang" /></td>
        <td class="field"><html:checkbox property="enableMultiLang" /></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.showAllLangs" /></td>
        <td class="field"><html:checkbox property="showAllLangs" /></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="createWebsite.locale" />
        <td class="field">
            <html:select property="locale" size="1" >
                <html:options collection="locales" property="value" labelProperty="label"/>
            </html:select>
        </td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="createWebsite.timeZone" />
        <td class="field">
            <html:select property="timeZone" size="1" >
                <html:options collection="timeZones" property="value" labelProperty="label"/>
            </html:select>
        </td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    
    <%-- ***** Comment settings ***** --%>
    
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.commentSettings" /></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.allowComments" /></td>
        <td class="field"><html:checkbox property="allowComments" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <c:if test="${!model.moderationRequired}">
    <tr>
        <td class="label"><fmt:message key="websiteSettings.moderateComments" /></td>
        <td class="field"><html:checkbox property="moderateComments" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    </c:if>
    
    <c:if test="${model.emailNotificationEnabled}">
        <tr>
            <td class="label"><fmt:message key="websiteSettings.emailComments" /></td>
            <td class="field"><html:checkbox property="emailComments" onclick="toggleNextRow(this)" /></input></td>
            <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
        </tr>

        <tr <c:if test="${!websiteFormEx.emailComments}">style="display: none"</c:if>>
            <td class="label"><fmt:message key="websiteSettings.emailFromAddress" /></td>
            <td class="field"><html:text size="50" property="emailFromAddress" /></input></td>
            <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
        </tr>
    </c:if>

    <%-- ***** Default entry comment settings ***** --%>

    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.defaultCommentSettings" /></h2></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.defaultAllowComments" /></td>
        <td class="field"><html:checkbox property="defaultAllowComments" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
     <tr>
        <td class="label"><fmt:message key="websiteSettings.defaultCommentDays" /></td>
        <td class="field">
         <html:select property="defaultCommentDays">
             <html:option key="weblogEdit.unlimitedCommentDays" value="0"  />
             <html:option key="weblogEdit.days1" value="1"  />
             <html:option key="weblogEdit.days2" value="2"  />
             <html:option key="weblogEdit.days3" value="3"  />
             <html:option key="weblogEdit.days4" value="4"  />
             <html:option key="weblogEdit.days5" value="5"  />
             <html:option key="weblogEdit.days7" value="7"  />
             <html:option key="weblogEdit.days10" value="10"  />
             <html:option key="weblogEdit.days20" value="20"  />
             <html:option key="weblogEdit.days30" value="30"  />
             <html:option key="weblogEdit.days60" value="60"  />
             <html:option key="weblogEdit.days90" value="90"  />
         </html:select>
        </td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
    
    <tr>
        <td class="label"><fmt:message key="websiteSettings.applyCommentDefaults" /></td>
        <td class="field"><html:checkbox property="applyCommentDefaults" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <%-- ***** Blogger API setting settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.bloggerApi" /></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.enableBloggerApi" /></td>
        <td class="field"><html:checkbox property="enableBloggerApi" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.bloggerApiCategory" /></td>
        <td class="field">
            <html:select property="bloggerCategoryId" size="1">
                <html:options collection="bloggerCategories"
                    property="id" labelProperty="path" />
            </html:select>
        </td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <%-- ***** Plugins "formatting" settings ***** --%>

    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.formatting" /></h2></td>
    </tr>

<c:choose>
    <c:when test="${model.hasPagePlugins}">
        <tr>
            <td class="label">Default Entry Formatters <br />(applied in the listed order)</td>
            <td class="field">
            <logic:iterate id="plugin" type="org.apache.roller.model.WeblogEntryPlugin"
                collection="<%= model.getPagePlugins() %>">
                <html:multibox property="defaultPluginsArray"
                    title="<%= plugin.getName() %>" value="<%= plugin.getName() %>" /></input>
                <label for="<%= plugin.getName() %>"><%= plugin.getName() %></label>
                <a href="javascript:void(0);" onmouseout="return nd();"
                onmouseover="return overlib('<%= plugin.getDescription() %>', STICKY, MOUSEOFF, TIMEOUT, 3000);">?</a>
                <br />
            </logic:iterate>
            </td>
            <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
        </tr>
    </c:when>
    <c:otherwise>
        <html:hidden property="defaultPlugins" />
    </c:otherwise>
</c:choose>


    <%-- ***** Spam prevention settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.spamPrevention" /></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.ignoreUrls" /></td>
        <td class="field"><html:textarea property="blacklist" rows="7" cols="40"/></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

    <%-- ***** Global admin only settings ***** --%>
    <c:choose>
        <c:when test="${model.globalAdminUser}">
            <tr>
                <td colspan="3"><h2><fmt:message key="websiteSettings.adminSettings" /></h2></td>
            </tr>
            <tr>
                <td class="label"><fmt:message key="websiteSettings.pageModels" /></td>
                <td class="field"><html:textarea property="pageModels" rows="7" cols="40"/></td>
                <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
            </tr>
        </c:when>
        <c:otherwise>
            <html:hidden property="pageModels" />
        </c:otherwise>
    </c:choose>

</table>

<br />
<div class="control">
    <input type="submit" value='<fmt:message key="websiteSettings.button.update" />' />
</div>
        
<br />
<br />

<h2><fmt:message key="websiteSettings.removeWebsiteHeading" /></h2>
<p>
    <fmt:message key="websiteSettings.removeWebsite" /><br/><br/>
    <span class="warning">
        <fmt:message key="websiteSettings.removeWebsiteWarning" />
    </span>
</p>
<br />
<input type="button" value='<fmt:message key="websiteSettings.button.remove" />'  
    onclick='document.websiteFormEx.method.value="removeOk"; document.websiteFormEx.submit()' />
    
<br />
<br />    
<br />

</html:form>


