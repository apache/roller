<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
-->
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


