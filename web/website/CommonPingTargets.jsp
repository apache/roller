<%@ page import="org.roller.presentation.RollerRequest"%>
<%@ include file="/taglibs.jsp" %>

<p class="subtitle">
    <fmt:message key="commonPingTargets.subtitle" />
</p>

<p/><fmt:message key="commonPingTargets.explanation"/><p/>

<table class="rollertable">

    <%-- Headings --%>
    <tr class="rollertable">
        <th class="rollertable" width="20%%"><fmt:message key="pingTarget.name" /></th>
        <th class="rollertable" width="70%"><fmt:message key="pingTarget.pingUrl" /></th>
        <th class="rollertable" width="5%"><fmt:message key="pingTarget.edit" /></th>
        <th class="rollertable" width="5%"><fmt:message key="pingTarget.remove" /></th>
    </tr>

    <%-- Listing of current common targets --%>
    <c:forEach var="pingTarget" items="${pingTargets}" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td class="rollertable">
               <str:truncateNicely lower="15" upper="20" ><c:out value="${pingTarget.name}" /></str:truncateNicely>
            </td>

            <td class="rollertable">
                <str:truncateNicely lower="70" upper="75" ><c:out value="${pingTarget.pingUrl}" /></str:truncateNicely>
            </td>

            <td class="rollertable" align="center">
               <roller:link page="/admin/commonPingTargets.do">
                   <roller:linkparam
                       id="<%= RollerRequest.PINGTARGETID_KEY %>"
                       name="pingTarget" property="id" />
                   <roller:linkparam
	                   id="method" value="editSelected" />
                   <img src='<c:url value="/images/page_white_edit.png"/>' border="0"
                        alt="<fmt:message key="pingTarget.edit" />" />
               </roller:link>
            </td>

            <td class="rollertable" align="center">
               <roller:link page="/admin/commonPingTargets.do">
                   <roller:linkparam
	                   id="<%= RollerRequest.PINGTARGETID_KEY %>"
	                   name="pingTarget" property="id" />
                   <roller:linkparam
	                   id="method" value="deleteSelected" />
                   <img src='<c:url value="/images/delete.png"/>' border="0"
                        alt="<fmt:message key="pingTarget.remove" />" />
               </roller:link>
            </td>

        </roller:row>
    </c:forEach>

</table>

<p/>
<html:form action="/admin/commonPingTargets" method="post">
    <div class="control">
       <html:hidden property="method" value="addNew" />
       <input type="submit" value='<fmt:message key="pingTarget.addNew"/>' />
    </div>
</html:form>
<p/>

