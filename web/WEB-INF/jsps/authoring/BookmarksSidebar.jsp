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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
<div class="sidebarInner">

            <h3><fmt:message key="mainPage.actions" /></h3>
            <hr size="1" noshade="noshade" />
            
            <p>
            <%-- Add Bookmark link --%>
            <img src='<c:url value="/images/link_add.png"/>' border="0"alt="icon" />
            <roller:link page="/roller-ui/authoring/bookmarkEdit.do">
                <roller:linkparam id="<%= RequestConstants.FOLDER_ID %>"
                    name="folder" property="id" />
                <fmt:message key="bookmarksForm.addBookmark" />
            </roller:link>
            </p>
			
			<%-- Add Folder link --%>
			<p>
            <img src='<c:url value="/images/folder_add.png"/>' border="0"alt="icon" />
			<roller:link page="/roller-ui/authoring/folderEdit.do">
			    <roller:linkparam id="<%= RequestConstants.PARENT_ID %>"
			         name="folder" property="id" />
			    <fmt:message key="bookmarksForm.addFolder" />
			</roller:link>            
            </p>
            
            <c:if test="${empty model.folderPath}">            
                <%-- Import bookmarks --%>
                <p>
                <img src='<c:url value="/images/link_add.png"/>' border="0"alt="icon" />
                <roller:link page="/roller-ui/authoring/importBookmarks.do">
                    <roller:linkparam id="<%= RequestConstants.FOLDER_ID %>"
                        name="folder" property="id" />
                    <fmt:message key="bookmarksForm.importBookmarks" />
                </roller:link>
                </p>                
            </c:if>

            <br />
            <br />
</div>
            
        </div>
    </div>
</div>	
