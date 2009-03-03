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

<script type="text/javascript">
<!--
function onDelete(id)
{
    document.mediaFileViewForm.mediaFileId.value=id;
    document.mediaFileViewForm.action='<s:url action="mediaFileView!delete" />';
    document.mediaFileViewForm.submit();
}

function onIncludeInGallery(id)
{
    document.mediaFileViewForm.mediaFileId.value=id;
    document.mediaFileViewForm.action='<s:url action="mediaFileView!includeInGallery" />';
    document.mediaFileViewForm.submit();
}

function onSelectDirectory(id) {
    document.mediaFileViewForm.directoryId.value = id;
    document.mediaFileViewForm.submit();
}

function onCreateDirectory()
{
    document.mediaFileViewForm.action='<s:url action="mediaFileView!createNewDirectory" />';
    document.mediaFileViewForm.submit();
}

function onDeleteSelected()
{
    if ( confirm("<s:text name='mediaFile.delete.confirm' />") ) {
        document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteSelected" />';
        document.mediaFileViewForm.submit();
    }
}

function onMoveSelected()
{
    if ( confirm("<s:text name='mediaFile.move.confirm' />") ) {
        document.mediaFileViewForm.action='<s:url action="mediaFileView!moveSelected" />';
        document.mediaFileViewForm.submit();
    }
}
-->
</script>

<p class="subtitle">
   View Uploaded Files
</p>
<p class="subtitle">
Path: /
<s:iterator id="directory" value="currentDirectoryHierarchy">
    <s:url id="getDirectoryByPathUrl" action="mediaFileView">
        <s:param name="directoryPath" value="#directory.key" />
        <s:param name="weblog" value="%{actionWeblog.handle}" />
    </s:url>
    <s:a href="%{getDirectoryByPathUrl}"><s:property value="#directory.value" /></s:a> /
</s:iterator>
</p>

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" onsubmit="editorCleanup()">
<s:url id="mediaFileHierarchicalViewURL" action="mediaFileHierarchicalView">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>
<p><span style="font-weight:bold">Tabular</span> | <s:a href="%{mediaFileHierarchicalViewURL}">Hierarchical</s:a></p>
 <div class="control">
    <span style="padding-left:20px">Sort by:</span>
    <s:select name="sortBy" list="sortOptions" listKey="key" listValue="value" onchange="document.mediaFileViewForm.submit();" />
	</span>
 </div>

    <s:hidden name="weblog" />
    <s:hidden name="directoryId" />
    <input type="hidden" name="mediaFileId" value="" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <div style="margin-top:10px">
	<ul>

    <s:iterator id="directory" value="childDirectories">
	<li>
	<img border="0" src='<s:url value="/images/folder.png"/>' style="cursor:pointer;width:120px;height:100px" alt="mediaFolder.png" onclick="onSelectDirectory('<s:property value="#directory.id"/>')"/>
    <label><s:property value="#directory.name" /></label>
	</li>
    </s:iterator>

    <s:iterator id="mediaFile" value="childFiles">
	<li>
	<s:if test="#mediaFile.imageFile">
    <s:url id="mediaFileURL" value="/roller-ui/rendering/media-resources/%{#mediaFile.id}"></s:url>
	</s:if>
	<s:else>
    <s:url id="mediaFileURL" value="/images/page.png"></s:url>
	</s:else>
	<img border="0" src='<s:property value="%{mediaFileURL}" />' width="120px" />
    <label><s:property value="#mediaFile.name" /></label>
    <input type="checkbox" name="selectedMediaFiles" value="<s:property value="#mediaFile.id"/>" style="float:left;clear:left"/>
    <a href='<s:url action="mediaFileEdit"><s:param name="mediaFileId" value="%{#mediaFile.id}" /><s:param name="weblog" value="%{actionWeblog.handle}" /></s:url>'>Edit</a>
	<a href="#" onclick="onDelete('<s:property value="#mediaFile.id" />')">Delete</a>
	<a href="#" onclick="onIncludeInGallery('<s:property value="#mediaFile.id" />')">Include in gallery</a>
	</li>
    </s:iterator>

</ul>
</div>

<div style="margin-left:320px">
New Directory:
<input type="text" name="newDirectoryName" size="30" />
<input type="button" value="Create" onclick="onCreateDirectory()" />
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


