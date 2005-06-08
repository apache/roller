
<%@ include file="/weblog/comments-header.jspf" %>

<body>

<div class="comments">

<roller:StatusMessage/>

<div class="commentTitle">
    <bean:write name="blogEntry" property="title"
        scope="request" filter="false" />
</div>

<table class="rollertable">
<logic:iterate id="comment" indexId="count" name="blogComments"
    scope="request" type="org.roller.pojos.CommentData">
    <%
    String rowClass = "rollertable_even";
    if (count.intValue() % 2 == 0)
        rowClass = "rollertable_even";
    else
        rowClass = "rollertable_odd";
    %>
    <tr class="<%= rowClass %>">
 <%@ include file="comment-display.jspf" %>
    </tr>
</logic:iterate>
</table>

</div>

<c:if test="${blogEntry.commentsStillAllowed}">
   <h3><fmt:message key="comments.header" /></h3>
   <%@ include file="comment-form.jspf" %>
</c:if>
<c:if test="${!blogEntry.commentsStillAllowed}">
   <fmt:message key="comments.disabled" />
</c:if>


<%@ include file="/theme/footer.jsp" %>
