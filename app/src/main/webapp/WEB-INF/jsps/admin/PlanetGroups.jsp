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

<p class="subtitle"><s:text name="planetGroups.subtitle" /></p>

<p>
    <s:if test="group == null" >
        <s:text name="planetGroups.prompt.add" />
    </s:if>
    <s:else>
        <s:text name="planetGroups.prompt.edit" />
    </s:else>
</p>

<s:form action="planetGroups!save">
	<s:hidden name="salt" />
    <s:hidden name="bean.id" />
    
    <div class="formrow">
        <label for="title" class="formrow" /><s:text name="planetGroups.title" /></label>
        <s:textfield name="bean.title" size="40" maxlength="255" />
        <img src="<s:url value="/images/help.png"/>" alt="help" title='<s:text name="planetGroups.tip.title" />' />
    </div>
    
    <div class="formrow">
        <label for="handle" class="formrow" /><s:text name="planetGroups.handle" /></label>
        <s:textfield name="bean.handle" size="40" maxlength="255" />
        <img src="<s:url value="/images/help.png"/>" alt="help" title='<s:text name="planetGroups.tip.handle" />' />
    </div>
    
    <p />
    
    <div class="formrow">
        <label class="formrow" />&nbsp;</label>
        <s:submit value="%{getText('generic.save')}" />
        &nbsp;
        <input type="button" 
               value='<s:text name="generic.cancel" />'
               onclick="window.location='<s:url action="planetGroups"/>'"/>
        
        <s:if test="group != null" >
            &nbsp;&nbsp;
            <s:url var="deleteUrl" action="planetGroups!delete">
                <s:param name="bean.id" value="%{bean.id}" />
            </s:url>
            <input type="button" 
                   value='<s:text name="generic.delete" />'
                   onclick="window.location='<s:property value="%{deleteUrl}"/>'" />
        </s:if>
    </div>
    
</s:form>

<br style="clear:left" />

<h2><s:text name="planetGroups.existingTitle" /></h2>
<p><i><s:text name="planetGroups.existingPrompt" /></i></p>

<table class="rollertable">
<tr class="rHeaderTr">
    <th class="rollertable" width="30%">
        <s:text name="planetGroups.column.title" />
    </th>
    <th class="rollertable" width="50%">
        <s:text name="planetGroups.column.handle" />
    </th>
    <th class="rollertable" width="10%">
        <s:text name="generic.edit" />
    </th>
    <th class="rollertable" width="10%">
        <s:text name="planetGroups.column.subscriptions" />
    </th>
</tr>

<s:iterator id="group" value="groups" status="rowstatus">
    <s:if test="#rowstatus.odd == true">
        <tr class="rollertable_odd">
    </s:if>
    <s:else>
        <tr class="rollertable_even">
    </s:else>
    
    <td class="rollertable">
        <s:property value="#group.title" />
    </td>
    
    <td class="rollertable">
        <s:property value="#group.handle" />
    </td>
    
    <td class="rollertable">
        <s:url var="groupUrl" action="planetGroups">
            <s:param name="bean.id" value="#group.id" />
        </s:url>
        <s:a href="%{groupUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                                     title="<s:text name='planetGroups.edit.tip' />" /></s:a>
    </td>       
    
    <td class="rollertable">
        <s:url var="subUrl" action="planetSubscriptions">
            <s:param name="groupHandle" value="#group.handle" />
        </s:url>
        <s:a href="%{subUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                                   title="<s:text name='planetGroups.subscriptions.tip' />" /></s:a>
    </td>       
    
    </tr>
</s:iterator>
</table>
