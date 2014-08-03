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
   <s:text name="pagesForm.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  
<p class="pagetip">
   <s:text name="pagesForm.tip" />
</p>

<s:if test="actionWeblog.editorTheme != 'custom'">
    <p><s:text name="pagesForm.themesReminder"><s:param value="actionWeblog.editorTheme"/></s:text></p>
</s:if>

<s:form action="templatesRemove">
<s:hidden name="salt" />
<s:hidden name="weblog" value="%{actionWeblog.handle}" />

<%-- table of pages --%>
<table class="rollertable">

<s:if test="!templates.isEmpty">

    <tr>
        <th width="30%"><s:text name="generic.name" /></th>
        <th width="10"><s:text name="pagesForm.action" /></th>
        <th width="55%"><s:text name="generic.description" /></th>
        <th width="10"><s:text name="pagesForm.remove" /></th>
        <th width="5"><input type="checkbox" onclick="toggleFunction(this.checked,'idSelections');"/></th>
    </tr>
    <s:iterator id="p" value="templates" status="rowstatus">
        <s:if test="#rowstatus.odd == true">
            <tr class="rollertable_odd">
        </s:if>
        <s:else>
            <tr class="rollertable_even">
        </s:else>

            <td style="vertical-align:middle">
                <s:if test="! #p.hidden">
                    <img src='<s:url value="/images/page_white.png"/>' border="0" alt="icon" />
                </s:if>
                <s:else>
                    <img src='<s:url value="/images/page_white_gear.png"/>' border="0" alt="icon" />
                </s:else>
                <s:url var="edit" action="templateEdit">
                    <s:param name="weblog" value="actionWeblog.handle" />
                    <s:param name="bean.id" value="#p.id" />
                </s:url>
                <s:a href="%{edit}"><s:property value="#p.name" /></s:a>
            </td>
            
            <td style="vertical-align:middle"><s:property value="#p.action.readableName" /></td>

            <td style="vertical-align:middle"><s:property value="#p.description" /></td>

            <td class="center" style="vertical-align:middle">
                 <s:if test="!#p.required || !customTheme" >
                     <s:url var="removeUrl" action="templateRemove">
                         <s:param name="weblog" value="actionWeblog.handle"/>
                         <s:param name="removeId" value="#p.id"/>
                     </s:url>
                     <s:a href="%{removeUrl}"><img src='<s:url value="/images/delete.png"/>' /></s:a>
                 </s:if>
                 <s:else>
                    <img src='<s:url value="/images/lock.png"/>' border="0" alt="icon" 
                        title='<s:text name="pagesForm.required"/>' />
                 </s:else>
            </td>
            <td class="center" style="vertical-align:middle">
                <s:if test="!#p.required || !customTheme" >
                    <input type="checkbox" name="idSelections" value="<s:property value="#p.id" />" />
                </s:if>
                 <s:else>
                    <input type="checkbox" name="idSelections" value="<s:property value="#p.id" />" disabled="disabled"/>
                 </s:else>
            </td>
        </tr>
    </s:iterator>
    
</s:if>
<s:else>
    <tr class="rollertable_odd">
        <td style="vertical-align:middle" colspan="5" >
            <s:text name="pageForm.notemplates"/>
        </td>
    </tr>
</s:else>
</table>

<br/>

<s:if test="!templates.isEmpty">
	<div class="control">
		<s:submit value="%{getText('pagesForm.deleteselected')}" />
	</div>
</s:if>

</s:form>
