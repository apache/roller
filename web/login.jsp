<%@ include file="/taglibs.jsp" %>
<% request.setAttribute("secure_login", 
    org.roller.config.RollerConfig.getProperty("securelogin.enabled")); %>
<c:if test='${secure_login == "true"}' >
  <roller:secure mode="secured" />
</c:if>
<tiles:insert page="/theme/tiles-simplepage.jsp">
   <tiles:put name="banner"       value="/theme/banner.jsp" />
   <tiles:put name="bannerStatus" value="/theme/bannerStatus.jsp" />
   <tiles:put name="head"         value="/theme/head.jsp" />
   <tiles:put name="styles"       value="/theme/css-banner.jsp" />
   <tiles:put name="messages"     value="/theme/messages.jsp" />
   <tiles:put name="content"      value="/loginBody.jsp" />
   <tiles:put name="footer"       value="/theme/footer.jsp" />
</tiles:insert>