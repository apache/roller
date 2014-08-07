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
                
                <h3><s:text name="mainPage.actions" /></h3>
                <hr size="1" noshade="noshade" />
                
                <p>
                    <%-- Add Bookmark link --%>
                    <img src='<s:url value="/images/link_add.png"/>' border="0"alt="icon" />
                    <s:url var="addBookmark" action="bookmarkAdd">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="folderId" value="%{folder.id}" />
                    </s:url>
                    <s:a href="%{addBookmark}"><s:text name="bookmarksForm.addBookmark" /></s:a>
                </p>
                
                <p>
                    <%-- Add Folder link --%>
                    <img src='<s:url value="/images/folder_add.png"/>' border="0"alt="icon" />
                    <s:url var="addFolder" action="folderAdd">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="folderId" value="%{folder.id}" />
                    </s:url>
                    <s:a href="%{addFolder}"><s:text name="bookmarksForm.addFolder" /></s:a>
                </p>
                
                <%-- Import bookmarks --%>
                <p>
                    <img src='<s:url value="/images/link_add.png"/>' border="0"alt="icon" />
                    <s:url var="importBookmarks" action="bookmarksImport">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                    </s:url>
                    <s:a href="%{importBookmarks}"><s:text name="bookmarksForm.importBookmarks" /></s:a>
                </p>

                <br />
                <br />
            </div>
            
        </div>
    </div>
</div>	
