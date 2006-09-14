// Cross-Browser Rich Text Editor
// http://www.kevinroth.com/rte/demo.htm
// Written by Kevin Roth (kevin@NOSPAMkevinroth.com - remove NOSPAM)
// Visit the support forums at http://www.kevinroth.com/forums/index.php?c=2
// This code is public domain. Redistribution and use of this code, with or without modification, is permitted.

//init variables
var isRichText = false;
var rng;
var currentRTE;
var allRTEs = "";

var isIE;
var isGecko;
var isSafari;
var isKonqueror;

var imagesPath;
var includesPath;
var cssFile;
var generateXHTML;

var lang = "en";
var encoding = "iso-8859-1";


function initRTE(imgPath, incPath, css, genXHTML) {
	//set browser vars
	var ua = navigator.userAgent.toLowerCase();
	isIE = ((ua.indexOf("msie") != -1) && (ua.indexOf("opera") == -1) && (ua.indexOf("webtv") == -1)); 
	isGecko = (ua.indexOf("gecko") != -1);
	isSafari = (ua.indexOf("safari") != -1);
	isKonqueror = (ua.indexOf("konqueror") != -1);
	
	generateXHTML = genXHTML;
	
	//check to see if designMode mode is available
	//Safari/Konqueror think they are designMode capable even though they are not
	if (document.getElementById && document.designMode && !isSafari && !isKonqueror) {
		isRichText = true;
	}
	
	if (isIE) {
		document.onmouseover = raiseButton;
		document.onmouseout  = normalButton;
		document.onmousedown = lowerButton;
		document.onmouseup   = raiseButton;
	}
	
	//set paths vars
	imagesPath = imgPath;
	includesPath = incPath;
	cssFile = css;
	
	if (isRichText) document.writeln('<style type="text/css">@import "' + includesPath + 'rte.css";</style>');
	
	//for testing standard textarea, uncomment the following line
	//isRichText = false;
}

