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

<h2 class="error"><fmt:message key="error.title.404" /></h2>

<c:set var="status_code" value="${requestScope['javax.servlet.error.status_code']}" />
<c:set var="message"     value="${requestScope['javax.servlet.error.message']}" />
<c:set var="type"        value="${requestScope['javax.servlet.error.type']}" />

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
    <td><fmt:message key="error.text.404" /></td>
</tr>
</table>

<br />
<br />



