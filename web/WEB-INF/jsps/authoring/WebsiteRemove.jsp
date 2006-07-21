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

<p class="subtitle">
<fmt:message key="websiteRemove.subtitle" />
</p>

<p>
<fmt:message key="websiteRemove.youSure"> 
    <fmt:param value="${website.name}" />
</fmt:message>
<br/>
<br/>
<span class="warning">
    <fmt:message key="websiteSettings.removeWebsiteWarning" />
</span>
</p>

<p>
<fmt:message key="websiteRemove.websiteId" /> = [<c:out value="${website.id}" />]
<br />
<fmt:message key="websiteRemove.websiteName" /> = [<c:out value="${website.name}" />]
</p>

<table>
<tr>
	<td>
		<html:form action="/roller-ui/authoring/website" method="post">
			<input type="submit" value='<fmt:message key="application.yes" />' ></input>
			<html:hidden property="method" value="remove"/></input>
			<html:hidden property="id" /></input>
		</html:form>
	</td>
	<td>
		<html:form action="/roller-ui/authoring/website" method="post">
			<input type="submit" value='<fmt:message key="application.no" />' ></input>
			<input type="hidden" name="weblog" value='<c:out value="${website.handle}" />' />
			<html:hidden property="id" /></input>
			<html:hidden property="method" value="edit"/></input>
		</html:form>
	</td>
</tr>
</table>