function writeRichText(rte, html, width, height, buttons, readOnly) {
	if (isRichText) {
		if (allRTEs.length > 0) allRTEs += ";";
		allRTEs += rte;
		
		if (readOnly) buttons = false;
		
		//adjust minimum table widths
		if (isIE) {
			if (buttons && (width < 540)) width = 540;
			var tablewidth = width;
		} else {
			if (buttons && (width < 540)) width = 540;
			var tablewidth = width + 4;
		}
		
		document.writeln('<div class="rteDiv">');
		if (buttons == true) {
			document.writeln('<table class="rteBack" cellpadding=2 cellspacing=0 id="Buttons1_' + rte + '" width="' + tablewidth + '">');
			document.writeln('	<tr>');
            document.writeln('		<td>');
			document.writeln('			<select id="formatblock_' + rte + '" onchange="selectFont(\'' + rte + '\', this.id);">');
			document.writeln('				<option value="">[Style]</option>');
			document.writeln('				<option value="<p>">Paragraph &lt;p&gt;</option>');
			document.writeln('				<option value="<h1>">Heading 1 &lt;h1&gt;</option>');
			document.writeln('				<option value="<h2>">Heading 2 &lt;h2&gt;</option>');
			document.writeln('				<option value="<h3>">Heading 3 &lt;h3&gt;</option>');
			document.writeln('				<option value="<h4>">Heading 4 &lt;h4&gt;</option>');
			document.writeln('				<option value="<h5>">Heading 5 &lt;h5&gt;</option>');
			document.writeln('				<option value="<h6>">Heading 6 &lt;h6&gt;</option>');
			document.writeln('				<option value="<address>">Address &lt;ADDR&gt;</option>');
			document.writeln('				<option value="<pre>">Formatted &lt;pre&gt;</option>');
			document.writeln('			</select>');
			document.writeln('		</td>');
			document.writeln('		<td>');
			document.writeln('			<select id="fontname_' + rte + '" onchange="selectFont(\'' + rte + '\', this.id)">');
			document.writeln('				<option value="Font" selected>[Font]</option>');
			document.writeln('				<option value="Arial, Helvetica, sans-serif">Arial</option>');
			document.writeln('				<option value="Courier New, Courier, mono">Courier New</option>');
			document.writeln('				<option value="Times New Roman, Times, serif">Times New Roman</option>');
			document.writeln('				<option value="Verdana, Arial, Helvetica, sans-serif">Verdana</option>');
			document.writeln('			</select>');
			document.writeln('		</td>');
			document.writeln('		<td>');
			document.writeln('			<select unselectable="on" id="fontsize_' + rte + '" onchange="selectFont(\'' + rte + '\', this.id);">');
			document.writeln('				<option value="Size">[Size]</option>');
			document.writeln('				<option value="1">1</option>');
			document.writeln('				<option value="2">2</option>');
			document.writeln('				<option value="3">3</option>');
			document.writeln('				<option value="4">4</option>');
			document.writeln('				<option value="5">5</option>');
			document.writeln('				<option value="6">6</option>');
			document.writeln('				<option value="7">7</option>');
			document.writeln('			</select>');
			document.writeln('		</td>');
			document.writeln('		<td width="100%">');
			document.writeln('		</td>');
			document.writeln('	</tr>');
			document.writeln('</table>');
			document.writeln('<table class="rteBack" cellpadding="0" cellspacing="0" id="Buttons2_' + rte + '" width="' + tablewidth + '">');
			document.writeln('	<tr>');

// ROLLER_MOD: changed icon images

			document.writeln('		<td><img id="bold" class="rteImage" src="' + imagesPath + 'text_bold.png" width="16" height="16" alt="Bold" title="Bold" onClick="rteCommand(\'' + rte + '\', \'bold\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_italic.png" width="16" height="16" alt="Italic" title="Italic" onClick="rteCommand(\'' + rte + '\', \'italic\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_underline.png" width="16" height="16" alt="Underline" title="Underline" onClick="rteCommand(\'' + rte + '\', \'underline\', \'\')"></td>');
			document.writeln('		<td><img class="rteVertSep" src="' + imagesPath + 'blackdot.gif" width="1" height="20" border="0" alt=""></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_align_left.png" width="16" height="16" alt="Align Left" title="Align Left" onClick="rteCommand(\'' + rte + '\', \'justifyleft\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_align_center.png" width="16" height="16" alt="Center" title="Center" onClick="rteCommand(\'' + rte + '\', \'justifycenter\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_align_right.png" width="16" height="16" alt="Align Right" title="Align Right" onClick="rteCommand(\'' + rte + '\', \'justifyright\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_align_justify.png" width="16" height="16" alt="Justify Full" title="Justify Full" onclick="rteCommand(\'' + rte + '\', \'justifyfull\', \'\')"></td>');
			document.writeln('		<td><img class="rteVertSep" src="' + imagesPath + 'blackdot.gif" width="1" height="20" border="0" alt=""></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_horizontalrule.png" width="16" height="16" alt="Horizontal Rule" title="Horizontal Rule" onClick="rteCommand(\'' + rte + '\', \'inserthorizontalrule\', \'\')"></td>');
			document.writeln('		<td><img class="rteVertSep" src="' + imagesPath + 'blackdot.gif" width="1" height="20" border="0" alt=""></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_list_numbers.png" width="16" height="16" alt="Ordered List" title="Ordered List" onClick="rteCommand(\'' + rte + '\', \'insertorderedlist\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_list_bullets.png" width="16" height="16" alt="Unordered List" title="Unordered List" onClick="rteCommand(\'' + rte + '\', \'insertunorderedlist\', \'\')"></td>');
			document.writeln('		<td><img class="rteVertSep" src="' + imagesPath + 'blackdot.gif" width="1" height="20" border="0" alt=""></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_indent_remove.png" width="16" height="16" alt="Outdent" title="Outdent" onClick="rteCommand(\'' + rte + '\', \'outdent\', \'\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'text_indent.png" width="16" height="16" alt="Indent" title="Indent" onClick="rteCommand(\'' + rte + '\', \'indent\', \'\')"></td>');
			document.writeln('		<td><div id="forecolor_' + rte + '"><img class="rteImage" src="' + imagesPath + 'color_swatch.png" width="16" height="16" alt="Text Color" title="Text Color" onClick="dlgColorPalette(\'' + rte + '\', \'forecolor\', \'\')"></div></td>');
			document.writeln('		<td><div id="hilitecolor_' + rte + '"><img class="rteImage" src="' + imagesPath + 'paintcan.png" width="16" height="16 alt="Background Color" title="Background Color" onClick="dlgColorPalette(\'' + rte + '\', \'hilitecolor\', \'\')"></div></td>');
			document.writeln('		<td><img class="rteVertSep" src="' + imagesPath + 'blackdot.gif" width="1" height="20" border="0" alt=""></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'link_add.png" width="16" height="16" alt="Insert Link" title="Insert Link" onClick="dlgInsertLink(\'' + rte + '\', \'link\')"></td>');
			document.writeln('		<td><img class="rteImage"   src="' + imagesPath + 'image_add.png" width="16" height="16" alt="Add Image" title="Add Image" onClick="addImage(\'' + rte + '\')"></td>');
			document.writeln('		<td><div id="table_' + rte + '"><img class="rteImage" src="' + imagesPath + 'table_add.png" width="16" height="16" alt="Insert Table" title="Insert Table" onClick="dlgInsertTable(\'' + rte + '\', \'table\', \'\')"></div></td>');
			document.writeln('		<td width="100%"></td>');
			document.writeln('	</tr>');
			document.writeln('</table>');
		}
        if (getCookie(rte + '_size') != null) {
            height = getCookie(rte + '_size');
        }
		document.writeln('<iframe class="rteFrame" id="' + rte + '" name="' + rte + '" width="' + width + '" height="' + height + '" src="' + includesPath + 'blank.htm"></iframe>');

// ROLLER_MOD: added resize buttons
			
        document.writeln('<table class="rteBack" cellpadding="0" cellspacing="0" id="Buttons3_' + rte + '" width="' + tablewidth + '"><tr>');        
		if (!readOnly) {
            document.writeln('<td><input type="checkbox" id="chkSrc' + rte + '" onclick="toggleHTMLSrc(\'' + rte + '\',' + buttons + ');" />&nbsp;<label for="chkSrc' + rte + '">View Source</label></td>');
        }
	    document.writeln('<td align="right"><a href="#" onclick="changeSize(\'' + rte + '\',\'' + rte + '_size\', 20)" /><img class="rteImageSm" src="' + imagesPath + 'arrow_down.png" width="16" height="16" border="0" /></a>'   );
	    document.writeln('<a href="#" onclick="changeSize(\'' + rte + '\',\'' + rte + '_size\', -20)" /><img class="rteImageSm" src="' + imagesPath + 'arrow_up.png" width="16" height="16" border="0" /></a></td>'   );
        document.writeln("</tr></table>");

		document.writeln('<iframe width="154" height="104" id="cp' + rte + '" src="' + includesPath + 'palette.htm" marginwidth="0" marginheight="0" scrolling="no" style="visibility:hidden; position: absolute;"></iframe>');
		document.writeln('<input type="hidden" id="hdn' + rte + '" name="' + rte + '" value="">');
		document.writeln('</div>');
		document.getElementById('hdn' + rte).value = html;
		enableDesignMode(rte, html, readOnly);

	} else {
		if (!readOnly) {
			document.writeln('<textarea name="' + rte + '" id="' + rte + '" style="width: ' + width + '; height: ' + height + ';">' + html + '</textarea>');
		} else {
			document.writeln('<textarea name="' + rte + '" id="' + rte + '" style="width: ' + width + '; height: ' + height + ';" readonly>' + html + '</textarea>');
		}
	}
}

// ROLLER_MOD: added resize method
function changeSize(id, cookiename, num) {
    var frame = document.getElementById(id);
    frame.height = Number(frame.height) + Number(num); 
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie(cookiename, frame.height, expires);
}


function enableDesignMode(rte, html, readOnly) {
	var frameHtml = "<html id=\"" + rte + "\">\n";
	frameHtml += "<head>\n";
	//to reference your stylesheet, set href property below to your stylesheet path and uncomment
	if (cssFile.length > 0) {
		frameHtml += "<link media=\"all\" type=\"text/css\" href=\"" + cssFile + "\" rel=\"stylesheet\">\n";
	} else {
		frameHtml += "<style>\n";
		frameHtml += "body {\n";
		frameHtml += "	background: #FFFFFF;\n";
		frameHtml += "	margin: 0px;\n";
		frameHtml += "	padding: 0px;\n";
		frameHtml += "}\n";
		frameHtml += "</style>\n";
	}
	frameHtml += "</head>\n";
	frameHtml += "<body>\n";
	frameHtml += html + "\n";
	frameHtml += "</body>\n";
	frameHtml += "</html>";
	
	if (document.all) {
		var oRTE = frames[rte].document;
		oRTE.open();
		oRTE.write(frameHtml);
		oRTE.close();
		if (!readOnly) {
			oRTE.designMode = "On";
			frames[rte].document.attachEvent("onkeypress", function evt_ie_keypress(event) {ieKeyPress(event, rte);});
		}
	} else {
		try {
			if (!readOnly) document.getElementById(rte).contentDocument.designMode = "on";
			try {
				var oRTE = document.getElementById(rte).contentWindow.document;
				oRTE.open();
				oRTE.write(frameHtml);
				oRTE.close();
				if (isGecko && !readOnly) {
					//attach a keyboard handler for gecko browsers to make keyboard shortcuts work
					oRTE.addEventListener("keypress", geckoKeyPress, true);
				}
			} catch (e) {
				alert("Error preloading content.");
			}
		} catch (e) {
			//gecko may take some time to enable design mode.
			//Keep looping until able to set.
			if (isGecko) {
				setTimeout("enableDesignMode('" + rte + "', '" + html + "', " + readOnly + ");", 10);
			} else {
				return false;
			}
		}
	}
}

function updateRTE(rte) {
	if (!isRichText) return;
	
	//check for readOnly mode
	var readOnly = false;
	if (document.all) {
		if (frames[rte].document.designMode != "On") readOnly = true;
	} else {
		if (document.getElementById(rte).contentDocument.designMode != "on") readOnly = true;
	}
	
	if (isRichText && !readOnly) {
		//if viewing source, switch back to design view
		if (document.getElementById("chkSrc" + rte).checked) document.getElementById("chkSrc" + rte).click();
		setHiddenVal(rte);
	}
}

function setHiddenVal(rte) {
	//set hidden form field value for current rte
	var oHdnField = document.getElementById('hdn' + rte);
	
	//convert html output to xhtml (thanks Timothy Bell and Vyacheslav Smolin!)
	if (oHdnField.value == null) oHdnField.value = "";
	if (document.all) {
		if (generateXHTML) {
			oHdnField.value = get_xhtml(frames[rte].document.body, lang, encoding);
		} else {
			oHdnField.value = frames[rte].document.body.innerHTML;
		}
	} else {
		if (generateXHTML) {
			oHdnField.value = get_xhtml(document.getElementById(rte).contentWindow.document.body, lang, encoding);
		} else {
			oHdnField.value = document.getElementById(rte).contentWindow.document.body.innerHTML;
		}
	}
	
	//if there is no content (other than formatting) set value to nothing
	if (stripHTML(oHdnField.value.replace("&nbsp;", " ")) == "" &&
		oHdnField.value.toLowerCase().search("<hr") == -1 &&
		oHdnField.value.toLowerCase().search("<img") == -1) oHdnField.value = "";
}

function updateRTEs() {
	var vRTEs = allRTEs.split(";");
	for (var i = 0; i < vRTEs.length; i++) {
		updateRTE(vRTEs[i]);
	}
}

function rteCommand(rte, command, option) {
	//function to perform command
	var oRTE;
	if (document.all) {
		oRTE = frames[rte];
	} else {
		oRTE = document.getElementById(rte).contentWindow;
	}
	
	try {
		oRTE.focus();
	  	oRTE.document.execCommand(command, false, option);
		oRTE.focus();
	} catch (e) {
//		alert(e);
//		setTimeout("rteCommand('" + rte + "', '" + command + "', '" + option + "');", 10);
	}
}

function toggleHTMLSrc(rte, buttons) {
	//contributed by Bob Hutzel (thanks Bob!)
	var oHdnField = document.getElementById('hdn' + rte);
	
	if (document.getElementById("chkSrc" + rte).checked) {
		//we are checking the box
		if (buttons) {
			showHideElement("Buttons1_" + rte, "hide");
			showHideElement("Buttons2_" + rte, "hide");
		}
		setHiddenVal(rte);
		if (document.all) {
			frames[rte].document.body.innerText = oHdnField.value;
		} else {
			var oRTE = document.getElementById(rte).contentWindow.document;
			var htmlSrc = oRTE.createTextNode(oHdnField.value);
			oRTE.body.innerHTML = "";
			oRTE.body.appendChild(htmlSrc);
		}
	} else {
		//we are unchecking the box
		if (buttons) {
			showHideElement("Buttons1_" + rte, "show");
			showHideElement("Buttons2_" + rte, "show");
		}
		if (document.all) {
			//fix for IE
			var output = escape(frames[rte].document.body.innerText);
			output = output.replace("%3CP%3E%0D%0A%3CHR%3E", "%3CHR%3E");
			output = output.replace("%3CHR%3E%0D%0A%3C/P%3E", "%3CHR%3E");
			frames[rte].document.body.innerHTML = unescape(output);
		} else {
			var oRTE = document.getElementById(rte).contentWindow.document;
			var htmlSrc = oRTE.body.ownerDocument.createRange();
			htmlSrc.selectNodeContents(oRTE.body);
			oRTE.body.innerHTML = htmlSrc.toString();
		}
	}
}

function dlgColorPalette(rte, command) {
	//function to display or hide color palettes
	setRange(rte);
	
	//get dialog position
	var oDialog = document.getElementById('cp' + rte);
	var buttonElement = document.getElementById(command + '_' + rte);
	var iLeftPos = getOffsetLeft(buttonElement);
	var iTopPos = getOffsetTop(buttonElement) + (buttonElement.offsetHeight + 4);
	oDialog.style.left = (iLeftPos) + "px";
	oDialog.style.top = (iTopPos) + "px";
	
	if ((command == parent.command) && (rte == currentRTE)) {
		//if current command dialog is currently open, close it
		if (oDialog.style.visibility == "hidden") {
			showHideElement(oDialog, 'show');
		} else {
			showHideElement(oDialog, 'hide');
		}
	} else {
		//if opening a new dialog, close all others
		var vRTEs = allRTEs.split(";");
		for (var i = 0; i < vRTEs.length; i++) {
			showHideElement('cp' + vRTEs[i], 'hide');
		}
		showHideElement(oDialog, 'show');
	}
	
	//save current values
	parent.command = command;
	currentRTE = rte;
}

function dlgInsertTable(rte, command) {
	//function to open/close insert table dialog
	//save current values
	parent.command = command;
	currentRTE = rte;
	InsertTable = popUpWin(includesPath + 'insert_table.htm', 'InsertTable', 360, 180, '');
}

function dlgInsertLink(rte, command) {
	//function to open/close insert table dialog
	//save current values
	parent.command = command;
	currentRTE = rte;
	InsertLink = popUpWin(includesPath + 'insert_link.htm', 'InsertLink', 360, 180, '');
	
	//get currently highlighted text and set link text value
	setRange(rte);
	var linkText = '';
	if (isIE) {
		linkText = stripHTML(rng.htmlText);
	} else {
		linkText = stripHTML(rng.toString());
	}
	setLinkText(linkText);
}

function setLinkText(linkText) {
	//set link text value in insert link dialog
	try {
		window.InsertLink.document.linkForm.linkText.value = linkText;
	} catch (e) {
		//may take some time to create dialog window.
		//Keep looping until able to set.
		setTimeout("setLinkText('" + linkText + "');", 10);
	}
}

function popUpWin (url, win, width, height, options) {
	var leftPos = (screen.availWidth - width) / 2;
	var topPos = (screen.availHeight - height) / 2;
	options += 'width=' + width + ',height=' + height + ',left=' + leftPos + ',top=' + topPos;
	return window.open(url, win, options);
}

function setColor(color) {
	//function to set color
	var rte = currentRTE;
	var parentCommand = parent.command;
	
	if (document.all) {
		if (parentCommand == "hilitecolor") parentCommand = "backcolor";
		
		//retrieve selected range
		rng.select();
	}
	
	rteCommand(rte, parentCommand, color);
	showHideElement('cp' + rte, "hide");
}

function addImage(rte) {
	//function to add image
	imagePath = prompt('Enter Image URL:', 'http://');				
	if ((imagePath != null) && (imagePath != "")) {
		rteCommand(rte, 'InsertImage', imagePath);
	}
}

// Ernst de Moor: Fix the amount of digging parents up, in case the RTE editor itself is displayed in a div.
// KJR 11/12/2004 Changed to position palette based on parent div, so palette will always appear in proper location regardless of nested divs
function getOffsetTop(elm) {
	var mOffsetTop = elm.offsetTop;
	var mOffsetParent = elm.offsetParent;
	var parents_up = 2; //the positioning div is 2 elements up the tree
	
	while(parents_up > 0) {
		mOffsetTop += mOffsetParent.offsetTop;
		mOffsetParent = mOffsetParent.offsetParent;
		parents_up--;
	}
	
	return mOffsetTop;
}

// Ernst de Moor: Fix the amount of digging parents up, in case the RTE editor itself is displayed in a div.
// KJR 11/12/2004 Changed to position palette based on parent div, so palette will always appear in proper location regardless of nested divs
function getOffsetLeft(elm) {
	var mOffsetLeft = elm.offsetLeft;
	var mOffsetParent = elm.offsetParent;
	var parents_up = 2;
	
	while(parents_up > 0) {
		mOffsetLeft += mOffsetParent.offsetLeft;
		mOffsetParent = mOffsetParent.offsetParent;
		parents_up--;
	}
	
	return mOffsetLeft;
}

function selectFont(rte, selectname) {
	//function to handle font changes
	var idx = document.getElementById(selectname).selectedIndex;
	// First one is always a label
	if (idx != 0) {
		var selected = document.getElementById(selectname).options[idx].value;
		var cmd = selectname.replace('_' + rte, '');
		rteCommand(rte, cmd, selected);
		document.getElementById(selectname).selectedIndex = 0;
	}
}

function insertHTML(html) {
	//function to add HTML -- thanks dannyuk1982
	var rte = currentRTE;
	
	var oRTE;
	if (document.all) {
		oRTE = frames[rte];
	} else {
		oRTE = document.getElementById(rte).contentWindow;
	}
	
	oRTE.focus();
	if (document.all) {
		var oRng = oRTE.document.selection.createRange();
		oRng.pasteHTML(html);
		oRng.collapse(false);
		oRng.select();
	} else {
		oRTE.document.execCommand('insertHTML', false, html);
	}
}

function showHideElement(element, showHide) {
	//function to show or hide elements
	//element variable can be string or object
	if (document.getElementById(element)) {
		element = document.getElementById(element);
	}
	
	if (showHide == "show") {
		element.style.visibility = "visible";
	} else if (showHide == "hide") {
		element.style.visibility = "hidden";
	}
}

function setRange(rte) {
	//function to store range of current selection
	var oRTE;
	if (document.all) {
		oRTE = frames[rte];
		var selection = oRTE.document.selection; 
		if (selection != null) rng = selection.createRange();
	} else {
		oRTE = document.getElementById(rte).contentWindow;
		var selection = oRTE.getSelection();
		rng = selection.getRangeAt(selection.rangeCount - 1).cloneRange();
	}
	return rng;
}

function stripHTML(oldString) {
	//function to strip all html
	var newString = oldString.replace(/(<([^>]+)>)/ig,"");
	
	//replace carriage returns and line feeds
   newString = newString.replace(/\r\n/g," ");
   newString = newString.replace(/\n/g," ");
   newString = newString.replace(/\r/g," ");
	
	//trim string
	newString = trim(newString);
	
	return newString;
}

function trim(inputString) {
   // Removes leading and trailing spaces from the passed string. Also removes
   // consecutive spaces and replaces it with one space. If something besides
   // a string is passed in (null, custom object, etc.) then return the input.
   if (typeof inputString != "string") return inputString;
   var retValue = inputString;
   var ch = retValue.substring(0, 1);
	
   while (ch == " ") { // Check for spaces at the beginning of the string
      retValue = retValue.substring(1, retValue.length);
      ch = retValue.substring(0, 1);
   }
   ch = retValue.substring(retValue.length - 1, retValue.length);
	
   while (ch == " ") { // Check for spaces at the end of the string
      retValue = retValue.substring(0, retValue.length - 1);
      ch = retValue.substring(retValue.length - 1, retValue.length);
   }
	
	// Note that there are two spaces in the string - look for multiple spaces within the string
   while (retValue.indexOf("  ") != -1) {
		// Again, there are two spaces in each of the strings
      retValue = retValue.substring(0, retValue.indexOf("  ")) + retValue.substring(retValue.indexOf("  ") + 1, retValue.length);
   }
   return retValue; // Return the trimmed string back to the user
}

//********************
//Gecko-Only Functions
//********************
function geckoKeyPress(evt) {
	//function to add bold, italic, and underline shortcut commands to gecko RTEs
	//contributed by Anti Veeranna (thanks Anti!)
	var rte = evt.target.id;
	
	if (evt.ctrlKey) {
		var key = String.fromCharCode(evt.charCode).toLowerCase();
		var cmd = '';
		switch (key) {
			case 'b': cmd = "bold"; break;
			case 'i': cmd = "italic"; break;
			case 'u': cmd = "underline"; break;
		};

		if (cmd) {
			rteCommand(rte, cmd, null);
			
			// stop the event bubble
			evt.preventDefault();
			evt.stopPropagation();
		}
 	}
}

//*****************
//IE-Only Functions
//*****************
function ieKeyPress(evt, rte) {
	var key = (evt.which || evt.charCode || evt.keyCode);
	var stringKey = String.fromCharCode(key).toLowerCase();
	
//the following breaks list and indentation functionality in IE (don't use)
//	switch (key) {
//		case 13:
//			//insert <br> tag instead of <p>
//			//change the key pressed to null
//			evt.keyCode = 0;
//			
//			//insert <br> tag
//			currentRTE = rte;
//			insertHTML('<br>');
//			break;
//	};
}

function checkspell() {
	//function to perform spell check
	try {
		var tmpis = new ActiveXObject("ieSpell.ieSpellExtension");
		tmpis.CheckAllLinkedDocuments(document);
	}
	catch(exception) {
		if(exception.number==-2146827859) {
			if (confirm("ieSpell not detected.  Click Ok to go to download page."))
				window.open("http://www.iespell.com/download.php","DownLoad");
		} else {
			alert("Error Loading ieSpell: Exception " + exception.number);
		}
	}
}

function raiseButton(e) {
	var el = window.event.srcElement;
	
	className = el.className;
	if (className == 'rteImage' || className == 'rteImageLowered') {
		el.className = 'rteImageRaised';
	}
}

function normalButton(e) {
	var el = window.event.srcElement;
	
	className = el.className;
	if (className == 'rteImageRaised' || className == 'rteImageLowered') {
		el.className = 'rteImage';
	}
}

function lowerButton(e) {
	var el = window.event.srcElement;
	
	className = el.className;
	if (className == 'rteImage' || className == 'rteImageRaised') {
		el.className = 'rteImageLowered';
	}
}
