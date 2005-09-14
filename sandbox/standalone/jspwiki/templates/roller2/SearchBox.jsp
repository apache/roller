<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%-- Provides a simple searchbox that can be easily included anywhere
     on the page --%>

<div class="searchbox">
  <form action="<wiki:Variable var="baseURL"/>Search.jsp"
      accept-charset="<wiki:ContentEncoding />">
    <wiki:LinkTo page="FindPage">Search Wiki:</wiki:LinkTo>
    <input type="text" name="query" size="15" />
    <input type="submit" name="ok" value="Find!" />
  </form>
</div>

