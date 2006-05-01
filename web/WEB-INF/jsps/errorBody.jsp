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
boolean debug = org.apache.roller.config.RollerRuntimeConfig.getBooleanProperty("site.debugMode");
request.setAttribute("debug", new Boolean(debug));
%>

<h2 class="error"><fmt:message key="errorPage.title" /></h2>

<c:choose>
<c:when test="${debug}">
    
<c:set var="status_code" value="${requestScope['javax.servlet.error.status_code']}" />
<c:set var="message"     value="${requestScope['javax.servlet.error.message']}" />
<c:set var="type"        value="${requestScope['javax.servlet.error.type']}" />
<c:set var="exception"   value="${requestScope['javax.servlet.error.exception']}" />

<table width="80%" border="1px" style="border-collapse: collapse;">
<tr>
    <td width="20%">Status Code</td>
    <td><c:out value="${status_code}" /></td>
</tr>
<tr>
    <td width="20%">Message</td>
    <td><c:out value="${message}" /></td>
</tr>
<tr>
    <td width="20%">Type</td>
    <td><c:out value="${type}" /></td>
</tr>
<tr>
    <td width="20%">Exception</td>
    <td><c:out value="${exception}" /></td>
</tr>
</table>

<c:if test="${!empty exception}">
    <% 
    java.io.StringWriter sw = new java.io.StringWriter();
    Throwable t = (Throwable)pageContext.getAttribute("exception");
    if (t != null) {
        t.printStackTrace(new java.io.PrintWriter(sw));
        String stackTrace = sw.toString();
        if (stackTrace.trim().length() > 0) {
        %>
        <p>Stack Trace:</p>
        <form>
            <textarea rows="30" style="font-size:8pt;width:80%">
            <%=  stackTrace %>
            </textarea>
        </form>
    <%  } 
    } %>
</c:if>

<br />
<br />

</c:when>

<c:otherwise>
    <p><fmt:message key="errorPage.message" /></p>
</c:otherwise>

</c:choose>




