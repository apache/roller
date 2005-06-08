<%@ include file="/weblog/comments-header.jspf" %>

<body>

<div class="comments">

<div class="commentTitle">
    <bean:write name="blogEntry" property="title" scope="request" />
</div>

<h3><fmt:message key="comments.preview" /></h3>
<bean:define id="comment" name="commentForm" type="org.roller.presentation.forms.CommentForm"/>

<% Integer count = new Integer(-1); // JvdM: 'count' needs to be there for comment-display.jspf %>
<table class="rollertable">
    <tr class="rollertable_even">
 <%@ include file="comment-display.jspf" %>
    </tr>
</table>

<div class="content"><fmt:message key="comments.preview.edit" /></div>

<%@ include file="comment-form.jspf" %>

</div>

<%@ include file="/theme/footer.jsp" %>