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
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/container-min.js'/>"></script>

<style>
a:link, a:visited, a:hover, a:active     { text-decoration:underline; }
body        {
                margin:0;
                padding:0;
                text-align:left;
            }
h1          {
                font-size:20px;
                font-weight:bold;
            }
.yui-overlay {
                position:fixed;
                background: #ffffff;
                z-index: 112;
                color:#000000;
                border: 4px solid #525252;
                text-align:left;
                top: 50%;
                left: 50%;
        }
</style>
<script type="text/javascript">

YAHOO.example = function() {
        var $D = YAHOO.util.Dom;
        var $E = YAHOO.util.Event;
            return {
                init : function() {
                var overlay_img = new YAHOO.widget.Overlay("overlay_img", { fixedcenter:true,
                                                                            visible:false,
                                                                            width:"577px",height:"530px"
                                                                          });
                overlay_img.render();
                var overlay = document.createElement('div');
                overlay.id = 'overlay';
                // Assign 100% height and width
                overlay.style.width = '100%';
                overlay.style.height = '100%';

                document.getElementsByTagName('body')[0].appendChild(overlay);
                overlay.style.display = 'none';
              }
        };

}();

YAHOO.util.Event.addListener(window, "load", YAHOO.example.init);

function onClose(textForInsertion)
{
        document.getElementById('overlay').style.display = 'none';
        document.getElementById('overlay_img').style.visibility = 'hidden';
        if (textForInsertion && textForInsertion.length > 0) {
            insertAtCursor(document.getElementById('EntryText'), textForInsertion);
        }
}

function insertAtCursor(textAreaElement, valueForInsertion) {
    if (document.selection) {
         textAreaElement.focus();
         var range = document.selection.createRange();
         range.text = valueForInsertion;
    }
    else if (textAreaElement.selectionStart || textAreaElement.selectionStart == '0') {

        var preText;
        var postText;
        if (textAreaElement.selectionStart == 0) {
            preText = '';
            postText = '';
        }
        else {
            preText = textAreaElement.value.substring(0, textAreaElement.selectionStart);
            postText = textAreaElement.value.substring(textAreaElement.selectionEnd, textAreaElement.value.length);
        }
        textAreaElement.value =  preText + valueForInsertion + postText;
        textAreaElement.selectionStart = preText.length + valueForInsertion.length;
        textAreaElement.selectionEnd = textAreaElement.selectionStart;
        textAreaElement.focus();
    } else {
        textAreaElement.value += valueForInsertion;
        textAreaElement.focus();
    }
}


function onClickAdd(){
        var browser=navigator.appName;
        document.getElementById("overlay_img").style.visibility = "visible";
        document.getElementById('overlay').style.display = 'block';
        document.getElementById("overlay_img").style.width = "650px";
        document.getElementById("overlay_img").style.height = "550px";
        document.getElementById("overlay_img").style.top = "40px";


                var frame = document.createElement('iframe');
                frame.setAttribute("id","myframe");
                frame.setAttribute("frameborder","no");
                frame.setAttribute("scrolling","auto");

                frame.setAttribute('src','<s:url action="mediaFileAdd" namespace="overlay"><s:param name="weblog"    value="%{actionWeblog.handle}" /></s:url>' );
                frame.style.width="100%";
                frame.style.height="100%";
                if (browser=="Microsoft Internet Explorer")
                {
                document.getElementById("overlay_img").style.top= "40px";
                document.getElementById("overlay_img").style.left= "170px";
                }
                document.getElementById("overlay_img").innerHTML = '<div ><a href="#" class="container-close" onclick="onClose()"></a></div>';
                document.getElementById("overlay_img").appendChild(frame);
}

