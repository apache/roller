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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script>
function confirmSubDelete(subUrl) {
  if (window.confirm('Are you sure you want to remove this subscription?')) {
    document.location.href='<s:url action="planetSubscriptions!delete" />?planetHandle=<s:property value="planetHandle"/>&subUrl='+encodeURIComponent(subUrl);
  }
}
</script>
        
      
<p class="subtitle">
    <s:text name="planetSubscriptions.subtitle.add" >
        <s:param value="planetHandle" />
    </s:text>
</p>
<p><s:text name="planetSubscriptions.prompt.add" /></p>

<s:form action="planetSubscriptions!save">
	<s:hidden name="salt" />
    <s:hidden name="planetHandle" />
    
    <div class="formrow">
        <label for="feedURL" class="formrow" /><s:text name="planetSubscription.feedUrl" /></label>
        <s:textfield name="subUrl" size="40" maxlength="255" onBlur="this.value=this.value.trim()"/>
        &nbsp;<s:submit value="%{getText('generic.save')}" />
    </div>
</s:form>

<br style="clear:left" />

<h2>
    <s:text name="planetSubscriptions.existingTitle" />
    <s:if test="planetHandle != 'all'" >
        &nbsp;[planet: <s:property value="planetHandle" />]
    </s:if>
</h2>

<table class="rollertable">
    <tr class="rHeaderTr">
        <th class="rollertable" width="30%">
            <s:text name="planetSubscriptions.column.title" />
        </th>
        <th class="rollertable" width="60%">
            <s:text name="planetSubscriptions.column.feedUrl" />
        </th>
        <th class="rollertable" width="10%">
            <s:text name="generic.delete" />
        </th>
    </tr>
    <s:iterator id="sub" value="subscriptions" status="rowstatus">
        <s:if test="#rowstatus.odd == true">
            <tr class="rollertable_odd">
        </s:if>
        <s:else>
            <tr class="rollertable_even">
        </s:else>
        
        <td class="rollertable">
            <s:property value="#sub.title" />
        </td>
        
        <td class="rollertable">
            <s:set name="feedURL" value="#sub.feedURL" />
            ${fn:substring(feedURL, 0, 100)}
        </td>
        
        <td class="rollertable">
            <img src='<s:url value="/images/delete.png"/>' />
            <a href="javascript: void(0);" onclick="confirmSubDelete('<s:property value="feedURL"/>')">
                <s:text name="generic.delete"/>
            </a>
        </td>       
        
        </tr>
    </s:iterator>
</table>
