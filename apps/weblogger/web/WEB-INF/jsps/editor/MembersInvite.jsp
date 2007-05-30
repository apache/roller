<%--
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
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script type="text/javascript">
// <!--
<%@ include file="/roller-ui/scripts/ajax-user.js" %>
// -->
</script> 

<p class="subtitle"><s:text name="inviteMember.subtitle" /></p>
<p><s:text name="inviteMember.prompt" /></p>

<s:form action="invite!save">
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />
    
    <div class="formrow">
       <label for="userName" class="formrow" />
           <s:text name="inviteMember.userName" /></label>
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
           <s:text name="inviteMember.permissions" /></label>
       <input type="radio" name="permissionsMask" value="3"  />
       <s:text name="inviteMember.administrator" />
       <input type="radio" name="permissionsMask" value="1" checked />
       <s:text name="inviteMember.author" />
       <input type="radio" name="permissionsMask" value="0" />
       <s:text name="inviteMember.limited" />
    </div>  
         
    <br />      
    <s:submit key="inviteMember.button.save" />
    <s:submit key="application.cancel" action="members" />

</s:form>

<%-- this forces focus to the userName field --%>
<script type="text/javascript">
<!--
    document.getElementById('userName').focus();
// -->
</script>
