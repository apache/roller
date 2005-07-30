<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="inviteMember.title" /></h1>

<p><fmt:message key="inviteMember.description" /></p>

<html:form action="/editor/inviteMember" method="post" focus="userName">

    <div class="formrow">
       <label for="userName" class="formrow" />
           <fmt:message key="inviteMember.userName" /></label>
       <html:text property="userName" size="30" maxlength="30" />
    </div>    
    
    <div class="formrow">
       <label for="userName" class="formrow" />
           <fmt:message key="inviteMember.permissions" /></label>
       <input type="radio" name="permissionsMask" value="3"  />
       <fmt:message key="inviteMember.administrator" />
       <input type="radio" name="permissionsMask" value="1" checked />
       <fmt:message key="inviteMember.author" />
       <input type="radio" name="permissionsMask" value="0" />
       <fmt:message key="inviteMember.limited" />
    </div>
                  
    <br />      
    <div class="control">
       <input type="submit" value='<fmt:message key="inviteMember.button.save" />'></input>
    </div>
    
</html:form>
    
<%@ include file="/theme/footer.jsp" %>


