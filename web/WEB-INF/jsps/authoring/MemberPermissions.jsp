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
        
<script>
// <!--  
function save() {
    radios = document.getElementsByTagName("input");
    var removing = false;
    for (var i=0; i<radios.length; i++) {
        if (radios[i].value == -1 && radios[i].checked) {
            removing = true;
        }
    }
    if (removing && !confirm("<fmt:message key='memberPermissions.confirmRemove' />")) return;
    document.memberPermissionsForm.submit();
}
// -->
</script>

<p class="subtitle">
    <fmt:message key="memberPermissions.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>

<p><fmt:message key="memberPermissions.description" /></p>

<html:form action="/roller-ui/authoring/memberPermissions" method="post">
    <html:hidden property="websiteId" />
    
    <table class="rollertable">
        <tr class="rHeaderTr">
           <th class="rollertable" width="20%">
               <fmt:message key="memberPermissions.userName" />
           </th>
           <th class="rollertable" width="20%">
               <fmt:message key="memberPermissions.administrator" />
           </th>
           <th class="rollertable" width="20%">
               <fmt:message key="memberPermissions.author" />
           </th>
           <th class="rollertable" width="20%">
               <fmt:message key="memberPermissions.limited" />
           </th>
           <th class="rollertable" width="20%">
               <fmt:message key="memberPermissions.remove" />
           </th>
        </tr>
        <c:forEach var="permission" items="${model.permissions}">
            <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">                       
                <td class="rollertable">
                    <img src='<c:url value="/images/user.png"/>' border="0" alt="icon" />
	                <c:out value="${permission.user.userName}" />
                </td>               
                <td class="rollertable">
                    <input type="radio" onchange="dirty()" 
                        <c:if test="${permission.permissionMask == 3}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="3" />
                </td>
                <td class="rollertable">
	                <input type="radio"  onchange="dirty()"
                        <c:if test="${permission.permissionMask == 1}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="1" />
                </td>                
                <td class="rollertable">
                    <input type="radio" onchange="dirty()"
                        <c:if test="${permission.permissionMask == 0}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="0" />
                </td>                
                <td class="rollertable">
                    <input type="radio" onchange="dirty()"
                        name='perm-<c:out value="${permission.id}" />' value="-1" />
                </td>
           </roller:row>
       </c:forEach>
    </table>
    <br />
     
    <div class="control">
       <input type="button" onclick="javascript:save()"
       value='<fmt:message key="memberPermissions.button.save" />'></input>
    </div>
    
</html:form>
        



