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
    
    <html:form action="/roller-ui/admin/planetSubscriptions" method="post">
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
            <label for="feedURL" class="formrow" />
                <fmt:message key="planetSubscription.feedUrl" /></label>
            <html:text property="feedURL" size="40" maxlength="255" />
            <img src="../images/help.png" alt="help" 
                title='<fmt:message key="planetSubscription.tip.feedUrl" />' />
        </div>
        
        <div class="formrow">
            <label for="siteURL" class="formrow" />
                <fmt:message key="planetSubscription.siteUrl" /></label>
            <html:text property="siteURL" size="40" maxlength="255" />
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

        <br style="clear:left" />

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
                           <c:out value="${subscription.feedURL}" />
                        </str:left>
                    </td>

                    <td class="rollertable">
                        <roller:link page="/roller-ui/admin/planetSubscriptions.do">
                            <roller:linkparam 
                                id="method" value="getSubscriptions" />                   
                            <roller:linkparam 
                                id="groupHandle" 
                                name="planetSubscriptionFormEx" 
                                property="groupHandle" />                   
                            <roller:linkparam 
                                id="feedUrl" name="subscription" property="feedURL" />                   
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