function onClickAddFromUpload(){
        var browser=navigator.appName;
        document.getElementById("overlay_img").style.visibility = "visible";
        document.getElementById('overlay').style.display = 'block';
        document.getElementById("overlay_img").style.width = "650px";
        document.getElementById("overlay_img").style.height = "500px";
        document.getElementById("overlay_img").style.top = "40px";
                var frame = document.createElement('iframe');
                frame.setAttribute("id","myframe");
                frame.setAttribute("frameborder","no");
                frame.setAttribute("scrolling","auto");
                frame.setAttribute('src','<s:url action="mediaFileSearch" namespace="overlay"><s:param name="weblog" value="%{actionWeblog.handle}" /></s:url>' );
                frame.style.width="100%";
                frame.style.height="100%";
                if (browser=="Microsoft Internet Explorer")
                {
                document.getElementById("overlay_img").style.top= "40px";
                document.getElementById("overlay_img").style.left= "170px";
                }

                document.getElementById("overlay_img").innerHTML = '<div ><a href="#" class="container-close" onclick="onClose()"></a></div>';
                document.getElementById("overlay_img").appendChild(frame);
}

function onClickAddExternal(){
        var browser=navigator.appName;
        document.getElementById("overlay_img").style.visibility = "visible";
        document.getElementById('overlay').style.display = 'block';
        document.getElementById("overlay_img").style.width = "515px";
        document.getElementById("overlay_img").style.height = "400px";
        document.getElementById("overlay_img").style.top = "100px";

                var frame = document.createElement('iframe');
                frame.setAttribute("id","myframe");
                frame.setAttribute("frameborder","no");
                frame.setAttribute("scrolling","auto");
                frame.setAttribute('src','<s:url action="mediaFileAddExternalInclude"><s:param name="weblog" value="%{actionWeblog.handle}" /></s:url>' );
                frame.style.width="100%";
                frame.style.height="100%";
                if (browser=="Microsoft Internet Explorer")
                {
                document.getElementById("overlay_img").style.top= "40px";
                document.getElementById("overlay_img").style.left= "170px";
                }
                document.getElementById("overlay_img").innerHTML = '<div ><a href="#" class="container-close" onclick="onClose()"></a></div>';
                document.getElementById("overlay_img").appendChild(frame);
}

</script>

<script type="text/javascript">
<!--
function editorCleanup() {
    // no-op
}
function changeSize(e, num) {
    a = e.rows + num;
    if (a > 0) e.rows = a;
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie("editorSize",e.rows,expires);
}
function changeSize1(e, num) {
    a = e.rows + num;
    if (a > 0) e.rows = a;
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie("editorSize1",e.rows,expires);
}
// -->
</script>

<div id="overlay_img" style="visibility:hidden"> </div>

<%-- ===================================================================== --%>
<p class="toplabel">
    <span style="float:left;"><s:text name="weblogEdit.content" /></span>
    <span style="font-weight:normal;float:right;">
        <a href="#" onClick="onClickAdd();";>Add media </a>&nbsp;
            <a href="#" onClick="onClickAddFromUpload();">Attach uploaded file</a>&nbsp;
            <a href="#" onClick="onClickAddExternal();">Add media from URL</a>
    </span>
</p>

<s:textarea id="EntryText" name="bean.text" cols="75" rows="25" cssStyle="width: 100%" tabindex="5"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize1") != null) {
        document.getElementById('entry_bean_text').rows = getCookie("editorSize1");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize1(document.getElementById('entry_bean_text'), 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize1(document.getElementById('entry_bean_text'), -5)" />
</td></tr></table>

<%-- ===================================================================== --%>
<p class="toplabel"><s:text name="weblogEdit.summary" /></p>

<s:textarea name="bean.summary" cols="75" rows="5" cssStyle="width: 100%" tabindex="6"/>
<script type="text/javascript">
    <!--
    if (getCookie("editorSize") != null) {
        document.getElementById('entry_bean_summary').rows = getCookie("editorSize");
    }
    -->
</script>
<table style="width:100%"><tr><td align="right">
  <!-- Add buttons to make this textarea taller or shorter -->
  <input type="button" name="taller" value=" &darr; " onclick="changeSize(document.getElementById('entry_bean_summary'), 5)" />
  <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(document.getElementById('entry_bean_summary'), -5)" />
</td></tr></table>
