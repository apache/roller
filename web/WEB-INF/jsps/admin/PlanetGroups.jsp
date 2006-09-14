<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
<!--
function cancelEditing()
{
    document.planetGroupForm.method.value = "cancelEditing";
    document.planetGroupForm.submit();
}
function deleteGroup()
{
    document.planetGroupForm.method.value = "deleteGroup";
    document.planetGroupForm.submit();
}
// -->
</script>
<c:if test="${!(model.unconfigured)}" >

    <p class="subtitle"><fmt:message key="planetGroups.subtitle" /></p>

    <p>
    <c:if test="${empty planetSubscriptionForm.id}" >
        <fmt:message key="planetGroups.prompt.add" />
    </c:if>
    <c:if test="${!empty planetSubscriptionForm.id}" >
        <fmt:message key="planetGroups.prompt.edit" />
    </c:if>
    </p>
    
    <html:form action="/roller-ui/admin/planetGroups" method="post">
        <html:hidden property="method" value="saveGroup" />
        <html:hidden property="id" />
        
        <div class="formrow">
            <label for="title" class="formrow" />
                <fmt:message key="planetGroups.title" /></label>
            <html:text property="title" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetGroups.tip.title" />' />
        </div>
        
        <div class="formrow">
            <label for="handle" class="formrow" />
                <fmt:message key="planetGroups.handle" /></label>
            <html:text property="handle" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetGroups.tip.handle" />' />
        </div>
        
        <p />
        <div class="formrow">
            <label class="formrow" />&nbsp;</label>
            <input type="submit" 
                value='<fmt:message key="planetGroups.button.save" />' />
            &nbsp;
            <input type="button" 
                value='<fmt:message key="planetGroups.button.cancel" />' 
                onclick="cancelEditing()"/>
            <c:if test="${!empty planetGroupForm.id}" >
                &nbsp;&nbsp;
                <input type="button" 
                   value='<fmt:message key="planetGroups.button.delete" />' 
                   onclick="deleteGroup()" />
            </c:if>
        </div>

    </html:form>
    
    <br style="clear:left" />
    
    <h2><fmt:message key="planetGroups.existingTitle" /></h2>
    <p><i><fmt:message key="planetGroups.existingPrompt" /></i></p>
    
    <table class="rollertable">
        <tr class="rHeaderTr">
           <th class="rollertable" width="30%">
               <fmt:message key="planetGroups.column.title" />
           </th>
           <th class="rollertable" width="50%">
               <fmt:message key="planetGroups.column.handle" />
           </th>
           <th class="rollertable" width="10%">
               <fmt:message key="planetGroups.column.edit" />
           </th>
           <th class="rollertable" width="10%">
               <fmt:message key="planetGroups.column.subscriptions" />
           </th>
        </tr>
        <c:forEach var="group" items="${model.groups}" >
            <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">
            
                <td class="rollertable">
                       <c:out value="${group.title}" />
                </td>
                
                <td class="rollertable">
                       <c:out value="${group.handle}" />
                </td>
                
                <td class="rollertable">
                    <roller:link page="/roller-ui/admin/planetGroups.do">
                        <roller:linkparam 
                            id="method" value="getGroups" />                   
                        <roller:linkparam 
                            id="groupHandle" name="group" property="handle" />                   
                        <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                            title="<fmt:message key='planetGroups.edit.tip' />" />
                    </roller:link>
                </td>       
                                        
                <td class="rollertable">
                    <roller:link page="/roller-ui/admin/planetSubscriptions.do">
                        <roller:linkparam 
                            id="method" value="getSubscriptions" />                   
                        <roller:linkparam 
                            id="groupHandle" name="group" property="handle" />                   
                        <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                            title="<fmt:message key='planetGroups.subscriptions.tip' />" />
                    </roller:link>
                </td>       
                                        
            </roller:row>
       </c:forEach>
    </table>
</c:if>
<c:if test="${model.unconfigured}" >
    <fmt:message key="planetGroups.unconfigured" />
</c:if>




