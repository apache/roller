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

<h3>
    <s:text name="categoryDeleteOK.removeCategory" />
    [<s:property value="category.name" />]
</h3>

<s:form action="categoryRemove!remove">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="removeId" />

    <s:if test="category.inUse" >
        <br />
        <span class="warning">
            <s:text name="categoryDeleteOK.warningCatInUse" />
        </span>
        <p><s:text name="categoryDeleteOK.youMustMoveEntries" /><p>
            <s:text name="categoryDeleteOK.moveToWhere" />
            <s:select name="targetCategoryId" list="allCategories" listKey="id" listValue="name" />
        </p>
    </s:if>
    <s:else>
        <p><s:text name="categoryDeleteOK.noEntriesInCat" /></p>
    </s:else>
    
    <p>
        <strong><s:text name="categoryDeleteOK.areYouSure" /></strong>
    </p>
    
    <s:submit value="%{getText('generic.yes')}" />&nbsp;
    <s:submit value="%{getText('generic.no')}" action="categoryRemove!cancel" />
    
</s:form>
