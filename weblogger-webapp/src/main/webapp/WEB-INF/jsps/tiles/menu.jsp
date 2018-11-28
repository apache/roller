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

<s:set name="tabMenu" value="menu"/>
<s:if test="#tabMenu != null">

<table class="menuTabTable" cellspacing="0" >
<tr>
<s:iterator id="tab" value="#tabMenu.tabs" >
    <s:if test="#tab.selected">
        <s:set name="selectedTab" value="#tab" />
        <td class="menuTabSelected">
    </s:if>
    <s:else>
        <td class="menuTabUnselected">
    </s:else>
    <div class="menu-tr">
        <div class="menu-tl">
            &nbsp;&nbsp;<a href="<s:url action="%{#tab.action}"><s:param name="weblog" value="actionWeblog.handle"/></s:url>"><s:text name="%{#tab.key}" /></a>&nbsp;&nbsp;
        </div>
    </div>
    </td>
    <td class="menuTabSeparator"></td>
</s:iterator>
</tr>
</table>

<table class="menuItemTable" cellspacing="0" >
    <tr>
        <td class="padleft">
            <s:iterator id="tabItem" value="#selectedTab.items" status="stat">
                <s:if test="!#stat.first">|</s:if>
                <s:if test="#tabItem.selected">
                    <a class="menuItemSelected" href="<s:url action="%{#tabItem.action}"><s:param name="weblog" value="actionWeblog.handle"/></s:url>"><s:text name="%{#tabItem.key}" /></a>
                </s:if>
                <s:else>
                    <a class="menuItemUnselected" href="<s:url action="%{#tabItem.action}"><s:param name="weblog" value="actionWeblog.handle"/></s:url>"><s:text name="%{#tabItem.key}" /></a>
                </s:else>
            </s:iterator>
        </td>
    </tr>
</table>

</s:if>
