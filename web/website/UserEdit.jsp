<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<br />
<h1><fmt:message key="userSettings.userSettings" /></h1>

<br /> 
<html:form action="/editor/user" method="post" focus="fullName">
    <html:hidden property="method" value="update"/></input>
    <html:hidden property="id"/></input>
    <html:hidden property="userName" /></input>
    
    <div class="formrow">
       <label for="fullName" class="formrow" /><fmt:message key="userSettings.fullname" /></label>
       <html:text property="fullName" size="30" maxlength="30" />
    </div>

    <c:if test="${cookieLogin != 'true'}">
    <div class="formrow">
       <label for="password" class="formrow" /><fmt:message key="userSettings.password" /></label>
       <html:password property="passwordText" size="20" maxlength="20" />
       <html:hidden property="password" />
    </div>
    <div class="formrow">
       <label for="passwordConfirm" class="formrow" /><fmt:message key="userSettings.passwordConfirm" /></label>
       <html:password property="passwordConfirm" size="20" maxlength="20" />
    </div>
    </c:if>

    <div class="formrow">
       <label for="" class="formrow" /><fmt:message key="userSettings.email" /></label>
       <html:text property="emailAddress" size="40" maxlength="40" />
    </div>

    <div class="formrow">
       <label for="locale" class="formrow" /><fmt:message key="userSettings.locale" /></label>
       <html:select property="locale" size="1" >
          <html:options collection="roller.locales" property="value" labelProperty="label"/>
       </html:select>
    </div>

    <div class="formrow">
       <label for="timezone" class="formrow" /><fmt:message key="userSettings.timezone" /></label>
       <html:select property="timezone" size="1" >
           <html:options collection="roller.timezones" property="value" labelProperty="label"/>
       </html:select>
    </div>

    <html:hidden property="theme" /></input>
    <%-- Not implemented for the front end yet
    <div class="formrow">
       <label for="theme" class="formrow" /><fmt:message key="fixme!" /></label>
       <html:select property="theme" size="1" >
           <html:options name="themes"/>
       </html:select>
    </div>
    --%>
    
    <br />
    <div class="control">
       <input type="submit" value='<fmt:message key="userSettings.save" />' /></input>
    </div>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>


