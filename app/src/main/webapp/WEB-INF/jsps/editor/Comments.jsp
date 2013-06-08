<%--
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
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-1.4.2.min.js" />"></script>

<script type="text/javascript">
    <!--
    function setChecked(val, name) {
        $("input[name=" + name + "]").attr('checked', val);
    }

    function bulkDelete() {
        if (window.confirm('<s:text name="commentManagement.confirmBulkDelete"><s:param value="bulkDeleteCount" /></s:text>')) {
            document.commentQueryForm.method.value = "bulkDelete";
            document.commentQueryForm.submit();
        }
    }
    -->
</script>

<p class="subtitle">
    <s:if test="bean.entryId != null && bean.entryId !='' ">
        <s:text name="commentManagement.entry.subtitle">
            <s:param value="firstComment.weblogEntry.title"/>
        </s:text>
    </s:if>
    <s:else>
        <s:text name="commentManagement.website.subtitle">
            <s:param value="%{actionWeblog.handle}"/>
        </s:text>
    </s:else>
</p>

<p class="pagetip"><s:text name="commentManagement.tip" /></p>

<s:if test="pager.items.isEmpty">
    <s:text name="commentManagement.noCommentsFound" />
</s:if>

<s:else>

    <%-- ============================================================= --%>
<%-- Comment table / form with checkboxes --%>
<%-- ============================================================= --%>

    <s:form action="comments!update">
		<s:hidden name="salt" />
        <s:hidden name="weblog" />
        <s:hidden name="bean.ids" />
        <s:hidden name="bean.entryId" />
        <s:hidden name="bean.offset" />
        <s:hidden name="bean.count" />
        <s:hidden name="bean.startDateString" />
        <s:hidden name="bean.endDateString" />
        <s:hidden name="bean.pendingString" />
        <s:hidden name="bean.spamString" />

        <%-- ============================================================= --%>
<%-- Number of comments and date message --%>
        <%-- ============================================================= --%>

        <div class="tablenav">

            <div style="float:left;">
                <s:text name="commentManagement.nowShowing">
                    <s:param value="pager.items.size()" />
                </s:text>
            </div>
            <div style="float:right;">
                <s:date name="firstComment.postTime" format="MM/dd/yy hh:mm a" />
                ---
                <s:date name="lastComment.postTime" format="MM/dd/yy hh:mm a" />
            </div>
            <br />


            <%-- ============================================================= --%>
    <%-- Next / previous links --%>
    <%-- ============================================================= --%>

            <s:if test="pager.prevLink != null && pager.nextLink != null">
                <br /><center>
                    &laquo;
                    <a href='<s:property value="pager.prevLink" />'>
                    <s:text name="commentManagement.prev" /></a>
                    | <a href='<s:property value="pager.nextLink" />'>
                    <s:text name="commentManagement.next" /></a>
                    &raquo;
                </center><br />
            </s:if>
            <s:elseif test="pager.prevLink != null">
                <br /><center>
                    &laquo;
                    <a href='<s:property value="pager.prevLink" />'>
                    <s:text name="commentManagement.prev" /></a>
                    | <s:text name="commentManagement.next" />
                    &raquo;
                </center><br />
            </s:elseif>
            <s:elseif test="pager.nextLink != null">
                <br /><center>
                    &laquo;
                    <s:text name="commentManagement.prev" />
                    | <a class="" href='<s:property value="pager.nextLink" />'>
                    <s:text name="commentManagement.next" /></a>
                    &raquo;
                </center><br />
            </s:elseif>
            <s:else><br /></s:else>

        </div> <%-- class="tablenav" --%>


