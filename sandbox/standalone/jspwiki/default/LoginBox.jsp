<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%-- Provides a small login/logout form to include in a side bar. --%>

<div class="loginbox" align="center">
  <p>
  <hr />
  <wiki:UserCheck status="unvalidated">
    <form action="<wiki:Variable var="baseURL"/>Login.jsp" accept-charset="UTF-8" method="POST" >
    <p>
      <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
      <input type="text" name="uid" size="8" />
      <br />
      <input type="password" name="passwd" size="8" />
      <br />
      <input type="submit" name="action" value="login" />
    </p>
    </form>
  </wiki:UserCheck>
  <wiki:UserCheck status="validated">
    <form action="<wiki:Variable var="baseURL"/>Login.jsp" accept-charset="UTF-8">
    <p>
      <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />
      <input type="submit" name="action" value="logout" />
    </p>
    </form>
  </wiki:UserCheck>
  </p>

</div>

