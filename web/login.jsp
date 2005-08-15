<%@ include file="/taglibs.jsp" %>
<tiles:insert page="/theme/tiles-simplepage.jsp">
   <tiles:put name="content"  value="/loginBody.jsp" />
   <tiles:put name="status"   value="/theme/status.jsp" />
   <tiles:put name="footer"   value="/theme/tiles-footer.jsp" />
   <tiles:put name="head"     value="/theme/tiles-head.jsp" />
   <tiles:put name="messages" value="/theme/messages.jsp" />
</tiles:insert>