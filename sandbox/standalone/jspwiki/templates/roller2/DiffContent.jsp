<%@ page import="com.ecyrd.jspwiki.tags.InsertDiffTag" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%!
    String getVersionText( Integer ver )
    {
        return ver.intValue() > 0 ? ("version "+ver) : "current version";
    }
%>

      <wiki:PageExists>
          Difference between 
          <%=getVersionText((Integer)pageContext.getAttribute(InsertDiffTag.ATTR_OLDVERSION, PageContext.REQUEST_SCOPE))%> 
          and 
          <%=getVersionText((Integer)pageContext.getAttribute(InsertDiffTag.ATTR_NEWVERSION, PageContext.REQUEST_SCOPE))%>:
          <div>
          <wiki:InsertDiff>
              <i>No difference detected.</i>
          </wiki:InsertDiff>
          </div>

      </wiki:PageExists>

      <wiki:NoSuchPage>
             This page does not exist.  Why don't you go and
             <wiki:EditLink>create it</wiki:EditLink>?
      </wiki:NoSuchPage>

      <p>
      Back to <wiki:LinkTo><wiki:PageName/></wiki:LinkTo>,
       or to the <wiki:PageInfoLink>Page History</wiki:PageInfoLink>.
       </p>
