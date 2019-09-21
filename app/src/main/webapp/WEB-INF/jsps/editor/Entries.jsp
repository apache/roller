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
<%-- Next / previous links --%>

<nav>
    <ul class="pager">
        <s:if test="pager.prevLink != null">
            <li class="previous">
                <a href='<s:property value="pager.prevLink" />'> 
                    <span aria-hidden="true">&larr;</span>Newer</a>
            </li>
        </s:if>
        <s:if test="pager.nextLink != null">
            <li class="next">
                <a href='<s:property value="pager.nextLink"/>'>Older
                    <span aria-hidden="true">&rarr;</span></a>
            </li>
        </s:if>
    </ul>
</nav>


<%-- ============================================================= --%>
<%-- Entry table--%>

<p style="text-align: center">
    <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
    <s:text name="weblogEntryQuery.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <s:text name="weblogEntryQuery.pending" />&nbsp;&nbsp;
    <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <s:text name="weblogEntryQuery.scheduled" />&nbsp;&nbsp;
</p>

<table class="rollertable table table-striped" width="100%">

<tr>
    <th class="rollertable" width="5%"> </th>
    <th class="rollertable" width="5%">
        <s:text name="weblogEntryQuery.pubTime" />
    </th>
    <th class="rollertable" width="5%">
        <s:text name="weblogEntryQuery.updateTime" />
    </th>
    <th class="rollertable">
        <s:text name="weblogEntryQuery.title" />
    </th>
    <th class="rollertable" width="15%">
        <s:text name="weblogEntryQuery.category" />
    </th>
    <th class="rollertable" width="5%"> </th>
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
        <s:url var="editUrl" action="entryEdit">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="bean.id" value="#post.id" />
        </s:url>
        <s:a href="%{editUrl}">
            <span class="glyphicon glyphicon-edit" data-toggle="tooltip" data-placement="top"
                  title="<s:text name='generic.edit'/>">
            </span>
        </s:a>
    </td>

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
        <s:if test="#post.status.name() == 'PUBLISHED'">
            <a href='<s:property value="#post.permalink" />'>
                <str:truncateNicely upper="80"><s:property value="#post.displayTitle" /></str:truncateNicely>
            </a>
        </s:if>
        <s:else>
            <str:truncateNicely upper="80"><s:property value="#post.displayTitle" /></str:truncateNicely>
        </s:else>
    </td>
    
    <td>
        <s:property value="#post.category.name" />
    </td>

    <td>
        <s:set var="postId" value="#post.id" />
        <s:set var="postTitle" value="#post.title" />
        <a href="#"
            onclick="showDeleteModal('<s:property value="postId" />', '<s:property value="postTitle"/>' )">
            <span class="glyphicon glyphicon-trash"
                  data-toggle="tooltip" data-placement="top" title="<s:text name='generic.delete'/>">
            </span>
        </a>
    </td>

    </tr>
</s:iterator>

</table>


<%-- ============================================================= --%>
<%-- Next / previous links --%>

<nav>
    <ul class="pager">
        <s:if test="pager.prevLink != null">
            <li class="previous">
                <a href='<s:property value="pager.prevLink" />'>
                    <span aria-hidden="true">&larr;</span> Older</a>
            </li>
        </s:if>
        <s:if test="pager.nextLink != null">
            <li class="next">
                <a href='<s:property value="pager.nextLink"/>'>Newer
                    <span aria-hidden="true">&rarr;</span></a>
            </li>
        </s:if>
    </ul>
</nav>

<s:if test="pager.items.isEmpty">
    <s:text name="weblogEntryQuery.noneFound" />
</s:if>


<div id="delete-entry-modal" class="modal fade delete-entry-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <s:set var="deleteAction">entryRemoveViaList!remove</s:set>
            
            <s:form action="%{#deleteAction}" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="removeId" id="removeId"/>
            
                <div class="modal-header">
                    <div class="modal-title">
                        <h3><s:text name="weblogEntryRemove.removeWeblogEntry"/></h3>
                        <p><s:text name="weblogEntryRemove.areYouSure"/></p>
                    </div>
                </div>
                
                <div class="modal-body">

                    <div class="form-group">
                        <label class="col-sm-3 control-label">
                            <s:text name="weblogEntryRemove.entryTitle"/>
                        </label>
                        <div class="col-sm-9 controls">
                            <p class="form-control-static" style="padding-top:0px" id="postTitleLabel"></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="col-sm-3 control-label">
                            <s:text name="weblogEntryRemove.entryId"/>
                        </label>
                        <div class="col-sm-9 controls">
                            <p class="form-control-static" style="padding-top:0px" id="postIdLabel"></p>
                        </div>
                    </div>

                </div>
                
                <div class="modal-footer">
                    <s:submit cssClass="btn" value="%{getText('generic.yes')}"/>
                    <button type="button" class="btn btn-default btn-primary" data-dismiss="modal">
                        <s:text name="generic.no" />
                    </button>
                </div>

            </s:form>
            
        </div>

    </div> 
    
</div>

<script>
    function showDeleteModal( postId, postTitle ) {
        $('#postIdLabel').html(postId);
        $('#postTitleLabel').html(postTitle);
        $('#removeId').val(postId);
        $('#delete-entry-modal').modal({show: true});
    }
</script>
