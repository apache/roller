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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle">
    <s:text name="weblogEntryQuery.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>
<p class="pagetip">
    <s:text name="weblogEntryQuery.tip" />
</p>


<%-- ============================================================= --%>
<%-- Number of comments and date message --%>
<%-- ============================================================= --%>

<div class="tablenav">
    
    <div style="float:left;">
        <s:text name="weblogEntryQuery.nowShowing">
            <s:param value="entries.size()" />
        </s:text>
    </div>
    <div style="float:right;">
        <s:date name="firstEntry.pubTime" format="MM/dd/yy hh:mm a" />
        --- 
        <s:date name="lastEntry.pubTime" format="MM/dd/yy hh:mm a" />
    </div>
    <br />
    
    
    <%-- ============================================================= --%>
    <%-- Next / previous links --%>
    <%-- ============================================================= --%>
    
    <%--
    <c:choose>
        <c:when test="${!empty model.prevLink && !empty model.nextLink}">
            <br /><center>
                &laquo;
                <a href='<s:property value="${model.prevLink}" />'>
                <s:text name="weblogEntryQuery.prev" /></a>
                | <a href='<s:property value="${model.nextLink}" />'>
                <s:text name="weblogEntryQuery.next" /></a>
                &raquo;
            </center><br />
        </c:when>
        <c:when test="${!empty model.prevLink}">
            <br /><center>
                &laquo;
                <a href='<s:property value="${model.prevLink}" />'>
                <s:text name="weblogEntryQuery.prev" /></a>
                | <s:text name="weblogEntryQuery.next" />
                &raquo;
            </center><br />
        </c:when>
        <c:when test="${!empty model.nextLink}">
            <br /><center>
                &laquo;
                <s:text name="weblogEntryQuery.prev" />
                | <a class="" href='<s:property value="${model.nextLink}" />'>
                <s:text name="weblogEntryQuery.next" /></a>
                &raquo;
            </center><br />
        </c:when>
        <c:otherwise><br /></c:otherwise>
    </c:choose>
    --%>
</div> <%-- class="tablenav" --%>


<%-- ============================================================= --%>
<%-- Entry table--%>
<%-- ============================================================= --%>

<p>
    <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
    <s:text name="weblogEntryQuery.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <s:text name="weblogEntryQuery.pending" />&nbsp;&nbsp;
</p>      

<table class="rollertable" width="100%">

<tr>
    <th class="rollertable" width="5%">
        <s:text name="weblogEntryQuery.pubTime" />
    </th>
    <th class="rollertable" width="5%">
        <s:text name="weblogEntryQuery.updateTime" />
    </th>
    <th class="rollertable">
        <s:text name="weblogEntryQuery.title" />
    </th>
    <th class="rollertable" width="5%">
        <s:text name="weblogEntryQuery.category" />
    </th>
    <th class="rollertable" width="5%">
    </th>
    <th class="rollertable" width="5%">
    </th>
</tr>

<s:iterator id="post" value="entries">
    <%-- <td> with style if comment is spam or pending --%>               
    <s:if test="#post.status == 'DRAFT'">
        <tr class="draftentry"> 
    </s:if>
    <s:elseif test="#post.status == 'PENDING'">
        <tr class="pendingentry"> 
    </s:elseif>
    <s:else>
        <tr>
    </s:else>
    
    <td>
        <s:property value="#post.pubTime" />
    </td>
    
    <td>
        <s:property value="#post.updateTime" />
    </td>
    
    <td>
        <str:truncateNicely upper="80"><s:property value="#post.displayTitle" /></str:truncateNicely>
    </td>
    
    <td>
        <s:property value="#post.category.name" />
    </td>
    
    <td>
        <s:url id="editUrl" action="entryEdit">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="bean.id" value="#post.id" />
        </s:url>
        <s:a href="%{editUrl}"><s:text name="weblogEntryQuery.edit" /></s:a>
    </td>
    
    <td>
        <s:if test="#post.status == 'PUBLISHED'">
            <a href='<s:property value="#post.permalink" />'><s:text name="weblogEntryQuery.view" /></a>
        </s:if>
    </td>
    
    </tr>
</s:iterator>

</table>

<s:if test="entries.isEmpty">
    <s:text name="weblogEntryQuery.noneFound" />
    <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</s:if>
