<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
// <!--
function cancel() {
    document.userAdminForm.method.value="cancel"; 
    document.userAdminForm.submit();
}
<%@ include file="/theme/scripts/ajax-user.js" %>
// -->
</script> 

<%-- If user name is not specified, then allow user to choose a user to be loaded --%>
<c:if test="${empty userAdminForm.userName}">

    <p class="subtitle"><fmt:message key="userAdmin.subtitle.searchUser" /></p>

    <html:form action="/admin/user" method="post" focus="userName">
        <input name="method" type="hidden" value="edit" />    
        <div class="formrow">
           <label for="userName" class="formrow" />
               <fmt:message key="inviteMember.userName" /></label>
           <div>
               <input name="userName" id="userName" size="30" maxlength="30" 
                   onfocus="onUserNameFocus()" onkeyup="onUserNameChange()" /><br />
           </div>
        </div>        
        <div class="formrow">
           <label class="formrow" />&nbsp;</label>
           <div>
               <select id="userList" size="10" onchange="onUserSelected()" style="width:300px"></select>
           </div>
        </div>                      
        <br />       
        <div class="control">
            <input type="submit" value='<fmt:message key="userAdmin.edit" />' />
        </div>
    </html:form>

</c:if>

<%-- If a user name is specified, then show the admin user form --%>
<c:if test="${not empty userAdminForm.userName || userAdminForm.newUser == true}">

    <c:choose>
        <c:when test="${not empty userAdminForm.userName}">		
            <p class="subtitle"><fmt:message key="userAdmin.subtitle.editUser" /></p>
        </c:when>
        <c:otherwise>
            <h1>
                <fmt:message key="userAdmin.title.createNewUser" />
            </h1>			
            <p class="subtitle"><fmt:message key="userAdmin.subtitle.createNewUser" /></p>
        </c:otherwise>
    </c:choose>
    
    <html:form action="/admin/user" method="post">
        <html:hidden property="method" value="update"/></input>        
        <html:hidden property="id"/></input>
        <html:hidden property="userName" /></input>
        <html:hidden property="adminCreated" /></input>
        <html:hidden property="newUser" /></input>
        
    <table class="formtable">
    <tr>
        <td class="label"><label for="fullName" /><fmt:message key="userSettings.fullname" /></label></td>
        <td class="field"><html:text property="fullName" size="30" maxlength="30" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.fullName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="userName" /><fmt:message key="userSettings.username" /></label></td>
        <td class="field"><html:text readonly="true"  style="background: #e5e5e5" property="userName" size="30" maxlength="30" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.userName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="passwordText" /><fmt:message key="userSettings.password" /></label></td>
        <td class="field">
           <html:password property="passwordText" size="20" maxlength="20" />
           <html:hidden property="password" />
       </td>
        <td class="description"><fmt:message key="userAdmin.tip.password" /></td>
    </tr>

    <tr>
        <td class="label"><label for="passwordConfirm" /><fmt:message key="userSettings.passwordConfirm" /></label></td>
        <td class="field"><html:password property="passwordConfirm" size="20" maxlength="20" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.passwordConfirm" /></td>
    </tr>

    <tr>
        <td class="label"><label for="emailAddress" /><fmt:message key="userSettings.email" /></label></td>
        <td class="field"><html:text property="emailAddress" size="40" maxlength="40" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
    </tr>

    <tr>
        <td class="label"><label for="locale" /><fmt:message key="userSettings.locale" /></label></td>
        <td class="field">
           <html:select property="locale" size="1" >
              <html:options collection="locales" property="value" labelProperty="label"/>
           </html:select>
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.locale" /></td>
    </tr>

    <tr>
        <td class="label"><label for="timeZone" /><fmt:message key="userSettings.timeZone" /></label></td>
        <td class="field">
           <html:select property="timeZone" size="1" >
               <html:options collection="timeZones" property="value" labelProperty="label"/>
           </html:select>
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.timeZone" /></td>
    </tr>

    <tr>
        <td class="label"><label for="userEnabled" /><fmt:message key="userAdmin.enabled" /></label></td>
        <td class="field">
           <html:checkbox property="enabled" value="true" />
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.enabled" /></td>
    </tr>
    
    <tr>
        <td class="label"><label for="userAdmin" /><fmt:message key="userAdmin.userAdmin" /></label></td>
        <td class="field">
           <html:checkbox property="userAdmin" value="true" />
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.userAdmin" /></td>
    </tr>
    
    <tr>
        <td class="label"><label for="delete" /><fmt:message key="userAdmin.delete" /></label></td>
        <td class="field">
           <html:checkbox property="delete" />
        </td>
        <td class="description"><span class="warning"><fmt:message key="userAdmin.warning" /></span></td>
    </tr>

    </table>
    <br />
    
    <h3><fmt:message key="userAdmin.userWeblogs" /></h3>
        
    <c:choose>
        <c:when test="${!empty model.permissions}"> 
   
            <p><fmt:message key="userAdmin.userMemberOf" /></p>  
            <table class="rollertable" style="width: 80%">
            <c:forEach var="perms" items="${model.permissions}">
               <tr>
                   <td width="%30">
                       <a href='<c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />'>
                           <c:out value="${perms.website.name}" /> [<c:out value="${perms.website.handle}" />] 
                       </a>
                   </td>
                   <td width="%15">
                       <c:url value="/editor/weblog.do" var="newEntry">
                           <c:param name="method" value="create" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/New16.gif"/>' />
                       <a href='<c:out value="${newEntry}" />'>
                           <fmt:message key="userAdmin.newEntry" /></a>
                   </td>
                   <td width="%15">
                       <c:url value="/editor/weblogQuery.do" var="editEntries">
                           <c:param name="method" value="query" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/Edit16.png"/>' />
                       <a href='<c:out value="${editEntries}" />'>
                           <fmt:message key="userAdmin.editEntries" /></a> 
                   </td>
                   <td width="%15">
                       <c:url value="/editor/website.do" var="manageWeblog">
                           <c:param name="method" value="edit" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/Edit16.png"/>' />
                       <a href='<c:out value="${manageWeblog}" />'>
                           <fmt:message key="userAdmin.manage" /></a>
                   </td>
               </tr>
            </c:forEach>    
            </table>
            
        </c:when>
        
        <c:otherwise>
            <fmt:message key="userAdmin.userHasNoWeblogs" />
        </c:otherwise>
        
    </c:choose>
    
    <br />
    <br />

    <div class="control">
       <input type="submit" value='<fmt:message key="userAdmin.save" />'/></input>
       <input type="button" value='<fmt:message key="application.cancel" />' onclick="cancel()"></input>
    </div>
        
    </html:form>
    
</c:if>




