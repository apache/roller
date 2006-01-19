<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
<!--
function cancelEditing()
{
    document.planetSubscriptionFormEx.method.value = "cancelEditing";
    document.planetSubscriptionFormEx.submit();
}
function deleteSubscription()
{
    document.planetSubscriptionFormEx.method.value = "deleteSubscription";
    document.planetSubscriptionFormEx.submit();
}
// -->
</script>
<c:if test="${!(model.unconfigured)}" >

    <h1>
        <fmt:message key="planetSubscriptions.title" />    
        <c:if test='${planetSubscriptionFormEx.groupHandle != "external"}' >
           &nbsp;[group: <c:out value="${planetSubscriptionFormEx.groupHandle}" />]
        </c:if>        
    </h1>
   
    <c:choose>
        <c:when test='${empty planetSubscriptionFormEx.id && planetSubscriptionFormEx.groupHandle == "external"}' >
            <p class="subtitle"><fmt:message key="planetSubscriptions.subtitle.addMain" /></p>
            <p><fmt:message key="planetSubscriptions.prompt.addMain" /></p>
        </c:when>
        <c:when test='${empty planetSubscriptionFormEx.id && planetSubscriptionFormEx.groupHandle != "external"}' >
            <p class="subtitle">
                <fmt:message key="planetSubscriptions.subtitle.add" >
                    <fmt:param value="${planetSubscriptionFormEx.groupHandle}" />
                </fmt:message>
            </p>
            <p><fmt:message key="planetSubscriptions.prompt.add" /></p>
        </c:when>
        <c:when test="${!empty planetSubscriptionFormEx.id}" >
            <p class="subtitle"><fmt:message key="planetSubscriptions.subtitle.edit" /></p>
            <p><fmt:message key="planetSubscriptions.prompt.edit" /></p>
        </c:when>
    </c:choose>
    
    <html:form action="/admin/planetSubscriptions" method="post">
        <html:hidden property="method" value="saveSubscription" />
        <html:hidden property="id" />
        <html:hidden property="groupHandle" />
        <html:hidden property="inboundlinks" />
        <html:hidden property="inboundblogs" />
        
        <div class="formrow">
            <label for="title" class="formrow" />
                <fmt:message key="planetSubscription.title" /></label>
            <html:text property="title" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetSubscription.tip.title" />' />
        </div>
        
        <div class="formrow">
            <label for="feedUrl" class="formrow" />
                <fmt:message key="planetSubscription.feedUrl" /></label>
            <html:text property="feedUrl" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetSubscription.tip.feedUrl" />' />
        </div>
        
        <div class="formrow">
            <label for="siteUrl" class="formrow" />
                <fmt:message key="planetSubscription.siteUrl" /></label>
            <html:text property="siteUrl" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetSubscription.tip.siteUrl" />' />
        </div>

        <p />
        <div class="formrow">
            <label class="formrow" />&nbsp;</label>
            <input type="submit" 
                value='<fmt:message key="planetSubscriptions.button.save" />' />
            &nbsp;
            <input type="button" 
                value='<fmt:message key="planetSubscriptions.button.cancel" />' 
                onclick="cancelEditing()"/>
            <c:if test="${!empty planetSubscriptionFormEx.id}" >
                &nbsp;&nbsp;
                <input type="button" 
                   value='<fmt:message key="planetSubscriptions.button.delete" />' 
                   onclick="deleteSubscription()" />
            </c:if>
        </div>

        <br />

        <h2>
            <fmt:message key="planetSubscriptions.existingTitle" />
            <c:if test='${planetSubscriptionFormEx.groupHandle != "external"}' >
               &nbsp;[group: <c:out value="${planetSubscriptionFormEx.groupHandle}" />]
            </c:if>
        </h2>
        <p><i><fmt:message key="planetSubscriptions.existingPrompt" /></i></p>

        <table class="rollertable">
            <tr class="rHeaderTr">
               <th class="rollertable" width="30%">
                   <fmt:message key="planetSubscriptions.column.title" />
               </th>
               <th class="rollertable" width="60%">
                   <fmt:message key="planetSubscriptions.column.feedUrl" />
               </th>
               <th class="rollertable" width="10%">
                   <fmt:message key="planetSubscriptions.column.edit" />
               </th>
            </tr>
            <c:forEach var="subscription" items="${model.subscriptions}" >
                <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

                    <td class="rollertable">
                           <c:out value="${subscription.title}" />
                    </td>

                    <td class="rollertable">
                        <str:left count="100" >
                           <c:out value="${subscription.feedUrl}" />
                        </str:left>
                    </td>

                    <td class="rollertable">
                        <roller:link page="/admin/planetSubscriptions.do">
                            <roller:linkparam 
                                id="method" value="getSubscriptions" />                   
                            <roller:linkparam 
                                id="groupHandle" 
                                name="planetSubscriptionFormEx" 
                                property="groupHandle" />                   
                            <roller:linkparam 
                                id="feedUrl" name="subscription" property="feedUrl" />                   
                            <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon" 
                                title="<fmt:message key='planetSubscription.edit.tip' />" />
                        </roller:link>
                    </td>       

                </roller:row>
           </c:forEach>
        </table>
    
    </html:form>

    
</c:if>
<c:if test="${model.unconfigured}" >
    <fmt:message key="planetSubscriptions.unconfigured" />
</c:if>

