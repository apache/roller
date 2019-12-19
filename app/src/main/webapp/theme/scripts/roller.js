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
/* This function is used to set cookies */
function setCookie(name,value,expires,path,domain,secure) {
  document.cookie = name + "=" + escape (value) +
    ((expires) ? "; expires=" + expires.toGMTString() : "") +
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") + ((secure) ? "; secure" : "");
}

/* This function is used to get cookies */
function getCookie(name) {
	var prefix = name + "=";
	var start = document.cookie.indexOf(prefix);

	if (start===-1) {
		return null;
	}

	var end = document.cookie.indexOf(";", start+prefix.length);
	if (end===-1) {
		end=document.cookie.length;
	}

	var value=document.cookie.substring(start+prefix.length, end);
	return unescape(value);
}

/* This function is used to delete cookies */
function deleteCookie(name,path,domain) {
  if (getCookie(name)) {
    document.cookie = name + "=" +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}

function rememberUser(theForm) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 365 * 60 * 60 * 1000); // sets it for approx 365 days.
    // sets it for entire domain, so freeroller will remember for all users
    setCookie("commentAuthor",theForm.name.value,expires,"/");
    setCookie("commentEmail",theForm.email.value,expires,"/");
    setCookie("commentUrl",theForm.url.value,expires,"/");
}

function forgetUser(theForm) {
    deleteCookie("commentAuthor","/");
    deleteCookie("commentEmail","/");
    deleteCookie("commentUrl","/");
}

/* This function is used to show/hide elements with a display:none style attribute */
function toggle(targetId) {
    if (document.getElementById) {
        target = document.getElementById(targetId);
    	if (target.style.display === "none") {
    		target.style.display = "";
    	} else {
    		target.style.display = "none";
    	}
    }
}

/* The toggleFolder and togglePlusMinus functions are for expanding/contracting folders */
function toggleFolder(targetId) {
    var expanded;
    if (document.getElementById) {
        target = document.getElementById(targetId);
    	if (target.style.display === "none") {
    		target.style.display = "";
            expanded = true;
    	} else {
    		target.style.display = "none";
            expanded = false;
    	}
        togglePlusMinus("i" + targetId);

        // set a cookie to remember this preference
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 365 * 60 * 60 * 1000); // sets it for approx 365 days.
        setCookie("rfolder-"+targetId,expanded,expires,"/");
    }
}

function togglePlusMinus(targetId) {
    if (document.getElementById) {
        target = document.getElementById(targetId);
    	if (target.innerHTML === "+") {
    		target.innerHTML = "-";
    	} else {
    		target.innerHTML = "+";
    	}
    }
}

/* This function is to set folders to expand/contract based on a user's preference */
function folderPreference(folderId) {
    var folderCookie = getCookie("rfolder-"+folderId);
    if (folderCookie != null) { // we have user's last setting
        var folder = document.getElementById(folderId);
        var plusMinus = document.getElementById("i"+folderId);
        if (folderCookie === "true") { // show
            folder.style.display = "";
            plusMinus.innerHTML = "-";
        } else { // hide
            folder.style.display = "none";
            plusMinus.innerHTML = "+";
        }
    }
}

function toggleNextRow(e) {
    if (e.type === "checkbox") {
        var checked = e.checked;
    } else if (e.type === "radio") {
        var v = e.value;
        var checked = (v === "1" || v === "y" || v === "true");
    }
    // var nextRow = e.parentNode.parentNode.nextSibling;
    // the above doesn't work on Mozilla since it treats white space as nodes
    var thisRow = e.parentNode.parentNode;
    var tableBody = thisRow.parentNode;
    var nextRow = tableBody.getElementsByTagName("tr")[thisRow.rowIndex+1];

    if (checked === true) {
        nextRow.style.display = "";
    } else {
        nextRow.style.display = "none";
    }
}

function toggleControl(toggleId, targetId) {
    var expanded;
    if (document.getElementById) {
        target = document.getElementById(targetId);
        toggle = document.getElementById(toggleId);
    	if (target.style.display === "none") {
    		target.style.display = "";
            expanded = true;

    	} else {
    		target.style.display = "none";
            expanded = false;
    	}
        togglePlusMinus("i" + targetId);

        // set a cookie to remember this preference
        var expires = new Date();
        expires.setTime(expires.getTime() + 24 * 365 * 60 * 60 * 1000); // sets it for approx 365 days.
        setCookie("control_"+targetId,expanded,expires,"/");
    }
}

function isblank(s) {
   for (var i=0; i<s.length; s++) {
      var c = s.charAt(i);
      if ((c!==' ') && (c!=='\n') && (c!=='')) return false;
   }
    return true;
}

// Show the document's title on the status bar
window.defaultStatus=document.title;

// Toggle check boxes
function toggleFunctionAll(toggle) {
	var inputs = document.getElementsByTagName('input');
	for(var i = 0; i < inputs.length ; i++) {
		if(inputs[i].name !== "control" && inputs[i].type === 'checkbox' && inputs[i].disabled === false ) {
			if (inputs[i].checked === true){
				inputs[i].checked = !inputs[i].checked;
			} else{
				inputs[i].checked = toggle;
			}
		}
	}
}

function toggleFunction(toggle,name) {;
	var inputs = document.getElementsByName(name);
	for(var i = 0; i < inputs.length ; i++) {
		if(inputs[i].type === 'checkbox' && inputs[i].disabled === false) {
           inputs[i].checked = toggle;
		}
	}
}

function isValidUrl(url) {
    return /^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url);
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(email);
}
