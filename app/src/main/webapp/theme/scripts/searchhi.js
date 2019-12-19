/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
/* http://www.kryogenix.org/code/browser/searchhi/ */
/* Modified 20021006 to fix query string parsing and add case insensitivity */
function highlightWord(node,word) {
	// Iterate into this nodes childNodes
	if (node.hasChildNodes) {
		var hi_cn;
		for (hi_cn=0;hi_cn<node.childNodes.length;hi_cn++) {
			highlightWord(node.childNodes[hi_cn],word);
		}
	}

	// And do this node itself
	if (node.nodeType == 3) { // text node
		var tempNodeVal = node.nodeValue.toLowerCase();
		var tempWordVal = word.toLowerCase();
		if (tempNodeVal.indexOf(tempWordVal) != -1) {
			var pn = node.parentNode;
			if (pn.className != "searchword") {
				// word has not already been highlighted!
				var nv = node.nodeValue;
				var ni = tempNodeVal.indexOf(tempWordVal);
				// Create a load of replacement nodes
				var before = document.createTextNode(nv.substr(0,ni));
				var docWordVal = nv.substr(ni,word.length);
				var after = document.createTextNode(nv.substr(ni+word.length));
				var hiwordtext = document.createTextNode(docWordVal);
				var hiword = document.createElement("span");
				hiword.className = "searchword";
				hiword.appendChild(hiwordtext);
				pn.insertBefore(before,node);
				pn.insertBefore(hiword,node);
				pn.insertBefore(after,node);
				pn.removeChild(node);
			}
		}
	}
}

function googleSearchHighlight() {
	if (!document.createElement) return;
	var ref = document.referrer;
	if (ref.indexOf('?') == -1) return;
	var qs = ref.substr(ref.indexOf('?')+1);
	var qsa = qs.split('&');
	for (var i=0;i<qsa.length;i++) {
		var qsip = qsa[i].split('=');
        if (qsip.length == 1) continue;
        if (qsip[0] == 'q' || qsip[0] == 'p') { // q= for Google, p= for Yahoo
            var words = unescape(qsip[1].replace(/\+/g,' ')).split(/\s+/);
            for (var w=0;w<words.length;w++) {
                highlightWord(document.getElementsByTagName("body")[0],words[w]);
            }
        }
	}
}

function highlightTerm() {
    if (!document.createElement) return;
    // ensure this only executes when showing search results
    if (document.getElementById("searchAgain")) {
        var searchTerm = document.getElementById("q").value;
        var words = unescape(searchTerm.replace(/\+/g,' ')).split(/\s+/);
        for (var w=0;w<words.length;w++) {
            highlightWord(document.getElementsByTagName("body")[0],words[w]);
        }
    }
}

window.onload = highlightTerm;
