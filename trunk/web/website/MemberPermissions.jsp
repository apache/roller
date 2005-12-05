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

<html:form action="/editor/memberPermissions" method="post">
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
        



