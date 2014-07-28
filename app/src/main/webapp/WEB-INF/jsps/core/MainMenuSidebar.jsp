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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
            <div class="sidebarInner">
                <h3><s:text name="yourWebsites.actions" /></h3>
                
                <hr size="1" noshade="noshade" />
                
                <h3><a href="<s:url action="profile"/>"><s:text name="yourWebsites.editProfile" /></a></h3>
                <p><s:text name="yourWebsites.editProfile.desc" /></p>

                <s:if test="getBooleanProp('webservices.enableAtomPub') && getProp('webservices.atomPubAuth') == 'oauth'">
                    <h3><a href="<s:url action="oauthKeys" />"><s:text name="yourWebsites.oauthKeys" /></a></h3>
                    <p><s:text name="yourWebsites.oauthKeys.desc" /></p>
                </s:if>

                <s:if test="getBooleanProp('site.allowUserWeblogCreation') && (getBooleanProp('groupblogging.enabled') || (existingPermissions.isEmpty && pendingPermissions.isEmpty))">
                    <h3><a href="<s:url action="createWeblog" />"><s:text name="yourWebsites.createWeblog" /></a></h3>
                    <p><s:text name="yourWebsites.createWeblog.desc" /></p>
                </s:if>

                <s:if test="userIsAdmin">               
                    <h3><a href="<s:url action="globalConfig" namespace="/roller-ui/admin" />"><s:text name="yourWebsites.globalAdmin" /></a></h3>          
                    <p><s:text name="yourWebsites.globalAdmin.desc" /></p>
                    
                    <s:if test="getBooleanProp('planet.aggregator.enabled')">               
                        <h3><a href="<s:url action="planetConfig" namespace="/roller-ui/admin" />"><s:text name="yourWebsites.planetAdmin" /></a></h3>
                        <p><s:text name="yourWebsites.planetAdmin.desc" /></p>
                    </s:if>
                </s:if>
                
                <br />
            </div>
            
        </div>
    </div>
</div>	
