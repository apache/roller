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

<s:if test="error">
    
    <h2><s:text name="installer.errorCreatingTables" /></h2> 
    <p><s:text name="installer.errorCreatingTablesExplanation" /></p> 
<pre>
<s:iterator value="messages"><s:property/><br /></s:iterator>
</pre>
    
</s:if>
<s:elseif test="success">

    <h2><s:text name="installer.tablesCreated" /></h2>
    
    <p><s:text name="installer.tablesCreatedExplanation" /></p>
    <p>
        <s:text name="installer.tryBootstrapping">
            <s:param><s:url action="install!bootstrap"/></s:param>
        </s:text>
    </p>
    
<pre>
<s:iterator value="messages"><s:property/><br /></s:iterator>
</pre>

</s:elseif>
<s:else>
    
    <h2><s:text name="installer.noDatabaseTablesFound" /></h2>

    <p>
        <s:text name="installer.noDatabaseTablesExplanation">
            <s:param value="databaseProductName" />
        </s:text>
    </p>
    <p><s:text name="installer.createTables" /></p>

    <s:form action="install!create">
		<s:hidden name="salt" />
        <s:submit value="%{getText('installer.yesCreateTables')}" />
    </s:form>
    
</s:else>

<br />
<br />
