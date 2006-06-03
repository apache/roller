<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.planet.PlanetAction" %>
<%@ page import="java.util.Locale" %>

<%
request.setAttribute("aggregation", ((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getAggregation(50));
request.setAttribute("topBlogs", ((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getTopSubscriptions(50));
request.setAttribute("popularWebsites",((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getPopularWebsites(65));
   
// custom groups, only shown if they exist      
request.setAttribute("featuredGroup1",((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getGroup("featuredGroup1"));
request.setAttribute("featuredGroup1_entries",((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getAggregation("featuredGroup1",20));

request.setAttribute("featuredGroup2",((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getGroup("featuredGroup2"));
request.setAttribute("featuredGroup2_entries",((PlanetAction.PlanetPageData)
   request.getAttribute("data")).getAggregation("featuredGroup2",20));
%>

<div class="entriesBox">
<div class="entriesBoxInner">

<c:forEach var="post" items="${aggregation}">

    <div class="entryBox">
    
        <a href='<c:out value="${post.permalink}" />' class="entryTitle">
           <str:truncateNicely upper="90" >
              <str:removeXml>
                 <c:out value="${post.title}" />
              </str:removeXml>
           </str:truncateNicely></a>
        </a><br />

        <span class="entryDetails">
            <a href='<c:out value="${post.subscription.siteUrl}" />' 
                class="entryDetails">
               <str:removeXml>
                  <c:out value="${post.subscription.title}" />
               </str:removeXml>
            </a>               
            <fmt:formatDate value="${post.published}" type="both" 
                dateStyle="medium" timeStyle="medium" />
            <br />
        </span>

        <str:truncateNicely upper="250" >
           <str:removeXml>
              <c:out value="${post.content}" escapeXml="false" />
           </str:removeXml>
        </str:truncateNicely>
        
    </div>

</c:forEach>
</div> <!-- entriesBoxInner -->
</div> <!-- entriesBox -->

<br />

<a href='<c:url value="/planetrss"/>' title='<fmt:message key="mainPage.rss.tip" />'>
    <img src='<c:url value="/images/rssbadge.gif"/>' border="0"
        alt='<fmt:message key="mainPage.rss.tip" />' />
</a>
<fmt:message key="mainPage.rss.instructions" />