<%-- ============================================================= --%>
<%-- Bulk comment delete link --%>
<%-- ============================================================= --%>

        <s:if test="bulkDeleteCount > 0">
            <p>
                <s:text name="commentManagement.bulkDeletePrompt1">
                    <s:param value="bulkDeleteCount" />
                </s:text>
                <a href="#" onclick="bulkDelete()">
                    <s:text name="commentManagement.bulkDeletePrompt2" />
                </a>
            </p>
        </s:if>


        <table class="rollertable" width="100%">

            <%-- ======================================================== --%>
            <%-- Comment table header --%>

            <tr>
                <th class="rollertable" width="5%">
                    <s:text name="commentManagement.columnApproved" />
                </th>
                <th class="rollertable" width="5%">
                    <s:text name="commentManagement.columnSpam" />
                </th>
                <th class="rollertable" width="5%" >
                    <s:text name="commentManagement.columnDelete" />
                </th>
                <th class="rollertable">
                    <s:text name="commentManagement.columnComment" />
                </th>
            </tr>

            <%-- ======================================================== --%>
            <%-- Select ALL and NONE buttons --%>

            <tr class="actionrow">
                <td align="center">
                    <s:text name="commentManagement.select" /><br/>

                    <a href="#" onclick='setChecked(1,"bean.approvedComments")'>
                    <s:text name="commentManagement.all" /></a><br />

                    <a href="#" onclick='setChecked(0,"bean.approvedComments")'>
                    <s:text name="commentManagement.none" /></a>
                </td>
                <td align="center">
                    <s:text name="commentManagement.select" /><br/>

                    <a href="#" onclick='setChecked(1,"bean.spamComments")'>
                    <s:text name="commentManagement.all" /></a><br />

                    <a href="#" onclick='setChecked(0,"bean.spamComments")'>
                    <s:text name="commentManagement.none" /></a>
                </td>
                <td align="center">
                    <s:text name="commentManagement.select" /><br/>

                    <a href="#" onclick='setChecked(1,"bean.deleteComments")'>
                    <s:text name="commentManagement.all" /></a><br />

                    <a href="#" onclick='setChecked(0,"bean.deleteComments")'>
                    <s:text name="commentManagement.none" /></a>
                </td>
                <td align="right">
                    <br />
                    <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <s:text name="commentManagement.pending" />&nbsp;&nbsp;
                    <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <s:text name="commentManagement.spam" />&nbsp;&nbsp;
                </td>
            </tr>


            <%-- ========================================================= --%>
