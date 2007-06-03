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
    <s:text name="customPingTargets.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>  

<s:if test="getProp('pings.disallowCustomTargets')">
    <!--  Otherwise custom targets are not allowed; explain the situation to the user -->
    <p class="pagetip">
        <s:text name="customPingTargets.disAllowedExplanation"/>
    </p>
</s:if>
<s:else>
<!-- Only show the form if custom targets are allowed -->
<p class="pagetip">
    <s:text name="customPingTargets.explanation"/>
</p>

<table class="rollertable">

<%-- Headings --%>
<tr class="rollertable">
    <th class="rollertable" width="20%%"><s:text name="pingTarget.name" /></th>
    <th class="rollertable" width="70%"><s:text name="pingTarget.pingUrl" /></th>
    <th class="rollertable" width="5%"><s:text name="pingTarget.edit" /></th>
    <th class="rollertable" width="5%"><s:text name="pingTarget.remove" /></th>
</tr>

<%-- Listing of current common targets --%>
<s:iterator id="pingTarget" value="pingTargets" status="rowstatus">
    
    <s:if test="#rowstatus.odd == true">
        <tr class="rollertable_odd">
    </s:if>
    <s:else>
        <tr class="rollertable_even">
    </s:else>
    
    <td class="rollertable"><s:property value="#pingTarget.name" /></td>
    
    <td class="rollertable"><s:property value="#pingTarget.pingUrl" /></td>
    
    <td class="rollertable" align="center">
        <s:url id="editPing" action="customPingTargetEdit">
            <s:param name="bean.id" value="#pingTarget.id" />
            <s:param name="weblog" value="actionWeblog.handle" />
        </s:url>
        <s:a href="%{editPing}">
            <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="<s:text name="pingTarget.edit" />" />
        </s:a>
    </td>
    
    <td class="rollertable" align="center">
        <s:url id="removePing" action="customPingTargets!deleteConfirm">
            <s:param name="pingTargetId" value="#pingTarget.id" />
            <s:param name="weblog" value="actionWeblog.handle" />
        </s:url>
        <s:a href="%{removePing}">
            <img src='<c:url value="/images/delete.png"/>' border="0" alt="<s:text name="pingTarget.remove" />" />
        </s:a>
    </td>
    
    </tr>
</s:iterator>

</table>

<div style="padding: 4px; font-weight: bold;">
    <s:url id="addPing" action="customPingTargetAdd">
        <s:param name="weblog" value="actionWeblog.handle" />
    </s:url>
    <img src='<s:url value="/images/add.png"/>' border="0"alt="icon" /><s:a href="%{addPing}">Add Ping Target</s:a>
</div>
</s:else>
