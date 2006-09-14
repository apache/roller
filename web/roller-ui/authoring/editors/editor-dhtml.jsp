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
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/taglibs.jsp" %>
<html:hidden property="summary" />
<html:hidden property="text" />
    
<script language="JavaScript">
<!--
    // dhtml editor requires IE 6
    var agt=navigator.userAgent.toLowerCase();
    msie = agt.indexOf("msie");
    ie   = (msie != -1);
    major = parseInt(navigator.appVersion);
    if (ie)
    {
        major = parseInt( agt.substring( msie+5 ) );
    }
    if (!ie || major < 6)
    {
        document.write("<span class=\"statusMsg\">");
        document.write("We recommend ");
        document.write("<a href=\"http://www.microsoft.com/windows/ie/default.asp\">IE 6 or higher</a> ");
        document.write("for using the dHtml editor. ")
        document.write("Please <a href=\"");
        document.write("roller-ui/authoring/website.do?method=edit&rmik=Settings\">");
        document.write("change to the text editor</a> or upgrade ");
        document.write("your browser.</span>");
    }
    
    function postWeblogEntry(publish)
    {
        document.weblogEntryFormEx.text.value = iView.document.body.innerHTML;
        if (publish)
            document.weblogEntryFormEx.publishEntry.value = "true";
        document.weblogEntryFormEx.submit();
    }

    var viewMode = 1; // WYSIWYG

    function selOn(ctrl)
    {
        ctrl.style.borderColor = '#000000';
        ctrl.style.backgroundColor = '#B5BED6';
        ctrl.style.cursor = 'hand'; 
    }

    function selOff(ctrl)
    {
        ctrl.style.borderColor = '#D6D3CE';  
        ctrl.style.backgroundColor = '#D6D3CE';
    }

    function selDown(ctrl)
    {
        ctrl.style.backgroundColor = '#8492B5';
    }

    function selUp(ctrl)
    {
        ctrl.style.backgroundColor = '#B5BED6';
    }

    function doBold()
    {
        iView.document.execCommand('bold', false, null);
    }

    function doItalic()
    {
        iView.document.execCommand('italic', false, null);
    }

    function doUnderline()
    {
        iView.document.execCommand('underline', false, null);
    }

    function doLeft()
    {
        iView.document.execCommand('justifyleft', false, null);
    }

    function doCenter()
    {
        iView.document.execCommand('justifycenter', false, null);
    }

    function doRight()
    {
        iView.document.execCommand('justifyright', false, null);
    }

    function doOrdList()
    {
        iView.document.execCommand('insertorderedlist', false, null);
    }

    function doBulList()
    {
        iView.document.execCommand('insertunorderedlist', false, null);
    }

    function doForeCol()
    {
        var fCol = prompt('Enter foreground color', '');

        if(fCol != null)
            iView.document.execCommand('forecolor', false, fCol);
    }

    function doBackCol()
    {
        var bCol = prompt('Enter background color', '');

        if(bCol != null)
            iView.document.execCommand('backcolor', false, bCol);
    }

    function doLink()
    {
        iView.document.execCommand('createlink');
    }

    function doImage()
    {
        var imgSrc = prompt('Enter image location', '');

        if(imgSrc != null)    
            iView.document.execCommand('insertimage', false, imgSrc);
    }

    function doRule()
    {
        iView.document.execCommand('inserthorizontalrule', false, null);
    }

    function doFont(fName)
    {
        if(fName != '')
            iView.document.execCommand('fontname', false, fName);
    }

    function doSize(fSize)
    {
        if(fSize != '')
            iView.document.execCommand('fontsize', false, fSize);
    }

    function doHead(hType)
    {
        if(hType != '')
        {
            iView.document.execCommand('formatblock', false, hType);  
            doFont(selFont.options[selFont.selectedIndex].value);
        }
    }

    function doToggleView()
    {  
        if(viewMode == 1)
        {
            iHTML = iView.document.body.innerHTML;
            iView.document.body.innerText = iHTML;

            // Hide all controls
            tblCtrls.style.display = 'none';
            tblCtrls2.style.display = 'none';
            //selFont.style.display = 'none';
            //selSize.style.display = 'none';
            //selHeading.style.display = 'none';
            iView.focus();

            viewMode = 2; // Code
        }
        else
        {
            iText = iView.document.body.innerText;
            iView.document.body.innerHTML = iText;

            // Show all controls
            tblCtrls.style.display = 'inline';
            tblCtrls2.style.display = 'inline';
            //selFont.style.display = 'inline';
            //selSize.style.display = 'inline';
            //selHeading.style.display = 'inline';
            iView.focus();

            viewMode = 1; // WYSIWYG
        }
    }
