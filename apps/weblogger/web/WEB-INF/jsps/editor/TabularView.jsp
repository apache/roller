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

<p class="subtitle">
   View Uploaded Files
</p>
<p><span style="font-weight:bold">Tabular</span>| Hierarchical</p>
 <div class="control">
    <span style="padding-left:20px">Sort by:</span>  
	<select>
	<option value="name">Name</option>
	<option value="Date Modified">Date Modified</option>
	<option value="Type">Type</option>
	</select>
	<span style="padding-left:300px">
	<a href="">&lt;&lt;First</a>
	<a href="">&lt;Previous</a>
	<a href="">1</a>
	<a href="">2</a>
	<a href="">3</a>
	<a href="">4</a>
	<a href="">Next&gt;</a>
	<a href="">Last&gt;&gt;</a>
    </div>
    
	<s:form id="entry" action="addMedia!submit" onsubmit="editorCleanup()">
    <s:hidden name="weblog" />


    
    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>
    
    <div style="margin-top:10px">
	<table border="0" cellspacing="30">
	<tr>
	<td>
	<img border="0" src='<s:url value="/images/mediaFolder.png"/>' width=120px alt="mediaFolder.png"/><br/>
    <label>Vegas Trip</label><br/>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/mediaFolder.png"/>' width=120px alt="mediaFolder.png"/><br/>
    <label>Yosemite Trip</label><br/>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/mediaFolder.png"/>' width=120px alt="mediaFolder.png"/><br/>
    <label>New York Trip Videos</label><br/>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/Winter.png"/>' width=120px alt="Winter.png"/><br/>
    <label>Winter.png</label><br/>
	<input type="checkbox" name="pic1" />
	<span style="padding-left:20px"<a href="">Edit</a>
	<a href="">More...</a></span>
	</td>
	</tr>
	<tr>
	<td>
	<img border="0" src='<s:url value="/images/library.png"/>' width=120px alt="Library.png"/><br/>
    <label>Library.png</label><br/>
	<input type="checkbox" name="pic2" />
	<span style="padding-left:20px"<a href="">Edit</a>
	<a href="">More...</a></span>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/cake1.png"/>' width=120px alt="cake1.png"/><br/>
    <label>Cake1.png</label><br/>
	<input type="checkbox" name="pic3" />
	<span style="padding-left:20px"<a href="">Edit</a>
	<a href="">More...</a></span>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/cake2.png"/>' width=120px alt="cake2.png"/><br/>
    <label>Cake2.png</label><br/>
	<input type="checkbox" name="pic4" />
	<span style="padding-left:20px"<a href="">Edit</a>
	<a href="">More...</a></span>
	</td>
	<td>
	<img border="0" src='<s:url value="/images/cake3.png"/>' width=120px alt="cake3.png"/><br/>
    <label>Cake3.png</label><br/>
	<input type="checkbox" name="pic5" />
	<span style="padding-left:20px"<a href="">Edit</a>
	<a href="">More...</a></span>
	</td>
	</tr>
</table>
</div>

<div style="margin-left:320px">
New Directory: 
<input type="text" size="30" />
<input type="button" name="create" value="Create" />
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
       <input type="button" value="Delete Selected" name="delete" />
	 <span style="padding-left:20px" <input type="button" value="Move Selected" name="move" /></span>
	 <span style="padding-left:20px" <select> <option name="vegas">Vegas Trip</option>
	 <option name="vegas">New York Trip Videos</option>
	 <option name="vegas">Yosemite Trip</option>
	 </select></span>
    </div>
    
    
   
	</s:form>
    

