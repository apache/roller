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

<%-- JavaScript for categories table --%> 
<script>
function onMove()
{
    if ( confirm("<s:text name='categoriesForm.move.confirm' />") ) 
    {
        document.categories.method.value = "moveSelected";
        document.categories.submit();
    }
}
</script>

<p class="subtitle">
    <s:text name="categoriesForm.subtitle" >
        <s:param value="weblog" />
    </s:text>
</p>
<p class="pagetip">
    <s:text name="categoriesForm.rootPrompt" />
</p>


<%-- Form is a table of categories each with checkbox --%>
<s:form action="categories!move">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="categoryId" /> 

    <table class="rollertable">
        
        <tr class="rollertable">
            <th class="rollertable" width="15%"><s:text name="generic.name" /></th>
            <th class="rollertable" width="40%"><s:text name="generic.description" /></th>
            <th class="rollertable" width="31%"><s:text name="categoriesForm.imageUrl" /></th>
            <th class="rollertable" width="7%"><s:text name="generic.edit" /></th>
            <th class="rollertable" width="7%"><s:text name="categoriesForm.remove" /></th>
        </tr>
        
        <s:if test="AllCategories != null && !AllCategories.isEmpty">
        
        <%-- Categories --%>
        <s:iterator id="category" value="AllCategories" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
            
                <td class="rollertable"><s:property value="#category.name" /></td>
                
                <td class="rollertable"><s:property value="#category.description" /></td>
                
                <td class="rollertable"><s:property value="#category.image" /></td>

                <td class="rollertable" align="center">
                    <s:url var="editUrl" action="categoryEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#category.id" />
                    </s:url>
                    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" /></s:a>
                </td>
                
                <td class="rollertable" align="center">
                    <s:if test="AllCategories.size() > 1">
                        <s:url var="removeUrl" action="categoryRemove">
                            <s:param name="weblog" value="%{actionWeblog.handle}" />
                            <s:param name="removeId" value="#category.id" />
                        </s:url>
                        <s:a href="%{removeUrl}"><img src='<s:url value="/images/delete.png"/>' border="0" alt="icon" /></s:a>
                    </s:if>
                </td>
                
            </tr>
        </s:iterator>
        
        </s:if>
        <s:else>
            <tr>
                <td style="vertical-align:middle" colspan="6"><s:text name="categoriesForm.noresults" /></td>
            </tr>
        </s:else>
        
    </table>
    
</s:form>
