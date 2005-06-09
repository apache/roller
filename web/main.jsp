<%@ 
page import="org.roller.presentation.MainPageAction" %><%@ 
page import="java.util.Locale" %><%@ 
include file="/taglibs.jsp" %><%@ 
include file="/theme/header.jsp" %>
<table>
<tr><%
request.setAttribute("pinnedPosts",
	((MainPageAction.MainPageData)request.getAttribute("data")).getWeblogEntriesPinnedToMain(5));
request.setAttribute("recentPosts",
	((MainPageAction.MainPageData)request.getAttribute("data")).getRecentWeblogEntries(45));
request.setAttribute("popularWebsites",
	((MainPageAction.MainPageData)request.getAttribute("data")).getPopularWebsites(65));
%>
<td width="70%">

    <div class="bannerBox">
    <%@ include file="local-banner.jspf" %>
    </div>

  <c:if test="${!empty pinnedPosts}">

    <div class="entryTitleBox">
       <fmt:message key="mainPage.pinnedEntries" />
    </div>

    <c:forEach var="post" items="${pinnedPosts}">
        <div class="entryBoxPinned">

            <a href='<c:out value="${baseURL}" /><c:out value="${post.permaLink}" />' class="entryTitle">
                <str:truncateNicely upper="90" >
                   <c:out value="${post.displayTitle}" />
                </str:truncateNicely></a>
            </a><br />

            <span class="entryDetails">

                <a href='<c:out value="${baseURL}" />/page/<c:out value="${post.website.user.userName}" />' class="entryDetails">
                <str:truncateNicely upper="50" >
                   <c:out value="${post.website.name}" />
                </str:truncateNicely></a> |
                <c:out value="${post.category.path}" /> |
                <fmt:formatDate value="${post.pubTime}" type="both" dateStyle="medium" timeStyle="medium" />
                <c:if test="${!empty post.link}">
                   | <a href='<c:out value="${post.link}" />' class="entryDetails"><fmt:message key="mainPage.link" /></a>
                </c:if>
                <br />

            </span>

            <roller:ApplyPlugins name="post" scope="page" maxLength="250" skipFlag="true" />

         </div>
    </c:forEach>
    <br />
  </c:if>

    <div class="entryTitleBox">
       <a href='<c:url value="/rss"/>' title='<fmt:message key="mainPage.rss.tip" />'>
          <img src='<c:url value="/images/rssbadge.gif"/>' align="right"  border="0"
             alt='<fmt:message key="mainPage.rss.tip" />' /></a>
       <fmt:message key="mainPage.recentEntries" />
    </div>

    <c:forEach var="post" items="${recentPosts}">
        <div class="entryBox">

            <a href='<c:out value="${baseURL}" /><c:out value="${post.permaLink}" />' class="entryTitle">
                <str:truncateNicely upper="90" >
                   <c:out value="${post.displayTitle}" />
                </str:truncateNicely></a>
            </a><br />

            <span class="entryDetails">

                <a href='<c:out value="${baseURL}" />/page/<c:out value="${post.website.user.userName}" />' class="entryDetails">
                <str:truncateNicely upper="50" >
                   <c:out value="${post.website.name}" />
                </str:truncateNicely></a> |
                <c:out value="${post.category.path}" /> |
                <fmt:formatDate value="${post.pubTime}" type="both" dateStyle="medium" timeStyle="medium" />
                <c:if test="${!empty post.link}">
                   | <a href='<c:out value="${post.link}" />' class="entryDetails"><fmt:message key="mainPage.link" /></a>
                </c:if>
                <br />

            </span>

            <roller:ApplyPlugins name="post" scope="page" stripHtml="true" maxLength="60" skipFlag="true" />

         </div>
    </c:forEach>

</td>
<td width="30%" valign="top">

    <div class="entryTitleBox"><fmt:message key="mainPage.searchWeblogs" /></div>

    <div class="entryBox">
        <form id="searchForm" method="get"
            action="<c:out value="${baseURL}" />/search"
            style="margin: 0; padding: 0" onsubmit="return validateSearch(this)">
            <input type="text" id="q" name="q" size="20"
                maxlength="255" value="<c:out value="${param.q}" />" />
            <input type="submit" value="<fmt:message key="mainPage.searchButton" />" />
        </form>
        <script type="text/javascript">
            function validateSearch(form) {
                if (form.q.value == "") {
                    alert("Please enter a search term to continue.");
                    form.q.focus();
                    return false;
                }
                return true;
            }
    </script>
    </div>

    <div class="entryTitleBox"><fmt:message key="mainPage.hotWeblogs" /></div>

    <div class="entryBox">
      <span class="hotBlogs">
      <c:if test="${not empty popularWebsites}"> <%-- to prevent invalid XHTML --%>
        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
        <c:forEach var="site" items="${popularWebsites}">
           <li style="list-style-type:none; padding-left:2px; margin: 0px">
               <a href='<c:out value="${baseURL}" />/page/<c:out value="${site.userName}" />'
                  title='<c:out value="${site.userName}" />' >
                  <str:truncateNicely lower="45" 
				  	upper="45" ><c:out 
					value="${site.websiteName}" /></str:truncateNicely></a>:
               <c:out value="${site.hits}" /> <fmt:message key="mainPage.hits" />
           </li>
        </c:forEach>
        </ul>
      </c:if>
      </span>
    </div>

</td>

</tr>
</table>

<br />

<%@ include file="/theme/footer.jsp" %>


