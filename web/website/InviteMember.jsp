<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="inviteMember.title" /></h1>

<html:form action="/editor/inviteMember" method="post" focus="handle">

    <div class="formrow">
       <label for="userName" class="formrow" /><fmt:message key="inviteMember.userName" /></label>
       <html:text property="userName" size="30" maxlength="30" />
    </div>

    <div class="control">
       <html:submit /></input>
    </div>
    
</html:form>
    
<%@ include file="/theme/footer.jsp" %>


