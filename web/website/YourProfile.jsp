<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="yourProfile.title" /></h1>

<p><fmt:message key="yourProfile.description" /></p>

<br /> 
<html:form action="/editor/yourProfile" method="post" focus="fullName">
    <html:hidden property="method" value="save"/></input>
    <html:hidden property="id"/></input>
    <html:hidden property="userName" /></input>
    <html:hidden property="isEnabled" /></input>
    
    <div class="formrow">
       <label for="fullName" class="formrow" /><fmt:message key="userSettings.fullname" /></label>
       <html:text property="fullName" size="30" maxlength="30" onchange="dirty()" />
    </div>

    <c:if test="${cookieLogin != 'true'}">
    <div class="formrow">
       <label for="password" class="formrow" /><fmt:message key="userSettings.password" /></label>
       <html:password property="passwordText" size="20" maxlength="20" onchange="dirty()"/>
       <html:hidden property="password" />
    </div>
    <div class="formrow">
       <label for="passwordConfirm" class="formrow" /><fmt:message key="userSettings.passwordConfirm" /></label>
       <html:password property="passwordConfirm" size="20" maxlength="20" onchange="dirty()"/>
    </div>
    </c:if>

    <div class="formrow">
       <label for="" class="formrow" /><fmt:message key="userSettings.email" /></label>
       <html:text property="emailAddress" size="40" maxlength="40" onchange="dirty()"/>
    </div>

    <div class="formrow">
       <label for="locale" class="formrow" /><fmt:message key="userSettings.locale" /></label>
       <html:select property="locale" size="1" onchange="dirty()">
           <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>
    </div>

    <div class="formrow">
       <label for="timeZone" class="formrow" /><fmt:message key="userSettings.timeZone" /></label>
       <html:select property="timeZone" size="1" onchange="dirty()">
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </div>

    <br />      
    <div class="control">
       <input type="submit" value='<fmt:message key="yourProfile.button.save" />'></input>
    </div>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>


