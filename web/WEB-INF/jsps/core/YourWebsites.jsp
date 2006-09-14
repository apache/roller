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

<%-- Choose appropriate prompt at start of page --%>
<c:choose>

    <%-- PROMPT: Welcome... you have no blog --%>
    <c:when test="${empty model.permissions && empty model.pendings}"> 
        <p><fmt:message key="yourWebsites.prompt.noBlog" />
        <roller:link forward="createWebsite">
           <fmt:message key="yourWebsites.createOne" />
        </roller:link></p>
    </c:when>      
    
    <%-- PROMPT: You have invitation(s) --%>
    <c:when test="${!empty model.pendings}">      
        <p><fmt:message key="yourWebsites.invitationsPrompt" /></p>
        
        <c:forEach var="invite" items="${model.pendings}">
            <fmt:message key="yourWebsites.youAreInvited" >
               <fmt:param value="${invite.website.handle}" />
            </fmt:message>
            <c:url value="/roller-ui/yourWebsites.do" var="acceptInvite">
                <c:param name="method" value="accept" />
                <c:param name="inviteId" value="${invite.id}" />
            </c:url>
            <a href='<c:out value="${acceptInvite}" />'>
                <fmt:message key="yourWebsites.accept" />
            </a> 
            &nbsp;|&nbsp;
            <c:url value="/roller-ui/yourWebsites.do" var="declineInvite">
                <c:param name="method" value="decline" />
                <c:param name="inviteId" value="${invite.id}" />
            </c:url>
            <a href='<c:out value="${declineInvite}" />'>
                <fmt:message key="yourWebsites.decline" />
            </a><br />
        </c:forEach>
        <br />
    </c:when>
    
    <%-- PROMPT: default ... select a weblog to edit --%>
    <c:otherwise> 
        <p class="subtitle"><fmt:message key="yourWebsites.prompt.hasBlog" /></p>        
    </c:otherwise>

</c:choose>

<%-- if we have weblogs, then loop through and list them --%>
<c:if test="${!empty model.permissions}">
    
    <c:forEach var="perms" items="${model.permissions}">

        <div class="yourWeblogBox">  

            <span class="mm_weblog_name"><img src='<c:url value="/images/folder.png"/>' />&nbsp;<c:out value="${perms.website.name}" /></span>
                
            <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
               <tr>
               <td valign="top">

                   <table cellpadding="0" cellspacing="0">
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.weblog' /></td>
                           <td><a href='<c:out value="${perms.website.URL}" />'>
                               <c:out value="${perms.website.absoluteURL}" />
                           </a></td>                          
                       </tr>
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.permission' /></td>
                           <td><c:if test="${perms.permissionMask == 0}" >LIMITED</c:if>
                           <c:if test="${perms.permissionMask == 1}" >AUTHOR</c:if>
                           <c:if test="${perms.permissionMask == 3}" >ADMIN</c:if></td>
                       </tr>
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.description' /></td>   
                           <td><c:out value="${perms.website.description}" escapeXml="false" /></td>
                       </tr>
                   </table>

               </td>
               
               <td class="mm_table_actions" width="20%" align="left" >

                       <c:url value="/roller-ui/authoring/weblog.do" var="newEntry">
                           <c:param name="method" value="create" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/table_edit.png"/>' />
                       <a href='<c:out value="${newEntry}" />'>
                           <fmt:message key="yourWebsites.newEntry" /></a>
                       <br />

                       <c:url value="/roller-ui/authoring/weblogEntryManagement.do" var="editEntries">
                           <c:param name="method" value="query" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/table_multiple.png"/>' />
                       <a href='<c:out value="${editEntries}" />'>
                           <fmt:message key="yourWebsites.editEntries" /></a> 
                       <br />

                       <%-- // I'm not sure this link needs to be here
                       <c:url value="/roller-ui/authoring/commentManagement.do" var="manageComments">
                           <c:param name="method" value="query" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/page_white_edit.png"/>' />
                       <a href='<c:out value="${manageComments}" />'>
                           <fmt:message key="yourWebsites.manageComments" /></a> 
                       <br />
                       --%>
                       
                       <c:if test="${perms.permissionMask == 3}">
                           <c:url value="/roller-ui/authoring/website.do" var="manageWeblog">
                               <c:param name="method" value="edit" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/cog.png"/>' />
                           <a href='<c:out value="${manageWeblog}" />'>
                               <fmt:message key="yourWebsites.manage" /></a> 
                           <br />
                       </c:if>

                       <%-- authors and limited bloggers can resign, but admin cannot resign if he/she is the last admin in the blog --%>
                       <c:if test="${perms.permissionMask == 0 || perms.permissionMask == 1 || perms.website.adminUserCount > 1 }">
                          <img src='<c:url value="/images/delete.png"/>' />
                          <c:url value="/roller-ui/yourWebsites.do" var="resignWeblog">
                               <c:param name="method" value="resign" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                          <a href='<c:out value="${resignWeblog}" />'>
                              <fmt:message key='yourWebsites.resign' />
                          </a>
                       </c:if>

               </td>
               </tr>
            </table>
            
        </div>
        
    </c:forEach>

</c:if>


