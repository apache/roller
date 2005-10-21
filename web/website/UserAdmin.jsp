<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<%-- If user name is not specified, then allow user to choose a user to be loaded --%>
<c:if test="${empty userAdminForm.userName}">
    <br />
    <h1><fmt:message key="userAdmin.searchUserTitle" /></h1>
    <br />
		<html:form action="/admin/user" method="post">
		    <html:hidden property="method" value="edit" />
		    <p><fmt:message key="userAdmin.enterUserName" /></p>
		    <strong><fmt:message key="userAdmin.editUser" />:</strong>         
		    <html:text property="userName" size="10" />
		    <input type="submit" value='<fmt:message key="userAdmin.edit" />' />
		</html:form>
</c:if>

<%-- If a user name is specified, then show the admin user form --%>
<c:if test="${not empty userAdminForm.userName}">
    <br />
	  <h1>
	     <fmt:message key="userAdmin.editUserTitle" >
	        <fmt:param value="${userAdminForm.userName}" />
	     </fmt:message>
	  </h1>			
    <br />
    <html:form action="/admin/user" method="post">
        <html:hidden property="method" value="update"/></input>
        
        <html:hidden property="id"/></input>
        <html:hidden property="userName" /></input>
        
        <div class="formrow">
           <label for="fullName" class="formrow" /><fmt:message key="userAdmin.fullname" /></label>
           <html:text property="fullName" size="30" maxlength="30" />
        </div>
        
        <c:if test="${cookieLogin != 'true'}">
        <div class="formrow">
           <label for="passwordText" class="formrow" /><fmt:message key="userAdmin.password" /></label>
           <html:password property="passwordText" size="20" maxlength="20" />
        </div>       
        <div class="formrow">
           <label for="passwordConfirm" class="formrow" />
              <fmt:message key="userAdmin.passwordConfirm" />
           </label>
           <html:password property="passwordConfirm" size="20" maxlength="20" />
        </div>
        </c:if>
        
        <div class="formrow">
           <label for="emailAddress" class="formrow" /><fmt:message key="userAdmin.email" /></label>
           <html:text property="emailAddress" size="40" maxlength="40" /></input>
        </div>        
        <br />
        
        <div class="formrow">
           <label for="userEnabled" class="formrow" /><fmt:message key="userAdmin.enabled" /></label>
           <html:checkbox property="userEnabled" value="true" />
        </div>
        <br /> 
       
        <div class="formrow">
           <label for="userAdmin" class="formrow" /><fmt:message key="userAdmin.userAdmin" /></label>
           <html:checkbox property="userAdmin" value="true" />
        </div>        
        <br />
        
        <div class="formrow">
           <label for="delete" class="formrow" /><fmt:message key="userAdmin.delete" /></label>
           <html:checkbox property="delete" />
           <span class="warning"><fmt:message key="userAdmin.warning" /></span>
        </div>        
        <br />
        
        <div class="control">
           <input type="submit" value='<fmt:message key="userAdmin.save" />'/></input>
        </div>
        
    </html:form>
    
</c:if>

<%@ include file="/theme/footer.jsp" %>


