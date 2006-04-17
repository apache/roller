<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/taglibs.jsp" %>
<html:hidden property="summary" />

<script type="text/javascript" src="<html:rewrite page="/theme/scripts/xmlp.js"/>"></script>
<script type="text/javascript">
<!--  
var expires = new Date();
expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.

function doParse() {
    var parser = new XMLParser();
    var form = document.forms[0];
    // wrap contents of textarea with root element
    var xml = "<root>" + form.text.value + "</root>";
    try 
    {
        parser.parse(xml);
    }
    catch (e) 
    {
        var msg = "Invalid XML: " + e.message;
            msg += "\n\nClick OK to continue posting with (possibly) invalid markup.";
        var ans = confirm(msg);
        if (ans) 
        {
            return true;
        } 
        else 
        {
            return false;
        }
    }
    return true;
}

function postWeblogEntry(publish)
{   
    if (document.getElementById("parseXML").checked) {
        setCookie("parseXML","true",expires,"/");
        if (doParse()) {        
            if (publish) {
                document.weblogEntryFormEx.publishEntry.value = "true";
            }
            document.weblogEntryFormEx.submit();
        }
    } else {
        deleteCookie("parseXML");
        if (publish) {
            document.weblogEntryFormEx.publishEntry.value = "true";
        }
        document.weblogEntryFormEx.submit();
    }
}

function htmlcode(theform,htmltag,prompttext) 
{
	// insert <x>yyy</x> style markup
	inserttext = prompt(tag_prompt+"\n<"+htmltag+">xxx</"+htmltag+">",'');
	if ((inserttext != null) && (inserttext != "")) {
		theform.text.value += "<"+htmltag+">"+inserttext+"</"+htmltag+"> ";
	}
	theform.text.focus();
}

// *******************************************************

function dolist(theform) {
// inserts list with option to have numbered or alphabetical type
	listtype = prompt(list_type_prompt, "");
	if ((listtype == "b") || (listtype == "1")) {
		if (listtype == "b") {
			thelist = "<ul>\n";
			listend = "</ul> ";
		}
		else {
			if (listtype == "1") {
				thelist = "<ol>\n";
				listend = "</ol> ";
			}
		}
	}
	else {
		thelist = "<ul>\n";
		listend = "</ul> ";
	}
	listentry = "initial";
	while ((listentry != "") && (listentry != null)) {
		listentry = prompt(list_item_prompt, "");
		if ((listentry != "") && (listentry != null))
			thelist = thelist+"<li>"+listentry+"\n";
		}
	theform.text.value += thelist+listend;
	theform.text.focus();
}

// *******************************************************

function htmllink(theform,thetag) {
// inserts named url or email link
	linktext = prompt(link_text_prompt,"");
	var prompttext;
	if (thetag == "url") {
		thetype = "a href=\"";
		prompt_text = link_url_prompt;
		prompt_contents = "http://";
	}
	else {
		thetype = "a href=mailto:";
		prompt_text = link_email_prompt;
		prompt_contents = "";
	}
	linkurl = prompt(prompt_text,prompt_contents);
	if ((linkurl != null) && (linkurl != "")) {
		theform.text.value += "<"+thetype+linkurl+"\">"+linktext+"</a> ";
	}
	else {
		theform.text.value += "<"+thetype+linkurl+"\">"+linktext+"</a> ";
	}
	theform.text.focus();
}

// *******************************************************

function insertimage(theform) {
	//insert <img src="" alt="" align=""> tag
	imgsrc = prompt(img_src_prompt,"http://");
	imgalt = prompt(img_alt_prompt,"");
	imgalign = prompt(img_align_prompt,"");
	if (imgalign == "r") imgalign = "right";
	if (imgalign == "l") imgalign = "left";
	theform.text.value += "<img src=\""+imgsrc+"\" alt=\""+imgalt+"\" align=\""+imgalign+"\">";
	theform.text.focus();
}

// *******************************************************

