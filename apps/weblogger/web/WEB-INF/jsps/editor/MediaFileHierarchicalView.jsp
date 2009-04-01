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

<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/fonts/fonts-min.css" />
<link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.6.0/build/treeview/assets/skins/sam/treeview.css" />
<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="http://yui.yahooapis.com/2.6.0/build/treeview/treeview-min.js"></script>


<!--begin custom header content for this example-->
<!--bring in the folder-style CSS for the TreeView Control-->
<link rel="stylesheet" type="text/css" href="http://developer.yahoo.com/yui/examples/treeview/assets/css/folders/tree.css">

<!-- Some custom style for the expand/contract section-->
<style>
#expandcontractdiv {border:1px dotted #dedede; background-color:#EBE4F2; margin:0 0 .5em 0; padding:0.4em;}
#treeDiv1 { background: #fff; padding:1em; margin-top:1em; }
</style>

<script src="http://yui.yahooapis.com/2.2.2/build/yahoo/yahoo-min.js" type="text/javascript"></script>
<script src="http://yui.yahooapis.com/2.2.2/build/connection/connection-min.js" type="text/javascript"></script>

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

            //We'll load node data based on what we get back when we
            //use Connection Manager topass the text label of the
            //expanding node to the Yahoo!
            //Search "related suggestions" API.  Here, we're at the
            //first part of the request -- we'll make the request to the
            //server.  In our success handler, we'll build our new children
            //and then return fnLoadComplete back to the tree.

            //Get the node's label and urlencode it; this is the word/s
            //on which we'll search for related words:
            //alert(node.label);
            //alert(node.data.key);
            var nodeLabel = encodeURI(node.label);
            var nodeKey = node.data.key;

            //prepare URL for XHR request:
            var sUrl = 'mediaFileView!fetchDirectoryContentLight.rol?directoryId=media-directory-id&weblog=<s:property value="actionWeblog.handle" />'.replace("media-directory-id", nodeKey);




            //prepare our callback object
            var callback = {

                //if our XHR call is successful, we want to make use
                //of the returned data and create child nodes.
                success: function(oResponse) {
                    YAHOO.log("XHR transaction was successful.", "info", "example");
                    //YAHOO.log(oResponse.responseText);
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

                    //When we're done creating child nodes, we execute the node's
                    //loadComplete callback method which comes in via the argument
                    //in the response object (we could also access it at node.loadComplete,
                    //if necessary):
                    oResponse.argument.fnLoadComplete();
                },

                //if our XHR call is not successful, we want to
                //fire the TreeView callback and let the Tree
                //proceed with its business.
                failure: function(oResponse) {
                    YAHOO.log("Failed to process XHR transaction.", "info", "example");
                    oResponse.argument.fnLoadComplete();
                },

                //our handlers for the XHR response will need the same
                //argument information we got to loadNodeData, so
                //we'll pass those along:
                argument: {
                    "node": node,
                    "fnLoadComplete": fnLoadComplete
                },

                //timeout -- if more than 7 seconds go by, we'll abort
                //the transaction and assume there are no children:
                timeout: 7000
            };

            //With our callback object ready, it's now time to
            //make our XHR call using Connection Manager's
            //asyncRequest method:
            YAHOO.util.Connect.asyncRequest('POST', sUrl, callback);
        }

        function buildTree() {
           //create a new tree:
           tree = new YAHOO.widget.TreeView("treeDiv1");

           //turn dynamic loading on for entire tree:
           tree.setDynamicLoad(loadNodeData, currentIconMode);

           //get root node for tree:
           var root = tree.getRoot();

           //add child nodes for tree; our top level nodes are
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
           // Use the isLeaf property to force the leaf node presentation for a given node.
           // This disables dynamic loading for the node.
                tempNode.isLeaf = true;
           }

           //render tree with these toplevel nodes; all descendants of these nodes
           //will be generated as needed by the dynamic loader.
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

//once the DOM has loaded, we can go ahead and set up our tree:
YAHOO.util.Event.onDOMReady(hierarchicalViewImpl.init, hierarchicalViewImpl,true)

</script>

