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
            <h3><fmt:message key="planet.rankings" /></h3>		
        </div>
    </div>
</div>	

<div class="sidebarBody">

<div class="sidebarInner">
  <c:if test="${not empty topBlogs}"> <%-- to prevent invalid XHTML --%>
    <ul style="list-style-type:none; padding-left:2px; margin: 0px">
        <c:forEach var="blog" items="${topBlogs}">
           <li style="list-style-type:none; padding-left:2px; margin: 0px">
               <a href='<c:out value="${blog.siteUrl}" />'
                  title='<c:out value="${blog.title}" />' >
                   <str:left count="120" >
                      <str:removeXml>
                          <c:out value="${blog.title}" />
                      </str:removeXml>
                   </str:left>... 
               </a>:
               <c:out value="${blog.inboundlinks}" />
               <fmt:message key="planet.links" />
           </li>
        </c:forEach>
    </ul>
  </c:if>  

 <br />	
</div>

</div>


<br />


    
    