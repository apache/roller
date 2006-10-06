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
<% 
try { 
%><%@ include file="/taglibs.jsp" %>

<script type="text/javascript">
<!-- 
function deleteYes() 
{
    document.categoryDeleteForm.confirmDelete.value = "true";
    document.categoryDeleteForm.submit();
}
function deleteNo() 
{
    document.categoryDeleteForm.confirmDelete.value = "false";
    document.categoryDeleteForm.submit();
}
//-->
</script>


<h3>
<fmt:message key="categoryDeleteOK.removeCategory" />
[<c:out value="${categoryDeleteForm.name}" />]
</h3>

<html:form action="/roller-ui/authoring/categoryDelete" method="post">

	<html:hidden property="categoryId" />
	<html:hidden property="confirmDelete" />

	<c:if test="${categoryDeleteForm.inUse}" >
                <br />
		<span class="warning">
		    <fmt:message key="categoryDeleteOK.warningCatInUse" />
		</span>
		<p><fmt:message key="categoryDeleteOK.youMustMoveEntries" /><p>
		<fmt:message key="categoryDeleteOK.moveToWhere" />
		<html:select property="moveToWeblogCategoryId" size="1">
			<html:optionsCollection property="cats" label="path" value="id" />
		</html:select>
		</p>
	</c:if>

	<c:if test="${!categoryDeleteForm.inUse}" >
		<p><fmt:message key="categoryDeleteOK.noEntriesInCat" /></p>
	</c:if>

	<p>
	<strong><fmt:message key="categoryDeleteOK.areYouSure" /></strong>
	</p>

	<input type="button" value="<fmt:message key='application.yes' />" onclick="deleteYes()" />
	<input type="button" value="<fmt:message key='application.no' />" onclick="deleteNo()" />

</html:form>


<% 
} catch (Throwable e) {
e.printStackTrace();
}
%>
