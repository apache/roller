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
function revokeInvite(id) {
    if (confirm('<fmt:message key="invitations.confirm.revoke" />')) {
        document.invitationsForm.method.value="revoke"; 
        document.invitationsForm.permissionId.value=id; 
        document.invitationsForm.submit();
    }
}
function done() {
    document.invitationsForm.method.value="cancel"; 
    document.invitationsForm.submit();
}
// -->
</script> 

<p class="subtitle">
    <fmt:message key="invitations.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>
<p><fmt:message key="invitations.prompt" /></p>

<html:form action="/roller-ui/authoring/invitations" method="post">
    <html:hidden property="weblog" />
    <html:hidden property="permissionId" />
    <input type="hidden" name="method" value="view" />
        
    <c:choose>
        <c:when test="${empty model.pendings}"> 
            <fmt:message key="invitations.noInvitations" />
        </c:when>  
        <c:when test="${!empty model.pendings}">  
            <table class="rollertable">
                <tr class="rHeaderTr">
                   <th class="rollertable" width="20%">
                       <fmt:message key="invitations.weblog" />
                   </th>
                   <th class="rollertable" width="20%">
                       <fmt:message key="invitations.user" />
                   </th>
                   <th class="rollertable" width="20%">
                       <fmt:message key="invitations.permission" />
                   </th>
                   <th class="rollertable" width="20%">
                       <fmt:message key="invitations.action" />
                   </th>
                </tr>        
                <c:forEach var="invite" items="${model.pendings}">
                    <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">                       
                        <td class="rollertable">
                            <c:out value="${invite.website.handle}" />
                        </td> 
                        <td class="rollertable">
                            <c:out value="${invite.user.userName}" />
                        </td> 
                        <td class="rollertable">
                            <c:if test="${invite.permissionMask == 0}" >LIMITED</c:if>
                            <c:if test="${invite.permissionMask == 1}" >AUTHOR</c:if>
                            <c:if test="${invite.permissionMask == 3}" >ADMIN</c:if>
                        </td> 
                        <td class="rollertable">
                            <a hrerf="#" onclick="revokeInvite('<c:out value="${invite.id}" />')">
                                <fmt:message key="invitations.revoke" />
                            </a>
                        </td> 
                    </roller:row>
                </c:forEach>
            </table>
        </c:when> 
    </c:choose>
    
    <br />
    <br />
    <input type="button" value='<fmt:message key="application.done" />' onclick="done()"></input>
    
</html:form>




