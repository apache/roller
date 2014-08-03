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
   <s:text name="pings.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  
<p class="pagetip">
    <s:text name="pings.explanation"/>
<p/>

<p/>
<h2><s:text name="pings.commonPingTargets"/></h2>
<p/>

<p/>
<s:text name="pings.commonPingTargetsExplanation"/>
<p/>

<table class="rollertable">
<%-- Headings --%>
<tr class="rollertable">
    <th class="rollertable" width="20%"><s:text name="generic.name" /></th>
    <th class="rollertable" width="40%"><s:text name="pingTarget.pingUrl" /></th>
    <th class="rollertable" width="20%" colspan=2><s:text name="pingTarget.auto" /></th>
    <th class="rollertable" width="20%"><s:text name="pingTarget.manual" /></th>
</tr>

<%-- Table of current common targets with actions --%>
<s:iterator id="pingTarget" value="commonPingTargets" status="rowstatus">
    <s:if test="#rowstatus.odd == true">
        <tr class="rollertable_odd">
    </s:if>
    <s:else>
        <tr class="rollertable_even">
    </s:else>
    
    <td class="rollertable">
        <str:truncateNicely lower="15" upper="20" ><s:property value="#pingTarget.name" /></str:truncateNicely>
    </td>
    
    <td class="rollertable">
        <str:truncateNicely lower="70" upper="75" ><s:property value="#pingTarget.pingUrl" /></str:truncateNicely>
    </td>
    
    <!-- TODO: Use icons here -->
    <td class="rollertable" align="center" >
        <s:if test="pingStatus[#pingTarget.id]">
            <span style="color: #00aa00; font-weight: bold;"><s:text name="pingTarget.enabled"/></span>&nbsp;
        </s:if>
        <s:else>
            <span style="color: #aaaaaa; font-weight: bold;"><s:text name="pingTarget.disabled"/></span>&nbsp;
        </s:else>
    </td>
    
    <!-- TODO: Use icons here -->
    <td class="rollertable" align="center" >
        <s:if test="pingStatus[#pingTarget.id]">
            <s:url var="disableUrl" action="pings!disable" >
                <s:param name="weblog" value="%{actionWeblog.handle}" />
                <s:param name="pingTargetId" value="#pingTarget.id" />
            </s:url>
            <s:a href="%{disableUrl}"><s:text name="pingTarget.disable"/></s:a>
        </s:if>
        <s:else>
            <s:url var="enableUrl" action="pings!enable" >
                <s:param name="weblog" value="%{actionWeblog.handle}" />
                <s:param name="pingTargetId" value="#pingTarget.id" />
            </s:url>
            <s:a href="%{enableUrl}"><s:text name="pingTarget.enable"/></s:a>
        </s:else>
    </td>
    
    <td class="rollertable">
        <s:url var="pingNowUrl" action="pings!pingNow" >
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="pingTargetId" value="#pingTarget.id" />
        </s:url>
        <s:a href="%{pingNowUrl}"><s:text name="pingTarget.sendPingNow"/></s:a>
    </td>
    
    </tr>
</s:iterator>
</table>

<br />
