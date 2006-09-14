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

<script type="text/javascript">
// <!--
function cancel() {
    document.inviteMemberForm.method.value="cancel"; 
    document.inviteMemberForm.submit();
}
<%@ include file="/roller-ui/scripts/ajax-user.js" %>
// -->
</script> 

<p class="subtitle"><fmt:message key="inviteMember.subtitle" /></p>
<p><fmt:message key="inviteMember.prompt" /></p>

<html:form action="/roller-ui/authoring/inviteMember" method="post" focus="userName">
    <html:hidden property="websiteId" />
    <input name="method" type="hidden" value="send" />
    
    <div class="formrow">
       <label for="userName" class="formrow" />
           <fmt:message key="inviteMember.userName" /></label>
       <div>
           <input name="userName" id="userName" size="30" maxlength="30" 
               onfocus="onUserNameFocus(true)" onkeyup="onUserNameChange(true)" /><br />
       </div>
    </div>    
    
    <div class="formrow">
       <label class="formrow" />&nbsp;</label>
       <div>
           <select id="userList" size="10" onchange="onUserSelected()" style="width:300px"></select>
       </div>
    </div>    
    
    <div style="clear:left">
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




