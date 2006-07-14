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
<%
BasePageModel pageModel = (BasePageModel)request.getAttribute("model");
String websiteHandle = pageModel.getWebsite().getHandle();
%>

<p class="subtitle">
   <fmt:message key="customPingTargets.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  

<c:choose>
  <c:when test="${allowCustomTargets}">
    <!-- Only show the form if custom targets are allowed -->
    <p class="pagetip">
    <fmt:message key="customPingTargets.explanation"/>
    </p>

    <table class="rollertable">
    
        <%-- Headings --%>
        <tr class="rollertable">
            <th class="rollertable" width="20%%"><fmt:message key="pingTarget.name" /></th>
            <th class="rollertable" width="70%"><fmt:message key="pingTarget.pingUrl" /></th>
            <th class="rollertable" width="5%"><fmt:message key="pingTarget.edit" /></th>
            <th class="rollertable" width="5%"><fmt:message key="pingTarget.remove" /></th>
        </tr>
    
        <%-- Listing of current common targets --%>
        <c:forEach var="pingTarget" items="${pingTargets}" >
            <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">
    
                <td class="rollertable">
                   <str:truncateNicely lower="15" upper="20" ><c:out value="${pingTarget.name}" /></str:truncateNicely>
                </td>
    
                <td class="rollertable">
                    <str:truncateNicely lower="70" upper="75" ><c:out value="${pingTarget.pingUrl}" /></str:truncateNicely>
                </td>
    
                <td class="rollertable" align="center">
                   <roller:link page="/roller-ui/authoring/customPingTargets.do">
                       <roller:linkparam
                           id="<%= RequestConstants.PINGTARGET_ID %>"
                           name="pingTarget" property="id" />
                       <roller:linkparam
                           id="weblog" value="<%= websiteHandle %>" />
                       <roller:linkparam
    	                   id="method" value="editSelected" />
                       <img src='<c:url value="/images/page_white_edit.png"/>' border="0"
                            alt="<fmt:message key="pingTarget.edit" />" />
                   </roller:link>
                </td>
    
                <td class="rollertable" align="center">
                   <roller:link page="/roller-ui/authoring/customPingTargets.do">
                       <roller:linkparam
    	                   id="<%= RequestConstants.PINGTARGET_ID %>"
    	                   name="pingTarget" property="id" />
                       <roller:linkparam
                           id="weblog" value="<%= websiteHandle %>" />
                       <roller:linkparam
    	                   id="method" value="deleteSelected" />
                       <img src='<c:url value="/images/delete.png"/>' border="0"
                            alt="<fmt:message key="pingTarget.remove" />" />
                   </roller:link>
                </td>
    
            </roller:row>
        </c:forEach>
    
    </table>
    
    <br />
    
    <html:form action="/roller-ui/authoring/customPingTargets" method="post">
        <div class="control">
           <html:hidden property="method" value="addNew" />
           <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />           
           <input type="submit" value='<fmt:message key="pingTarget.addNew"/>' />
        </div>
    </html:form>
  </c:when>
  
  <c:otherwise>
     <!--  Otherwise custom targets are not allowed; explain the situation to the user -->
    <p class="pagetip">
        <fmt:message key="customPingTargets.disAllowedExplanation"/>
    </p>
  </c:otherwise>
  
</c:choose>


