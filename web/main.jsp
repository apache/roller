<%@ page import="org.roller.presentation.MainPageAction" %>
<%@ page import="java.util.Locale" %>
<%@ include file="/taglibs.jsp" %>
<% 
boolean planetEnabled = 
    RollerConfig.getBooleanProperty("planet.aggregator.enabled");
request.setAttribute("planetEnabled", new Boolean(planetEnabled));

request.setAttribute("pinnedPosts",
	((MainPageAction.MainPageData)request.getAttribute("data")).getWeblogEntriesPinnedToMain(5));
request.setAttribute("recentPosts",
	((MainPageAction.MainPageData)request.getAttribute("data")).getRecentWeblogEntries(45));
request.setAttribute("popularWebsites",
	((MainPageAction.MainPageData)request.getAttribute("data")).getPopularWebsites(65));
%>

<c:if test="${!planetEnabled}">
    <div class="entryTitleBox">
       <fmt:message key="mainPage.recentEntries" />
    </div>
</c:if>

<div class="entriesBox">
<div class="entriesBoxInner">

<c:if test="${!empty pinnedPosts}">

    <c:forEach var="post" items="${pinnedPosts}">
        <div class="entryBoxPinned">

            <a href='<c:out value="${baseURL}" /><c:out value="${post.permaLink}" />' class="entryTitle">
                <str:truncateNicely upper="90" >
                   <c:out value="${post.displayTitle}" />
                </str:truncateNicely></a>
            </a>
            <br /> 

            <span class="entryDetails">

                <a href='<c:out value="${baseURL}" />/page/<c:out value="${post.website.handle}" />' class="entryDetails">
                
                <str:truncateNicely upper="50" >
                   <c:out value="${post.website.name}" />
                </str:truncateNicely></a> |
                <c:out value="${post.category.name}" /> |
                <fmt:formatDate value="${post.pubTime}" type="both" dateStyle="medium" timeStyle="medium" /> |
                <fmt:message key="mainPage.postedBy" />&nbsp;
                <c:out value="${post.creator.userName}" />
                <c:if test="${!empty post.link}">
                   | <a href='<c:out value="${post.link}" />' class="entryDetails"><fmt:message key="mainPage.link" /></a>
                </c:if>
                <br />

            </span>
            <roller:ApplyPlugins name="post" scope="page" skipFlag="false" />

         </div>
    </c:forEach>
</c:if>

<c:forEach var="post" items="${recentPosts}">
    <c:if test="${!post.pinnedToMain}">
    <div class="entryBox">

        <a href='<c:out value="${baseURL}" /><c:out value="${post.permaLink}" />' class="entryTitle">
            <str:truncateNicely upper="90" >
               <c:out value="${post.displayTitle}" />
            </str:truncateNicely></a>
        </a><br />

        <span class="entryDetails">
            <a href='<c:out value="${baseURL}" />/page/<c:out value="${post.website.handle}" />'>
            <str:truncateNicely upper="50" >
               <c:out value="${post.website.name}" />
            </str:truncateNicely></a> |
            <c:out value="${post.category.name}" /> |
            <fmt:formatDate value="${post.pubTime}" type="both" dateStyle="medium" timeStyle="medium" /> |
            <fmt:message key="mainPage.postedBy" />
            <c:out value="${post.creator.userName}" />
            <c:if test="${!empty post.link}">
               | <a href='<c:out value="${post.link}" />' class="entryDetails"><fmt:message key="mainPage.link" /></a>
            </c:if>
            <br />
        </span>
        
        <span class="entryDescription">
        <roller:ApplyPlugins name="post" scope="page" stripHtml="true" maxLength="120" skipFlag="true" />
        </span>
        
    </div>
    </c:if>
</c:forEach>

</div> <!-- entriesBoxInner -->
</div> <!-- entriesBox -->

<br />

<a href='<c:url value="/rss"/>' title='<fmt:message key="mainPage.rss.tip" />'>
    <img src='<c:url value="/images/rssbadge.gif"/>' border="0"
        alt='<fmt:message key="mainPage.rss.tip" />' />
</a>
<fmt:message key="mainPage.rss.instructions" />




