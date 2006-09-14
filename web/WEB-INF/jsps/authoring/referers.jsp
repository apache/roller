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

<%-- Form allows deleting of referers --%>
<p class="subtitle">
    <fmt:message key="referers.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>  
<p class="pagetip">
    <fmt:message key="referers.tip" />
</p>

<html:form action="/roller-ui/authoring/referers" method="post">
<input type=hidden name="weblog" value='<c:out value="${model.website.handle}" />' />
<input type="hidden" name="method" value="delete" />

<%-- Table of referers, with check box for each --%>
<table width="75%" class="rollertable" >
    <tr class="rollertable">
        <th class="rollertable"></th>
        <th class="rollertable"><fmt:message key="referers.url" /></th>
        <th class="rollertable"><fmt:message key="referers.hits" /></th>
    </tr>
 
    <c:forEach var="referer" items="${referers}">
        <tr>
            <td class="rollertable">
                <input type="checkbox" name="id" value='<c:out value="${referer.id}" />' />
            </td>
            <td class="rollertable">               
               <c:out value="${referer.displayUrl}" escapeXml="false" />
            </td>
            <td class="rollertable"><c:out value="${referer.dayHits}" /></td>
        </tr>
    </c:forEach> 
    
</table>

<br />
<input type="submit" value='<fmt:message key="referers.deleteSelected" />' /></input>
</html:form>

<br />

<%-- Form allows reset of day hits --%>
<h1><fmt:message key="referers.hitCounters" /></h1>
<p><fmt:message key="referers.hits" />: <c:out value="${pageHits}"/></p>
<html:form action="/roller-ui/authoring/referers" method="post">
    <input type=hidden name="weblog" value='<c:out value="${model.website.handle}" />' />
    <input type="hidden" name="method" value="reset" />
    <input type="submit" value='<fmt:message key="referers.reset" />' /></input>
</html:form>

