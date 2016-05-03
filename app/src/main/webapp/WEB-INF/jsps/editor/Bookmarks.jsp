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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.1.1.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    confirmLabel: '<s:text name="generic.confirm"/>',
    saveLabel: '<s:text name="generic.save"/>',
    cancelLabel: '<s:text name="generic.cancel"/>',
    editTitle: '<s:text name="generic.edit"/>',
    addTitle: '<s:text name="bookmarkForm.add.title"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/bookmarks.js'/>"></script>

<p class="subtitle">
    <s:text name="bookmarksForm.subtitle" >
        <s:param value="weblog" />
    </s:text>
</p>
<p class="pagetip">
    <s:text name="bookmarksForm.rootPrompt" />
</p>

<%-- Form is a table of bookmarks, each with checkbox for deleting --%>
<s:form id="bookmarksForm">
  <s:hidden name="salt" />
  <s:hidden name="weblog" id="actionWeblog" />

    <table class="rollertable">

        <tr class="rHeaderTr">
            <th class="rollertable" width="5%"><input name="control" type="checkbox" onclick="toggleFunctionAll(this.checked);"
                title="<s:text name="bookmarksForm.selectAllLabel"/>"/></th>
            <th class="rollertable" width="25%"><s:text name="generic.name" /></th>
            <th class="rollertable" width="25%"><s:text name="bookmarksForm.url" /></th>
            <th class="rollertable" width="35%"><s:text name="generic.description" /></th>
            <th class="rollertable" width="5%"><s:text name="generic.edit" /></th>
            <th class="rollertable" width="5%"><s:text name="bookmarksForm.visitLink" /></th>
        </tr>

        <s:if test="weblogObj.bookmarks.size > 0">

        <%-- Bookmarks --%>
        <s:iterator id="bookmark" value="weblogObj.bookmarks" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>

                <td class="rollertable center" style="vertical-align:middle">
                    <input type="checkbox" name="selectedBookmarks"
                    title="<s:text name="bookmarksForm.selectOneLabel"><s:param value="#bookmark.name"/></s:text>"
                    value="<s:property value="#bookmark.id"/>" />
                </td>

                <td class="rollertable" id='bkname-<s:property value="#bookmark.id"/>'><s:property value="#bookmark.name"/></td>

                <td class="rollertable" id='bkurl-<s:property value="#bookmark.id"/>'><s:property value="#bookmark.url"/></td>

                <td class="rollertable" id='bkdescription-<s:property value="#bookmark.id"/>'><s:property value="#bookmark.description" /></td>

                <td class="rollertable" align="center">
                    <a href="#" class="edit-link" id='bkid-<s:property value="#bookmark.id"/>' data-id='<s:property value="#bookmark.id"/>'><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon"
                             title="<s:text name='bookmarksForm.edit.tip' />"/></a>
                </td>

                <td class="rollertable" align="center">
                    <s:if test="#bookmark.url != null" >
                        <a href="<s:property value="#bookmark.url" />">
                            <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon" title="<s:text name='bookmarksForm.visitLink.tip' />" />
                        </a>
                    </s:if>
                </td>

            </tr>
        </s:iterator>

        </s:if>
        <s:else>
            <tr>
                <td style="vertical-align:middle" colspan="7"><s:text name="bookmarksForm.noresults" /></td>
            </tr>
        </s:else>
    </table>

    <div class="control clearfix">
        <input type="submit" value="<s:text name='bookmarksForm.addBookmark'/>" id="add-link" formaction='<s:url action="bookmarks"/>'/>

        <s:if test="weblogObj.bookmarks.size > 0">
            <%-- Delete-selected button --%>
            <input type="submit" value="<s:text name='bookmarksForm.delete'/>" id="delete-link" formaction='<s:url action="bookmarks!delete"/>' />
        </s:if>
    </div>

</s:form>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='bookmarksForm.delete.confirm' /></p>
</div>

<div id="bookmark-edit" style="display:none">
    <span id="bookmark-edit-error" style="display:none"><s:text name='bookmarkForm.error.duplicateName'/></span>
    <p class="pagetip">
        <s:text name="bookmarkForm.requiredFields">
            <s:param><s:text name="generic.name"/></s:param>
            <s:param><s:text name="bookmarkForm.url"/></s:param>
        </s:text>
    </p>
    <form>
    <table>
        <tr>
            <td style="width:30%"><label for="bookmark-edit-name"><s:text name='generic.name'/></label></td>
            <td><input id="bookmark-edit-name" maxlength="80" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-url"><s:text name='bookmarkForm.url'/></label></td>
            <td><input id="bookmark-edit-url" maxlength="120" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-description"><s:text name='generic.description'/></label></td>
            <td><input id="bookmark-edit-description" maxlength="120" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </table>
    </form>
</div>
