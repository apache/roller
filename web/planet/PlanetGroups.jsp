<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
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

    <h1><fmt:message key="planetGroups.pagetitle" /></h1>
    <p class="subtitle"><fmt:message key="planetGroups.subtitle" /></p>

    <p>
    <c:if test="${empty planetSubscriptionForm.id}" >
        <fmt:message key="planetGroups.prompt.add" />
    </c:if>
    <c:if test="${!empty planetSubscriptionForm.id}" >
        <fmt:message key="planetGroups.prompt.edit" />
    </c:if>
    </p>
    
    <html:form action="/admin/planetGroups" method="post">
        <html:hidden property="method" value="saveGroup" />
        <html:hidden property="id" />
        
        <div class="formrow">
            <label for="title" class="formrow" />
                <fmt:message key="planetGroups.title" /></label>
            <html:text property="title" size="40" maxlength="255" />
            <img src="../images/help.jpeg" alt="help" 
                title='<fmt:message key="planetGroups.tip.title" />' />
        </div>
        
        <div class="formrow">
            <label for="handle" class="formrow" />
                <fmt:message key="planetGroups.handle" /></label>
            <html:text property="handle" size="40" maxlength="255" />
            <img src="../images/help.jpeg" alt="help" 
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
    <br />
    
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
                    <roller:link page="/admin/planetGroups.do">
                        <roller:linkparam 
                            id="method" value="getGroups" />                   
                        <roller:linkparam 
                            id="groupHandle" name="group" property="handle" />                   
                        <img src='<c:url value="/images/Edit16.png"/>' border="0" alt="icon" 
                            title="<fmt:message key='planetGroups.edit.tip' />" />
                    </roller:link>
                </td>       
                                        
                <td class="rollertable">
                    <roller:link page="/admin/planetSubscriptions.do">
                        <roller:linkparam 
                            id="method" value="getSubscriptions" />                   
                        <roller:linkparam 
                            id="groupHandle" name="group" property="handle" />                   
                        <img src='<c:url value="/images/Edit16.png"/>' border="0" alt="icon" 
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

<%@ include file="/theme/footer.jsp" %>


