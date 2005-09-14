<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.*" %>
<%@ page import="java.util.Collection" %>

<%-- FIXME: Get rid of the scriptlets. --%>
<%
    Collection list = (Collection)pageContext.getAttribute( "searchresults",
                                                             PageContext.REQUEST_SCOPE );

    String query = (String)pageContext.getAttribute( "query",
                                                     PageContext.REQUEST_SCOPE );
    if( query == null ) query = "";
%>

      <h2>Find pages</h2>

      <% if( list != null ) 
      {
      %>
          <h4>Search results for '<%=query%>'</h4>

          <p>
          <i>Found <%=list.size()%> hits, here are the top 20.</i>
          </p>

          <table border="0" cellpadding="4">

          <tr>
             <th width="30%" align="left">Page</th>
             <th align="left">Score</th>
          </tr>          
          <% if( list.size() > 0 ) { %>
              <wiki:SearchResultIterator list="<%=list%>" id="searchref" maxItems="20">
                  <tr>
                      <td width="30%"><wiki:LinkTo><wiki:PageName/></wiki:LinkTo></td>
                      <td><%=searchref.getScore()%></td>
                  </tr>
              </wiki:SearchResultIterator>
          <% } else { %>
              <tr>
                  <td width="30%"><b>No results</b></td>
              </tr>
          <% } %>

          </table>
          <p>
          <a href="http://www.google.com/search?q=<%=query%>" target="_blank">Try this same search on Google!</a>
          </p>
          <p><hr /></p>
      <%
      }
      %>

      <form action="<wiki:Variable var="baseURL"/>Search.jsp"
            accept-charset="ISO-8859-1,UTF-8">

      <p>
      Enter your query here:<br />
      <input type="text" name="query" size="40" value="<%=query%>" /></p>

      <p>
      <input type="submit" name="ok" value="Find!" /></p>
      </form>

      <p>
      Use '+' to require a word, '-' to forbid a word.  For example:

      <pre>
          +java -emacs jsp
      </pre>

      finds pages that MUST include the word "java", and MAY NOT include
      the word "emacs".  Also, pages that contain the word "jsp" are
      ranked before the pages that don't.</p>
      <p>
      All searches are case insensitive.  If a page contains both
      forbidden and required keywords, it is not shown.</p>

