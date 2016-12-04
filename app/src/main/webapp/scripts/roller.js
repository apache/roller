/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
*
* Source file modified from the original ASF source; all changes made
* are also under Apache License.
*/
function setCookie(name,value,expires,path,domain,secure) {
  document.cookie = name + "=" + escape (value) +
    ((expires) ? "; expires=" + expires.toGMTString() : "") +
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") + ((secure) ? "; secure" : "");
}

function getCookie(name) {
	var prefix = name + "="
	var start = document.cookie.indexOf(prefix)

	if (start==-1) {
		return null;
	}

	var end = document.cookie.indexOf(";", start+prefix.length)
	if (end==-1) {
		end=document.cookie.length;
	}

	var value=document.cookie.substring(start+prefix.length, end)
	return unescape(value);
}

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
    setCookie("commentAuthor",theForm.name.value,expires,"/");
    setCookie("commentEmail",theForm.email.value,expires,"/");
    setCookie("commentUrl",theForm.url.value,expires,"/");
}

function forgetUser(theForm) {
    deleteCookie("commentAuthor","/");
    deleteCookie("commentEmail","/");
    deleteCookie("commentUrl","/");
}

// Toggle check boxes
function toggleFunction(toggle,name) {;
	var inputs = document.getElementsByName(name);
	for(var i = 0; i < inputs.length ; i++) {
		if(inputs[i].type == 'checkbox' && inputs[i].disabled == false) {
           inputs[i].checked = toggle;
		}
	}
};
