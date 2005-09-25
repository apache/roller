<%@ include file="/taglibs.jsp" %>

<%
org.roller.presentation.search.SearchResultsPageModel searchResults =
   (org.roller.presentation.search.SearchResultsPageModel)
      request.getAttribute("searchResults");       
%>

<%-- Display the search pager --%>
    
<c:choose>
   <c:when test="${empty searchResults.website}">
      <c:set var="siteText" value="this site" />
   </c:when>
   <c:otherwise>
      <c:set var="siteText" value="$(searchResults.website.handle}" />
   </c:otherwise>
</c:choose>
  
<%-- Display search summary --%>

<p>
    <fmt:message key="macro.searchresults.searchFor" >
       <fmt:param value="${siteText}" />
    </fmt:message>
    
    <%-- "You searched for blah" and link it to dictionary.com --%>    
    <fmt:message var="dictionaryUrlTitle" key="macro.searchresults.title">
       <fmt:param value="${searchResults.term}" />
    </fmt:message>
    <c:url var="dictionaryUrl" 
         value="http://dictionary.com/search?q=${searchResults.term}" />    
    "<a href='<c:out value="${dictionaryUrl}" />' 
       title='<c:out value="${dictionaryUrlTitle}" />'
       class="dictionary"><c:out value="${searchResults.term}" /></a>".

    <%-- "X entries found, try your search on google.com?" --%>
    <fmt:message key="macro.searchresults.hits_1">
       <fmt:param value="${searchResults.hits}" />
    </fmt:message>    
    <c:url var="googleUrl" 
         value="http://google.com/search?q=${searchResults.term}%20site:${searchResults.baseURL}" />    
    <a href='<c:out value="${googleUrl}" />'
        class="google"><fmt:message key="macro.searchresults.hits_2" /></a>

    <%-- Form to search again --%>
    <form method="get" action="$ctxPath/search"
        style="margin: 5px">
        <input type="text" id="q" name="q" size="31"
            maxlength="255" value='<c:out value="${searchResults.term}" />'
            style="padding-left: 1px" />
        <input type="hidden" name="weblog" value='<c:out value="${request.param.weblog}" />' />
        
        <%-- Combobox allows restrict by category --%>
        <c:if test="${!empty searchResults.categories}">
            <select name="c">
            <option value="">- Restrict By Category -</option>
            <c:forEach var="cat" items="${searchResults.categories}">
                <c:choose>
                    <c:when test="${cat == request.param.c}">
                       <option selected="selected"><c:out value="${cat}" /></option>
                    </c:when>
                    <c:otherwise>
                       <option><c:out value="${cat}" /></option>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            </select>
        </c:if>
        <input type="submit" value="$text.get("macro.searchresults.again")" />
    </form>
</p>

<script type="text/javascript"
    src="$ctxPath/theme/scripts/searchhi.js"></script>
<br />


<%-- Display results, if we have them --%>
    
<c:if test="${searchResults.hits > 0}">
    <c:set var="min" value="${searchResults.offset + 1}" />
    <c:set var="max" value="${searchResults.offset + searchResults.limit}" />
    <c:if test="${max > searchResults.hits}">
        <c:set var="max" value="${searchResults.hits}" />
    </c:if>
    <h3><c:out value="${min}" /> - <c:out value="${max}" /> of <c:out value="${searchResults.hits}" /> found.</h3>
    <br />
    
    <%-- Search results is map of maps, keyed by date objects --%>
    <% request.setAttribute("keys", searchResults.getResults().keySet()); %>    
    <%-- Iterate through keys of map --%>
    <c:forEach var="dayKey" items="${keys}">
    
        <%-- Get map of  entries for one day --%>
        <c:set var="dayMap" value="${searchResults.results[dayKey]}" />        
        
            <%-- Display date --%>
            <h3><c:out value="${dayKey}" /></h3><br />
            
            <%-- Loop to display entries --%>
            <div class="daybox" style="margin: 0px 5px 0px 10px">
            <c:forEach var="post" items="${dayMap}">

                <a href='<c:out value="${searchResults.baseURL}" /><c:out value="${post.permaLink}" />' class="entryTitle">
                    <str:truncateNicely upper="90" >
                       <c:out value="${post.displayTitle}" />
                    </str:truncateNicely></a>
                </a><br />
                
                <span class="entryDetails">
                    <a href='<c:out value="${searchResults.baseURL}" />/page/<c:out value="${post.website.handle}" />'>
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
        
               <span style="font-size:x-small;">(<a
                 href='<c:url value="?q=${searchResults.term}&weblog=${post.website.handle}" />'>restrict search</a> 
                 to just this blog)
               </span>
                     
            </c:forEach>
            </div> <%-- daybox --%>
            <br />
            <br />
        </div>
                
    </c:forEach>
    
    
    <%-- Display the search pager --%>
    
    <h3 style="text-align:center;">
    <c:set var="numPages" value="${searchResults.hits / searchResults.limit}" />
    <c:set var="remainder" value="${searchResults.hits % searchResults.limit}" />
    <c:if test="${remainder > 0}">
       <c:set var="numPages" value="${numPages + 1}" />
    </c:if>
    <c:if test="${numPages > 1}">
    
       <br />
       <br />
       <c:forEach var="pageNum" begin="1" end="${numPages}" >
          <c:set var="i" value="${pageNum - 1}" />
          <c:set var="start" value="${searchResults.limit * i}" />
          <a href='<c:url value="?q=${searchResults.term}&weblog=${request.param.handle}&n=${searchResults.limit}&o=${start}"/>'>
             <c:out value="${pageNum}" />
          </a> 
          <c:if test="${pageNum != numPages}">|</c:if>
       </c:forEach>
       <br />
       <br />

    </c:if>
    </h3> 
    
</c:if>


