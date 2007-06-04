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

<h1>
    <s:text name="planetSubscriptions.title" />    
    <s:if test='groupHandle != "all"' >
        &nbsp;[group: <s:property value="groupHandle" />]
    </s:if>        
</h1>


<s:if test="subscription == null && groupHandle == 'all'" >
    <p class="subtitle"><s:text name="planetSubscriptions.subtitle.addMain" /></p>
    <p><s:text name="planetSubscriptions.prompt.addMain" /></p>
</s:if>
<s:elseif test="subscription == null">
    <p class="subtitle">
        <s:text name="planetSubscriptions.subtitle.add" >
            <s:param value="groupHandle" />
        </s:text>
    </p>
    <p><s:text name="planetSubscriptions.prompt.add" /></p>
</s:elseif>
<s:else>
    <p class="subtitle"><s:text name="planetSubscriptions.subtitle.edit" /></p>
    <p><s:text name="planetSubscriptions.prompt.edit" /></p>
</s:else>


<s:form action="planetSubscriptions!save">
    <s:hidden name="bean.id" />
    <s:hidden name="groupHandle" />
    
    <div class="formrow">
        <label for="title" class="formrow" /><s:text name="planetSubscription.title" /></label>
        <s:textfield name="bean.title" size="40" maxlength="255" />
        <img src="<s:url value="/images/help.png"/>" alt="help" title='<s:text name="planetSubscription.tip.title" />' />
    </div>
    
    <div class="formrow">
        <label for="feedURL" class="formrow" /><s:text name="planetSubscription.feedUrl" /></label>
        <s:textfield name="bean.newsfeedURL" size="40" maxlength="255" />
        <img src="<s:url value="/images/help.png"/>" alt="help" title='<s:text name="planetSubscription.tip.feedUrl" />' />
    </div>
    
    <div class="formrow">
        <label for="siteURL" class="formrow" /><s:text name="planetSubscription.siteUrl" /></label>
        <s:textfield name="bean.websiteURL" size="40" maxlength="255" />
        <img src="<s:url value="/images/help.png"/>" alt="help" title='<s:text name="planetSubscription.tip.siteUrl" />' />
    </div>
    
    <p />
    <div class="formrow">
        <label class="formrow" />&nbsp;</label>
        <s:submit key="planetSubscriptions.button.save" />
        &nbsp;
        <input type="button" 
               value='<s:text name="planetSubscriptions.button.cancel" />' 
               onclick="window.location='<s:url action="planetSubscriptions" />'"/>
        
        <s:if test="bean.id != null" >
            &nbsp;&nbsp;
            <s:url id="deleteUrl" action="planetSubscriptions!delete">
                <s:param name="bean.id" value="%{bean.id}" />
            </s:url>
            <input type="button" 
                   value='<s:text name="planetSubscriptions.button.delete" />' 
                   onclick="window.location='<s:url value="%{deleteUrl}" />'" />
        </s:if>
    </div>
    
</s:form>

<br style="clear:left" />

<h2>
    <s:text name="planetSubscriptions.existingTitle" />
    <s:if test="groupHandle != 'all'" >
        &nbsp;[group: <s:property value="groupHandle" />]
    </s:if>
</h2>
<p><i><s:text name="planetSubscriptions.existingPrompt" /></i></p>

<table class="rollertable">
    <tr class="rHeaderTr">
        <th class="rollertable" width="30%">
            <s:text name="planetSubscriptions.column.title" />
        </th>
        <th class="rollertable" width="60%">
            <s:text name="planetSubscriptions.column.feedUrl" />
        </th>
        <th class="rollertable" width="10%">
            <s:text name="planetSubscriptions.column.edit" />
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
            <str:left count="100" >
                <s:property value="#sub.feedURL" />
            </str:left>
        </td>
        
        <td class="rollertable">
            <s:url id="subUrl" action="planetSubscriptions">
                <s:param name="bean.id" value="#sub.id" />
                <s:param name="groupHandle" value="%{groupHandle}" />
            </s:url>
            <s:a href="%{subUrl}"><img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                                       title="<s:text name='planetSubscription.edit.tip' />" /></s:a>
        </td>       
        
        </tr>
    </s:iterator>
</table>
