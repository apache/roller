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

<%-- Form allows deleting of referers --%>
<p class="subtitle">
    <s:text name="referers.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>  
<p class="pagetip">
    <s:text name="referers.tip" />
</p>

<s:form action="referrers!remove">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    
    <%-- Table of referers, with check box for each --%>
    <table width="75%" class="rollertable" >
        <tr class="rollertable">
            <th class="rollertable"></th>
            <th class="rollertable"><s:text name="referers.url" /></th>
            <th class="rollertable"><s:text name="referers.hits" /></th>
        </tr>
        
        <s:iterator id="ref" value="referrers">
            <tr>
                <td class="rollertable">
                    <input type="checkbox" name="removeIds" value="<s:property value="#ref.id" />" />
                </td>
                <td class="rollertable">               
                    <s:property value="#ref.displayUrl" escape="false" />
                </td>
                <td class="rollertable"><s:property value="#ref.dayHits" /></td>
            </tr>
        </s:iterator> 
        
    </table>
    
    <br />
    <s:submit value="%{getText('referers.deleteSelected')}" />
    
</s:form>

<br />

<%-- Form allows reset of day hits --%>
<h1><s:text name="referers.hitCounters" /></h1>
<p><s:text name="referers.hits" />: <s:property value="dayHits"/></p>
<s:form action="referrers!reset">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:submit value="%{getText('referers.reset')}" />
</s:form>
