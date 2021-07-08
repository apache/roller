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
    <a id="createWeblogLink" href="<s:url action="createWeblog"/>"><s:text name="yourWebsites.createOne" /></a></p>
</s:if>    

<%-- PROMPT: You have invitation(s) --%>
<s:elseif test="! pendingPermissions.isEmpty">
    <p><s:text name="yourWebsites.invitationsPrompt" /></p>
    
    <s:iterator var="invite" value="pendingPermissions">
        <s:text name="yourWebsites.youAreInvited" >
            <s:param value="#invite.weblog.handle" />
        </s:text>
        
        <s:url action="menu!accept" var="acceptInvite">
            <s:param name="inviteId" value="#invite.weblog.id" />
        </s:url>
        <a href='<s:property value="acceptInvite" />'>
            <s:text name="yourWebsites.accept" />
        </a> 
        &nbsp;|&nbsp;
        <s:url action="menu!decline" var="declineInvite">
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
    
    <s:iterator var="perms" value="existingPermissions">

        <div class="well yourWeblogBox">

            <h3 class="mm_weblog_name">
                <span class="glyphicon glyphicon-folder-open" aria-hidden="true"></span>
                &nbsp;<s:property value="#perms.weblog.name" />
            </h3>

            <p> <a href='<s:property value="#perms.weblog.absoluteURL" />'>
            <s:property value="#perms.weblog.absoluteURL" /></a></p>

            <p><s:property value="#perms.weblog.about" escapeHtml="false" /></p>

            <p>You have 
            <s:if test='#perms.hasAction("admin")'>ADMIN </s:if>
            <s:if test='#perms.hasAction("post")'>AUTHOR </s:if>
            <s:if test='#perms.hasAction("edit_draft")'>LIMITED </s:if>
            <s:text name='yourWebsites.permission'/></p>
            
            <div class="btn-group" role="group" aria-label="...">

                <%-- New entry button --%>
                <s:url action="entryAdd" namespace="/roller-ui/authoring" var="newEntry">
                    <s:param name="weblog" value="#perms.weblog.handle"/>
                </s:url>
                <s:a href="%{newEntry}" cssClass="btn btn-default">
                    <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
                    <s:text name="yourWebsites.newEntry"/>
                </s:a>

                <s:if test='!(#perms.hasAction("edit_draft"))'>
                    
                    <%-- Show Entries button with count for users above LIMITED permission --%>
                    <s:url action="entries" namespace="/roller-ui/authoring" var="editEntries">
                        <s:param name="weblog" value="#perms.weblog.handle"/>
                    </s:url>
                    <s:a href="%{editEntries}" cssClass="btn btn-default">
                        <span class="glyphicon glyphicon-list" aria-hidden="true"></span>
                        <s:text name="yourWebsites.editEntries"/>
                        <span class="badge"><s:property value="#perms.weblog.entryCount"/></span>
                    </s:a>

                </s:if>

                <s:if test='!(#perms.hasAction("edit_draft"))'>
                    
                    <%-- Show Comments button with count for users above LIMITED permission --%>
                    <s:url action="comments" namespace="/roller-ui/authoring" var="manageComments">
                        <s:param name="weblog" value="#perms.weblog.handle"/>
                    </s:url>
                    <s:a href="%{manageComments}" cssClass="btn btn-default">
                        <span class="glyphicon glyphicon-comment" aria-hidden="true"></span>
                        <s:text name="yourWebsites.manageComments"/>
                        <span class="badge"><s:property value="#perms.weblog.commentCount"/></span>
                    </s:a>

                </s:if>


                <%-- Only admins get access to theme and config settings --%>
                <s:if test='#perms.hasAction("admin")'>

                    <%-- And only show theme option if custom themes are enabled --%>
                    <s:if test="getProp('themes.customtheme.allowed')">
                        <s:if test="#perms.weblog.editorTheme == 'custom'">
                            <s:url action="templates" namespace="/roller-ui/authoring" var="weblogTheme">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                        </s:if>
                        <s:else>
                            <s:url action="themeEdit" namespace="/roller-ui/authoring" var="weblogTheme">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                        </s:else>
                        <a href='<s:property value="weblogTheme" />' class="btn btn-default">
                            <span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>
                            <s:text name="yourWebsites.theme" />
                        </a>
                    </s:if>
                    
                    <%-- settings button --%>
                    <s:url action="weblogConfig" namespace="/roller-ui/authoring" var="manageWeblog">
                        <s:param name="weblog" value="#perms.weblog.handle"/>
                    </s:url>
                    <a href='<s:property value="manageWeblog" />' class="btn btn-default">
                        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                        <s:text name="yourWebsites.manage"/>
                    </a>

                </s:if>

                <%-- don't allow last admin to resign from blog --%>
                <s:if test='!(#perms.hasAction("admin") && #perms.weblog.adminUserCount == 1)'>

                    <button type="button" class="btn btn-default">
                        <span class="glyphicon glyphicon-trash" aria-hidden="true"></span>
                        <s:url action="memberResign" namespace="/roller-ui/authoring" var="resignWeblog">
                            <s:param name="weblog" value="#perms.weblog.handle"/>
                        </s:url>
                        <a href='<s:property value="resignWeblog" />'>
                            <s:text name='yourWebsites.resign'/>
                        </a>
                    </button>

                </s:if>

            </div>

        </div>
        
    </s:iterator>

</s:if>
