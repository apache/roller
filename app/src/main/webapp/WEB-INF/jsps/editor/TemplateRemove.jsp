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

<p class="subtitle">
    <s:text name="pageRemove.subtitle" />
</p>

<p>
    <s:text name="pageRemove.youSure"> 
        <s:param value="template.name" />
    </s:text>
    <br/>
	<br/>
	<span class="warning">
		<s:text name="pageRemoves.youSureWarning" />
	</span>
</p>

<p>
    <s:text name="pageRemove.pageId" /> = [<s:property value="template.id" />]
    <br />
    <s:text name="pageRemove.pageName" /> = [<s:property value="template.name" />]
</p>

<table>
    <tr>
        <td>
            <s:form action="templateRemove!remove">
                <s:hidden name="salt" />

                <s:hidden name="removeId" />
                <s:hidden name="weblog" value="%{actionWeblog.handle}" />
                
                <s:submit value="%{getText('generic.yes')}" />&nbsp;
                <s:submit value="%{getText('generic.no')}" action="templateRemove!cancel" />
            </s:form>
        </td>
    </tr>
</table>
