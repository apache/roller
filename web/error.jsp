<%@ include file="/taglibs.jsp" %>
<tiles:insert page="/theme/tiles-simplepage.jsp">
   <tiles:put name="content"  value="/errorBody.jsp" />
   <tiles:put name="banner"   value="/theme/banner.jsp" />
   <tiles:put name="footer"   value="/theme/footer.jsp" />
   <tiles:put name="head"     value="/theme/head.jsp" />
   <tiles:put name="messages" value="/theme/messages.jsp" />
</tiles:insert>