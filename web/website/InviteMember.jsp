<%@ include file="/taglibs.jsp" %>

<script type="text/javascript">
// <!--
function cancel() {
    document.inviteMemberForm.method.value="cancel"; 
    document.inviteMemberForm.submit();
}
<%@ include file="/theme/scripts/ajax-user.js" %>
// -->
</script> 

<h1><fmt:message key="inviteMember.title" /></h1>

<p><fmt:message key="inviteMember.description" /></p>

<html:form action="/editor/inviteMember" method="post" focus="userName">
    <input name="method" type="hidden" value="save" />
    
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
    
     <div class="permissionsMask">
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
    <input type="submit" value='<fmt:message key="inviteMember.button.save" />'></input>
    <input type="button" value='<fmt:message key="application.cancel" />' onclick="cancel()"></input>

</html:form>




