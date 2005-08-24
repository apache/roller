<%@ page import="org.roller.presentation.RollerRequest"%>
<%@ include file="/taglibs.jsp" %>

<p class="subtitle">
   <fmt:message key="pings.subtitle" >
       <fmt:param value="${model.rollerSession.currentWebsite.handle}" />
   </fmt:message>
</p>  
<p class="pagetip">
    <fmt:message key="pings.explanation"/>
<p/>

<p/>
<h2><fmt:message key="pings.commonPingTargets"/></h2>
<p/>

<p/>
<fmt:message key="pings.commonPingTargetsExplanation"/>
<p/>

<table class="rollertable">
  <%-- Headings --%>
  <tr class="rollertable">
  <th class="rollertable" width="20%"><fmt:message key="pingTarget.name" /></th>
  <th class="rollertable" width="50%"><fmt:message key="pingTarget.pingUrl" /></th>
  <th class="rollertable" width="20%" colspan=2><fmt:message key="pingTarget.auto" /></th>
  <th class="rollertable" width="10%"><fmt:message key="pingTarget.manual" /></th>
  </tr>

  <%-- Table of current common targets with actions --%>
  <c:forEach var="pingTarget" items="${commonPingTargets}" >
    <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

    <td class="rollertable">
           <str:truncateNicely lower="15" upper="20" ><c:out value="${pingTarget.name}" /></str:truncateNicely>
           </td>

           <td class="rollertable">
            <str:truncateNicely lower="70" upper="75" ><c:out value="${pingTarget.pingUrl}" /></str:truncateNicely>
            </td>

            <!-- TODO: Use icons here -->
            <td class="rollertable" align="center" >
            <c:choose>
              <c:when test="${isEnabled[pingTarget.id]}">
              <span style="{color: #00aa00; font-weight: bold;}"><fmt:message key="pingTarget.enabled"/></span>&nbsp;
              </c:when>
              <c:otherwise >
              <span style="{color: #aaaaaa; font-weight: bold;}"><fmt:message key="pingTarget.disabled"/></span>&nbsp;
              </c:otherwise>
            </c:choose>
            </td>

            <!-- TODO: Use icons here -->
            <td class="rollertable" align="center" >
            <c:choose>
              <c:when test="${isEnabled[pingTarget.id]}">
                 <roller:link page="/editor/pingSetup.do">
                     <roller:linkparam
                        id="<%= RollerRequest.PINGTARGETID_KEY %>"
                        name="pingTarget" property="id" />
                     <roller:linkparam
                       id="method" value="disableSelected" />
                     <fmt:message key="pingTarget.disable"/>
                 </roller:link>
              </c:when>
              <c:otherwise >
                 <roller:link page="/editor/pingSetup.do">
                     <roller:linkparam
                        id="<%= RollerRequest.PINGTARGETID_KEY %>"
                        name="pingTarget" property="id" />
                     <roller:linkparam
                       id="method" value="enableSelected" />
                     <fmt:message key="pingTarget.enable"/>
                 </roller:link>
              </c:otherwise>
            </c:choose>
            </td>

            <td class="rollertable">
                 <roller:link page="/editor/pingSetup.do">
                     <roller:linkparam
                        id="<%= RollerRequest.PINGTARGETID_KEY %>"
                        name="pingTarget" property="id" />
                     <roller:linkparam
                       id="method" value="pingSelectedNow" />
                    <fmt:message key="pingTarget.sendPingNow"/>
                 </roller:link>
                 </td>

    </roller:row>
  </c:forEach>
</table>

<br />

<c:if test="${allowCustomTargets}">
  <h2><fmt:message key="pings.customPingTargets"/></h2>
  
  <p/>
  <c:choose>
    <c:when test="${!empty customPingTargets}">
      <fmt:message key="pings.customPingTargetsExplanationNonEmpty"/>
    </c:when>
    <c:otherwise>
      <fmt:message key="pings.customPingTargetsExplanationEmpty"/>
    </c:otherwise>
  </c:choose>
  <p/>

  <c:if test="${!empty customPingTargets}">
  <table class="rollertable">
     <%-- Headings --%>
     <tr class="rollertable">
     <th class="rollertable" width="20%"><fmt:message key="pingTarget.name" /></th>
     <th class="rollertable" width="50%"><fmt:message key="pingTarget.pingUrl" /></th>
     <th class="rollertable" width="20%" colspan=2><fmt:message key="pingTarget.auto" /></th>
     <th class="rollertable" width="10%"><fmt:message key="pingTarget.manual" /></th>
     </tr>

      <%-- Table of current custom targets with actions --%>
      <c:forEach var="pingTarget" items="${customPingTargets}" >
          <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

          <td class="rollertable">
                 <str:truncateNicely lower="15" upper="20" ><c:out value="${pingTarget.name}" /></str:truncateNicely>
                 </td>

                 <td class="rollertable">
                  <str:truncateNicely lower="70" upper="75" ><c:out value="${pingTarget.pingUrl}" /></str:truncateNicely>
                  </td>

                  <!-- TODO: Use icons here -->
                  <td class="rollertable" align="center" >
                  <c:choose>
                    <c:when test="${isEnabled[pingTarget.id]}">
                    <span style="{color: #00aa00; font-weight: bold;}"><fmt:message key="pingTarget.enabled"/></span>&nbsp;
                    </c:when>
                    <c:otherwise >
                    <span style="{color: #aaaaaa; font-weight: bold;}"><fmt:message key="pingTarget.disabled"/></span>&nbsp;
                    </c:otherwise>
                  </c:choose>
                  </td>

                  <!-- TODO: Use icons here -->
                  <td class="rollertable" align="center" >
                  <c:choose>
                    <c:when test="${isEnabled[pingTarget.id]}">
                       <roller:link page="/editor/pingSetup.do">
                           <roller:linkparam
                             id="<%= RollerRequest.PINGTARGETID_KEY %>"
                             name="pingTarget" property="id" />
                           <roller:linkparam
                             id="method" value="disableSelected" />
                           <fmt:message key="pingTarget.disable"/>
                       </roller:link>
                    </c:when>
                    <c:otherwise >
                       <roller:link page="/editor/pingSetup.do">
                           <roller:linkparam
                              id="<%= RollerRequest.PINGTARGETID_KEY %>"
                              name="pingTarget" property="id" />
                           <roller:linkparam
                             id="method" value="enableSelected" />
                           <fmt:message key="pingTarget.enable"/>
                       </roller:link>
                    </c:otherwise>
                  </c:choose>
                  </td>

                  <td class="rollertable">
                     <roller:link page="/editor/pingSetup.do">
                         <roller:linkparam
                            id="<%= RollerRequest.PINGTARGETID_KEY %>"
                            name="pingTarget" property="id" />
                         <roller:linkparam
                            id="method" value="pingSelectedNow" />
                         <fmt:message key="pingTarget.sendPingNow"/>
                     </roller:link>
                  </td>

          </roller:row>
      </c:forEach>
      </table>
  </c:if><!-- end if non-empty custom targets list -->
</c:if><!-- end if custom ping targets are allowed -->


