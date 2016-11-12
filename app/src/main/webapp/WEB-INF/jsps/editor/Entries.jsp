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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
-->
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://sargue.net/jsptags/time" prefix="javatime" %>
<script src="<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: {
        "<fmt:message key='generic.delete'/>": function() {
          document.location.href='<s:url action="entryEdit!removeViaList" />?weblogId=<s:property value="weblogId"/>&entryId='
            + encodeURIComponent($(this).data('entryId'));
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $(".delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').data('entryId',  $(this).attr("data-entryId")).dialog('open');
    });
  });
</script>

<p class="subtitle">
    <s:text name="weblogEntryQuery.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>
<p class="pagetip">
    <fmt:message key="weblogEntryQuery.tip" />
</p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

            <div class="sidebarInner">

                <h3><fmt:message key="weblogEntryQuery.sidebarTitle" /></h3>
                <hr size="1" noshade="noshade" />

                <p><fmt:message key="weblogEntryQuery.sidebarDescription" /></p>

                <s:form action="entries">
                    <sec:csrfInput/>
                    <s:hidden name="weblogId" />

                    <%-- ========================================================= --%>
                    <%-- filter by category --%>

                    <div class="sideformrow">
                        <label for="categoryId" class="sideformrow">
                        <fmt:message key="weblogEntryQuery.label.category" /></label>
                        <s:select name="bean.categoryName" list="categories" listKey="left" listValue="right" size="1" />
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by date --%>

                    <div class="sideformrow">
                        <label for="startDateString" class="sideformrow"><fmt:message key="weblogEntryQuery.label.startDate" />:</label>
                        <script>
                        $(function() {
                            $( "#entries_startDateString" ).datepicker({
                                showOn: "button",
                                buttonImage: "../../images/calendar.png",
                                buttonImageOnly: true,
                                changeMonth: true,
                                changeYear: true
                            });
                        });
                        </script>
                        <s:textfield name="startDateString" size="12" readonly="true"/>
                    </div>

                    <div class="sideformrow">
                        <label for="endDateString" class="sideformrow"><fmt:message key="weblogEntryQuery.label.endDate" />:</label>
                        <script>
                        $(function() {
                            $( "#entries_endDateString" ).datepicker({
                                showOn: "button",
                                buttonImage: "../../images/calendar.png",
                                buttonImageOnly: true,
                                changeMonth: true,
                                changeYear: true
                            });
                        });
                        </script>
                        <s:textfield name="endDateString" size="12" readonly="true"/>
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by status --%>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="weblogEntryQuery.label.status" />:
                            <br />
                            <br />
                            <br />
                            <br />
                            <br />
                            <br />
                        </label>
                        <div>
                            <s:radio theme="strutsoverride" name="bean.status" list="statusOptions" listKey="left" listValue="right" />
                        </div>
                    </div>

                    <%-- ========================================================= --%>
                    <%-- sort by --%>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="weblogEntryQuery.label.sortby" />:
                            <br />
                            <br />
                        </label>
                        <div>
                            <s:radio theme="strutsoverride" name="bean.sortBy" list="sortByOptions" listKey="left" listValue="right" />
                        </div>
                    </div>

                    <%-- ========================================================= --%>
                    <%-- search button --%>

                    <br />

                    <s:submit value="%{getText('weblogEntryQuery.button.query')}" />

                </s:form>

                <br />
                <br />
            </div> <!-- sidebarInner -->

        </div>
    </div>
</div>


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
            <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>
            <s:if test="firstEntry.pubTime != null">
                <javatime:format value="${firstEntry.pubTime}" pattern="${dateFormat}"/>
            </s:if>
            ---
            <s:if test="lastEntry.pubTime != null">
                <javatime:format value="${lastEntry.pubTime}" pattern="${dateFormat}"/>
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
            <fmt:message key="weblogEntryQuery.prev" /></a>
            | <a href='<s:property value="pager.nextLink" />'>
            <fmt:message key="weblogEntryQuery.next" /></a>
            &raquo;
        </center><br />
    </s:if>
    <s:elseif test="pager.prevLink != null">
        <br /><center>
            &laquo;
            <a href='<s:property value="pager.prevLink" />'>
            <fmt:message key="weblogEntryQuery.prev" /></a>
            | <fmt:message key="weblogEntryQuery.next" />
            &raquo;
        </center><br />
    </s:elseif>
    <s:elseif test="pager.nextLink != null">
        <br /><center>
            &laquo;
            <fmt:message key="weblogEntryQuery.prev" />
            | <a class="" href='<s:property value="pager.nextLink" />'>
            <fmt:message key="weblogEntryQuery.next" /></a>
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
    <fmt:message key="weblogEntryQuery.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="weblogEntryQuery.pending" />&nbsp;&nbsp;
    <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="weblogEntryQuery.scheduled" />&nbsp;&nbsp;
</p>

<table class="rollertable" width="100%">

<tr>
    <th width="5%"><fmt:message key="weblogEntryQuery.pubTime" /></th>
    <th width="5%"><fmt:message key="weblogEntryQuery.updateTime" /></th>
    <th><fmt:message key="weblogEntryQuery.title" /></th>
    <th width="5%"><fmt:message key="weblogEntryQuery.category" /></th>
    <th width="5%"></th>
    <th width="5%"></th>
    <th width="5%"></th>
</tr>

<s:iterator var="post" value="pager.items">
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
            <s:set var="tempTime1" value="#post.pubTime"/>
            <javatime:format value="${tempTime1}" pattern="${dateFormat}"/>
        </s:if>
    </td>

    <td>
        <s:if test="#post.updateTime != null">
            <s:set var="tempTime2" value="#post.updateTime"/>
            <javatime:format value="${tempTime2}" pattern="${dateFormat}"/>
        </s:if>
    </td>

    <td>
        <str:truncateNicely upper="80"><s:property value="#post.title" /></str:truncateNicely>
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
            <s:param name="weblogId" value="%{actionWeblog.id}" />
            <s:param name="entryId" value="#post.id" />
        </s:url>
        <s:a href="%{editUrl}"><fmt:message key="generic.edit" /></s:a>
    </td>

    <td>
        <a href="#" class="delete-link" data-entryId="<s:property value='#post.id'/>"><fmt:message key="generic.delete" /></a>
    </td>

    </tr>
</s:iterator>
</table>

<div id="confirm-delete" title="<fmt:message key='weblogEdit.deleteEntry'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="weblogEntryRemove.areYouSure"/></p>
</div>

<s:if test="pager.items.isEmpty">
    <s:text name="weblogEntryQuery.noneFound" />
    <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</s:if>
