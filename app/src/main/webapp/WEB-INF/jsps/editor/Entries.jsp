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
<%-- Number of entries and date message --%>
<%-- ============================================================= --%>

<div class="tablenav">
    
    <div style="float:left;">
        <s:text name="weblogEntryQuery.nowShowing">
            <s:param value="pager.items.size()" />
        </s:text>
    </div>
    <s:if test="pager.items.size() > 0">
        <div style="float:right;">
            <s:if test="firstEntry.pubTime != null">
                <s:text name="weblogEntryQuery.date.toStringFormat">
                    <s:param value="firstEntry.pubTime" />
                </s:text>
            </s:if>
            ---
            <s:if test="lastEntry.pubTime != null">
                <s:text name="weblogEntryQuery.date.toStringFormat">
                    <s:param value="lastEntry.pubTime" />
                </s:text>
            </s:if>
        </div>
    </s:if>
    <br />
    
    
    <%-- ============================================================= --%>
    <%-- Next / previous links --%>
    <%-- ============================================================= --%>
    
    <s:if test="pager.prevLink != null && pager.nextLink != null">
        <br /><center>
            &laquo;
            <a href='<s:property value="pager.prevLink" />'>
            <s:text name="weblogEntryQuery.prev" /></a>
            | <a href='<s:property value="pager.nextLink" />'>
            <s:text name="weblogEntryQuery.next" /></a>
            &raquo;
        </center><br />
    </s:if>
    <s:elseif test="pager.prevLink != null">
        <br /><center>
            &laquo;
            <a href='<s:property value="pager.prevLink" />'>
            <s:text name="weblogEntryQuery.prev" /></a>
            | <s:text name="weblogEntryQuery.next" />
            &raquo;
        </center><br />
    </s:elseif>
    <s:elseif test="pager.nextLink != null">
        <br /><center>
            &laquo;
            <s:text name="weblogEntryQuery.prev" />
            | <a class="" href='<s:property value="pager.nextLink" />'>
            <s:text name="weblogEntryQuery.next" /></a>
            &raquo;
        </center><br />
    </s:elseif>
    <s:else><br /></s:else>
    
</div> <%-- class="tablenav" --%>


<%-- ============================================================= --%>
<%-- Entry table--%>
<%-- ============================================================= --%>

<p>
    <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
    <s:text name="weblogEntryQuery.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <s:text name="weblogEntryQuery.pending" />&nbsp;&nbsp;
    <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <s:text name="weblogEntryQuery.scheduled" />&nbsp;&nbsp;
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
    <th class="rollertable" width="5%">
    </th>
</tr>

<s:iterator id="post" value="pager.items">
    <%-- <td> with style if comment is spam or pending --%>
    <s:if test="#post.status.name() == 'DRAFT'">
        <tr class="draftentry"> 
    </s:if>
    <s:elseif test="#post.status.name() == 'PENDING'">
        <tr class="pendingentry"> 
    </s:elseif>
    <s:elseif test="#post.status.name() == 'SCHEDULED'">
        <tr class="scheduledentry"> 
    </s:elseif>
    <s:else>
        <tr>
    </s:else>
    
    <td>
        <s:if test="#post.pubTime != null">
            <s:text name="weblogEntryQuery.date.toStringFormat">
                <s:param value="#post.pubTime" />
            </s:text>
        </s:if>
    </td>
    
    <td>
        <s:if test="#post.updateTime != null">
            <s:text name="weblogEntryQuery.date.toStringFormat">
                <s:param value="#post.updateTime" />
            </s:text>
        </s:if>
    </td>
    
    <td>
        <str:truncateNicely upper="80"><s:property value="#post.displayTitle" /></str:truncateNicely>
    </td>
    
    <td>
        <s:property value="#post.category.name" />
    </td>
    
    <td>
        <s:if test="#post.status.name() == 'PUBLISHED'">
            <a href='<s:property value="#post.permalink" />'><s:text name="weblogEntryQuery.view" /></a>
        </s:if>
    </td>

    <td>
        <s:url var="editUrl" action="entryEdit">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="bean.id" value="#post.id" />
        </s:url>
        <s:a href="%{editUrl}"><s:text name="generic.edit" /></s:a>
    </td>

    <td>
        <s:url var="deleteUrl" action="entryRemoveViaList">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="removeId" value="#post.id" />
        </s:url>
        <s:a href="%{deleteUrl}"><s:text name="generic.delete" /></s:a>
    </td>

    </tr>
</s:iterator>

</table>

<s:if test="pager.items.isEmpty">
    <s:text name="weblogEntryQuery.noneFound" />
    <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</s:if>