// -->
</script>
<style>

    .butClass
    {    
        border: 1px solid;
        border-color: #D6D3CE;
    }

    .tdClass
    {
        padding-left: 3px;
        padding-top:3px;
    }

</style>

<table width="95%" height="30px" border="0" cellspacing="0" cellpadding="0" bgcolor="#D6D3CE">
<tr>
    <td id="tblCtrls" class="tdClass">
        <img alt="Bold" class="butClass" src="<%= request.getContextPath() %>/images/editor/bold.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBold()">
        <img alt="Italic" class="butClass" src="<%= request.getContextPath() %>/images/editor/italic.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doItalic()">
        <img alt="Underline" class="butClass" src="<%= request.getContextPath() %>/images/editor/underline.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doUnderline()">

        <img alt="Left" class="butClass" src="<%= request.getContextPath() %>/images/editor/left.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doLeft()">
        <img alt="Center" class="butClass" src="<%= request.getContextPath() %>/images/editor/center.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doCenter()">
        <img alt="Right" class="butClass" src="<%= request.getContextPath() %>/images/editor/right.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doRight()">

        <img alt="Ordered List" class="butClass" src="<%= request.getContextPath() %>/images/editor/ordlist.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doOrdList()">
        <img alt="Bulleted List" class="butClass" src="<%= request.getContextPath() %>/images/editor/bullist.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBulList()">

        <img alt="Text Color" class="butClass" src="<%= request.getContextPath() %>/images/editor/forecol.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doForeCol()">
        <img alt="Background Color" class="butClass" src="<%= request.getContextPath() %>/images/editor/bgcol.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doBackCol()">

        <img alt="Hyperlink" class="butClass" src="<%= request.getContextPath() %>/images/editor/link.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doLink()">
        <img alt="Image" class="butClass" src="<%= request.getContextPath() %>/images/editor/image.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doImage()">
        <img alt="Horizontal Rule" class="butClass" src="<%= request.getContextPath() %>/images/editor/rule.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doRule()">

    </td>
    <td class="tdClass" colspan="1" width="20%" align="right">
      <img alt="Toggle Mode" class="butClass" src="<%= request.getContextPath() %>/images/editor/mode.gif" onMouseOver="selOn(this)" onMouseOut="selOff(this)" onMouseDown="selDown(this)" onMouseUp="selUp(this)" onClick="doToggleView()">
      &nbsp;&nbsp;&nbsp;
    </td>
</tr>
</table>

<table width="95%" height="30px" border="0" cellspacing="0" cellpadding="0" bgcolor="#D6D3CE">
<tr>
    <td id="tblCtrls2" class="tdClass" colspan="1" width="80%">
      <select id="selFont" onChange="doFont(this.options[this.selectedIndex].value)">
        <option value="">-- Font --</option>
        <option value="Arial">Arial</option>
        <option value="Courier">Courier</option>
        <option value="Sans Serif">Sans Serif</option>
        <option value="Tahoma">Tahoma</option>
        <option value="Verdana">Verdana</option>
        <option value="Wingdings">Wingdings</option>
      </select>
      <select id="selSize" onChange="doSize(this.options[this.selectedIndex].value)">
        <option value="">-- Size --</option>
        <option value="1">Very Small</option>
        <option value="2">Small</option>
        <option value="3">Medium</option>
        <option value="4">Large</option>
        <option value="5">Larger</option>
        <option value="6">Very Large</option>
      </select>
      <select id="selHeading" onChange="doHead(this.options[this.selectedIndex].value)">
        <option value="">-- Heading --</option>
        <option value="Heading 1">H1</option>
        <option value="Heading 2">H2</option>
        <option value="Heading 3">H3</option>
        <option value="Heading 4">H4</option>
        <option value="Heading 5">H5</option>
        <option value="Heading 6">H6</option>
      </select>
    </td>
</tr>

</table>

<% // hack to support iframe's src attribute doing a call out 
// No longer needed because form is now session scope
//session.setAttribute("weblogEntryFormEx", 
    //request.getAttribute("weblogEntryFormEx")); 
%>
<table width="95%" border="0" cellspacing="0" cellpadding="0" >
<tr>
    <td><iframe id="iView" width="100%" height="250" tabindex="2">
        Your browser does not support iframes.  
        Either change browsers or select a 
        different editor for your website.</iframe>
    </td>
</tr>
</table>
<script language="JavaScript">
    iView.document.designMode = 'On';
    iView.document.open();
    iView.document.write(document.weblogEntryFormEx.text.value);
    iView.document.close();
</script>

