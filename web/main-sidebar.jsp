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

<div class="sidebarBodyHead">
    <div class="menu-tr">
        <div class="menu-tl">
            <h3><fmt:message key="mainPage.hotWeblogs" /></h3>		
        </div>
    </div>
</div>	

<div class="sidebarBody">       
    <c:if test="${not empty popularWebsites}"> <%-- to prevent invalid XHTML --%>
    <ul>
        <c:forEach var="site" items="${popularWebsites}">
           <li>
               <a href='<c:out value="${baseURL}" />/page/<c:out value="${site.websiteHandle}" />'
                  title='<c:out value="${site.websiteHandle}" />' >
                  <str:truncateNicely lower="45" 
                    upper="45" ><c:out 
                    value="${site.websiteName}" /></str:truncateNicely></a>:
               <c:out value="${site.hits}" /> <fmt:message key="mainPage.hits" />
           </li>
        </c:forEach>
    </ul>
    </c:if>
    <br />
</div>


    
    