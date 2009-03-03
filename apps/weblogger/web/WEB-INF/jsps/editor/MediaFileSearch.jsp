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
<%-- JavaScript for media file search page--%>
<script type="text/javascript">
<!--

function onDelete(id)
{
    document.mediaFileSearchForm.mediaFileId.value=id;
    document.mediaFileSearchForm.action='<s:url action="mediaFileSearch!delete" />';
    document.mediaFileSearchForm.submit();
}

function onIncludeInGallery(id)
{
    document.mediaFileSearchForm.mediaFileId.value=id;
    document.mediaFileSearchForm.action='<s:url action="mediaFileSearch!includeInGallery" />';
    document.mediaFileSearchForm.submit();
}


function onDeleteSelected()
{
    if ( confirm("<s:text name='mediaFile.delete.confirm' />") ) {
        document.mediaFileSearchForm.action='<s:url action="mediaFileSearch!deleteSelected" />';
        document.mediaFileSearchForm.submit();
    }
}

function onMoveSelected()
{
    if ( confirm("<s:text name='mediaFile.move.confirm' />") ) {
        document.mediaFileSearchForm.action='<s:url action="mediaFileSearch!moveSelected" />';
        document.mediaFileSearchForm.submit();
    }
}
function onCreateDirectory()
{
    document.mediaFileSearchForm.action='<s:url action="mediaFileSearch!createDirByPath" />';
    document.mediaFileSearchForm.submit();
}
function onNext()
{
    document.mediaFileSearchForm["bean.pageNum"].value = parseInt(document.mediaFileSearchForm["bean.pageNum"].value) +  1;
    document.mediaFileSearchForm.submit();
}
function onPrevious()
{
    document.mediaFileSearchForm["bean.pageNum"].value = parseInt(document.mediaFileSearchForm["bean.pageNum"].value) -  1;
    document.mediaFileSearchForm.submit();
}
-->
</script>

<p class="subtitle">
   Search uploaded files
</p>

	<s:form id="mediaFileSearchForm" name="mediaFileSearchForm" action="mediaFileSearch!search" onsubmit="editorCleanup()">
    <s:hidden name="weblog" />
    <input type="hidden" name="mediaFileId" value="" />
    <table class="mediaFileSearchTable" cellpadding="0" cellspacing="0" width="100%">
        <tr>
            <td>
                <label for="name">Name</label>
            </td>
            <td>
                <s:textfield name="bean.name" size="40" maxlength="255" />
            </td>
            <td>
                <label for="type">File Type</label>
            </td>
            <td>
                <s:select name="bean.type" list="fileTypes" />
            </td>
        </tr>
        <tr>
            <td>
                <label for="size">Size</label>
            </td>
            <td>
                <s:select name="bean.sizeFilterType" list="sizeFilterTypes" listKey="key" listValue="value" />
                <s:textfield name="bean.size" size="10" maxlength="15" />
                <s:select name="bean.sizeUnit" list="sizeUnits" listKey="key" listValue="value" />
            </td>
            <td>
                <label for="tags">Tags</label>
            </td>
            <td>
                <s:textfield name="bean.tags" size="20	" maxlength="50" />
            </td>
        </tr>
        <tr>
            <td>
                <input type="submit" name="search" value="Search" />
            </td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
            <td>&nbsp;</td>
        </tr>
     </table>

 <div class="control">
    <span style="padding-left:20px">Sort by:</span>
    <s:select name="bean.sortOption" list="sortOptions" listKey="key" listValue="value" />
	<s:if test="!pager.justOnePage">
	<span style="padding-left:300px">
	<s:if test="pager.hasPrevious()"><a href="#" onclick="onPrevious()">&lt;Previous</a></s:if>
	<s:if test="pager.hasNext()"><a href="#" onclick="onNext()">Next&gt;</a></s:if>
	<span>
	</s:if>
    </div>

    <s:hidden name="bean.pageNum" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <ul style="margin-top:10px">

    <s:iterator id="mediaFile" value="pager.items">
    <li class="mediaFileSearchResult">
	<img border="0" src='<s:url value="/roller-ui/rendering/media-resources/%{#mediaFile.id}" />' width="120px" alt="mediaFolder.png"/><br/>
	<input type="checkbox" name="selectedMediaFiles" value="<s:property value="#mediaFile.id"/>" style="float:left;clear:left"/>
    <label><s:property value="#mediaFile.name" /></label>
    <label><s:property value="#mediaFile.path" /></label>
	<a href='<s:url action="mediaFileEdit"><s:param name="mediaFileId" value="%{#mediaFile.id}" /><s:param name="weblog" value="%{actionWeblog.handle}" /></s:url>'>Edit</a>
	<a href="#" onclick="onDelete('<s:property value="#mediaFile.id" />')">Delete</a>
	<a href="#" onclick="onIncludeInGallery('<s:property value="#mediaFile.id" />')">Include in gallery</a>
    </li>
    </s:iterator>
    </ul>


<br/>
<div style="width: 100%; clear:both; padding-top:2em">
<label>New Directory:</label>
<input type="text" name="newDirectoryPath" size="30" />
<input type="button" value="Create" onclick="onCreateDirectory()" />
</div>

</div>





    <%-- ================================================================== --%>
    <%-- Weblog edit or preview --%>






    <%-- ================================================================== --%>
    <%-- plugin chooser --%>




    <%-- ================================================================== --%>
    <%-- advanced settings  --%>


    <%-- ================================================================== --%>
    <%-- the button box --%>

	<br/>
	<div class="control">
     <input type="button" style="padding-left:20px" value="Delete Selected" onclick="onDeleteSelected()" />
     <input type="button" style="padding-left:20px" value="Move Selected" onclick="onMoveSelected()" />
	 <span style="padding-left:20px">
         <s:select name="selectedDirectory" list="allDirectories" listKey="id" listValue="path" />
	 </span>
    </div>
	</s:form>


