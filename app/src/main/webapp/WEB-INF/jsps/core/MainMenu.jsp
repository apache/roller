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

<%-- PROMPT: Welcome... you have no blog --%>
<s:if test="existingPermissions.isEmpty && pendingPermissions.isEmpty"> 
    <p><s:text name="yourWebsites.prompt.noBlog" />
    <a href="<s:url action="createWeblog"/>"><s:text name="yourWebsites.createOne" /></a></p>
</s:if>    

<%-- PROMPT: You have invitation(s) --%>
<s:elseif test="! pendingPermissions.isEmpty">
    <p><s:text name="yourWebsites.invitationsPrompt" /></p>
    
    <s:iterator id="invite" value="pendingPermissions">
        <s:text name="yourWebsites.youAreInvited" >
            <s:param value="#invite.weblog.handle" />
        </s:text>
        
        <s:url action="menu!accept" id="acceptInvite">
            <s:param name="inviteId" value="#invite.weblog.id" />
        </s:url>
        <a href='<s:property value="acceptInvite" />'>
            <s:text name="yourWebsites.accept" />
        </a> 
        &nbsp;|&nbsp;
        <s:url action="menu!decline" id="declineInvite">
            <s:param name="inviteId" value="#invite.weblog.id" />
        </s:url>
        <a href='<s:property value="declineInvite" />'>
            <s:text name="yourWebsites.decline" />
        </a><br />
    </s:iterator>
    <br />
</s:elseif>

<%-- PROMPT: default ... select a weblog to edit --%>
<s:else> 
    <p class="subtitle"><s:text name="yourWebsites.prompt.hasBlog" /></p>        
</s:else>

<%-- if we have weblogs, then loop through and list them --%>
<s:if test="! existingPermissions.isEmpty">
    
    <s:iterator id="perms" value="existingPermissions">

        <div class="yourWeblogBox">  

            <span class="mm_weblog_name"><img src='<c:url value="/images/folder.png"/>' />&nbsp;<s:property value="#perms.weblog.name" /></span>
                
            <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
               <tr>
               <td valign="top">

                   <table cellpadding="0" cellspacing="0">
                       
                       <tr>
                           <td class="mm_subtable_label"><s:text name='yourWebsites.weblog'/></td>
                           <td><a href='<s:property value="#perms.weblog.absoluteURL" />'>
                               <s:property value="#perms.weblog.absoluteURL" />
                           </a></td>                          
                       </tr>
                       
                       <tr>
                           <td class="mm_subtable_label"><s:text name='yourWebsites.permission'/></td>
                           <td><s:if test='#perms.hasAction("admin")'  >ADMIN</s:if>
                           <s:if test='#perms.hasAction("post")'       >AUTHOR</s:if>
                           <s:if test='#perms.hasAction("edit_draft")' >LIMITED</s:if></td>
                       </tr>
                       
                       <tr>
                           <td class="mm_subtable_label"><s:text name='generic.description' /></td>
                           <td><s:property value="#perms.weblog.about" escape="false" /></td>
                       </tr>

                       <tr>
                           <td class="mm_subtable_label"><s:text name='yourWebsites.userCount' /></td>   
                           <td><s:property value="#perms.weblog.userCount" /></td>
                       </tr>

                       <tr>
                           <td class="mm_subtable_label"><s:text name='yourWebsites.todaysHits' /></td>   
                           <td><s:property value="#perms.weblog.todaysHits" /></td>
                       </tr>
                       
                   </table>

               </td>
               
               <td class="mm_table_actions" width="20%" align="left" >

                       <s:url action="entryAdd" namespace="/roller-ui/authoring" id="newEntry">
                           <s:param name="weblog" value="#perms.weblog.handle" />
                       </s:url>
                       <img src='<s:url value="/images/table_edit.png"/>' />
                       <s:a href="%{newEntry}"><s:text name="yourWebsites.newEntry" /></s:a>
                       <br />

                       <%-- Show Entries link with count for users above LIMITED permission --%>
                       <s:if test='!(#perms.hasAction("edit_draft"))'>
                           <s:url action="entries" namespace="/roller-ui/authoring" id="editEntries">
                               <s:param name="weblog" value="#perms.weblog.handle" />
                           </s:url>
                           <img src='<s:url value="/images/table_multiple.png"/>' />
                           <s:a href="%{editEntries}"><s:text name="yourWebsites.editEntries" /> (<s:property value="#perms.weblog.entryCount" />)</s:a>
                           <br />
                       </s:if>

                       <%-- Show Comments link with count for users above LIMITED permission --%>
                       <s:if test='!(#perms.hasAction("edit_draft"))'>
                           <s:url action="comments" namespace="/roller-ui/authoring" id="manageComments">
                               <s:param name="weblog" value="#perms.weblog.handle" />
                           </s:url>
                           <img src='<s:url value="/images/page_white_edit.png"/>' />
                           <s:a href="%{manageComments}"><s:text name="yourWebsites.manageComments" /> (<s:property value="#perms.weblog.commentCount" />)</s:a>
                           <br />
                       </s:if>

                       <%-- Only admins get access to theme and config settings --%>
                       <s:if test='#perms.hasAction("admin")'>
                           
                           <%-- And only show theme option if custom themes are enabled --%>
                           <s:if test="getProp('themes.customtheme.allowed')">
                               <s:if test="#perms.weblog.editorTheme == 'custom'">
                                   <s:url action="templates" namespace="/roller-ui/authoring" id="weblogTheme">
                                       <s:param name="weblog" value="#perms.weblog.handle" />
                                   </s:url>
                               </s:if>
                               <s:else>
                                   <s:url action="themeEdit" namespace="/roller-ui/authoring" id="weblogTheme">
                                       <s:param name="weblog" value="#perms.weblog.handle" />
                                   </s:url>
                               </s:else>
                               <img src='<s:url value="/roller-ui/images/layout.png"/>' />
                               <a href='<s:property value="weblogTheme" />'>
                                   <s:text name="yourWebsites.theme" /></a> 
                               <br />
                           </s:if>
                           
                           <s:url action="weblogConfig" namespace="/roller-ui/authoring" id="manageWeblog">
                               <s:param name="weblog" value="#perms.weblog.handle" />
                           </s:url>
                           <img src='<s:url value="/images/cog.png"/>' />
                           <a href='<s:property value="manageWeblog" />'>
                               <s:text name="yourWebsites.manage" /></a> 
                           <br />
                       </s:if>

                       <%-- don't allow last admin to resign from blog --%>
                       <s:if test='!(#perms.hasAction("admin") && #perms.weblog.adminUserCount == 1)'>
                          <img src='<c:url value="/images/delete.png"/>' />
                          <s:url action="memberResign" namespace="/roller-ui/authoring" id="resignWeblog">
                              <s:param name="weblog" value="#perms.weblog.handle" />
                          </s:url>
                          <a href='<s:property value="resignWeblog" />'>
                              <s:text name='yourWebsites.resign' />
                          </a>
                       </s:if>

               </td>
               </tr>
            </table>
            
        </div>
        
    </s:iterator>

</s:if>
