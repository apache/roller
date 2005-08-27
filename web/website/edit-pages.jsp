<%@ include file="/taglibs.jsp" %><%
request.setAttribute("customTheme", org.roller.pojos.Theme.CUSTOM); %>

<roller:StatusMessage/>

<p class="subtitle">
   <fmt:message key="pagesForm.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
<p class="pagetip">
   <fmt:message key="pagesForm.tip" />
</p>

<c:if test="${website.editorTheme ne customTheme}">
<p><fmt:message key="pagesForm.themesReminder"><fmt:param value="${website.editorTheme}"/></fmt:message></p>
</c:if>

<%-- table of pages --%>
<table class="rollertable">
    <tr>
        <th width="10%"><fmt:message key="pagesForm.name" /></th>
        <th width="10%"><fmt:message key="pagesForm.link" /></th>
        <th width="70%"><fmt:message key="pagesForm.description" /></th>
        <th width="5%"><fmt:message key="pagesForm.edit" /></th>
        <th width="5%"><fmt:message key="pagesForm.remove" /></th>
    </tr>
    <logic:iterate id="p" name="pages" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td><bean:write name="p" property="name" /></td>
            <td><bean:write name="p" property="link" /></td>
            <td><bean:write name="p" property="description" /></td>

            <%-- links to edit and remove page actions --%>
            <td class="center">
               <roller:link forward="editPage">
                  <roller:linkparam id="username" name="user" property="userName" />
                  <roller:linkparam id="pageId" name="p" property="id" />
                  <img src='<c:url value="/images/Edit16.png"/>' border="0" alt="icon" />
               </roller:link>
            </td>

            <td class="center">
               <c:choose>
                 <c:when test="${p.id != website.defaultPageId}">
                   <roller:link forward="removePage.ok">
                      <roller:linkparam id="username" name="user" property="userName" />
                      <roller:linkparam id="pageId" name="p" property="id" />
                      <img src='<c:url value="/images/Remove16.gif"/>' border="0" alt="icon" />
                   </roller:link>
                 </c:when>
                 <c:otherwise>
                    <fmt:message key="pagesForm.required"/>
                 </c:otherwise>
               </c:choose>
            </td>

        </roller:row>
    </logic:iterate>
</table>

<p><fmt:message key="pagesForm.hiddenNote" /></p>



