<%@ include file="/taglibs.jsp" %>

<p class="subtitle">
   <fmt:message key="websiteSettings.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
   
<html:form action="/editor/website" method="post">
    <html:hidden property="method" value="update"/></input>

    <html:hidden property="id"/></input>
    <html:hidden property="weblogDayPageId" />
    <html:hidden property="locale"/></input>
    <html:hidden property="handle"/></input>
    <html:hidden property="timeZone"/></input>
    <html:hidden property="editorTheme"/></input>
    <html:hidden property="enabled"/></input>
    <html:hidden property="defaultCategoryId"/></input>

<table class="formtableNoDesc">

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
        <td class="label"><fmt:message key="websiteSettings.homePage" /></td>
        <td class="field">
            <html:select property="defaultPageId" size="1">
                <html:options collection="pages"
                    property="id" labelProperty="name" />
            </html:select>
        </td>
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

    
    <%-- ***** Comment settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.commentSettings" /></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.allowComments" /></td>
        <td class="field"><html:checkbox property="allowComments" /></input></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>
<%
boolean emailComments = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
if (emailComments) { %>
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
<% } %>

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


<% if (org.roller.presentation.velocity.ContextLoader.hasPlugins()) { %>
    <tr>
        <td class="label">Default Entry Formatters <br />(applied in the listed order)</td>
        <td class="field">
        <logic:iterate id="plugin" type="org.roller.presentation.velocity.PagePlugin"
            collection="<%= org.roller.presentation.velocity.ContextLoader.getPagePlugins() %>">
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
<% } else { %>
    <html:hidden property="defaultPlugins" />
<% } %>


    <%-- ***** Spam prevention settings ***** --%>
    
    <tr>
        <td colspan="3"><h2><fmt:message key="websiteSettings.spamPrevention" /></h2></td>
    </tr>

    <tr>
        <td class="label"><fmt:message key="websiteSettings.ignoreUrls" /></td>
        <td class="field"><html:textarea property="ignoreWords" rows="7" cols="40"/></td>
        <td class="description"><%-- <fmt:message key="websiteSettings.tip." /> --%></td>
    </tr>

</table>

<br />
<div class="control">
    <input type="submit" value='<fmt:message key="websiteSettings.button.update" />' />
</div>
        
<c:if test="${model.website.adminUserCount == 1 && model.groupBloggingEnabled}">
    <br />
    <br />
    <h2><fmt:message key="websiteSettings.removeWebsiteHeading" /></h2>
    <p>
        <fmt:message key="websiteSettings.removeWebsite" /><br/>
        <span class="warning">
            <fmt:message key="websiteSettings.removeWebsiteWarning" />
        </span>
    </p>
    <br />
    <input type="button" value='<fmt:message key="websiteSettings.button.remove" />'  
        onclick='document.websiteFormEx.method.value="removeOk"; document.websiteFormEx.submit()' />
    <br />
    <br />
</c:if>
        
<br />
</html:form>


