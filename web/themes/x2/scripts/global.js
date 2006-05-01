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
/* Function to route non-standards compliant browsers to upgrade */
/*
if (!document.getElementById) {
    window.location = 
	   "http://archive.webstandards.org/upgrade/"
}
*/

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}

/* This function is used to change the style class of an element */
function swapClass(obj, newStyle) {
    obj.className = newStyle;
}

function isUndefined(value) {   
    var undef;   
    return value == undef; 
}

/* Function for showing and hiding layers (divs) */
function toggleBox(szDivID, iState) // 1 visible, 0 hidden
{
    var divName = document.getElementById(szDivID);
    divName.style.visibility = iState ? "visible" : "hidden";
}

/* Function for showing and hiding rows or divs */
function toggleRow(szDivID, iState) // 1 visible, 0 hidden
{
    isNS = (navigator.appName == "Netscape") ? true : false;
    var displayType;
    if (isNS) {
        displayType = "table-row";
    } else {
        displayType = "block";
    }
    var obj = document.getElementById(szDivID);
    obj.style.display = iState ? displayType : "none";
}

function checkAll(theForm) { // check all the checkboxes in the list
  for (var i=0;i<theForm.elements.length;i++) {
    var e = theForm.elements[i];
		var eName = e.name;
    	if (eName != 'allbox' && 
            (e.type.indexOf("checkbox") == 0)) {
        	e.checked = theForm.allbox.checked;		
		}
	} 
}

/* Function to clear a form of all it's values */
function clearForm(frmObj) {
	for(var i = 0; i < frmObj.length; i++) {
		if(frmObj.elements[i].type.indexOf("text") == 0 || 
				frmObj.elements[i].type.indexOf("password") == 0) {
					frmObj.elements[i].value="";
		} else if (frmObj.elements[i].type.indexOf("radio") == 0) {
			frmObj.elements[i].checked=false;
		} else if (frmObj.elements[i].type.indexOf("checkbox") == 0) {
			frmObj.elements[i].checked = false;
		} else if (frmObj.elements[i].type.indexOf("select") == 0) {
			for(var j = 0; j < frmObj.elements[i].length ; j++) {
				frmObj.elements[i].options[j].selected=false;
			}
            frmObj.elements[i].options[0].selected=true;
		}
	} 
}

/* Function to hide form elements that show through
   the search form when it is visible */
function toggleForm(frmObj, iState) // 1 visible, 0 hidden 
{
	for(var i = 0; i < frmObj.length; i++) {
		if (frmObj.elements[i].type.indexOf("select") == 0 || frmObj.elements[i].type.indexOf("checkbox") == 0) {
            frmObj.elements[i].style.visibility = iState ? "visible" : "hidden";
		}
	} 
}
    
/* This function is used to open a story in a popup window */
function openStory(url, w, h) {
    var screenWidth = window.screen.availWidth;
    var screenHeight = window.screen.availHeight;
    var winParams = "width="+w+",height="+h;
        winParams += ",left="+(screenWidth-w)/2+",top="+(screenHeight-h)/2;
        winParams += ",toolbar,scrollbars,resizable,status=yes";

    openWindow(url, 'Story', winParams);
}

/* This function is used to open a pop-up window */
function openWindow(url, winName, winParams) {
	popupWin = window.open(url, winName, winParams);
    popupWin.focus();
}

// This resizes the browser window to take up the user's
// entire screen height & width
function growToFullWindow() {
    var screenWidth = window.screen.availWidth;
    var screenHeight = window.screen.availHeight;
    top.window.resizeTo(screenWidth,screenHeight); 
    top.window.moveTo(0,0);
}

function shrinkWindow(newWidth, newHeight) {
    top.window.resizeTo(newWidth,newHeight); 
    top.window.moveTo(0,0);
}

/* This function is used to set cookies */
function setCookie(name,value,expires,path,domain,secure) {
  document.cookie = name + "=" + escape (value) +
    ((expires) ? "; expires=" + expires.toGMTString() : "") +
    ((path) ? "; path=" + path : "") +
    ((domain) ? "; domain=" + domain : "") + ((secure) ? "; secure" : "");
}

/* This function is used to get cookies */
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

/* This function is used to delete cookies */
function deleteCookie(name,path,domain) {
  if (getCookie(name)) {
    document.cookie = name + "=" +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      "; expires=Thu, 01-Jan-70 00:00:01 GMT";
  }
}

function setTheme(themeName) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 365 * 60 * 60 * 1000); // sets it for approx 365 days.
    setCookie("roller-theme",themeName,expires,"/");
    document.location.reload();
}

/* This function is used to show/hide elements with a display:none style attribute */ 
function toggle(targetId) {
    if (document.getElementById) {
        target = document.getElementById(targetId);
    	if (target.style.display == "none") {
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
    	if (target.style.display == "none") {
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
    	if (target.innerHTML == "+") {
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
        folder = document.getElementById(folderId);
        plusMinus = document.getElementById("i"+folderId);
        if (folderCookie == "true") { // show
            folder.style.display = "";
            plusMinus.innerHTML = "-";
        } else { // hide
            folder.style.display = "none";
            plusMinus.innerHTML = "+"; 
        }
    }
}

// Show the document's title on the status bar
window.defaultStatus=document.title;