function insertmedia(theform,type) {
	if (type == 'img') {
		//insert <img src="" alt="" align=""> tag'
		imgsrc = "";
		imgsrc = theform.imagefile.options[theform.imagefile.selectedIndex].value;
		imgsrc2 = ""
		for (i=0; i < imgsrc.length; i++) {
			if (imgsrc.charAt(i) == "'") {
				imgsrc2 = imgsrc2 + "\"";
			}
			else {
				imgsrc2 = imgsrc2 + imgsrc.charAt(i);
			}
		}
		imgalt = prompt(img_alt_prompt,"");
		imgalign = prompt(img_align_prompt,"");
		if (imgalign == "r") imgalign = "right";
		if (imgalign == "l") imgalign = "left";
		theform.text.value += "<img src="+imgsrc2+" alt=\""+imgalt+"\" align=\""+imgalign+"\">";
	}
	else {
		//type must be 'other'
		linksrc = "";
		linksrc = theform.mediafile.options[theform.mediafile.selectedIndex].value;
		linktext = "";
		linktext = prompt("Please enter the text for this link: ");
		theform.text.value += "<a href="+linksrc+">"+linktext+"</a>";
	}
	theform.text.focus();
}

// *******************************************************
// the following are the text prompts for buttons etc.
// DO NOT ADD LINE-BREAKS BETWEEN THE "...." QUOTES!

// MINI-HELP MESSAGES

b_text = "Insert BOLD text";
i_text = "Insert ITALIC text";
u_text = "Insert UNDERLINED text";

url_text = "Insert a hyperlink into your message";
email_text = "Insert an email-link into your message";
img_text = "Insert an image into your message";

list_text = "Insert an ordered list into your message";
quote_text = "Insert a quote into your message";

// TEXT FOR POP-UP PROMPTS

tag_prompt = "Enter the text to be formatted:";

font_formatter_prompt = "Enter the text to be formatted with the specified";

img_src_prompt = "Enter the url to the image you would like displayed";
img_alt_prompt = "Enter the text to appear while the image is downloading";
img_align_prompt = "Image alignment: leave blank for no alignment, r for right, l for left";

link_text_prompt = "Enter the text to be displayed for the link (optional)";
link_url_prompt = "Enter the full URL for the link";
link_email_prompt = "Enter the email address for the link";

list_type_prompt = "What type of list do you want? Enter '1' for a numbered list, enter 'b' for an bulleted list.";
list_item_prompt = "Enter a list item.\nLeave the box empty or press 'Cancel' to complete the list.";
// -->
</script>

<div>
<div style="text-align: right; width: 95%">
<div style="float: left">
<input type="checkbox" name="parseXML" id="parseXML" 
    onclick="parseXML()"> <label for="parseXML">Validate as XML</label>
<script type="text/javascript">
<!--
// determine user's parse XML preference and check box appropriately
if (getCookie("parseXML") == "true") {
    document.getElementById('parseXML').checked = true;
} else {
    document.getElementById('parseXML').checked = false;
}
// -->
</script>
</div>

<input type="button" name="bold" value="bold" tabindex="3"
    onclick="htmlcode(weblogEntryFormEx,'strong',tag_prompt)" />
<input type="button" name="italics" value="italics" tabindex="4"
    onclick="htmlcode(weblogEntryFormEx,'em',tag_prompt)" />
<input type="button" name="underline" value="underline" tabindex="5"
    onclick="htmlcode(weblogEntryFormEx,'u',tag_prompt)" />
<input type="button" name="list" value="list" tabindex="6"
    onclick="dolist(weblogEntryFormEx)" />
<input type="button" name="url" value="link" tabindex="7"
    onclick="htmllink(weblogEntryFormEx,'url')" />
<input type="button" name="email" value="e-mail" 
    onclick="htmllink(weblogEntryFormEx,'email')" tabindex="8" />
<input type="button" name="image" value="image" tabindex="9"
    onclick="insertimage(weblogEntryFormEx)" />
</div>


<html:textarea property="text" cols="75" rows="20" 
    styleId="text" style="width: 95%" tabindex="2"/> 
</div>

        <script type="text/javascript">
            <!--
            if (getCookie("editorSize") != null) {
                document.weblogEntryFormEx.text.rows = getCookie("editorSize");
            }
            -->
        </script>

       <div style="float:right">
          <script type="text/javascript">
            <!--
            function changeSize(e,num) {
                e.form.text.rows = e.form.text.rows + num;
                var expires = new Date();
                expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
                setCookie("editorSize",e.form.text.rows,expires);
            }
            -->
          </script>
          <!-- Add buttons to make this textarea taller or shorter -->
          <input type="button" name="taller" value=" &darr; " onclick="changeSize(this,5)" />
          <input type="button" name="shorter" value=" &uarr; " onclick="changeSize(this,-5)" />
       </div>