<%-- Loop through comments --%>
<%-- ========================================================= --%>

            <s:iterator id="comment" value="pager.items" status="rowstatus">
                <tr>
                    <td>
                        <%-- a bit funky to use checkbox list here, but using checkbox didn't work for me :(
             we are effectively just creating a checkbox list of 1 item for each iteration of our collection --%>
                        <s:checkboxlist name="bean.approvedComments" list="{#comment}" listKey="id" listValue="name" />
                    </td>
                    <td>
                        <%-- a bit funky to use checkbox list here, but using checkbox didn't work for me :(
             we are effectively just creating a checkbox list of 1 item for each iteration of our collection --%>
                        <s:checkboxlist name="bean.spamComments" list="{#comment}" listKey="id" listValue="name" />
                    </td>
                    <td>
                        <%-- a bit funky to use checkbox list here, but using checkbox didn't work for me :(
             we are effectively just creating a checkbox list of 1 item for each iteration of our collection --%>
                        <s:checkboxlist name="bean.deleteComments" list="{#comment}" listKey="id" listValue="name" />
                    </td>

                    <%-- ======================================================== --%>
    <%-- Display comment details and text --%>

    <%-- <td> with style if comment is spam or pending --%>
                    <s:if test="#comment.status == 'SPAM'">
                        <td class="spamcomment">
                    </s:if>
                    <s:elseif test="#comment.status == 'PENDING'">
                        <td class="pendingcomment">
                    </s:elseif>
                    <s:else>
                        <td>
                        </s:else>

                        <%-- comment details table in table --%>
                        <table style="border:none; padding:0px; margin:0px">
                            <tr>
                                <td style="border: none; padding:0px;">
                                <s:text name="commentManagement.entryTitled" /></td>
                                <td class="details" style="border: none; padding:0px;">
                                    <a href='<s:property value="#comment.weblogEntry.permalink" />'>
                                    <s:property value="#comment.weblogEntry.title" /></a>
                                </td>
                            </tr>

                            <tr>
                                <td style="border: none; padding:0px;">
                                <s:text name="commentManagement.commentBy" /></td>
                                <td class="details" style="border: none; padding:0px;">
                                    <s:if test="#comment.email != null && #comment.name != null">
                                        <s:text name="commentManagement.commentByBoth" >
                                            <s:param><s:property value="#comment.name" /></s:param>
                                            <s:param><s:property value="#comment.email" /></s:param>
                                            <s:param><s:property value="#comment.email" /></s:param>
                                            <s:param><s:property value="#comment.remoteHost" /></s:param>
                                        </s:text>
                                    </s:if>
                                    <s:elseif test="#comment.email == null && #comment.name == null">
                                        <s:text name="commentManagement.commentByIP" >
                                            <s:param><s:property value="#comment.remoteHost" /></s:param>
                                        </s:text>
                                    </s:elseif>
                                    <s:else>
                                        <s:text name="commentManagement.commentByName" >
                                            <s:param><s:property value="#comment.name" /></s:param>
                                            <s:param><s:property value="#comment.remoteHost" /></s:param>
                                        </s:text>
                                    </s:else>

                                    <s:if test="#comment.url != null">
                                        <br /><a href='<s:property value="#comment.url" />'>
                                        <str:truncateNicely upper="60" appendToEnd="..."><s:property value="#comment.url" /></str:truncateNicely></a>
                                    </s:if>
                                </td>
                            </tr>
                            <tr>
                                <td style="border: none; padding:0px;">
                                <s:text name="commentManagement.postTime" /></td>
                                <td class="details" style="border: none; padding:0px;">
                                <s:property value="#comment.postTime" /></td>
                            </tr>
                        </table> <%-- end comment details table in table --%>

                        <%-- comment content --%>
                        <br />
                        <span class="details">

                            <s:if test="#comment.content.length() > 1000">
                                <span id="comment-<s:property value="#comment.id"/>"><str:truncateNicely upper="1000" appendToEnd="..."><s:property value="#comment.content" escape="true" /></str:truncateNicely></span>
                                <div id="link-<s:property value="#comment.id"/>">
                                    <a onclick='readMoreComment("<s:property value="#comment.id"/>")'>
                                        <s:text name="commentManagement.readmore" />
                                    </a>
                                </div>
                            </s:if>
                            <s:else>
                                <span width="200px" id="comment-<s:property value="#comment.id"/>"><s:property value="#comment.content" escape="true" /></span>
                            </s:else>

                        <br />
                        <br />
                            <div>
                                <a id="editlink-<s:property value="#comment.id"/>" onclick='editComment("<s:property value="#comment.id"/>")'>
                                    <s:text name="commentManagement.editComment" />
                                </a>
                            </div>

                            <span id="savelink-<s:property value="#comment.id"/>" style="display: none">
                                <a onclick='saveComment("<s:property value="#comment.id"/>")'>
                                    <s:text name="commentManagement.saveComment" />
                                </a> &nbsp;|&nbsp;
                            </span>
                            
                            <span id="cancellink-<s:property value="#comment.id"/>" style="display: none">
                                <a onclick='editCommentCancel("<s:property value="#comment.id"/>")'>
                                    <s:text name="commentManagement.cancelComment" />
                                </a>
                            </span>

                        </span>

                    </td>
                </tr>
            </s:iterator>
        </table>
        <br />


    <script type="text/javascript">
    <!--
    var comments = {};

    function editComment(id) {
        // make sure we have the full comment
        if ($("#link-" + id).size() > 0) readMoreComment(id, editComment);

        // save the original comment value
        comments[id] = $("#comment-" + id).html();

        $("#editlink-" + id).hide();
        $("#savelink-" + id).show();
        $("#cancellink-" + id).show();

        // put comment in a textarea for editing
        $("#comment-" + id).html("<textarea style='width:100%' rows='10'>" + comments[id] + "</textarea>");
    }

    function saveComment(id) {
        var content = $("#comment-" + id).children()[0].value;
        $.ajax({
            type: "POST",
            url: '<%= request.getContextPath()%>/roller-ui/authoring/commentdata?id=' + id,
            data: content,
            dataType: "text",
            processData: "false",
            contentType: "text/plain",
            success: function (rdata) {
                if (status != "success") {
                    var cdata = eval("(" + rdata + ")");
                    $("#editlink-" + id).show();
                    $("#savelink-" + id).hide();
                    $("#cancellink-" + id).hide();
                    $("#comment-" + id).html(cdata.content);
                } else {
                    alert('<s:text name="commentManagement.saveError" />');
                }
            }
        });
    }

    function editCommentCancel(id) {
        $("#editlink-" + id).show();
        $("#savelink-" + id).hide();
        $("#cancellink-" + id).hide();
        if (comments[id]) {
            $("#comment-" + id).html(comments[id]);
            comments[id] = null;
        }
    }

    function readMoreComment(id, callback) {
        $.ajax({
            type: "GET",
            url: '<%= request.getContextPath()%>/roller-ui/authoring/commentdata?id=' + id,
            success: function(data) {
                var cdata = eval("(" + data + ")");
                $("#comment-" + cdata.id).html(cdata.content);
                $("#link-" + id).detach();
                if (callback) callback(id);
            }
        });
    }
    -->
</script>


        <%-- ========================================================= --%>
<%-- Save changes and  cancel buttons --%>
<%-- ========================================================= --%>

        <s:submit value="%{getText('commentManagement.update')}" />

    </s:form>

</s:else>
