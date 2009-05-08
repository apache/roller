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

<style type = "text/css">
  .indented {
    margin-left:5%;
    /*padding: 5% 5% 5% 5%;*/
    color: #000;
    width : 95%;}
</style>

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/fonts-min.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/treeview.css'/>" />
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/treeview-min.js'/>"></script>
<!--begin custom header content for this example-->
<!--bring in the folder-style CSS for the TreeView Control-->

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/tree.css'/>" />
<!-- Some custom style for the expand/contract section-->
<style>
#expandcontractdiv {border:1px dotted #dedede; background-color:#EBE4F2; margin:0 0 .5em 0; padding:0.4em;}
#treeDiv1 { background: #fff; padding:1em; margin-top:1em; }
</style>

<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/yahoo-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/connection-min.js'/>"></script>

<p class="subtitle">
   View Uploaded Files
</p>

<s:url id="mediaFileTabularViewURL" action="mediaFileView">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>

<p><s:a href="%{mediaFileTabularViewURL}">Tabular</s:a> | <span style="font-weight:bold">Hierarchical</span></p>

<div id="treeDiv1"></div>

<script type="text/javascript">

hierarchicalViewImpl = function() {

    var tree, currentIconMode;

    function changeIconMode() {
        var newVal = parseInt(this.value);
        if (newVal != currentIconMode) {
            currentIconMode = newVal;
        }
        buildTree();
    }

    function loadNodeData(node, fnLoadComplete)  {

            var nodeLabel = encodeURI(node.label);
            var nodeKey = node.data.key;

            var sUrl = 'mediaFileView!fetchDirectoryContentLight.rol?directoryId=media-directory-id&weblog=<s:property value="actionWeblog.handle" />'.replace("media-directory-id", nodeKey);

            var callback = {
                success: function(oResponse) {
                    var oResults = eval("(" + oResponse.responseText + ")");
                    if((oResults.Result) && (oResults.Result.length)) {
                        //Result is an array if more than one result, string otherwise
                        if(YAHOO.lang.isArray(oResults.Result)) {
                            for (var i=0, j=oResults.Result.length; i<j; i++) {
                                var tempNode = new YAHOO.widget.TextNode(oResults.Result[i], node, false);
                                if (oResults.Result[i].type == 'file') {
                                    tempNode.isLeaf = true;
                                }
                            }
                        } else {
                            //there is only one result; comes as string:
                            var tempNode = new YAHOO.widget.TextNode(oResults.Result, node, false)
                        }
                    }

                    oResponse.argument.fnLoadComplete();
                },

                failure: function(oResponse) {
                    YAHOO.log("Failed to process XHR transaction.", "info", "example");
                    oResponse.argument.fnLoadComplete();
                },

                argument: {
                    "node": node,
                    "fnLoadComplete": fnLoadComplete
                },

                timeout: 7000
            };

            YAHOO.util.Connect.asyncRequest('POST', sUrl, callback);
        }

    function buildTree() {
           tree = new YAHOO.widget.TreeView("treeDiv1");

           tree.setDynamicLoad(loadNodeData, currentIconMode);

           var root = tree.getRoot();

           var aDirectories = [
    <s:iterator id="directory" value="childDirectories" status="dirStatus">
{label:"<s:property value="#directory.name" />",key:"<s:property value="#directory.id" />"}<s:if test="!#dirStatus.last">,</s:if>
    </s:iterator>
           ];

           for (var i=0, j=aDirectories.length; i<j; i++) {
                var tempNode = new YAHOO.widget.TextNode(aDirectories[i], root, false);
           }

           var aFiles = [
    <s:iterator id="mediaFile" value="childFiles" status="fileStatus">
{label:"<s:property value="#mediaFile.name" />",key:"<s:property value="#mediaFile.id" />"}<s:if test="!#fileStatus.last">,</s:if>
    </s:iterator>
           ];

           for (var i=0, j=aFiles.length; i<j; i++) {
                var tempNode = new YAHOO.widget.TextNode(aFiles[i], root, false);
                tempNode.isLeaf = true;
           }

           tree.draw();
        }


    return {
        init: function() {
            YAHOO.util.Event.on(["mode0", "mode1"], "click", changeIconMode);
            var el = document.getElementById("mode1");
            if (el && el.checked) {
                currentIconMode = parseInt(el.value);
            } else {
                currentIconMode = 0;
            }

            buildTree();
        }

    }
} ();

YAHOO.util.Event.onDOMReady(hierarchicalViewImpl.init, hierarchicalViewImpl,true)

</script>

