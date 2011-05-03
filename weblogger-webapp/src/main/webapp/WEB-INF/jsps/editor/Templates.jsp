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

<%-- table of pages --%>
<table class="rollertable">
    <tr>
        <th width="30%"><s:text name="pagesForm.name" /></th>
        <th width="10"><s:text name="pagesForm.action" /></th>
        <th width="60%"><s:text name="pagesForm.description" /></th>
        <th width="10"><s:text name="pagesForm.remove" /></th>
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
                <s:url id="edit" action="templateEdit">
                    <s:param name="weblog" value="actionWeblog.handle" />
                    <s:param name="bean.id" value="#p.id" />
                </s:url>
                <s:a href="%{edit}"><s:property value="#p.name" /></s:a>
            </td>
            
            <td style="vertical-align:middle"><s:property value="#p.action" /></td>

            <td style="vertical-align:middle"><s:property value="#p.description" /></td>
                        
            <td class="center" style="vertical-align:middle">
                 <s:if test="!#p.required">
                     <s:url id="removeUrl" action="templateRemove">
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

        </tr>
    </s:iterator>
    <s:if test="templates.isEmpty">
        <tr class="rollertable_odd">
            <td style="vertical-align:middle" colspan="3" >
                no templates defined
            </td>
        </tr>
    </s:if>
</table>
