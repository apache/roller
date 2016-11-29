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

<div class="sidebarFade" id="ngapp-div" ng-app="tightblogApp" ng-controller="EntryEditController as ctrl">
    <div class="menu-tr">
        <div class="menu-tl">
            
            <div class="sidebarInner">
                
                <h3><fmt:message key="weblogEdit.comments" /></h3>

                <s:set var="localCommentCount" value="entry.commentCount"/>
                <s:if test="#localCommentCount > 0">
                    <s:url var="commentsURL" action="comments">
                       <s:param name="entryId" value="entryId" />
                       <s:param name="weblogId" value="weblogId" />
                    </s:url>
                    <s:text name="weblogEdit.hasComments">
                        <s:param value="%{commentsURL}" />
                        <s:param value="localCommentCount" />
                    </s:text>
                </s:if>
                <s:else>
                    <span><fmt:message key="generic.none" /></span>
                </s:else>
                
                <div ng-show="ctrl.recentEntries.PENDING.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="weblogEdit.pendingEntries" /></h3>

                    <span ng-repeat="post in ctrl.recentEntries.PENDING">
                        <span class="entryEditSidebarLink">
                            <img src='<s:url value="/images/table_error.png"/>'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a ng-href="{{post.editUrl}}">{{post.title | limitTo:50}}{{post.title > 50 ? '...' : ''}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <div ng-show="ctrl.recentEntries.DRAFT.length > 0">
                    <hr size="1" noshade="noshade" />
                    <h3><fmt:message key="weblogEdit.draftEntries" /></h3>

                    <span ng-repeat="post in ctrl.recentEntries.DRAFT">
                        <span class="entryEditSidebarLink">
                            <img src='<s:url value="/images/table_error.png"/>'
                                 align="absmiddle" border="0" alt="icon" title="Edit" />
                            <a ng-href="{{post.editUrl}}">{{post.title | limitTo:50}}{{post.title > 50 ? '...' : ''}}</a>
                        </span>
                        <br>
                    </span>
                </div>

                <s:if test="userAnAuthor">
                    
                    <div ng-show="ctrl.recentEntries.PUBLISHED.length > 0">
                        <hr size="1" noshade="noshade" />
                        <h3><fmt:message key="weblogEdit.publishedEntries" /></h3>

                        <span ng-repeat="post in ctrl.recentEntries.PUBLISHED">
                            <span class="entryEditSidebarLink">
                                <img src='<s:url value="/images/table_error.png"/>'
                                     align="absmiddle" border="0" alt="icon" title="Edit" />
                                <a ng-href="{{post.editUrl}}">{{post.title | limitTo:50}}{{post.title > 50 ? '...' : ''}}</a>
                            </span>
                            <br>
                        </span>
                    </div>

                    <div ng-show="ctrl.recentEntries.SCHEDULED.length > 0">
                        <hr size="1" noshade="noshade" />
                        <h3><fmt:message key="weblogEdit.scheduledEntries" /></h3>

                        <span ng-repeat="post in ctrl.recentEntries.SCHEDULED">
                            <span class="entryEditSidebarLink">
                                <img src='<s:url value="/images/table_error.png"/>'
                                     align="absmiddle" border="0" alt="icon" title="Edit" />
                                <a ng-href="{{post.editUrl}}">{{post.title | limitTo:50}}{{post.title > 50 ? '...' : ''}}</a>
                            </span>
                            <br>
                        </span>
                    </div>

                </s:if>
                
                <br />
                <br />
            </div>
            
        </div>
    </div>
</div>
