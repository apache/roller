<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="memberPermissions.title" /></h1>

<p><fmt:message key="memberPermissions.description" /></p>

<html:form action="/editor/memberPermissions" method="post" focus="handle">
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
                    <input type="radio" 
                        <c:if test="${permission.permissionMask == 3}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="3" />
                </td>
                <td class="rollertable">
	                <input type="radio" 
                        <c:if test="${permission.permissionMask == 1}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="1" />
                </td>                
                <td class="rollertable">
                    <input type="radio" 
                        <c:if test="${permission.permissionMask == 0}">checked</c:if>
                        name='perm-<c:out value="${permission.id}" />' value="0" />
                </td>                
                <td class="rollertable">
                    <input type="radio" 
                        name='perm-<c:out value="${permission.id}" />' value="-1" />
                </td>
           </roller:row>
       </c:forEach>
    </table>
        
    <div class="control">
       <input type="submit" value='<fmt:message key="memberPermissions.button.save" />'></input>
    </div>
    
</html:form>
    
<%@ include file="/theme/footer.jsp" %>


