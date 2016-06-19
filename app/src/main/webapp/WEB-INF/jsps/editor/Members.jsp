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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="memberPermissions.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<p><s:text name="memberPermissions.description" /></p>

<s:form action="members!save">
    <sec:csrfInput/>
    <s:hidden name="weblogId" value="%{actionWeblog.id}" />

    <div style="text-align: right; padding-bottom: 6px;">
        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <s:text name="commentManagement.pending" />&nbsp;
    </div>

    <table class="rollertable">
        <tr class="rHeaderTr">
           <th width="20%"><s:text name="memberPermissions.userName" /></th>
           <th width="20%"><s:text name="memberPermissions.administrator" /></th>
           <th width="20%"><s:text name="memberPermissions.author" /></th>
           <th width="20%"><s:text name="memberPermissions.limited" /></th>
           <th width="20%"><s:text name="memberPermissions.remove" /></th>
        </tr>
        <s:iterator var="role" value="weblogRoles" status="rowstatus">
            <s:if test="#role.pending">
                <tr class="rollertable_pending">
            </s:if>
            <s:elseif test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:elseif>
            <s:else>
                <tr class="rollertable_even">
            </s:else>

                <td>
                    <img src='<s:url value="/images/user.png"/>' border="0" alt="icon" />
	                <s:property value="#role.user.userName" />
                </td>
                <td>
                    <input type="radio"
                        <s:if test='#role.weblogRole.name() == "OWNER"'>checked</s:if>
                        name='role-<s:property value="#role.user.id" />' value="OWNER" />
                </td>
                <td>
	                <input type="radio"
                        <s:if test='#role.weblogRole.name() == "POST"'>checked</s:if>
                        name='role-<s:property value="#role.user.id" />' value="POST" />
                </td>
                <td>
                    <input type="radio"
                        <s:if test='#role.weblogRole.name() == "EDIT_DRAFT"'>checked</s:if>
                        name='role-<s:property value="#role.user.id" />' value="EDIT_DRAFT" />
                </td>
                <td>
                    <input type="radio"
                        name='role-<s:property value="#role.user.id" />' value="-1" />
                </td>
           </tr>
       </s:iterator>
    </table>
    <br />

    <div class="control">
       <s:submit value="%{getText('generic.save')}" />
    </div>

</s:form>

<br>
<br>

<p><s:text name="inviteMember.prompt" /></p>
<s:form>
    <sec:csrfInput/>
    <s:hidden id="invite_weblog" name="weblogId" value="%{actionWeblog.id}"/>

    <select name="userId" id="membersinvite-select-user"/><br>

    <label for="permissionString" class="formrow" /><s:text name="inviteMember.permissions" /></label>

    <input type="radio" name="permissionString" value="OWNER"  />
    <s:text name="inviteMember.administrator" />

    <input type="radio" name="permissionString" value="POST" checked />
    <s:text name="inviteMember.author" />

    <input type="radio" name="permissionString" value="EDIT_DRAFT" />
    <s:text name="inviteMember.limited" /><br><br>

    <s:submit id="invite_button" value="%{getText('inviteMember.button.save')}" action="members!invite"/>
</s:form>

<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
$(function() {
  $.ajax({
     type: "GET",
     url: contextPath + '/tb-ui/authoring/rest/weblog/' + $('#invite_weblog').attr('value') + '/potentialmembers',
     success: function(data, textStatus, xhr) {
       for (var key in data) {
         $('#membersinvite-select-user').append('<option value="' + key + '">' + data[key] + '</option>');
       }
       if (document.getElementById('membersinvite-select-user').length == 0) {
         document.getElementById('invite_button').disabled = true;
       }
     }
  });
});
</script>
