<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">

             <h3><fmt:message key="planet.rankings" /></h3>
             <hr size="1" noshade="noshade" />
             
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
           
             <br />	
		
            </div>
        </div>
    </div>
</div>	

<br />


    
    