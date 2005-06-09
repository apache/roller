<% try { %>
<%@ 
page import="org.roller.presentation.planet.PlanetAction" %><%@ 
page import="java.util.Locale" %><%@ 
include file="/taglibs.jsp" %><%@ 
include file="/theme/header.jsp" %>

<table>
<tr>
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
<td width="70%">

    <div class="bannerBox">
    <%@ include file="local-banner.jspf" %>
    </div>
    
    <div class="entryTitleBox">
       <a href='<c:url value="/planetrss"/>' 
          title='<fmt:message key="mainPage.rss.tip" />'>
          <img src='<c:url value="/images/rssbadge.gif"/>' align="right"  border="0"
             alt='<fmt:message key="mainPage.rss.tip" />' /></a>
       <fmt:message key="mainPage.recentEntries" />
    </div>

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

</td>
<td width="30%" valign="top">

    <div class="entryTitleBox"><fmt:message key="planet.rankings" /></div>

    <div class="entryBox">
      <span class="hotBlogs">
      <c:if test="${not empty topBlogs}"> <%-- to prevent invalid XHTML --%>
        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
            <c:forEach var="blog" items="${topBlogs}">
               <li style="list-style-type:none; padding-left:2px; margin: 0px">
                   <a href='<c:out value="${blog.siteUrl}" />'
                      title='<c:out value="${blog.title}" />' >
                       <str:left count="120" >
                          <str:removeXml>
                              <c:out value="${blog.title}" />
                          </str:removeXml>
                       </str:left>... 
                   </a>:
                   <c:out value="${blog.inboundlinks}" />
                   <fmt:message key="planet.links" />
               </li>
            </c:forEach>
        </ul>
      </c:if>
      </span>
    </div>

    <div class="entryTitleBox"><fmt:message key="planet.hotBlogs" /></div>

    <div class="entryBox">
      <span class="hotBlogs">
      <c:if test="${not empty popularWebsites}"> <%-- to prevent invalid XHTML --%>
        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
        <c:forEach var="site" items="${popularWebsites}">
           <li style="list-style-type:none; padding-left:2px; margin: 0px">
               <a href='<c:out value="${baseURL}" />/page/<c:out value="${site.userName}" />'
                  title='<c:out value="${site.userName}" />' >
                  <str:truncateNicely lower="45" upper="45" >
                     <c:out value="${site.websiteName}" />
                  </str:truncateNicely></a>:
               <c:out value="${site.hits}" /> 
               <fmt:message key="mainPage.hits" />
           </li>
        </c:forEach>
        </ul>
      </c:if>
      </span>
    </div>

    <c:if test="${not empty featuredGroup1_entries && not empty featuredGroup1}"> 
    <div class="entryTitleBox"><c:out value="${featuredGroup1.title}" /></div>
    <div class="entryBox">
      <span class="hotBlogs">
        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
            <c:forEach var="post" items="${featuredGroup1_entries}">
               <li style="list-style-type:none; padding-left:2px; margin: 0px">
                   <a href='<c:out value="${post.permalink}" />' >
	                   <str:left count="50" >
	                      <c:out value="${post.title}" />
	                   </str:left>...
                   </a>
               </li>
            </c:forEach>
        </ul>
      </span>
    </div>
    </c:if>

    <c:if test="${not empty featuredGroup2_entries && not empty featuredGroup2}"> 
    <div class="entryTitleBox"><c:out value="${featuredGroup2.title}" /></div>
    <div class="entryBox">
      <span class="hotBlogs">
        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
            <c:forEach var="post" items="${featuredGroup2_entries}">
               <li style="list-style-type:none; padding-left:2px; margin: 0px">
                   <a href='<c:out value="${post.permalink}" />' >
	                   <str:left count="50" >
	                      <c:out value="${post.title}" />
	                   </str:left>...
	               </a>
               </li>
            </c:forEach>
        </ul>
      </span>
    </div>
    </c:if>

</td>

</tr>
</table>

<br />

<%@ include file="/theme/footer.jsp" %>
<% } catch (Exception e) {e.printStackTrace();} %>

