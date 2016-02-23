/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/XinhaCore.js */
  /*--------------------------------------------------------------------------
    --  Xinha (is not htmlArea) - http://xinha.org
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Copyright (c) 2005-2008 Xinha Developer Team and contributors
    --  
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    -------------------------------------------------------------------------*/

Xinha.version={"Release":"Trunk","Head":"$HeadURL: http://svn.xinha.org/tags/0.96.1/XinhaCore.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),"Date":"$LastChangedDate: 2010-05-11 17:40:06 -0400 (Tue, 11 May 2010) $".replace(/^[^:]*:\s*([0-9\-]*) ([0-9:]*) ([+0-9]*) \((.*)\)\s*\$/,"$4 $2 $3"),"Revision":"$LastChangedRevision: 1263 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),"RevisionBy":"$LastChangedBy: gogo $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1")};
Xinha._resolveRelativeUrl=function(_1,_2){
if(_2.match(/^([^:]+\:)?\/\//)){
return _2;
}else{
var b=_1.split("/");
if(b[b.length-1]===""){
b.pop();
}
var p=_2.split("/");
if(p[0]=="."){
p.shift();
}
while(p[0]==".."){
b.pop();
p.shift();
}
return b.join("/")+"/"+p.join("/");
}
};
if(typeof _editor_url=="string"){
_editor_url=_editor_url.replace(/\x2f*$/,"/");
if(!_editor_url.match(/^([^:]+\:)?\//)){
(function(){
var _5=window.location.toString().replace(/\?.*$/,"").split("/");
_5.pop();
_editor_url=Xinha._resolveRelativeUrl(_5.join("/"),_editor_url);
})();
}
}else{
alert("WARNING: _editor_url is not set!  You should set this variable to the editor files path; it should preferably be an absolute path, like in '/xinha/', but it can be relative if you prefer.  Further we will try to load the editor files correctly but we'll probably fail.");
_editor_url="";
}
if(typeof _editor_lang=="string"){
_editor_lang=_editor_lang.toLowerCase();
}else{
_editor_lang="en";
}
if(typeof _editor_skin!=="string"){
_editor_skin="";
}
if(typeof _editor_icons!=="string"){
_editor_icons="";
}
var __xinhas=[];
Xinha.agt=navigator.userAgent.toLowerCase();
Xinha.is_ie=((Xinha.agt.indexOf("msie")!=-1)&&(Xinha.agt.indexOf("opera")==-1));
Xinha.ie_version=parseFloat(Xinha.agt.substring(Xinha.agt.indexOf("msie")+5));
Xinha.is_opera=(Xinha.agt.indexOf("opera")!=-1);
if(Xinha.is_opera&&Xinha.agt.match(/opera[\/ ]([0-9.]+)/)){
Xinha.opera_version=parseFloat(RegExp.$1);
}else{
Xinha.opera_version=0;
}
Xinha.is_khtml=(Xinha.agt.indexOf("khtml")!=-1);
Xinha.is_webkit=(Xinha.agt.indexOf("applewebkit")!=-1);
Xinha.webkit_version=parseInt(navigator.appVersion.replace(/.*?AppleWebKit\/([\d]).*?/,"$1"),10);
Xinha.is_safari=(Xinha.agt.indexOf("safari")!=-1);
Xinha.is_chrome=(Xinha.agt.indexOf("chrome")!=-1);
Xinha.is_mac=(Xinha.agt.indexOf("mac")!=-1);
Xinha.is_mac_ie=(Xinha.is_ie&&Xinha.is_mac);
Xinha.is_win_ie=(Xinha.is_ie&&!Xinha.is_mac);
Xinha.is_gecko=(navigator.product=="Gecko")||Xinha.is_opera;
Xinha.is_real_gecko=(navigator.product=="Gecko"&&!Xinha.is_webkit);
Xinha.is_ff2=Xinha.is_real_gecko&&parseInt(navigator.productSub.substr(0,10),10)<20071210;
Xinha.isRunLocally=document.URL.toLowerCase().search(/^file:/)!=-1;
Xinha.is_designMode=(typeof document.designMode!="undefined"&&!Xinha.is_ie);
Xinha.checkSupportedBrowser=function(){
return Xinha.is_real_gecko||(Xinha.is_opera&&Xinha.opera_version>=9.2)||Xinha.ie_version>=5.5||Xinha.webkit_version>=522;
};
Xinha.isSupportedBrowser=Xinha.checkSupportedBrowser();
if(Xinha.isRunLocally&&Xinha.isSupportedBrowser){
alert("Xinha *must* be installed on a web server. Locally opened files (those that use the \"file://\" protocol) cannot properly function. Xinha will try to initialize but may not be correctly loaded.");
}
function Xinha(_6,_7){
if(!Xinha.isSupportedBrowser){
return;
}
if(!_6){
throw new Error("Tried to create Xinha without textarea specified.");
}
if(typeof _7=="undefined"){
this.config=new Xinha.Config();
}else{
this.config=_7;
}
if(typeof _6!="object"){
_6=Xinha.getElementById("textarea",_6);
}
this._textArea=_6;
this._textArea.spellcheck=false;
Xinha.freeLater(this,"_textArea");
this._initial_ta_size={w:_6.style.width?_6.style.width:(_6.offsetWidth?(_6.offsetWidth+"px"):(_6.cols+"em")),h:_6.style.height?_6.style.height:(_6.offsetHeight?(_6.offsetHeight+"px"):(_6.rows+"em"))};
if(document.getElementById("loading_"+_6.id)||this.config.showLoading){
if(!document.getElementById("loading_"+_6.id)){
Xinha.createLoadingMessage(_6);
}
this.setLoadingMessage(Xinha._lc("Constructing object"));
}
this._editMode="wysiwyg";
this.plugins={};
this._timerToolbar=null;
this._timerUndo=null;
this._undoQueue=[this.config.undoSteps];
this._undoPos=-1;
this._customUndo=true;
this._mdoc=document;
this.doctype="";
this.__htmlarea_id_num=__xinhas.length;
__xinhas[this.__htmlarea_id_num]=this;
this._notifyListeners={};
var _8={right:{on:true,container:document.createElement("td"),panels:[]},left:{on:true,container:document.createElement("td"),panels:[]},top:{on:true,container:document.createElement("td"),panels:[]},bottom:{on:true,container:document.createElement("td"),panels:[]}};
for(var i in _8){
if(!_8[i].container){
continue;
}
_8[i].div=_8[i].container;
_8[i].container.className="panels panels_"+i;
Xinha.freeLater(_8[i],"container");
Xinha.freeLater(_8[i],"div");
}
this._panels=_8;
this._statusBar=null;
this._statusBarTree=null;
this._statusBarTextMode=null;
this._statusBarItems=[];
this._framework={};
this._htmlArea=null;
this._iframe=null;
this._doc=null;
this._toolBar=this._toolbar=null;
this._toolbarObjects={};
this.plugins.Events={name:"Events",developer:"The Xinha Core Developer Team",instance:_7.Events};
};
Xinha.onload=function(){
};
Xinha.init=function(){
Xinha.onload();
};
Xinha.RE_tagName=/(<\/|<)\s*([^ \t\n>]+)/ig;
Xinha.RE_doctype=/(<!doctype((.|\n)*?)>)\n?/i;
Xinha.RE_head=/<head>((.|\n)*?)<\/head>/i;
Xinha.RE_body=/<body[^>]*>((.|\n|\r|\t)*?)<\/body>/i;
Xinha.RE_Specials=/([\/\^$*+?.()|{}\[\]])/g;
Xinha.escapeStringForRegExp=function(_a){
return _a.replace(Xinha.RE_Specials,"\\$1");
};
Xinha.RE_email=/^[_a-z\d\-\.]{3,}@[_a-z\d\-]{2,}(\.[_a-z\d\-]{2,})+$/i;
Xinha.RE_url=/(https?:\/\/)?(([a-z0-9_]+:[a-z0-9_]+@)?[a-z0-9_\-]{2,}(\.[a-z0-9_\-]{2,}){2,}(:[0-9]+)?(\/\S+)*)/i;
Xinha.Config=function(){
this.version=Xinha.version.Revision;
this.width="auto";
this.height="auto";
this.sizeIncludesBars=true;
this.sizeIncludesPanels=true;
this.panel_dimensions={left:"200px",right:"200px",top:"100px",bottom:"100px"};
this.iframeWidth=null;
this.statusBar=true;
this.htmlareaPaste=false;
this.mozParaHandler="best";
this.getHtmlMethod="DOMwalk";
this.undoSteps=20;
this.undoTimeout=500;
this.changeJustifyWithDirection=false;
this.fullPage=false;
this.pageStyle="";
this.pageStyleSheets=[];
this.baseHref=null;
this.expandRelativeUrl=true;
this.stripBaseHref=true;
this.stripSelfNamedAnchors=true;
this.only7BitPrintablesInURLs=true;
this.sevenBitClean=false;
this.specialReplacements={};
this.inwardHtml=function(_b){
return _b;
};
this.outwardHtml=function(_c){
return _c;
};
this.autofocus=false;
this.killWordOnPaste=true;
this.makeLinkShowsTarget=true;
this.charSet=(typeof document.characterSet!="undefined")?document.characterSet:document.charset;
this.browserQuirksMode=null;
this.imgURL="images/";
this.popupURL="popups/";
this.htmlRemoveTags=null;
this.flowToolbars=true;
this.toolbarAlign="left";
this.showFontStylesInToolbar=false;
this.showLoading=false;
this.stripScripts=true;
this.convertUrlsToLinks=true;
this.colorPickerCellSize="6px";
this.colorPickerGranularity=18;
this.colorPickerPosition="bottom,right";
this.colorPickerWebSafe=false;
this.colorPickerSaveColors=20;
this.fullScreen=false;
this.fullScreenMargins=[0,0,0,0];
this.fullScreenSizeDownMethod="initSize";
this.toolbar=[["popupeditor"],["separator","formatblock","fontname","fontsize","bold","italic","underline","strikethrough"],["separator","forecolor","hilitecolor","textindicator"],["separator","subscript","superscript"],["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],["separator","insertorderedlist","insertunorderedlist","outdent","indent"],["separator","inserthorizontalrule","createlink","insertimage","inserttable"],["linebreak","separator","undo","redo","selectall","print"],(Xinha.is_gecko?[]:["cut","copy","paste","overwrite","saveas"]),["separator","killword","clearfonts","removeformat","toggleborders","splitblock","lefttoright","righttoleft"],["separator","htmlmode","showhelp","about"]];
this.fontname={"&#8212; font &#8212;":"","Arial":"arial,helvetica,sans-serif","Courier New":"courier new,courier,monospace","Georgia":"georgia,times new roman,times,serif","Tahoma":"tahoma,arial,helvetica,sans-serif","Times New Roman":"times new roman,times,serif","Verdana":"verdana,arial,helvetica,sans-serif","impact":"impact","WingDings":"wingdings"};
this.fontsize={"&#8212; size &#8212;":"","1 (8 pt)":"1","2 (10 pt)":"2","3 (12 pt)":"3","4 (14 pt)":"4","5 (18 pt)":"5","6 (24 pt)":"6","7 (36 pt)":"7"};
this.formatblock={"&#8212; format &#8212;":"","Heading 1":"h1","Heading 2":"h2","Heading 3":"h3","Heading 4":"h4","Heading 5":"h5","Heading 6":"h6","Normal":"p","Address":"address","Formatted":"pre"};
this.dialogOptions={"centered":true,"greyout":true,"closeOnEscape":true};
this.Events={};
this.customSelects={};
this.debug=false;
this.URIs={"blank":_editor_url+"popups/blank.html","link":_editor_url+"modules/CreateLink/link.html","insert_image":_editor_url+"modules/InsertImage/insert_image.html","insert_table":_editor_url+"modules/InsertTable/insert_table.html","select_color":_editor_url+"popups/select_color.html","help":_editor_url+"popups/editor_help.html"};
this.btnList={bold:["Bold",Xinha._lc({key:"button_bold",string:["ed_buttons_main.png",3,2]},"Xinha"),false,function(e){
e.execCommand("bold");
}],italic:["Italic",Xinha._lc({key:"button_italic",string:["ed_buttons_main.png",2,2]},"Xinha"),false,function(e){
e.execCommand("italic");
}],underline:["Underline",Xinha._lc({key:"button_underline",string:["ed_buttons_main.png",2,0]},"Xinha"),false,function(e){
e.execCommand("underline");
}],strikethrough:["Strikethrough",Xinha._lc({key:"button_strikethrough",string:["ed_buttons_main.png",3,0]},"Xinha"),false,function(e){
e.execCommand("strikethrough");
}],subscript:["Subscript",Xinha._lc({key:"button_subscript",string:["ed_buttons_main.png",3,1]},"Xinha"),false,function(e){
e.execCommand("subscript");
}],superscript:["Superscript",Xinha._lc({key:"button_superscript",string:["ed_buttons_main.png",2,1]},"Xinha"),false,function(e){
e.execCommand("superscript");
}],justifyleft:["Justify Left",["ed_buttons_main.png",0,0],false,function(e){
e.execCommand("justifyleft");
}],justifycenter:["Justify Center",["ed_buttons_main.png",1,1],false,function(e){
e.execCommand("justifycenter");
}],justifyright:["Justify Right",["ed_buttons_main.png",1,0],false,function(e){
e.execCommand("justifyright");
}],justifyfull:["Justify Full",["ed_buttons_main.png",0,1],false,function(e){
e.execCommand("justifyfull");
}],orderedlist:["Ordered List",["ed_buttons_main.png",0,3],false,function(e){
e.execCommand("insertorderedlist");
}],unorderedlist:["Bulleted List",["ed_buttons_main.png",1,3],false,function(e){
e.execCommand("insertunorderedlist");
}],insertorderedlist:["Ordered List",["ed_buttons_main.png",0,3],false,function(e){
e.execCommand("insertorderedlist");
}],insertunorderedlist:["Bulleted List",["ed_buttons_main.png",1,3],false,function(e){
e.execCommand("insertunorderedlist");
}],outdent:["Decrease Indent",["ed_buttons_main.png",1,2],false,function(e){
e.execCommand("outdent");
}],indent:["Increase Indent",["ed_buttons_main.png",0,2],false,function(e){
e.execCommand("indent");
}],forecolor:["Font Color",["ed_buttons_main.png",3,3],false,function(e){
e.execCommand("forecolor");
}],hilitecolor:["Background Color",["ed_buttons_main.png",2,3],false,function(e){
e.execCommand("hilitecolor");
}],undo:["Undoes your last action",["ed_buttons_main.png",4,2],false,function(e){
e.execCommand("undo");
}],redo:["Redoes your last action",["ed_buttons_main.png",5,2],false,function(e){
e.execCommand("redo");
}],cut:["Cut selection",["ed_buttons_main.png",5,0],false,function(e,cmd){
e.execCommand(cmd);
}],copy:["Copy selection",["ed_buttons_main.png",4,0],false,function(e,cmd){
e.execCommand(cmd);
}],paste:["Paste from clipboard",["ed_buttons_main.png",4,1],false,function(e,cmd){
e.execCommand(cmd);
}],selectall:["Select all",["ed_buttons_main.png",3,5],false,function(e){
e.execCommand("selectall");
}],inserthorizontalrule:["Horizontal Rule",["ed_buttons_main.png",6,0],false,function(e){
e.execCommand("inserthorizontalrule");
}],createlink:["Insert Web Link",["ed_buttons_main.png",6,1],false,function(e){
e._createLink();
}],insertimage:["Insert/Modify Image",["ed_buttons_main.png",6,3],false,function(e){
e.execCommand("insertimage");
}],inserttable:["Insert Table",["ed_buttons_main.png",6,2],false,function(e){
e.execCommand("inserttable");
}],htmlmode:["Toggle HTML Source",["ed_buttons_main.png",7,0],true,function(e){
e.execCommand("htmlmode");
}],toggleborders:["Toggle Borders",["ed_buttons_main.png",7,2],false,function(e){
e._toggleBorders();
}],print:["Print document",["ed_buttons_main.png",8,1],false,function(e){
if(Xinha.is_gecko){
e._iframe.contentWindow.print();
}else{
e.focusEditor();
print();
}
}],saveas:["Save as",["ed_buttons_main.png",9,1],false,function(e){
e.execCommand("saveas",false,"noname.htm");
}],about:["About this editor",["ed_buttons_main.png",8,2],true,function(e){
e.getPluginInstance("AboutBox").show();
}],showhelp:["Help using editor",["ed_buttons_main.png",9,2],true,function(e){
e.execCommand("showhelp");
}],splitblock:["Split Block","ed_splitblock.gif",false,function(e){
e._splitBlock();
}],lefttoright:["Direction left to right",["ed_buttons_main.png",0,2],false,function(e){
e.execCommand("lefttoright");
}],righttoleft:["Direction right to left",["ed_buttons_main.png",1,2],false,function(e){
e.execCommand("righttoleft");
}],overwrite:["Insert/Overwrite","ed_overwrite.gif",false,function(e){
e.execCommand("overwrite");
}],wordclean:["MS Word Cleaner",["ed_buttons_main.png",5,3],false,function(e){
e._wordClean();
}],clearfonts:["Clear Inline Font Specifications",["ed_buttons_main.png",5,4],true,function(e){
e._clearFonts();
}],removeformat:["Remove formatting",["ed_buttons_main.png",4,4],false,function(e){
e.execCommand("removeformat");
}],killword:["Clear MSOffice tags",["ed_buttons_main.png",4,3],false,function(e){
e.execCommand("killword");
}]};
this.dblclickList={"a":[function(e,_3b){
e._createLink(_3b);
}],"img":[function(e,_3d){
e._insertImage(_3d);
}]};
this.iconList={dialogCaption:_editor_url+"images/xinha-small-icon.gif",wysiwygmode:[_editor_url+"images/ed_buttons_main.png",7,1]};
for(var i in this.btnList){
var btn=this.btnList[i];
if(typeof btn!="object"){
continue;
}
if(typeof btn[1]!="string"){
btn[1][0]=_editor_url+this.imgURL+btn[1][0];
}else{
btn[1]=_editor_url+this.imgURL+btn[1];
}
btn[0]=Xinha._lc(btn[0]);
}
};
Xinha.Config.prototype.registerIcon=function(id,_41){
this.iconList[id]=_41;
};
Xinha.Config.prototype.registerButton=function(id,_43,_44,_45,_46,_47){
if(typeof id=="string"){
this.btnList[id]=[_43,_44,_45,_46,_47];
}else{
if(typeof id=="object"){
this.btnList[id.id]=[id.tooltip,id.image,id.textMode,id.action,id.context];
}else{
alert("ERROR [Xinha.Config::registerButton]:\ninvalid arguments");
return false;
}
}
};
Xinha.prototype.registerPanel=function(_48,_49){
if(!_48){
_48="right";
}
this.setLoadingMessage("Register "+_48+" panel ");
var _4a=this.addPanel(_48);
if(_49){
_49.drawPanelIn(_4a);
}
};
Xinha.Config.prototype.registerDropdown=function(_4b){
this.customSelects[_4b.id]=_4b;
};
Xinha.Config.prototype.hideSomeButtons=function(_4c){
var _4d=this.toolbar;
for(var i=_4d.length;--i>=0;){
var _4f=_4d[i];
for(var j=_4f.length;--j>=0;){
if(_4c.indexOf(" "+_4f[j]+" ")>=0){
var len=1;
if(/separator|space/.test(_4f[j+1])){
len=2;
}
_4f.splice(j,len);
}
}
}
};
Xinha.Config.prototype.addToolbarElement=function(id,_53,_54){
var _55=this.toolbar;
var a,i,j,o,sid;
var _5b=false;
var _5c=false;
var _5d=0;
var _5e=0;
var _5f=0;
var _60=false;
var _61=false;
if((id&&typeof id=="object")&&(id.constructor==Array)){
_5b=true;
}
if((_53&&typeof _53=="object")&&(_53.constructor==Array)){
_5c=true;
_5d=_53.length;
}
if(_5b){
for(i=0;i<id.length;++i){
if((id[i]!="separator")&&(id[i].indexOf("T[")!==0)){
sid=id[i];
}
}
}else{
sid=id;
}
for(i=0;i<_55.length;++i){
a=_55[i];
for(j=0;j<a.length;++j){
if(a[j]==sid){
return;
}
}
}
for(i=0;!_61&&i<_55.length;++i){
a=_55[i];
for(j=0;!_61&&j<a.length;++j){
if(_5c){
for(o=0;o<_5d;++o){
if(a[j]==_53[o]){
if(o===0){
_61=true;
j--;
break;
}else{
_5f=i;
_5e=j;
_5d=o;
}
}
}
}else{
if(a[j]==_53){
_61=true;
break;
}
}
}
}
if(!_61&&_5c){
if(_53.length!=_5d){
j=_5e;
a=_55[_5f];
_61=true;
}
}
if(_61){
if(_54===0){
if(_5b){
a[j]=id[id.length-1];
for(i=id.length-1;--i>=0;){
a.splice(j,0,id[i]);
}
}else{
a[j]=id;
}
}else{
if(_54<0){
j=j+_54+1;
}else{
if(_54>0){
j=j+_54;
}
}
if(_5b){
for(i=id.length;--i>=0;){
a.splice(j,0,id[i]);
}
}else{
a.splice(j,0,id);
}
}
}else{
_55[0].splice(0,0,"separator");
if(_5b){
for(i=id.length;--i>=0;){
_55[0].splice(0,0,id[i]);
}
}else{
_55[0].splice(0,0,id);
}
}
};
Xinha.Config.prototype.removeToolbarElement=Xinha.Config.prototype.hideSomeButtons;
Xinha.replaceAll=function(_62){
var tas=document.getElementsByTagName("textarea");
for(var i=tas.length;i>0;new Xinha(tas[--i],_62).generate()){
}
};
Xinha.replace=function(id,_66){
var ta=Xinha.getElementById("textarea",id);
return ta?new Xinha(ta,_66).generate():null;
};
Xinha.prototype._createToolbar=function(){
this.setLoadingMessage(Xinha._lc("Create Toolbar"));
var _68=this;
var _69=document.createElement("div");
this._toolBar=this._toolbar=_69;
_69.className="toolbar";
_69.align=this.config.toolbarAlign;
Xinha.freeLater(this,"_toolBar");
Xinha.freeLater(this,"_toolbar");
var _6a=null;
var _6b={};
this._toolbarObjects=_6b;
this._createToolbar1(_68,_69,_6b);
function noselect(e){
if(e.tagName){
e.unselectable="on";
}
if(e.childNodes){
for(var i=0;i<e.childNodes.length;i++){
if(e.tagName){
noselect(e.childNodes(i));
}
}
}
};
if(Xinha.is_ie){
noselect(_69);
}
this._htmlArea.appendChild(_69);
return _69;
};
Xinha.prototype._setConfig=function(_6e){
this.config=_6e;
};
Xinha.prototype._rebuildToolbar=function(){
this._createToolbar1(this,this._toolbar,this._toolbarObjects);
if(Xinha._currentlyActiveEditor){
if(Xinha._currentlyActiveEditor==this){
this.activateEditor();
}
}else{
this.disableToolbar();
}
};
Xinha._createToolbarBreakingElement=function(){
var brk=document.createElement("div");
brk.style.height="1px";
brk.style.width="1px";
brk.style.lineHeight="1px";
brk.style.fontSize="1px";
brk.style.clear="both";
return brk;
};
Xinha.prototype._createToolbar1=function(_70,_71,_72){
while(_71.lastChild){
_71.removeChild(_71.lastChild);
}
var _73;
if(_70.config.flowToolbars){
_71.appendChild(Xinha._createToolbarBreakingElement());
}
function newLine(){
if(typeof _73!="undefined"&&_73.childNodes.length===0){
return;
}
var _74=document.createElement("table");
_74.border="0px";
_74.cellSpacing="0px";
_74.cellPadding="0px";
if(_70.config.flowToolbars){
if(Xinha.is_ie){
_74.style.styleFloat="left";
}else{
_74.style.cssFloat="left";
}
}
_71.appendChild(_74);
var _75=document.createElement("tbody");
_74.appendChild(_75);
_73=document.createElement("tr");
_75.appendChild(_73);
_74.className="toolbarRow";
};
newLine();
function setButtonStatus(id,_77){
var _78=this[id];
var el=this.element;
if(_78!=_77){
switch(id){
case "enabled":
if(_77){
Xinha._removeClass(el,"buttonDisabled");
el.disabled=false;
}else{
Xinha._addClass(el,"buttonDisabled");
el.disabled=true;
}
break;
case "active":
if(_77){
Xinha._addClass(el,"buttonPressed");
}else{
Xinha._removeClass(el,"buttonPressed");
}
break;
}
this[id]=_77;
}
};
function createSelect(txt){
var _7b=null;
var el=null;
var cmd=null;
var _7e=_70.config.customSelects;
var _7f=null;
var _80="";
switch(txt){
case "fontsize":
case "fontname":
case "formatblock":
_7b=_70.config[txt];
cmd=txt;
break;
default:
cmd=txt;
var _81=_7e[cmd];
if(typeof _81!="undefined"){
_7b=_81.options;
_7f=_81.context;
if(typeof _81.tooltip!="undefined"){
_80=_81.tooltip;
}
}else{
alert("ERROR [createSelect]:\nCan't find the requested dropdown definition");
}
break;
}
if(_7b){
el=document.createElement("select");
el.title=_80;
el.style.width="auto";
el.name=txt;
var obj={name:txt,element:el,enabled:true,text:false,cmd:cmd,state:setButtonStatus,context:_7f};
Xinha.freeLater(obj);
_72[txt]=obj;
for(var i in _7b){
if(typeof _7b[i]!="string"){
continue;
}
var op=document.createElement("option");
op.innerHTML=Xinha._lc(i);
op.value=_7b[i];
if(txt=="fontname"&&_70.config.showFontStylesInToolbar){
op.style.fontFamily=_7b[i];
}
el.appendChild(op);
}
Xinha._addEvent(el,"change",function(){
_70._comboSelected(el,txt);
});
}
return el;
};
function createButton(txt){
var el,btn,obj=null;
switch(txt){
case "separator":
if(_70.config.flowToolbars){
newLine();
}
el=document.createElement("div");
el.className="separator";
break;
case "space":
el=document.createElement("div");
el.className="space";
break;
case "linebreak":
newLine();
return false;
case "textindicator":
el=document.createElement("div");
el.appendChild(document.createTextNode("A"));
el.className="indicator";
el.title=Xinha._lc("Current style");
obj={name:txt,element:el,enabled:true,active:false,text:false,cmd:"textindicator",state:setButtonStatus};
Xinha.freeLater(obj);
_72[txt]=obj;
break;
default:
btn=_70.config.btnList[txt];
}
if(!el&&btn){
el=document.createElement("a");
el.style.display="block";
el.href="javascript:void(0)";
el.style.textDecoration="none";
el.title=btn[0];
el.className="button";
el.style.direction="ltr";
obj={name:txt,element:el,enabled:true,active:false,text:btn[2],cmd:btn[3],state:setButtonStatus,context:btn[4]||null};
Xinha.freeLater(el);
Xinha.freeLater(obj);
_72[txt]=obj;
el.ondrag=function(){
return false;
};
Xinha._addEvent(el,"mouseout",function(ev){
if(obj.enabled){
Xinha._removeClass(el,"buttonActive");
if(obj.active){
Xinha._addClass(el,"buttonPressed");
}
}
});
Xinha._addEvent(el,"mousedown",function(ev){
if(obj.enabled){
Xinha._addClass(el,"buttonActive");
Xinha._removeClass(el,"buttonPressed");
Xinha._stopEvent(Xinha.is_ie?window.event:ev);
}
});
Xinha._addEvent(el,"click",function(ev){
ev=ev||window.event;
_70.btnClickEvent={clientX:ev.clientX,clientY:ev.clientY};
if(obj.enabled){
Xinha._removeClass(el,"buttonActive");
if(Xinha.is_gecko){
_70.activateEditor();
}
obj.cmd(_70,obj.name,obj,ev);
Xinha._stopEvent(ev);
}
});
var _8c=Xinha.makeBtnImg(btn[1]);
var img=_8c.firstChild;
Xinha.freeLater(_8c);
Xinha.freeLater(img);
el.appendChild(_8c);
obj.imgel=img;
obj.swapImage=function(_8e){
if(typeof _8e!="string"){
img.src=_8e[0];
img.style.position="relative";
img.style.top=_8e[2]?("-"+(18*(_8e[2]+1))+"px"):"-18px";
img.style.left=_8e[1]?("-"+(18*(_8e[1]+1))+"px"):"-18px";
}else{
obj.imgel.src=_8e;
img.style.top="0px";
img.style.left="0px";
}
};
}else{
if(!el){
el=createSelect(txt);
}
}
return el;
};
var _8f=true;
for(var i=0;i<this.config.toolbar.length;++i){
if(!_8f){
}else{
_8f=false;
}
if(this.config.toolbar[i]===null){
this.config.toolbar[i]=["separator"];
}
var _91=this.config.toolbar[i];
for(var j=0;j<_91.length;++j){
var _93=_91[j];
var _94;
if(/^([IT])\[(.*?)\]/.test(_93)){
var _95=RegExp.$1=="I";
var _96=RegExp.$2;
if(_95){
_96=Xinha._lc(_96);
}
_94=document.createElement("td");
_73.appendChild(_94);
_94.className="label";
_94.innerHTML=_96;
}else{
if(typeof _93!="function"){
var _97=createButton(_93);
if(_97){
_94=document.createElement("td");
_94.className="toolbarElement";
_73.appendChild(_94);
_94.appendChild(_97);
}else{
if(_97===null){
alert("FIXME: Unknown toolbar item: "+_93);
}
}
}
}
}
}
if(_70.config.flowToolbars){
_71.appendChild(Xinha._createToolbarBreakingElement());
}
return _71;
};
Xinha.makeBtnImg=function(_98,doc){
if(!doc){
doc=document;
}
if(!doc._xinhaImgCache){
doc._xinhaImgCache={};
Xinha.freeLater(doc._xinhaImgCache);
}
var _9a=null;
if(Xinha.is_ie&&((!doc.compatMode)||(doc.compatMode&&doc.compatMode=="BackCompat"))){
_9a=doc.createElement("span");
}else{
_9a=doc.createElement("div");
_9a.style.position="relative";
}
_9a.style.overflow="hidden";
_9a.style.width="18px";
_9a.style.height="18px";
_9a.className="buttonImageContainer";
var img=null;
if(typeof _98=="string"){
if(doc._xinhaImgCache[_98]){
img=doc._xinhaImgCache[_98].cloneNode();
}else{
if(Xinha.ie_version<7&&/\.png$/.test(_98[0])){
img=doc.createElement("span");
img.style.display="block";
img.style.width="18px";
img.style.height="18px";
img.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\""+_98+"\")";
img.unselectable="on";
}else{
img=doc.createElement("img");
img.src=_98;
}
}
}else{
if(doc._xinhaImgCache[_98[0]]){
img=doc._xinhaImgCache[_98[0]].cloneNode();
}else{
if(Xinha.ie_version<7&&/\.png$/.test(_98[0])){
img=doc.createElement("span");
img.style.display="block";
img.style.width="18px";
img.style.height="18px";
img.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\""+_98[0]+"\")";
img.unselectable="on";
}else{
img=doc.createElement("img");
img.src=_98[0];
}
img.style.position="relative";
}
img.style.top=_98[2]?("-"+(18*(_98[2]+1))+"px"):"-18px";
img.style.left=_98[1]?("-"+(18*(_98[1]+1))+"px"):"-18px";
}
_9a.appendChild(img);
return _9a;
};
Xinha.prototype._createStatusBar=function(){
this.setLoadingMessage(Xinha._lc("Create Statusbar"));
var _9c=document.createElement("div");
_9c.style.position="relative";
_9c.className="statusBar";
_9c.style.width="100%";
Xinha.freeLater(this,"_statusBar");
var _9d=document.createElement("div");
_9d.className="statusBarWidgetContainer";
_9d.style.position="absolute";
_9d.style.right="0";
_9d.style.top="0";
_9d.style.padding="3px 3px 3px 10px";
_9c.appendChild(_9d);
var _9e=document.createElement("span");
_9e.className="statusBarTree";
_9e.innerHTML=Xinha._lc("Path")+": ";
this._statusBarTree=_9e;
Xinha.freeLater(this,"_statusBarTree");
_9c.appendChild(_9e);
var _9f=document.createElement("span");
_9f.innerHTML=Xinha.htmlEncode(Xinha._lc("You are in TEXT MODE.  Use the [<>] button to switch back to WYSIWYG."));
_9f.style.display="none";
this._statusBarTextMode=_9f;
Xinha.freeLater(this,"_statusBarTextMode");
_9c.appendChild(_9f);
_9c.style.whiteSpace="nowrap";
var _a0=this;
this.notifyOn("before_resize",function(evt,_a2){
_a0._statusBar.style.width=null;
});
this.notifyOn("resize",function(evt,_a4){
if(Xinha.is_ie&&Xinha.ie_version==6){
_a0._statusBar.style.width="100%";
}else{
var _a5=_a4["width"];
_a0._statusBar.style.width=_a5+"px";
}
});
this.notifyOn("modechange",function(evt,_a7){
for(var i in _a0._statusWidgets){
var _a9=_a0._statusWidgets[i];
for(var _aa=0;_aa<_a9.modes.length;_aa++){
if(_a9.modes[_aa]==_a7.mode){
var _ab=true;
}
}
if(typeof _ab=="undefined"){
_a9.block.style.display="none";
}else{
_a9.block.style.display="";
}
}
});
if(!this.config.statusBar){
_9c.style.display="none";
}
return _9c;
};
Xinha.prototype.registerStatusWidget=function(id,_ad){
_ad=_ad||["wysiwyg"];
if(!this._statusWidgets){
this._statusWidgets={};
}
var _ae=document.createElement("div");
_ae.className="statusBarWidget";
_ae=this._statusBar.firstChild.appendChild(_ae);
var _af=false;
for(var i=0;i<_ad.length;i++){
if(_ad[i]==this._editMode){
_af=true;
}
}
_ae.style.display=_af==true?"":"none";
this._statusWidgets[id]={block:_ae,modes:_ad};
return _ae;
};
Xinha.prototype.generate=function(){
if(!Xinha.isSupportedBrowser){
return;
}
var i;
var _b2=this;
var url;
var _b4=false;
var _b5=document.getElementsByTagName("link");
if(!document.getElementById("XinhaCoreDesign")){
_editor_css=(typeof _editor_css=="string")?_editor_css:"Xinha.css";
for(i=0;i<_b5.length;i++){
if((_b5[i].rel=="stylesheet")&&(_b5[i].href==_editor_url+_editor_css)){
_b4=true;
}
}
if(!_b4){
Xinha.loadStyle(_editor_css,null,"XinhaCoreDesign",true);
}
}
if(_editor_skin!==""&&!document.getElementById("XinhaSkin")){
_b4=false;
for(i=0;i<_b5.length;i++){
if((_b5[i].rel=="stylesheet")&&(_b5[i].href==_editor_url+"skins/"+_editor_skin+"/skin.css")){
_b4=true;
}
}
if(!_b4){
Xinha.loadStyle("skins/"+_editor_skin+"/skin.css",null,"XinhaSkin");
}
}
var _b6=function(){
_b2.generate();
};
if(Xinha.is_ie){
url=_editor_url+"modules/InternetExplorer/InternetExplorer.js";
if(!Xinha.loadPlugins([{plugin:"InternetExplorer",url:url}],_b6)){
return false;
}
if(!this.plugins.InternetExplorer){
_b2._browserSpecificPlugin=_b2.registerPlugin("InternetExplorer");
}
}else{
if(Xinha.is_webkit){
url=_editor_url+"modules/WebKit/WebKit.js";
if(!Xinha.loadPlugins([{plugin:"WebKit",url:url}],_b6)){
return false;
}
if(!this.plugins.Webkit){
_b2._browserSpecificPlugin=_b2.registerPlugin("WebKit");
}
}else{
if(Xinha.is_opera){
url=_editor_url+"modules/Opera/Opera.js";
if(!Xinha.loadPlugins([{plugin:"Opera",url:url}],_b6)){
return false;
}
if(!this.plugins.Opera){
_b2._browserSpecificPlugin=_b2.registerPlugin("Opera");
}
}else{
if(Xinha.is_gecko){
url=_editor_url+"modules/Gecko/Gecko.js";
if(!Xinha.loadPlugins([{plugin:"Gecko",url:url}],_b6)){
return false;
}
if(!this.plugins.Gecko){
_b2._browserSpecificPlugin=_b2.registerPlugin("Gecko");
}
}
}
}
}
if(typeof Dialog=="undefined"&&!Xinha._loadback(_editor_url+"modules/Dialogs/dialog.js",_b6,this)){
return false;
}
if(typeof Xinha.Dialog=="undefined"&&!Xinha._loadback(_editor_url+"modules/Dialogs/XinhaDialog.js",_b6,this)){
return false;
}
url=_editor_url+"modules/FullScreen/full-screen.js";
if(!Xinha.loadPlugins([{plugin:"FullScreen",url:url}],_b6)){
return false;
}
url=_editor_url+"modules/ColorPicker/ColorPicker.js";
if(!Xinha.loadPlugins([{plugin:"ColorPicker",url:url}],_b6)){
return false;
}else{
if(typeof Xinha.getPluginConstructor("ColorPicker")!="undefined"&&!this.plugins.colorPicker){
_b2.registerPlugin("ColorPicker");
}
}
var _b7=_b2.config.toolbar;
for(i=_b7.length;--i>=0;){
for(var j=_b7[i].length;--j>=0;){
switch(_b7[i][j]){
case "popupeditor":
if(!this.plugins.FullScreen){
_b2.registerPlugin("FullScreen");
}
break;
case "insertimage":
url=_editor_url+"modules/InsertImage/insert_image.js";
if(typeof Xinha.prototype._insertImage=="undefined"&&!Xinha.loadPlugins([{plugin:"InsertImage",url:url}],_b6)){
return false;
}else{
if(typeof Xinha.getPluginConstructor("InsertImage")!="undefined"&&!this.plugins.InsertImage){
_b2.registerPlugin("InsertImage");
}
}
break;
case "createlink":
url=_editor_url+"modules/CreateLink/link.js";
if(typeof Xinha.getPluginConstructor("Linker")=="undefined"&&!Xinha.loadPlugins([{plugin:"CreateLink",url:url}],_b6)){
return false;
}else{
if(typeof Xinha.getPluginConstructor("CreateLink")!="undefined"&&!this.plugins.CreateLink){
_b2.registerPlugin("CreateLink");
}
}
break;
case "inserttable":
url=_editor_url+"modules/InsertTable/insert_table.js";
if(!Xinha.loadPlugins([{plugin:"InsertTable",url:url}],_b6)){
return false;
}else{
if(typeof Xinha.getPluginConstructor("InsertTable")!="undefined"&&!this.plugins.InsertTable){
_b2.registerPlugin("InsertTable");
}
}
break;
case "about":
url=_editor_url+"modules/AboutBox/AboutBox.js";
if(!Xinha.loadPlugins([{plugin:"AboutBox",url:url}],_b6)){
return false;
}else{
if(typeof Xinha.getPluginConstructor("AboutBox")!="undefined"&&!this.plugins.AboutBox){
_b2.registerPlugin("AboutBox");
}
}
break;
}
}
}
if(Xinha.is_gecko&&_b2.config.mozParaHandler!="built-in"){
if(!Xinha.loadPlugins([{plugin:"EnterParagraphs",url:_editor_url+"modules/Gecko/paraHandlerBest.js"}],_b6)){
return false;
}
if(!this.plugins.EnterParagraphs){
_b2.registerPlugin("EnterParagraphs");
}
}
var _b9=this.config.getHtmlMethod=="TransformInnerHTML"?_editor_url+"modules/GetHtml/TransformInnerHTML.js":_editor_url+"modules/GetHtml/DOMwalk.js";
if(!Xinha.loadPlugins([{plugin:"GetHtmlImplementation",url:_b9}],_b6)){
return false;
}else{
if(!this.plugins.GetHtmlImplementation){
_b2.registerPlugin("GetHtmlImplementation");
}
}
function getTextContent(_ba){
return _ba.textContent||_ba.text;
};
if(_editor_skin){
this.skinInfo={};
var _bb=Xinha._geturlcontent(_editor_url+"skins/"+_editor_skin+"/skin.xml",true);
if(_bb){
var _bc=_bb.getElementsByTagName("meta");
for(i=0;i<_bc.length;i++){
this.skinInfo[_bc[i].getAttribute("name")]=_bc[i].getAttribute("value");
}
var _bd=_bb.getElementsByTagName("recommendedIcons");
if(!_editor_icons&&_bd.length&&getTextContent(_bd[0])){
_editor_icons=getTextContent(_bd[0]);
}
}
}
if(_editor_icons){
var _be=Xinha._geturlcontent(_editor_url+"iconsets/"+_editor_icons+"/iconset.xml",true);
if(_be){
var _bf=_be.getElementsByTagName("icon");
var _c0,id,_c2,_c3,x,y;
for(i=0;i<_bf.length;i++){
_c0=_bf[i];
id=_c0.getAttribute("id");
if(_c0.getElementsByTagName(_editor_lang).length){
_c0=_c0.getElementsByTagName(_editor_lang)[0];
}else{
_c0=_c0.getElementsByTagName("default")[0];
}
_c2=getTextContent(_c0.getElementsByTagName("path")[0]);
_c2=(!/^\//.test(_c2)?_editor_url:"")+_c2;
_c3=_c0.getAttribute("type");
if(_c3=="map"){
x=parseInt(getTextContent(_c0.getElementsByTagName("x")[0]),10);
y=parseInt(getTextContent(_c0.getElementsByTagName("y")[0]),10);
if(this.config.btnList[id]){
this.config.btnList[id][1]=[_c2,x,y];
}
if(this.config.iconList[id]){
this.config.iconList[id]=[_c2,x,y];
}
}else{
if(this.config.btnList[id]){
this.config.btnList[id][1]=_c2;
}
if(this.config.iconList[id]){
this.config.iconList[id]=_c2;
}
}
}
}
}
this.setLoadingMessage(Xinha._lc("Generate Xinha framework"));
this._framework={"table":document.createElement("table"),"tbody":document.createElement("tbody"),"tb_row":document.createElement("tr"),"tb_cell":document.createElement("td"),"tp_row":document.createElement("tr"),"tp_cell":this._panels.top.container,"ler_row":document.createElement("tr"),"lp_cell":this._panels.left.container,"ed_cell":document.createElement("td"),"rp_cell":this._panels.right.container,"bp_row":document.createElement("tr"),"bp_cell":this._panels.bottom.container,"sb_row":document.createElement("tr"),"sb_cell":document.createElement("td")};
Xinha.freeLater(this._framework);
var fw=this._framework;
fw.table.border="0";
fw.table.cellPadding="0";
fw.table.cellSpacing="0";
fw.tb_row.style.verticalAlign="top";
fw.tp_row.style.verticalAlign="top";
fw.ler_row.style.verticalAlign="top";
fw.bp_row.style.verticalAlign="top";
fw.sb_row.style.verticalAlign="top";
fw.ed_cell.style.position="relative";
fw.tb_row.appendChild(fw.tb_cell);
fw.tb_cell.colSpan=3;
fw.tp_row.appendChild(fw.tp_cell);
fw.tp_cell.colSpan=3;
fw.ler_row.appendChild(fw.lp_cell);
fw.ler_row.appendChild(fw.ed_cell);
fw.ler_row.appendChild(fw.rp_cell);
fw.bp_row.appendChild(fw.bp_cell);
fw.bp_cell.colSpan=3;
fw.sb_row.appendChild(fw.sb_cell);
fw.sb_cell.colSpan=3;
fw.tbody.appendChild(fw.tb_row);
fw.tbody.appendChild(fw.tp_row);
fw.tbody.appendChild(fw.ler_row);
fw.tbody.appendChild(fw.bp_row);
fw.tbody.appendChild(fw.sb_row);
fw.table.appendChild(fw.tbody);
var _c7=fw.table;
this._htmlArea=_c7;
Xinha.freeLater(this,"_htmlArea");
_c7.className="htmlarea";
fw.tb_cell.appendChild(this._createToolbar());
var _c8=document.createElement("iframe");
_c8.src=this.popupURL(_b2.config.URIs.blank);
_c8.id="XinhaIFrame_"+this._textArea.id;
fw.ed_cell.appendChild(_c8);
this._iframe=_c8;
this._iframe.className="xinha_iframe";
Xinha.freeLater(this,"_iframe");
var _c9=this._createStatusBar();
this._statusBar=fw.sb_cell.appendChild(_c9);
var _ca=this._textArea;
_ca.parentNode.insertBefore(_c7,_ca);
_ca.className="xinha_textarea";
Xinha.removeFromParent(_ca);
fw.ed_cell.appendChild(_ca);
Xinha.addDom0Event(this._textArea,"click",function(){
if(Xinha._currentlyActiveEditor!=this){
_b2.updateToolbar();
}
return true;
});
if(_ca.form){
Xinha.prependDom0Event(this._textArea.form,"submit",function(){
_b2.firePluginEvent("onBeforeSubmit");
_b2._textArea.value=_b2.outwardHtml(_b2.getHTML());
return true;
});
var _cb=_ca.value;
Xinha.prependDom0Event(this._textArea.form,"reset",function(){
_b2.setHTML(_b2.inwardHtml(_cb));
_b2.updateToolbar();
return true;
});
if(!_ca.form.xinha_submit){
try{
_ca.form.xinha_submit=_ca.form.submit;
_ca.form.submit=function(){
this.onsubmit();
this.xinha_submit();
};
}
catch(ex){
}
}
}
Xinha.prependDom0Event(window,"unload",function(){
_b2.firePluginEvent("onBeforeUnload");
_ca.value=_b2.outwardHtml(_b2.getHTML());
if(!Xinha.is_ie){
_c7.parentNode.replaceChild(_ca,_c7);
}
return true;
});
_ca.style.display="none";
_b2.initSize();
this.setLoadingMessage(Xinha._lc("Finishing"));
_b2._iframeLoadDone=false;
if(Xinha.is_opera){
_b2.initIframe();
}else{
Xinha._addEvent(this._iframe,"load",function(e){
if(!_b2._iframeLoadDone){
_b2._iframeLoadDone=true;
_b2.initIframe();
}
return true;
});
}
};
Xinha.prototype.initSize=function(){
this.setLoadingMessage(Xinha._lc("Init editor size"));
var _cd=this;
var _ce=null;
var _cf=null;
switch(this.config.width){
case "auto":
_ce=this._initial_ta_size.w;
break;
case "toolbar":
_ce=this._toolBar.offsetWidth+"px";
break;
default:
_ce=/[^0-9]/.test(this.config.width)?this.config.width:this.config.width+"px";
break;
}
_cf=this.config.height=="auto"?this._initial_ta_size.h:/[^0-9]/.test(this.config.height)?this.config.height:this.config.height+"px";
this.sizeEditor(_ce,_cf,this.config.sizeIncludesBars,this.config.sizeIncludesPanels);
this.notifyOn("panel_change",function(){
_cd.sizeEditor();
});
};
Xinha.prototype.sizeEditor=function(_d0,_d1,_d2,_d3){
if(this._risizing){
return;
}
this._risizing=true;
var _d4=this._framework;
this.notifyOf("before_resize",{width:_d0,height:_d1});
this.firePluginEvent("onBeforeResize",_d0,_d1);
this._iframe.style.height="100%";
this._textArea.style.height="1px";
this._iframe.style.width="0px";
this._textArea.style.width="0px";
if(_d2!==null){
this._htmlArea.sizeIncludesToolbars=_d2;
}
if(_d3!==null){
this._htmlArea.sizeIncludesPanels=_d3;
}
if(_d0){
this._htmlArea.style.width=_d0;
if(!this._htmlArea.sizeIncludesPanels){
var _d5=this._panels.right;
if(_d5.on&&_d5.panels.length&&Xinha.hasDisplayedChildren(_d5.div)){
this._htmlArea.style.width=(this._htmlArea.offsetWidth+parseInt(this.config.panel_dimensions.right,10))+"px";
}
var _d6=this._panels.left;
if(_d6.on&&_d6.panels.length&&Xinha.hasDisplayedChildren(_d6.div)){
this._htmlArea.style.width=(this._htmlArea.offsetWidth+parseInt(this.config.panel_dimensions.left,10))+"px";
}
}
}
if(_d1){
this._htmlArea.style.height=_d1;
if(!this._htmlArea.sizeIncludesToolbars){
this._htmlArea.style.height=(this._htmlArea.offsetHeight+this._toolbar.offsetHeight+this._statusBar.offsetHeight)+"px";
}
if(!this._htmlArea.sizeIncludesPanels){
var _d7=this._panels.top;
if(_d7.on&&_d7.panels.length&&Xinha.hasDisplayedChildren(_d7.div)){
this._htmlArea.style.height=(this._htmlArea.offsetHeight+parseInt(this.config.panel_dimensions.top,10))+"px";
}
var _d8=this._panels.bottom;
if(_d8.on&&_d8.panels.length&&Xinha.hasDisplayedChildren(_d8.div)){
this._htmlArea.style.height=(this._htmlArea.offsetHeight+parseInt(this.config.panel_dimensions.bottom,10))+"px";
}
}
}
_d0=this._htmlArea.offsetWidth;
_d1=this._htmlArea.offsetHeight;
var _d9=this._panels;
var _da=this;
var _db=1;
function panel_is_alive(pan){
if(_d9[pan].on&&_d9[pan].panels.length&&Xinha.hasDisplayedChildren(_d9[pan].container)){
_d9[pan].container.style.display="";
return true;
}else{
_d9[pan].container.style.display="none";
return false;
}
};
if(panel_is_alive("left")){
_db+=1;
}
if(panel_is_alive("right")){
_db+=1;
}
_d4.tb_cell.colSpan=_db;
_d4.tp_cell.colSpan=_db;
_d4.bp_cell.colSpan=_db;
_d4.sb_cell.colSpan=_db;
if(!_d4.tp_row.childNodes.length){
Xinha.removeFromParent(_d4.tp_row);
}else{
if(!Xinha.hasParentNode(_d4.tp_row)){
_d4.tbody.insertBefore(_d4.tp_row,_d4.ler_row);
}
}
if(!_d4.bp_row.childNodes.length){
Xinha.removeFromParent(_d4.bp_row);
}else{
if(!Xinha.hasParentNode(_d4.bp_row)){
_d4.tbody.insertBefore(_d4.bp_row,_d4.ler_row.nextSibling);
}
}
if(!this.config.statusBar){
Xinha.removeFromParent(_d4.sb_row);
}else{
if(!Xinha.hasParentNode(_d4.sb_row)){
_d4.table.appendChild(_d4.sb_row);
}
}
_d4.lp_cell.style.width=this.config.panel_dimensions.left;
_d4.rp_cell.style.width=this.config.panel_dimensions.right;
_d4.tp_cell.style.height=this.config.panel_dimensions.top;
_d4.bp_cell.style.height=this.config.panel_dimensions.bottom;
_d4.tb_cell.style.height=this._toolBar.offsetHeight+"px";
_d4.sb_cell.style.height=this._statusBar.offsetHeight+"px";
var _dd=_d1-this._toolBar.offsetHeight-this._statusBar.offsetHeight;
if(panel_is_alive("top")){
_dd-=parseInt(this.config.panel_dimensions.top,10);
}
if(panel_is_alive("bottom")){
_dd-=parseInt(this.config.panel_dimensions.bottom,10);
}
this._iframe.style.height=_dd+"px";
var _de=_d0;
if(panel_is_alive("left")){
_de-=parseInt(this.config.panel_dimensions.left,10);
}
if(panel_is_alive("right")){
_de-=parseInt(this.config.panel_dimensions.right,10);
}
var _df=this.config.iframeWidth?parseInt(this.config.iframeWidth,10):null;
this._iframe.style.width=(_df&&_df<_de)?_df+"px":_de+"px";
this._textArea.style.height=this._iframe.style.height;
this._textArea.style.width=this._iframe.style.width;
this.notifyOf("resize",{width:this._htmlArea.offsetWidth,height:this._htmlArea.offsetHeight});
this.firePluginEvent("onResize",this._htmlArea.offsetWidth,this._htmlArea.offsetWidth);
this._risizing=false;
};
Xinha.prototype.registerPanel=function(_e0,_e1){
if(!_e0){
_e0="right";
}
this.setLoadingMessage("Register "+_e0+" panel ");
var _e2=this.addPanel(_e0);
if(_e1){
_e1.drawPanelIn(_e2);
}
};
Xinha.prototype.addPanel=function(_e3){
var div=document.createElement("div");
div.side=_e3;
if(_e3=="left"||_e3=="right"){
div.style.width=this.config.panel_dimensions[_e3];
if(this._iframe){
div.style.height=this._iframe.style.height;
}
}
Xinha.addClasses(div,"panel");
this._panels[_e3].panels.push(div);
this._panels[_e3].div.appendChild(div);
this.notifyOf("panel_change",{"action":"add","panel":div});
this.firePluginEvent("onPanelChange","add",div);
return div;
};
Xinha.prototype.removePanel=function(_e5){
this._panels[_e5.side].div.removeChild(_e5);
var _e6=[];
for(var i=0;i<this._panels[_e5.side].panels.length;i++){
if(this._panels[_e5.side].panels[i]!=_e5){
_e6.push(this._panels[_e5.side].panels[i]);
}
}
this._panels[_e5.side].panels=_e6;
this.notifyOf("panel_change",{"action":"remove","panel":_e5});
this.firePluginEvent("onPanelChange","remove",_e5);
};
Xinha.prototype.hidePanel=function(_e8){
if(_e8&&_e8.style.display!="none"){
try{
var pos=this.scrollPos(this._iframe.contentWindow);
}
catch(e){
}
_e8.style.display="none";
this.notifyOf("panel_change",{"action":"hide","panel":_e8});
this.firePluginEvent("onPanelChange","hide",_e8);
try{
this._iframe.contentWindow.scrollTo(pos.x,pos.y);
}
catch(e){
}
}
};
Xinha.prototype.showPanel=function(_ea){
if(_ea&&_ea.style.display=="none"){
try{
var pos=this.scrollPos(this._iframe.contentWindow);
}
catch(e){
}
_ea.style.display="";
this.notifyOf("panel_change",{"action":"show","panel":_ea});
this.firePluginEvent("onPanelChange","show",_ea);
try{
this._iframe.contentWindow.scrollTo(pos.x,pos.y);
}
catch(e){
}
}
};
Xinha.prototype.hidePanels=function(_ec){
if(typeof _ec=="undefined"){
_ec=["left","right","top","bottom"];
}
var _ed=[];
for(var i=0;i<_ec.length;i++){
if(this._panels[_ec[i]].on){
_ed.push(_ec[i]);
this._panels[_ec[i]].on=false;
}
}
this.notifyOf("panel_change",{"action":"multi_hide","sides":_ec});
this.firePluginEvent("onPanelChange","multi_hide",_ec);
};
Xinha.prototype.showPanels=function(_ef){
if(typeof _ef=="undefined"){
_ef=["left","right","top","bottom"];
}
var _f0=[];
for(var i=0;i<_ef.length;i++){
if(!this._panels[_ef[i]].on){
_f0.push(_ef[i]);
this._panels[_ef[i]].on=true;
}
}
this.notifyOf("panel_change",{"action":"multi_show","sides":_ef});
this.firePluginEvent("onPanelChange","multi_show",_ef);
};
Xinha.objectProperties=function(obj){
var _f3=[];
for(var x in obj){
_f3[_f3.length]=x;
}
return _f3;
};
Xinha.prototype.editorIsActivated=function(){
try{
return Xinha.is_designMode?this._doc.designMode=="on":this._doc.body.contentEditable;
}
catch(ex){
return false;
}
};
Xinha._someEditorHasBeenActivated=false;
Xinha._currentlyActiveEditor=null;
Xinha.prototype.activateEditor=function(){
if(this.currentModal){
return;
}
if(Xinha._currentlyActiveEditor){
if(Xinha._currentlyActiveEditor==this){
return true;
}
Xinha._currentlyActiveEditor.deactivateEditor();
}
if(Xinha.is_designMode&&this._doc.designMode!="on"){
try{
if(this._iframe.style.display=="none"){
this._iframe.style.display="";
this._doc.designMode="on";
this._iframe.style.display="none";
}else{
this._doc.designMode="on";
}
if(Xinha.is_opera){
this.setEditorEvents(true);
}
}
catch(ex){
}
}else{
if(Xinha.is_ie&&this._doc.body.contentEditable!==true){
this._doc.body.contentEditable=true;
}
}
Xinha._someEditorHasBeenActivated=true;
Xinha._currentlyActiveEditor=this;
var _f5=this;
this.enableToolbar();
};
Xinha.prototype.deactivateEditor=function(){
this.disableToolbar();
if(Xinha.is_designMode&&this._doc.designMode!="off"){
try{
this._doc.designMode="off";
}
catch(ex){
}
}else{
if(!Xinha.is_designMode&&this._doc.body.contentEditable!==false){
this._doc.body.contentEditable=false;
}
}
if(Xinha._currentlyActiveEditor!=this){
return;
}
Xinha._currentlyActiveEditor=false;
};
Xinha.prototype.initIframe=function(){
this.disableToolbar();
var doc=null;
var _f7=this;
try{
if(_f7._iframe.contentDocument){
this._doc=_f7._iframe.contentDocument;
}else{
this._doc=_f7._iframe.contentWindow.document;
}
doc=this._doc;
if(!doc){
if(Xinha.is_gecko){
setTimeout(function(){
_f7.initIframe();
},50);
return false;
}else{
alert("ERROR: IFRAME can't be initialized.");
}
}
}
catch(ex){
setTimeout(function(){
_f7.initIframe();
},50);
return false;
}
Xinha.freeLater(this,"_doc");
doc.open("text/html","replace");
var _f8="",_f9;
if(_f7.config.browserQuirksMode===false){
_f9="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
}else{
if(_f7.config.browserQuirksMode===true){
_f9="";
}else{
_f9=Xinha.getDoctype(document);
}
}
if(!_f7.config.fullPage){
_f8+=_f9+"\n";
_f8+="<html>\n";
_f8+="<head>\n";
_f8+="<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+_f7.config.charSet+"\">\n";
if(typeof _f7.config.baseHref!="undefined"&&_f7.config.baseHref!==null){
_f8+="<base href=\""+_f7.config.baseHref+"\"/>\n";
}
_f8+=Xinha.addCoreCSS();
if(typeof _f7.config.pageStyleSheets!=="undefined"){
for(var i=0;i<_f7.config.pageStyleSheets.length;i++){
if(_f7.config.pageStyleSheets[i].length>0){
_f8+="<link rel=\"stylesheet\" type=\"text/css\" href=\""+_f7.config.pageStyleSheets[i]+"\">";
}
}
}
if(_f7.config.pageStyle){
_f8+="<style type=\"text/css\">\n"+_f7.config.pageStyle+"\n</style>";
}
_f8+="</head>\n";
_f8+="<body"+(_f7.config.bodyID?(" id=\""+_f7.config.bodyID+"\""):"")+(_f7.config.bodyClass?(" class=\""+_f7.config.bodyClass+"\""):"")+">\n";
_f8+=_f7.inwardHtml(_f7._textArea.value);
_f8+="</body>\n";
_f8+="</html>";
}else{
_f8=_f7.inwardHtml(_f7._textArea.value);
if(_f8.match(Xinha.RE_doctype)){
_f7.setDoctype(RegExp.$1);
}
var _fb=_f8.match(/<link\s+[\s\S]*?["']\s*\/?>/gi);
_f8=_f8.replace(/<link\s+[\s\S]*?["']\s*\/?>\s*/gi,"");
if(_fb){
_f8=_f8.replace(/<\/head>/i,_fb.join("\n")+"\n</head>");
}
}
doc.write(_f8);
doc.close();
if(this.config.fullScreen){
this._fullScreen();
}
this.setEditorEvents();
if((typeof _f7.config.autofocus!="undefined")&&_f7.config.autofocus!==false&&((_f7.config.autofocus==_f7._textArea.id)||_f7.config.autofocus==true)){
_f7.activateEditor();
_f7.focusEditor();
}
};
Xinha.prototype.whenDocReady=function(f){
var e=this;
if(this._doc&&this._doc.body){
f();
}else{
setTimeout(function(){
e.whenDocReady(f);
},50);
}
};
Xinha.prototype.setMode=function(_fe){
var _ff;
if(typeof _fe=="undefined"){
_fe=this._editMode=="textmode"?"wysiwyg":"textmode";
}
switch(_fe){
case "textmode":
this.firePluginEvent("onBeforeMode","textmode");
this._toolbarObjects.htmlmode.swapImage(this.config.iconList.wysiwygmode);
this.setCC("iframe");
_ff=this.outwardHtml(this.getHTML());
this.setHTML(_ff);
this.deactivateEditor();
this._iframe.style.display="none";
this._textArea.style.display="";
if(this.config.statusBar){
this._statusBarTree.style.display="none";
this._statusBarTextMode.style.display="";
}
this.findCC("textarea");
this.notifyOf("modechange",{"mode":"text"});
this.firePluginEvent("onMode","textmode");
break;
case "wysiwyg":
this.firePluginEvent("onBeforeMode","wysiwyg");
this._toolbarObjects.htmlmode.swapImage([this.imgURL("images/ed_buttons_main.png"),7,0]);
this.setCC("textarea");
_ff=this.inwardHtml(this.getHTML());
this.deactivateEditor();
this.setHTML(_ff);
this._iframe.style.display="";
this._textArea.style.display="none";
this.activateEditor();
if(this.config.statusBar){
this._statusBarTree.style.display="";
this._statusBarTextMode.style.display="none";
}
this.findCC("iframe");
this.notifyOf("modechange",{"mode":"wysiwyg"});
this.firePluginEvent("onMode","wysiwyg");
break;
default:
alert("Mode <"+_fe+"> not defined!");
return false;
}
this._editMode=_fe;
};
Xinha.prototype.setFullHTML=function(html){
var _101=RegExp.multiline;
RegExp.multiline=true;
if(html.match(Xinha.RE_doctype)){
this.setDoctype(RegExp.$1);
}
RegExp.multiline=_101;
if(0){
if(html.match(Xinha.RE_head)){
this._doc.getElementsByTagName("head")[0].innerHTML=RegExp.$1;
}
if(html.match(Xinha.RE_body)){
this._doc.getElementsByTagName("body")[0].innerHTML=RegExp.$1;
}
}else{
var reac=this.editorIsActivated();
if(reac){
this.deactivateEditor();
}
var _103=/<html>((.|\n)*?)<\/html>/i;
html=html.replace(_103,"$1");
this._doc.open("text/html","replace");
this._doc.write(html);
this._doc.close();
if(reac){
this.activateEditor();
}
this.setEditorEvents();
return true;
}
};
Xinha.prototype.setEditorEvents=function(_104){
var _105=this;
var doc=this._doc;
_105.whenDocReady(function(){
if(!_104){
Xinha._addEvents(doc,["mousedown"],function(){
_105.activateEditor();
return true;
});
if(Xinha.is_ie){
Xinha._addEvent(_105._doc.getElementsByTagName("html")[0],"click",function(){
if(_105._iframe.contentWindow.event.srcElement.tagName.toLowerCase()=="html"){
var r=_105._doc.body.createTextRange();
r.collapse();
r.select();
}
return true;
});
}
}
Xinha._addEvents(doc,["keydown","keypress","mousedown","mouseup","drag"],function(_108){
return _105._editorEvent(Xinha.is_ie?_105._iframe.contentWindow.event:_108);
});
Xinha._addEvents(doc,["dblclick"],function(_109){
return _105._onDoubleClick(Xinha.is_ie?_105._iframe.contentWindow.event:_109);
});
if(_104){
return;
}
for(var i in _105.plugins){
var _10b=_105.plugins[i].instance;
Xinha.refreshPlugin(_10b);
}
if(typeof _105._onGenerate=="function"){
_105._onGenerate();
}
Xinha.addDom0Event(window,"resize",function(e){
if(Xinha.ie_version>7&&!window.parent){
if(_105.execResize){
_105.sizeEditor();
_105.execResize=false;
}else{
_105.execResize=true;
}
}else{
_105.sizeEditor();
}
});
_105.removeLoadingMessage();
});
};
Xinha.getPluginConstructor=function(_10d){
return Xinha.plugins[_10d]||window[_10d];
};
Xinha.prototype.registerPlugin=function(){
if(!Xinha.isSupportedBrowser){
return;
}
var _10e=arguments[0];
if(_10e===null||typeof _10e=="undefined"||(typeof _10e=="string"&&Xinha.getPluginConstructor(_10e)=="undefined")){
return false;
}
var args=[];
for(var i=1;i<arguments.length;++i){
args.push(arguments[i]);
}
return this.registerPlugin2(_10e,args);
};
Xinha.prototype.registerPlugin2=function(_111,args){
if(typeof _111=="string"&&typeof Xinha.getPluginConstructor(_111)=="function"){
var _113=_111;
_111=Xinha.getPluginConstructor(_111);
}
if(typeof _111=="undefined"){
return false;
}
if(!_111._pluginInfo){
_111._pluginInfo={name:_113};
}
var obj=new _111(this,args);
if(obj){
var _115={};
var info=_111._pluginInfo;
for(var i in info){
_115[i]=info[i];
}
_115.instance=obj;
_115.args=args;
this.plugins[_111._pluginInfo.name]=_115;
return obj;
}else{
Xinha.debugMsg("Can't register plugin "+_111.toString()+".","warn");
}
};
Xinha.getPluginDir=function(_118,_119){
if(Xinha.externalPlugins[_118]){
return Xinha.externalPlugins[_118][0];
}
if(_119||(Xinha.getPluginConstructor(_118)&&(typeof Xinha.getPluginConstructor(_118).supported!="undefined")&&!Xinha.getPluginConstructor(_118).supported)){
return _editor_url+"unsupported_plugins/"+_118;
}
return _editor_url+"plugins/"+_118;
};
Xinha.loadPlugin=function(_11a,_11b,url){
if(!Xinha.isSupportedBrowser){
return;
}
Xinha.setLoadingMessage(Xinha._lc("Loading plugin $plugin="+_11a+"$"));
if(typeof Xinha.getPluginConstructor(_11a)!="undefined"){
if(_11b){
_11b(_11a);
}
return true;
}
Xinha._pluginLoadStatus[_11a]="loading";
function multiStageLoader(_11d,_11e){
var _11f,dir,file,_122;
switch(_11d){
case "start":
_11f="old_naming";
dir=Xinha.getPluginDir(_11e);
file=_11e+".js";
break;
case "old_naming":
_11f="unsupported";
dir=Xinha.getPluginDir(_11e);
file=_11e.replace(/([a-z])([A-Z])([a-z])/g,function(str,l1,l2,l3){
return l1+"-"+l2.toLowerCase()+l3;
}).toLowerCase()+".js";
_122="You are using an obsolete naming scheme for the Xinha plugin "+_11e+". Please rename "+file+" to "+_11e+".js";
break;
case "unsupported":
_11f="unsupported_old_name";
dir=Xinha.getPluginDir(_11e,true);
file=_11e+".js";
_122="You are using the unsupported Xinha plugin "+_11e+". If you wish continued support, please see http://trac.xinha.org/ticket/1297";
break;
case "unsupported_old_name":
_11f="";
dir=Xinha.getPluginDir(_11e,true);
file=_11e.replace(/([a-z])([A-Z])([a-z])/g,function(str,l1,l2,l3){
return l1+"-"+l2.toLowerCase()+l3;
}).toLowerCase()+".js";
_122="You are using the unsupported Xinha plugin "+_11e+". If you wish continued support, please see http://trac.xinha.org/ticket/1297";
break;
default:
Xinha._pluginLoadStatus[_11e]="failed";
Xinha.debugMsg("Xinha was not able to find the plugin "+_11e+". Please make sure the plugin exists.","warn");
return;
}
var url=dir+"/"+file;
function statusCallback(_12c){
Xinha.getPluginConstructor(_12c).supported=_11d.indexOf("unsupported")!==0;
_11b(_12c);
};
Xinha._loadback(url,statusCallback,this,_11e);
Xinha.ping(url,function(){
if(_122){
Xinha.debugMsg(_122);
}
},function(){
Xinha.removeFromParent(document.getElementById(url));
multiStageLoader(_11f,_11e);
});
};
if(!url){
if(Xinha.externalPlugins[_11a]){
Xinha._loadback(Xinha.externalPlugins[_11a][0]+Xinha.externalPlugins[_11a][1],_11b,this,_11a);
}else{
var _12d=this;
multiStageLoader("start",_11a);
}
}else{
Xinha._loadback(url,_11b,this,_11a);
}
return false;
};
Xinha._pluginLoadStatus={};
Xinha.externalPlugins={};
Xinha.plugins={};
Xinha.loadPlugins=function(_12e,_12f,url){
if(!Xinha.isSupportedBrowser){
return;
}
var m,i;
for(i=0;i<_12e.length;i++){
if(typeof _12e[i]=="object"){
m=_12e[i].url.match(/(.*)(\/[^\/]*)$/);
Xinha.externalPlugins[_12e[i].plugin]=[m[1],m[2]];
_12e[i]=_12e[i].plugin;
}
}
var _133=true;
var _134=Xinha.cloneObject(_12e);
for(i=0;i<_134.length;i++){
var p=_134[i];
if(p=="FullScreen"&&!Xinha.externalPlugins.FullScreen){
continue;
}
if(typeof Xinha._pluginLoadStatus[p]=="undefined"){
Xinha.loadPlugin(p,function(_136){
Xinha.setLoadingMessage(Xinha._lc("Finishing"));
if(typeof Xinha.getPluginConstructor(_136)!="undefined"){
Xinha._pluginLoadStatus[_136]="ready";
}else{
Xinha._pluginLoadStatus[_136]="failed";
}
},url);
_133=false;
}else{
if(Xinha._pluginLoadStatus[p]=="loading"){
_133=false;
}
}
}
if(_133){
return true;
}
if(_12f){
setTimeout(function(){
if(Xinha.loadPlugins(_12e,_12f)){
_12f();
}
},50);
}
return _133;
};
Xinha.refreshPlugin=function(_137){
if(_137&&typeof _137.onGenerate=="function"){
_137.onGenerate();
}
if(_137&&typeof _137.onGenerateOnce=="function"){
_137._ongenerateOnce=_137.onGenerateOnce;
delete (_137.onGenerateOnce);
_137._ongenerateOnce();
delete (_137._ongenerateOnce);
}
};
Xinha.prototype.firePluginEvent=function(_138){
var _139=[];
for(var i=1;i<arguments.length;i++){
_139[i-1]=arguments[i];
}
for(i in this.plugins){
var _13b=this.plugins[i].instance;
if(_13b==this._browserSpecificPlugin){
continue;
}
if(_13b&&typeof _13b[_138]=="function"){
var _13c=(i=="Events")?this:_13b;
if(_13b[_138].apply(_13c,_139)){
return true;
}
}
}
_13b=this._browserSpecificPlugin;
if(_13b&&typeof _13b[_138]=="function"){
if(_13b[_138].apply(_13b,_139)){
return true;
}
}
return false;
};
Xinha.loadStyle=function(_13d,_13e,id,_140){
var url=_editor_url||"";
if(_13e){
url=Xinha.getPluginDir(_13e)+"/";
}
url+=_13d;
if(/^\//.test(_13d)){
url=_13d;
}
var head=document.getElementsByTagName("head")[0];
var link=document.createElement("link");
link.rel="stylesheet";
link.href=url;
link.type="text/css";
if(id){
link.id=id;
}
if(_140&&head.getElementsByTagName("link")[0]){
head.insertBefore(link,head.getElementsByTagName("link")[0]);
}else{
head.appendChild(link);
}
};
Xinha.loadScript=function(_144,_145,_146){
var url=_editor_url||"";
if(_145){
url=Xinha.getPluginDir(_145)+"/";
}
url+=_144;
if(/^\//.test(_144)){
url=_144;
}
Xinha._loadback(url,_146);
};
Xinha.includeAssets=function(){
var _148={pendingAssets:[],loaderRunning:false,loadedScripts:[]};
_148.callbacks=[];
_148.loadNext=function(){
var self=this;
this.loaderRunning=true;
if(this.pendingAssets.length){
var nxt=this.pendingAssets[0];
this.pendingAssets.splice(0,1);
switch(nxt.type){
case "text/css":
Xinha.loadStyle(nxt.url,nxt.plugin);
return this.loadNext();
case "text/javascript":
Xinha.loadScript(nxt.url,nxt.plugin,function(){
self.loadNext();
});
}
}else{
this.loaderRunning=false;
this.runCallback();
}
};
_148.loadScript=function(url,_14c){
var self=this;
this.pendingAssets.push({"type":"text/javascript","url":url,"plugin":_14c});
if(!this.loaderRunning){
this.loadNext();
}
return this;
};
_148.loadScriptOnce=function(url,_14f){
for(var i=0;i<this.loadedScripts.length;i++){
if(this.loadedScripts[i].url==url&&this.loadedScripts[i].plugin==_14f){
return this;
}
}
return this.loadScript(url,_14f);
};
_148.loadStyle=function(url,_152){
var self=this;
this.pendingAssets.push({"type":"text/css","url":url,"plugin":_152});
if(!this.loaderRunning){
this.loadNext();
}
return this;
};
_148.whenReady=function(_154){
this.callbacks.push(_154);
if(!this.loaderRunning){
this.loadNext();
}
return this;
};
_148.runCallback=function(){
while(this.callbacks.length){
var _155=this.callbacks.splice(0,1);
_155[0]();
_155=null;
}
return this;
};
for(var i=0;i<arguments.length;i++){
if(typeof arguments[i]=="string"){
if(arguments[i].match(/\.css$/i)){
_148.loadStyle(arguments[i]);
}else{
_148.loadScript(arguments[i]);
}
}else{
if(arguments[i].type){
if(arguments[i].type.match(/text\/css/i)){
_148.loadStyle(arguments[i].url,arguments[i].plugin);
}else{
if(arguments[i].type.match(/text\/javascript/i)){
_148.loadScript(arguments[i].url,arguments[i].plugin);
}
}
}else{
if(arguments[i].length>=1){
if(arguments[i][0].match(/\.css$/i)){
_148.loadStyle(arguments[i][0],arguments[i][1]);
}else{
_148.loadScript(arguments[i][0],arguments[i][1]);
}
}
}
}
}
return _148;
};
Xinha.prototype.debugTree=function(){
var ta=document.createElement("textarea");
ta.style.width="100%";
ta.style.height="20em";
ta.value="";
function debug(_158,str){
for(;--_158>=0;){
ta.value+=" ";
}
ta.value+=str+"\n";
};
function _dt(root,_15b){
var tag=root.tagName.toLowerCase(),i;
var ns=Xinha.is_ie?root.scopeName:root.prefix;
debug(_15b,"- "+tag+" ["+ns+"]");
for(i=root.firstChild;i;i=i.nextSibling){
if(i.nodeType==1){
_dt(i,_15b+2);
}
}
};
_dt(this._doc.body,0);
document.body.appendChild(ta);
};
Xinha.getInnerText=function(el){
var txt="",i;
for(i=el.firstChild;i;i=i.nextSibling){
if(i.nodeType==3){
txt+=i.data;
}else{
if(i.nodeType==1){
txt+=Xinha.getInnerText(i);
}
}
}
return txt;
};
Xinha.prototype._wordClean=function(){
var _162=this;
var _163={empty_tags:0,cond_comm:0,mso_elmts:0,mso_class:0,mso_style:0,mso_xmlel:0,orig_len:this._doc.body.innerHTML.length,T:new Date().getTime()};
var _164={empty_tags:"Empty tags removed: ",cond_comm:"Conditional comments removed",mso_elmts:"MSO invalid elements removed",mso_class:"MSO class names removed: ",mso_style:"MSO inline style removed: ",mso_xmlel:"MSO XML elements stripped: "};
function showStats(){
var txt="Xinha word cleaner stats: \n\n";
for(var i in _163){
if(_164[i]){
txt+=_164[i]+_163[i]+"\n";
}
}
txt+="\nInitial document length: "+_163.orig_len+"\n";
txt+="Final document length: "+_162._doc.body.innerHTML.length+"\n";
txt+="Clean-up took "+((new Date().getTime()-_163.T)/1000)+" seconds";
alert(txt);
};
function clearClass(node){
var newc=node.className.replace(/(^|\s)mso.*?(\s|$)/ig," ");
if(newc!=node.className){
node.className=newc;
if(!/\S/.test(node.className)){
node.removeAttribute("className");
++_163.mso_class;
}
}
};
function clearStyle(node){
var _16a=node.style.cssText.split(/\s*;\s*/);
for(var i=_16a.length;--i>=0;){
if(/^mso|^tab-stops/i.test(_16a[i])||/^margin\s*:\s*0..\s+0..\s+0../i.test(_16a[i])){
++_163.mso_style;
_16a.splice(i,1);
}
}
node.style.cssText=_16a.join("; ");
};
function removeElements(el){
if(("link"==el.tagName.toLowerCase()&&(el.attributes&&/File-List|Edit-Time-Data|themeData|colorSchemeMapping/.test(el.attributes.rel.nodeValue)))||/^(style|meta)$/i.test(el.tagName)){
Xinha.removeFromParent(el);
++_163.mso_elmts;
return true;
}
return false;
};
function checkEmpty(el){
if(/^(a|span|b|strong|i|em|font|div|p)$/i.test(el.tagName)&&!el.firstChild){
Xinha.removeFromParent(el);
++_163.empty_tags;
return true;
}
return false;
};
function parseTree(root){
clearClass(root);
clearStyle(root);
var next;
for(var i=root.firstChild;i;i=next){
next=i.nextSibling;
if(i.nodeType==1&&parseTree(i)){
if((Xinha.is_ie&&root.scopeName!="HTML")||(!Xinha.is_ie&&/:/.test(i.tagName))){
for(var _171=i.childNodes&&i.childNodes.length-1;i.childNodes&&i.childNodes.length&&i.childNodes[_171];--_171){
if(i.nextSibling){
i.parentNode.insertBefore(i.childNodes[_171],i.nextSibling);
}else{
i.parentNode.appendChild(i.childNodes[_171]);
}
}
Xinha.removeFromParent(i);
continue;
}
if(checkEmpty(i)){
continue;
}
if(removeElements(i)){
continue;
}
}else{
if(i.nodeType==8){
if(/(\s*\[\s*if\s*(([gl]te?|!)\s*)?(IE|mso)\s*(\d+(\.\d+)?\s*)?\]>)/.test(i.nodeValue)){
Xinha.removeFromParent(i);
++_163.cond_comm;
}
}
}
}
return true;
};
parseTree(this._doc.body);
this.updateToolbar();
};
Xinha.prototype._clearFonts=function(){
var D=this.getInnerHTML();
if(confirm(Xinha._lc("Would you like to clear font typefaces?"))){
D=D.replace(/face="[^"]*"/gi,"");
D=D.replace(/font-family:[^;}"']+;?/gi,"");
}
if(confirm(Xinha._lc("Would you like to clear font sizes?"))){
D=D.replace(/size="[^"]*"/gi,"");
D=D.replace(/font-size:[^;}"']+;?/gi,"");
}
if(confirm(Xinha._lc("Would you like to clear font colours?"))){
D=D.replace(/color="[^"]*"/gi,"");
D=D.replace(/([^\-])color:[^;}"']+;?/gi,"$1");
}
D=D.replace(/(style|class)="\s*"/gi,"");
D=D.replace(/<(font|span)\s*>/gi,"");
this.setHTML(D);
this.updateToolbar();
};
Xinha.prototype._splitBlock=function(){
this._doc.execCommand("formatblock",false,"div");
};
Xinha.prototype.forceRedraw=function(){
this._doc.body.style.visibility="hidden";
this._doc.body.style.visibility="";
};
Xinha.prototype.focusEditor=function(){
switch(this._editMode){
case "wysiwyg":
try{
if(Xinha._someEditorHasBeenActivated){
this.activateEditor();
this._iframe.contentWindow.focus();
}
}
catch(ex){
}
break;
case "textmode":
try{
this._textArea.focus();
}
catch(e){
}
break;
default:
alert("ERROR: mode "+this._editMode+" is not defined");
}
return this._doc;
};
Xinha.prototype._undoTakeSnapshot=function(){
++this._undoPos;
if(this._undoPos>=this.config.undoSteps){
this._undoQueue.shift();
--this._undoPos;
}
var take=true;
var txt=this.getInnerHTML();
if(this._undoPos>0){
take=(this._undoQueue[this._undoPos-1]!=txt);
}
if(take){
this._undoQueue[this._undoPos]=txt;
}else{
this._undoPos--;
}
};
Xinha.prototype.undo=function(){
if(this._undoPos>0){
var txt=this._undoQueue[--this._undoPos];
if(txt){
this.setHTML(txt);
}else{
++this._undoPos;
}
}
};
Xinha.prototype.redo=function(){
if(this._undoPos<this._undoQueue.length-1){
var txt=this._undoQueue[++this._undoPos];
if(txt){
this.setHTML(txt);
}else{
--this._undoPos;
}
}
};
Xinha.prototype.disableToolbar=function(_177){
if(this._timerToolbar){
clearTimeout(this._timerToolbar);
}
if(typeof _177=="undefined"){
_177=[];
}else{
if(typeof _177!="object"){
_177=[_177];
}
}
for(var i in this._toolbarObjects){
var btn=this._toolbarObjects[i];
if(_177.contains(i)){
continue;
}
if(typeof btn.state!="function"){
continue;
}
btn.state("enabled",false);
}
};
Xinha.prototype.enableToolbar=function(){
this.updateToolbar();
};
Xinha.prototype.updateToolbar=function(_17a){
if(this.suspendUpdateToolbar){
return;
}
var doc=this._doc;
var text=(this._editMode=="textmode");
var _17d=null;
if(!text){
_17d=this.getAllAncestors();
if(this.config.statusBar&&!_17a){
while(this._statusBarItems.length){
var item=this._statusBarItems.pop();
item.el=null;
item.editor=null;
item.onclick=null;
item.oncontextmenu=null;
item._xinha_dom0Events.click=null;
item._xinha_dom0Events.contextmenu=null;
item=null;
}
this._statusBarTree.innerHTML=" ";
this._statusBarTree.appendChild(document.createTextNode(Xinha._lc("Path")+": "));
for(var i=_17d.length;--i>=0;){
var el=_17d[i];
if(!el){
continue;
}
var a=document.createElement("a");
a.href="javascript:void(0);";
a.el=el;
a.editor=this;
this._statusBarItems.push(a);
Xinha.addDom0Event(a,"click",function(){
this.blur();
this.editor.selectNodeContents(this.el);
this.editor.updateToolbar(true);
return false;
});
Xinha.addDom0Event(a,"contextmenu",function(){
this.blur();
var info="Inline style:\n\n";
info+=this.el.style.cssText.split(/;\s*/).join(";\n");
alert(info);
return false;
});
var txt=el.tagName.toLowerCase();
switch(txt){
case "b":
txt="strong";
break;
case "i":
txt="em";
break;
case "strike":
txt="del";
break;
}
if(typeof el.style!="undefined"){
a.title=el.style.cssText;
}
if(el.id){
txt+="#"+el.id;
}
if(el.className){
txt+="."+el.className;
}
a.appendChild(document.createTextNode(txt));
this._statusBarTree.appendChild(a);
if(i!==0){
this._statusBarTree.appendChild(document.createTextNode(String.fromCharCode(187)));
}
Xinha.freeLater(a);
}
}
}
for(var cmd in this._toolbarObjects){
var btn=this._toolbarObjects[cmd];
var _186=true;
if(typeof btn.state!="function"){
continue;
}
if(btn.context&&!text){
_186=false;
var _187=btn.context;
var _188=[];
if(/(.*)\[(.*?)\]/.test(_187)){
_187=RegExp.$1;
_188=RegExp.$2.split(",");
}
_187=_187.toLowerCase();
var _189=(_187=="*");
for(var k=0;k<_17d.length;++k){
if(!_17d[k]){
continue;
}
if(_189||(_17d[k].tagName.toLowerCase()==_187)){
_186=true;
var _18b=null;
var att=null;
var comp=null;
var _18e=null;
for(var ka=0;ka<_188.length;++ka){
_18b=_188[ka].match(/(.*)(==|!=|===|!==|>|>=|<|<=)(.*)/);
att=_18b[1];
comp=_18b[2];
_18e=_18b[3];
if(!eval(_17d[k][att]+comp+_18e)){
_186=false;
break;
}
}
if(_186){
break;
}
}
}
}
btn.state("enabled",(!text||btn.text)&&_186);
if(typeof cmd=="function"){
continue;
}
var _190=this.config.customSelects[cmd];
if((!text||btn.text)&&(typeof _190!="undefined")){
_190.refresh(this);
continue;
}
switch(cmd){
case "fontname":
case "fontsize":
if(!text){
try{
var _191=(""+doc.queryCommandValue(cmd)).toLowerCase();
if(!_191){
btn.element.selectedIndex=0;
break;
}
var _192=this.config[cmd];
var _193=0;
for(var j in _192){
if((j.toLowerCase()==_191)||(_192[j].substr(0,_191.length).toLowerCase()==_191)){
btn.element.selectedIndex=_193;
throw "ok";
}
++_193;
}
btn.element.selectedIndex=0;
}
catch(ex){
}
}
break;
case "formatblock":
var _195=[];
for(var _196 in this.config.formatblock){
if(typeof this.config.formatblock[_196]=="string"){
_195[_195.length]=this.config.formatblock[_196];
}
}
var _197=this._getFirstAncestor(this.getSelection(),_195);
if(_197){
for(var x=0;x<_195.length;x++){
if(_195[x].toLowerCase()==_197.tagName.toLowerCase()){
btn.element.selectedIndex=x;
}
}
}else{
btn.element.selectedIndex=0;
}
break;
case "textindicator":
if(!text){
try{
var _199=btn.element.style;
_199.backgroundColor=Xinha._makeColor(doc.queryCommandValue(Xinha.is_ie?"backcolor":"hilitecolor"));
if(/transparent/i.test(_199.backgroundColor)){
_199.backgroundColor=Xinha._makeColor(doc.queryCommandValue("backcolor"));
}
_199.color=Xinha._makeColor(doc.queryCommandValue("forecolor"));
_199.fontFamily=doc.queryCommandValue("fontname");
_199.fontWeight=doc.queryCommandState("bold")?"bold":"normal";
_199.fontStyle=doc.queryCommandState("italic")?"italic":"normal";
}
catch(ex){
}
}
break;
case "htmlmode":
btn.state("active",text);
break;
case "lefttoright":
case "righttoleft":
var _19a=this.getParentElement();
while(_19a&&!Xinha.isBlockElement(_19a)){
_19a=_19a.parentNode;
}
if(_19a){
btn.state("active",(_19a.style.direction==((cmd=="righttoleft")?"rtl":"ltr")));
}
break;
default:
cmd=cmd.replace(/(un)?orderedlist/i,"insert$1orderedlist");
try{
btn.state("active",(!text&&doc.queryCommandState(cmd)));
}
catch(ex){
}
break;
}
}
if(this._customUndo&&!this._timerUndo){
this._undoTakeSnapshot();
var _19b=this;
this._timerUndo=setTimeout(function(){
_19b._timerUndo=null;
},this.config.undoTimeout);
}
this.firePluginEvent("onUpdateToolbar");
};
Xinha.getEditor=function(ref){
for(var i=__xinhas.length;i--;){
var _19e=__xinhas[i];
if(_19e&&(_19e._textArea.id==ref||_19e._textArea.name==ref||_19e._textArea==ref)){
return _19e;
}
}
return null;
};
Xinha.prototype.getPluginInstance=function(_19f){
if(this.plugins[_19f]){
return this.plugins[_19f].instance;
}else{
return null;
}
};
Xinha.prototype.getAllAncestors=function(){
var p=this.getParentElement();
var a=[];
while(p&&(p.nodeType==1)&&(p.tagName.toLowerCase()!="body")){
a.push(p);
p=p.parentNode;
}
a.push(this._doc.body);
return a;
};
Xinha.prototype._getFirstAncestor=function(sel,_1a3){
var prnt=this.activeElement(sel);
if(prnt===null){
try{
prnt=(Xinha.is_ie?this.createRange(sel).parentElement():this.createRange(sel).commonAncestorContainer);
}
catch(ex){
return null;
}
}
if(typeof _1a3=="string"){
_1a3=[_1a3];
}
while(prnt){
if(prnt.nodeType==1){
if(_1a3===null){
return prnt;
}
for(var _1a5=0;_1a5<_1a3.length;++_1a5){
if(typeof _1a3[_1a5]=="string"&&_1a3[_1a5]==prnt.tagName.toLowerCase()){
return prnt;
}else{
if(typeof _1a3[_1a5]=="function"&&_1a3[_1a5](this,prnt)){
return prnt;
}
}
}
if(prnt.tagName.toLowerCase()=="body"){
break;
}
if(prnt.tagName.toLowerCase()=="table"){
break;
}
}
prnt=prnt.parentNode;
}
return null;
};
Xinha.prototype._getAncestorBlock=function(sel){
var prnt=(Xinha.is_ie?this.createRange(sel).parentElement:this.createRange(sel).commonAncestorContainer);
while(prnt&&(prnt.nodeType==1)){
switch(prnt.tagName.toLowerCase()){
case "div":
case "p":
case "address":
case "blockquote":
case "center":
case "del":
case "ins":
case "pre":
case "h1":
case "h2":
case "h3":
case "h4":
case "h5":
case "h6":
case "h7":
return prnt;
case "body":
case "noframes":
case "dd":
case "li":
case "th":
case "td":
case "noscript":
return null;
default:
break;
}
}
return null;
};
Xinha.prototype._createImplicitBlock=function(type){
var sel=this.getSelection();
if(Xinha.is_ie){
sel.empty();
}else{
sel.collapseToStart();
}
var rng=this.createRange(sel);
};
Xinha.prototype.surroundHTML=function(_1ab,_1ac){
var html=this.getSelectedHTML();
this.insertHTML(_1ab+html+_1ac);
};
Xinha.prototype.hasSelectedText=function(){
return this.getSelectedHTML()!=="";
};
Xinha.prototype._comboSelected=function(el,txt){
this.focusEditor();
var _1b0=el.options[el.selectedIndex].value;
switch(txt){
case "fontname":
case "fontsize":
this.execCommand(txt,false,_1b0);
break;
case "formatblock":
if(!_1b0){
this.updateToolbar();
break;
}
if(!Xinha.is_gecko||_1b0!=="blockquote"){
_1b0="<"+_1b0+">";
}
this.execCommand(txt,false,_1b0);
break;
default:
var _1b1=this.config.customSelects[txt];
if(typeof _1b1!="undefined"){
_1b1.action(this,_1b0,el,txt);
}else{
alert("FIXME: combo box "+txt+" not implemented");
}
break;
}
};
Xinha.prototype._colorSelector=function(_1b2){
var _1b3=this;
if(Xinha.is_gecko){
try{
_1b3._doc.execCommand("useCSS",false,false);
_1b3._doc.execCommand("styleWithCSS",false,true);
}
catch(ex){
}
}
var btn=_1b3._toolbarObjects[_1b2].element;
var _1b5;
if(_1b2=="hilitecolor"){
if(Xinha.is_ie){
_1b2="backcolor";
_1b5=Xinha._colorToRgb(_1b3._doc.queryCommandValue("backcolor"));
}else{
_1b5=Xinha._colorToRgb(_1b3._doc.queryCommandValue("hilitecolor"));
}
}else{
_1b5=Xinha._colorToRgb(_1b3._doc.queryCommandValue("forecolor"));
}
var _1b6=function(_1b7){
_1b3._doc.execCommand(_1b2,false,_1b7);
};
if(Xinha.is_ie){
var _1b8=_1b3.createRange(_1b3.getSelection());
_1b6=function(_1b9){
_1b8.select();
_1b3._doc.execCommand(_1b2,false,_1b9);
};
}
var _1ba=new Xinha.colorPicker({cellsize:_1b3.config.colorPickerCellSize,callback:_1b6,granularity:_1b3.config.colorPickerGranularity,websafe:_1b3.config.colorPickerWebSafe,savecolors:_1b3.config.colorPickerSaveColors});
_1ba.open(_1b3.config.colorPickerPosition,btn,_1b5);
};
Xinha.prototype.execCommand=function(_1bb,UI,_1bd){
var _1be=this;
this.focusEditor();
_1bb=_1bb.toLowerCase();
if(this.firePluginEvent("onExecCommand",_1bb,UI,_1bd)){
this.updateToolbar();
return false;
}
switch(_1bb){
case "htmlmode":
this.setMode();
break;
case "hilitecolor":
case "forecolor":
this._colorSelector(_1bb);
break;
case "createlink":
this._createLink();
break;
case "undo":
case "redo":
if(this._customUndo){
this[_1bb]();
}else{
this._doc.execCommand(_1bb,UI,_1bd);
}
break;
case "inserttable":
this._insertTable();
break;
case "insertimage":
this._insertImage();
break;
case "showhelp":
this._popupDialog(_1be.config.URIs.help,null,this);
break;
case "killword":
this._wordClean();
break;
case "cut":
case "copy":
case "paste":
this._doc.execCommand(_1bb,UI,_1bd);
if(this.config.killWordOnPaste){
this._wordClean();
}
break;
case "lefttoright":
case "righttoleft":
if(this.config.changeJustifyWithDirection){
this._doc.execCommand((_1bb=="righttoleft")?"justifyright":"justifyleft",UI,_1bd);
}
var dir=(_1bb=="righttoleft")?"rtl":"ltr";
var el=this.getParentElement();
while(el&&!Xinha.isBlockElement(el)){
el=el.parentNode;
}
if(el){
if(el.style.direction==dir){
el.style.direction="";
}else{
el.style.direction=dir;
}
}
break;
case "justifyleft":
case "justifyright":
_1bb.match(/^justify(.*)$/);
var ae=this.activeElement(this.getSelection());
if(ae&&ae.tagName.toLowerCase()=="img"){
ae.align=ae.align==RegExp.$1?"":RegExp.$1;
}else{
this._doc.execCommand(_1bb,UI,_1bd);
}
break;
default:
try{
this._doc.execCommand(_1bb,UI,_1bd);
}
catch(ex){
if(this.config.debug){
alert(ex+"\n\nby execCommand("+_1bb+");");
}
}
break;
}
this.updateToolbar();
return false;
};
Xinha.prototype._editorEvent=function(ev){
var _1c3=this;
if(typeof _1c3._textArea["on"+ev.type]=="function"){
_1c3._textArea["on"+ev.type](ev);
}
if(this.isKeyEvent(ev)){
if(_1c3.firePluginEvent("onKeyPress",ev)){
return false;
}
if(this.isShortCut(ev)){
this._shortCuts(ev);
}
}
if(ev.type=="mousedown"){
if(_1c3.firePluginEvent("onMouseDown",ev)){
return false;
}
}
if(_1c3._timerToolbar){
clearTimeout(_1c3._timerToolbar);
}
if(!this.suspendUpdateToolbar){
_1c3._timerToolbar=setTimeout(function(){
_1c3.updateToolbar();
_1c3._timerToolbar=null;
},250);
}
};
Xinha.prototype._onDoubleClick=function(ev){
var _1c5=this;
var _1c6=Xinha.is_ie?ev.srcElement:ev.target;
var tag=_1c6.tagName;
var _1c8=_1c6.className;
if(tag){
tag=tag.toLowerCase();
if(_1c8&&(this.config.dblclickList[tag+"."+_1c8]!=undefined)){
this.config.dblclickList[tag+"."+_1c8][0](_1c5,_1c6);
}else{
if(this.config.dblclickList[tag]!=undefined){
this.config.dblclickList[tag][0](_1c5,_1c6);
}
}
}
};
Xinha.prototype._shortCuts=function(ev){
var key=this.getKey(ev).toLowerCase();
var cmd=null;
var _1cc=null;
switch(key){
case "b":
cmd="bold";
break;
case "i":
cmd="italic";
break;
case "u":
cmd="underline";
break;
case "s":
cmd="strikethrough";
break;
case "l":
cmd="justifyleft";
break;
case "e":
cmd="justifycenter";
break;
case "r":
cmd="justifyright";
break;
case "j":
cmd="justifyfull";
break;
case "z":
cmd="undo";
break;
case "y":
cmd="redo";
break;
case "v":
cmd="paste";
break;
case "n":
cmd="formatblock";
_1cc="p";
break;
case "0":
cmd="killword";
break;
case "1":
case "2":
case "3":
case "4":
case "5":
case "6":
cmd="formatblock";
_1cc="h"+key;
break;
}
if(cmd){
this.execCommand(cmd,false,_1cc);
Xinha._stopEvent(ev);
}
};
Xinha.prototype.convertNode=function(el,_1ce){
var _1cf=this._doc.createElement(_1ce);
while(el.firstChild){
_1cf.appendChild(el.firstChild);
}
return _1cf;
};
Xinha.prototype.scrollToElement=function(e){
if(!e){
e=this.getParentElement();
if(!e){
return;
}
}
var _1d1=Xinha.getElementTopLeft(e);
this._iframe.contentWindow.scrollTo(_1d1.left,_1d1.top);
};
Xinha.prototype.getEditorContent=function(){
return this.outwardHtml(this.getHTML());
};
Xinha.prototype.setEditorContent=function(html){
this.setHTML(this.inwardHtml(html));
};
Xinha.updateTextareas=function(){
var e;
for(var i=0;i<__xinhas.length;i++){
e=__xinhas[i];
e._textArea.value=e.getEditorContent();
}
};
Xinha.prototype.getHTML=function(){
var html="";
switch(this._editMode){
case "wysiwyg":
if(!this.config.fullPage){
html=Xinha.getHTML(this._doc.body,false,this).trim();
}else{
html=this.doctype+"\n"+Xinha.getHTML(this._doc.documentElement,true,this);
}
break;
case "textmode":
html=this._textArea.value;
break;
default:
alert("Mode <"+this._editMode+"> not defined!");
return false;
}
return html;
};
Xinha.prototype.outwardHtml=function(html){
for(var i in this.plugins){
var _1d8=this.plugins[i].instance;
if(_1d8&&typeof _1d8.outwardHtml=="function"){
html=_1d8.outwardHtml(html);
}
}
html=html.replace(/<(\/?)b(\s|>|\/)/ig,"<$1strong$2");
html=html.replace(/<(\/?)i(\s|>|\/)/ig,"<$1em$2");
html=html.replace(/<(\/?)strike(\s|>|\/)/ig,"<$1del$2");
html=html.replace(/(<[^>]*on(click|mouse(over|out|up|down))=['"])if\(window\.parent &amp;&amp; window\.parent\.Xinha\)\{return false\}/gi,"$1");
var _1d9=location.href.replace(/(https?:\/\/[^\/]*)\/.*/,"$1")+"/";
html=html.replace(/https?:\/\/null\//g,_1d9);
html=html.replace(/((href|src|background)=[\'\"])\/+/ig,"$1"+_1d9);
html=this.outwardSpecialReplacements(html);
html=this.fixRelativeLinks(html);
if(this.config.sevenBitClean){
html=html.replace(/[^ -~\r\n\t]/g,function(c){
return (c!=Xinha.cc)?"&#"+c.charCodeAt(0)+";":c;
});
}
html=html.replace(/(<script[^>]*((type=[\"\']text\/)|(language=[\"\'])))(freezescript)/gi,"$1javascript");
if(this.config.fullPage){
html=Xinha.stripCoreCSS(html);
}
if(typeof this.config.outwardHtml=="function"){
html=this.config.outwardHtml(html);
}
return html;
};
Xinha.prototype.inwardHtml=function(html){
for(var i in this.plugins){
var _1dd=this.plugins[i].instance;
if(_1dd&&typeof _1dd.inwardHtml=="function"){
html=_1dd.inwardHtml(html);
}
}
html=html.replace(/<(\/?)del(\s|>|\/)/ig,"<$1strike$2");
html=html.replace(/(<[^>]*on(click|mouse(over|out|up|down))=["'])/gi,"$1if(window.parent &amp;&amp; window.parent.Xinha){return false}");
html=this.inwardSpecialReplacements(html);
html=html.replace(/(<script[^>]*((type=[\"\']text\/)|(language=[\"\'])))(javascript)/gi,"$1freezescript");
var _1de=new RegExp("((href|src|background)=['\"])/+","gi");
html=html.replace(_1de,"$1"+location.href.replace(/(https?:\/\/[^\/]*)\/.*/,"$1")+"/");
html=this.fixRelativeLinks(html);
if(this.config.fullPage){
html=Xinha.addCoreCSS(html);
}
if(typeof this.config.inwardHtml=="function"){
html=this.config.inwardHtml(html);
}
return html;
};
Xinha.prototype.outwardSpecialReplacements=function(html){
for(var i in this.config.specialReplacements){
var from=this.config.specialReplacements[i];
var to=i;
if(typeof from.replace!="function"||typeof to.replace!="function"){
continue;
}
var reg=new RegExp(Xinha.escapeStringForRegExp(from),"g");
html=html.replace(reg,to.replace(/\$/g,"$$$$"));
}
return html;
};
Xinha.prototype.inwardSpecialReplacements=function(html){
for(var i in this.config.specialReplacements){
var from=i;
var to=this.config.specialReplacements[i];
if(typeof from.replace!="function"||typeof to.replace!="function"){
continue;
}
var reg=new RegExp(Xinha.escapeStringForRegExp(from),"g");
html=html.replace(reg,to.replace(/\$/g,"$$$$"));
}
return html;
};
Xinha.prototype.fixRelativeLinks=function(html){
if(typeof this.config.expandRelativeUrl!="undefined"&&this.config.expandRelativeUrl){
if(html==null){
return "";
}
var src=html.match(/(src|href)="([^"]*)"/gi);
var b=document.location.href;
if(src){
var url,_1ed,_1ee,_1ef,_1f0;
for(var i=0;i<src.length;++i){
url=src[i].match(/(src|href)="([^"]*)"/i);
_1ed=url[2].match(/\.\.\//g);
if(_1ed){
_1ee=new RegExp("(.*?)(([^/]*/){"+_1ed.length+"})[^/]*$");
_1ef=b.match(_1ee);
_1f0=url[2].replace(/(\.\.\/)*/,_1ef[1]);
html=html.replace(new RegExp(Xinha.escapeStringForRegExp(url[2])),_1f0);
}
}
}
}
if(typeof this.config.stripSelfNamedAnchors!="undefined"&&this.config.stripSelfNamedAnchors){
var _1f2=new RegExp("((href|src|background)=\")("+Xinha.escapeStringForRegExp(window.unescape(document.location.href.replace(/&/g,"&amp;")))+")([#?][^'\" ]*)","g");
html=html.replace(_1f2,"$1$4");
}
if(typeof this.config.stripBaseHref!="undefined"&&this.config.stripBaseHref){
var _1f3=null;
if(typeof this.config.baseHref!="undefined"&&this.config.baseHref!==null){
_1f3=new RegExp("((href|src|background|action)=\")("+Xinha.escapeStringForRegExp(this.config.baseHref.replace(/([^\/]\/)(?=.+\.)[^\/]*$/,"$1"))+")","g");
html=html.replace(_1f3,"$1");
}
_1f3=new RegExp("((href|src|background|action)=\")("+Xinha.escapeStringForRegExp(document.location.href.replace(/^(https?:\/\/[^\/]*)(.*)/,"$1"))+")","g");
html=html.replace(_1f3,"$1");
}
return html;
};
Xinha.prototype.getInnerHTML=function(){
if(!this._doc.body){
return "";
}
var html="";
switch(this._editMode){
case "wysiwyg":
if(!this.config.fullPage){
html=this._doc.body.innerHTML;
}else{
html=this.doctype+"\n"+this._doc.documentElement.innerHTML;
}
break;
case "textmode":
html=this._textArea.value;
break;
default:
alert("Mode <"+this._editMode+"> not defined!");
return false;
}
return html;
};
Xinha.prototype.setHTML=function(html){
if(!this.config.fullPage){
this._doc.body.innerHTML=html;
}else{
this.setFullHTML(html);
}
this._textArea.value=html;
};
Xinha.prototype.setDoctype=function(_1f6){
this.doctype=_1f6;
};
Xinha._object=null;
Array.prototype.isArray=true;
RegExp.prototype.isRegExp=true;
Xinha.cloneObject=function(obj){
if(!obj){
return null;
}
var _1f8=obj.isArray?[]:{};
if(obj.constructor.toString().match(/\s*function Function\(/)||typeof obj=="function"){
_1f8=obj;
}else{
if(obj.isRegExp){
_1f8=eval(obj.toString());
}else{
for(var n in obj){
var node=obj[n];
if(typeof node=="object"){
_1f8[n]=Xinha.cloneObject(node);
}else{
_1f8[n]=node;
}
}
}
}
return _1f8;
};
Xinha.extend=function(_1fb,_1fc){
function inheritance(){
};
inheritance.prototype=_1fc.prototype;
_1fb.prototype=new inheritance();
_1fb.prototype.constructor=_1fb;
_1fb.parentConstructor=_1fc;
_1fb.superClass=_1fc.prototype;
};
Xinha.flushEvents=function(){
var x=0;
var e=Xinha._eventFlushers.pop();
while(e){
try{
if(e.length==3){
Xinha._removeEvent(e[0],e[1],e[2]);
x++;
}else{
if(e.length==2){
e[0]["on"+e[1]]=null;
e[0]._xinha_dom0Events[e[1]]=null;
x++;
}
}
}
catch(ex){
}
e=Xinha._eventFlushers.pop();
}
};
Xinha._eventFlushers=[];
if(document.addEventListener){
Xinha._addEvent=function(el,_200,func){
el.addEventListener(_200,func,false);
Xinha._eventFlushers.push([el,_200,func]);
};
Xinha._removeEvent=function(el,_203,func){
el.removeEventListener(_203,func,false);
};
Xinha._stopEvent=function(ev){
ev.preventDefault();
ev.stopPropagation();
};
}else{
if(document.attachEvent){
Xinha._addEvent=function(el,_207,func){
el.attachEvent("on"+_207,func);
Xinha._eventFlushers.push([el,_207,func]);
};
Xinha._removeEvent=function(el,_20a,func){
el.detachEvent("on"+_20a,func);
};
Xinha._stopEvent=function(ev){
try{
ev.cancelBubble=true;
ev.returnValue=false;
}
catch(ex){
}
};
}else{
Xinha._addEvent=function(el,_20e,func){
alert("_addEvent is not supported");
};
Xinha._removeEvent=function(el,_211,func){
alert("_removeEvent is not supported");
};
Xinha._stopEvent=function(ev){
alert("_stopEvent is not supported");
};
}
}
Xinha._addEvents=function(el,evs,func){
for(var i=evs.length;--i>=0;){
Xinha._addEvent(el,evs[i],func);
}
};
Xinha._removeEvents=function(el,evs,func){
for(var i=evs.length;--i>=0;){
Xinha._removeEvent(el,evs[i],func);
}
};
Xinha.addOnloadHandler=function(func,_21d){
_21d=_21d?_21d:window;
var init=function(){
if(arguments.callee.done){
return;
}
arguments.callee.done=true;
if(Xinha.onloadTimer){
clearInterval(Xinha.onloadTimer);
}
func();
};
if(Xinha.is_ie){
document.attachEvent("onreadystatechange",function(){
if(document.readyState==="complete"){
document.detachEvent("onreadystatechange",arguments.callee);
init();
}
});
if(document.documentElement.doScroll&&typeof window.frameElement==="undefined"){
(function(){
if(arguments.callee.done){
return;
}
try{
document.documentElement.doScroll("left");
}
catch(error){
setTimeout(arguments.callee,0);
return;
}
init();
})();
}
}else{
if(/applewebkit|KHTML/i.test(navigator.userAgent)){
Xinha.onloadTimer=_21d.setInterval(function(){
if(/loaded|complete/.test(_21d.document.readyState)){
init();
}
},10);
}else{
_21d.document.addEventListener("DOMContentLoaded",init,false);
}
}
Xinha._addEvent(_21d,"load",init);
};
Xinha.addDom0Event=function(el,ev,fn){
Xinha._prepareForDom0Events(el,ev);
el._xinha_dom0Events[ev].unshift(fn);
};
Xinha.prependDom0Event=function(el,ev,fn){
Xinha._prepareForDom0Events(el,ev);
el._xinha_dom0Events[ev].push(fn);
};
Xinha.getEvent=function(ev){
return ev||window.event;
};
Xinha._prepareForDom0Events=function(el,ev){
if(typeof el._xinha_dom0Events=="undefined"){
el._xinha_dom0Events={};
Xinha.freeLater(el,"_xinha_dom0Events");
}
if(typeof el._xinha_dom0Events[ev]=="undefined"){
el._xinha_dom0Events[ev]=[];
if(typeof el["on"+ev]=="function"){
el._xinha_dom0Events[ev].push(el["on"+ev]);
}
el["on"+ev]=function(_228){
var a=el._xinha_dom0Events[ev];
var _22a=true;
for(var i=a.length;--i>=0;){
el._xinha_tempEventHandler=a[i];
if(el._xinha_tempEventHandler(_228)===false){
el._xinha_tempEventHandler=null;
_22a=false;
break;
}
el._xinha_tempEventHandler=null;
}
return _22a;
};
Xinha._eventFlushers.push([el,ev]);
}
};
Xinha.prototype.notifyOn=function(ev,fn){
if(typeof this._notifyListeners[ev]=="undefined"){
this._notifyListeners[ev]=[];
Xinha.freeLater(this,"_notifyListeners");
}
this._notifyListeners[ev].push(fn);
};
Xinha.prototype.notifyOf=function(ev,args){
if(this._notifyListeners[ev]){
for(var i=0;i<this._notifyListeners[ev].length;i++){
this._notifyListeners[ev][i](ev,args);
}
}
};
Xinha._blockTags=" body form textarea fieldset ul ol dl li div "+"p h1 h2 h3 h4 h5 h6 quote pre table thead "+"tbody tfoot tr td th iframe address blockquote title meta link style head ";
Xinha.isBlockElement=function(el){
return el&&el.nodeType==1&&(Xinha._blockTags.indexOf(" "+el.tagName.toLowerCase()+" ")!=-1);
};
Xinha._paraContainerTags=" body td th caption fieldset div ";
Xinha.isParaContainer=function(el){
return el&&el.nodeType==1&&(Xinha._paraContainerTags.indexOf(" "+el.tagName.toLowerCase()+" ")!=-1);
};
Xinha._closingTags=" a abbr acronym address applet b bdo big blockquote button caption center cite code del dfn dir div dl em fieldset font form frameset h1 h2 h3 h4 h5 h6 i iframe ins kbd label legend map menu noframes noscript object ol optgroup pre q s samp script select small span strike strong style sub sup table textarea title tt u ul var ";
Xinha.needsClosingTag=function(el){
return el&&el.nodeType==1&&(Xinha._closingTags.indexOf(" "+el.tagName.toLowerCase()+" ")!=-1);
};
Xinha.htmlEncode=function(str){
if(!str){
return "";
}
if(typeof str.replace=="undefined"){
str=str.toString();
}
str=str.replace(/&/ig,"&amp;");
str=str.replace(/</ig,"&lt;");
str=str.replace(/>/ig,"&gt;");
str=str.replace(/\xA0/g,"&nbsp;");
str=str.replace(/\x22/g,"&quot;");
return str;
};
Xinha.prototype.stripBaseURL=function(_235){
if(this.config.baseHref===null||!this.config.stripBaseHref){
return _235;
}
var _236=this.config.baseHref.replace(/^(https?:\/\/[^\/]+)(.*)$/,"$1");
var _237=new RegExp(_236);
return _235.replace(_237,"");
};
if(typeof String.prototype.trim!="function"){
String.prototype.trim=function(){
return this.replace(/^\s+/,"").replace(/\s+$/,"");
};
}
Xinha._makeColor=function(v){
if(typeof v!="number"){
return v;
}
var r=v&255;
var g=(v>>8)&255;
var b=(v>>16)&255;
return "rgb("+r+","+g+","+b+")";
};
Xinha._colorToRgb=function(v){
if(!v){
return "";
}
var r,g,b;
function hex(d){
return (d<16)?("0"+d.toString(16)):d.toString(16);
};
if(typeof v=="number"){
r=v&255;
g=(v>>8)&255;
b=(v>>16)&255;
return "#"+hex(r)+hex(g)+hex(b);
}
if(v.substr(0,3)=="rgb"){
var re=/rgb\s*\(\s*([0-9]+)\s*,\s*([0-9]+)\s*,\s*([0-9]+)\s*\)/;
if(v.match(re)){
r=parseInt(RegExp.$1,10);
g=parseInt(RegExp.$2,10);
b=parseInt(RegExp.$3,10);
return "#"+hex(r)+hex(g)+hex(b);
}
return null;
}
if(v.substr(0,1)=="#"){
return v;
}
return null;
};
Xinha.prototype._popupDialog=function(url,_243,init){
Dialog(this.popupURL(url),_243,init);
};
Xinha.prototype.imgURL=function(file,_246){
if(typeof _246=="undefined"){
return _editor_url+file;
}else{
return Xinha.getPluginDir(_246)+"/img/"+file;
}
};
Xinha.prototype.popupURL=function(file){
var url="";
if(file.match(/^plugin:\/\/(.*?)\/(.*)/)){
var _249=RegExp.$1;
var _24a=RegExp.$2;
if(!/\.(html?|php)$/.test(_24a)){
_24a+=".html";
}
url=Xinha.getPluginDir(_249)+"/popups/"+_24a;
}else{
if(file.match(/^\/.*?/)||file.match(/^https?:\/\//)){
url=file;
}else{
url=_editor_url+this.config.popupURL+file;
}
}
return url;
};
Xinha.getElementById=function(tag,id){
var el,i,objs=document.getElementsByTagName(tag);
for(i=objs.length;--i>=0&&(el=objs[i]);){
if(el.id==id){
return el;
}
}
return null;
};
Xinha.prototype._toggleBorders=function(){
var _250=this._doc.getElementsByTagName("TABLE");
if(_250.length!==0){
if(!this.borders){
this.borders=true;
}else{
this.borders=false;
}
for(var i=0;i<_250.length;i++){
if(this.borders){
Xinha._addClass(_250[i],"htmtableborders");
}else{
Xinha._removeClass(_250[i],"htmtableborders");
}
}
}
return true;
};
Xinha.addCoreCSS=function(html){
var _253="<style title=\"XinhaInternalCSS\" type=\"text/css\">"+".htmtableborders, .htmtableborders td, .htmtableborders th {border : 1px dashed lightgrey ! important;}\n"+"html, body { border: 0px; } \n"+"body { background-color: #ffffff; } \n"+"img, hr { cursor: default } \n"+"</style>\n";
if(html&&/<head>/i.test(html)){
return html.replace(/<head>/i,"<head>"+_253);
}else{
if(html){
return _253+html;
}else{
return _253;
}
}
};
Xinha.prototype.addEditorStylesheet=function(_254){
var _255=this._doc.createElement("link");
_255.rel="stylesheet";
_255.type="text/css";
_255.title="XinhaInternalCSS";
_255.href=_254;
this._doc.getElementsByTagName("HEAD")[0].appendChild(_255);
};
Xinha.stripCoreCSS=function(html){
return html.replace(/<style[^>]+title="XinhaInternalCSS"(.|\n)*?<\/style>/ig,"").replace(/<link[^>]+title="XinhaInternalCSS"(.|\n)*?>/ig,"");
};
Xinha._removeClass=function(el,_258){
if(!(el&&el.className)){
return;
}
var cls=el.className.split(" ");
var ar=[];
for(var i=cls.length;i>0;){
if(cls[--i]!=_258){
ar[ar.length]=cls[i];
}
}
el.className=ar.join(" ");
};
Xinha._addClass=function(el,_25d){
Xinha._removeClass(el,_25d);
el.className+=" "+_25d;
};
Xinha.addClasses=function(el,_25f){
if(el!==null){
var _260=el.className.trim().split(" ");
var ours=_25f.split(" ");
for(var x=0;x<ours.length;x++){
var _263=false;
for(var i=0;_263===false&&i<_260.length;i++){
if(_260[i]==ours[x]){
_263=true;
}
}
if(_263===false){
_260[_260.length]=ours[x];
}
}
el.className=_260.join(" ").trim();
}
};
Xinha.removeClasses=function(el,_266){
var _267=el.className.trim().split();
var _268=[];
var _269=_266.trim().split();
for(var i=0;i<_267.length;i++){
var _26b=false;
for(var x=0;x<_269.length&&!_26b;x++){
if(_267[i]==_269[x]){
_26b=true;
}
}
if(!_26b){
_268[_268.length]=_267[i];
}
}
return _268.join(" ");
};
Xinha.addClass=Xinha._addClass;
Xinha.removeClass=Xinha._removeClass;
Xinha._addClasses=Xinha.addClasses;
Xinha._removeClasses=Xinha.removeClasses;
Xinha._hasClass=function(el,_26e){
if(!(el&&el.className)){
return false;
}
var cls=el.className.split(" ");
for(var i=cls.length;i>0;){
if(cls[--i]==_26e){
return true;
}
}
return false;
};
Xinha._postback_send_charset=true;
Xinha._postback=function(url,data,_273,_274){
var req=null;
req=Xinha.getXMLHTTPRequestObject();
var _276="";
if(typeof data=="string"){
_276=data;
}else{
if(typeof data=="object"){
for(var i in data){
_276+=(_276.length?"&":"")+i+"="+encodeURIComponent(data[i]);
}
}
}
function callBack(){
if(req.readyState==4){
if(((req.status/100)==2)||Xinha.isRunLocally&&req.status===0){
if(typeof _273=="function"){
_273(req.responseText,req);
}
}else{
if(Xinha._postback_send_charset){
Xinha._postback_send_charset=false;
Xinha._postback(url,data,_273,_274);
}else{
if(typeof _274=="function"){
_274(req);
}else{
alert("An error has occurred: "+req.statusText+"\nURL: "+url);
}
}
}
}
};
req.onreadystatechange=callBack;
req.open("POST",url,true);
req.setRequestHeader("Content-Type","application/x-www-form-urlencoded"+(Xinha._postback_send_charset?"; charset=UTF-8":""));
req.send(_276);
};
Xinha._getback=function(url,_279,_27a){
var req=null;
req=Xinha.getXMLHTTPRequestObject();
function callBack(){
if(req.readyState==4){
if(((req.status/100)==2)||Xinha.isRunLocally&&req.status===0){
_279(req.responseText,req);
}else{
if(typeof _27a=="function"){
_27a(req);
}else{
alert("An error has occurred: "+req.statusText+"\nURL: "+url);
}
}
}
};
req.onreadystatechange=callBack;
req.open("GET",url,true);
req.send(null);
};
Xinha.ping=function(url,_27d,_27e){
var req=null;
req=Xinha.getXMLHTTPRequestObject();
function callBack(){
if(req.readyState==4){
if(((req.status/100)==2)||Xinha.isRunLocally&&req.status===0){
if(_27d){
_27d(req);
}
}else{
if(_27e){
_27e(req);
}
}
}
};
var _280="GET";
req.onreadystatechange=callBack;
req.open(_280,url,true);
req.send(null);
};
Xinha._geturlcontent=function(url,_282){
var req=null;
req=Xinha.getXMLHTTPRequestObject();
req.open("GET",url,false);
req.send(null);
if(((req.status/100)==2)||Xinha.isRunLocally&&req.status===0){
return (_282)?req.responseXML:req.responseText;
}else{
return "";
}
};
if(typeof dumpValues=="undefined"){
dumpValues=function(o){
var s="";
for(var prop in o){
if(window.console&&typeof window.console.log=="function"){
if(typeof console.firebug!="undefined"){
console.log(o);
}else{
console.log(prop+" = "+o[prop]+"\n");
}
}else{
s+=prop+" = "+o[prop]+"\n";
}
}
if(s){
if(document.getElementById("errors")){
document.getElementById("errors").value+=s;
}else{
var x=window.open("","debugger");
x.document.write("<pre>"+s+"</pre>");
}
}
};
}
if(!Array.prototype.contains){
Array.prototype.contains=function(_288){
var _289=this;
for(var i=0;i<_289.length;i++){
if(_288==_289[i]){
return true;
}
}
return false;
};
}
if(!Array.prototype.indexOf){
Array.prototype.indexOf=function(_28b){
var _28c=this;
for(var i=0;i<_28c.length;i++){
if(_28b==_28c[i]){
return i;
}
}
return null;
};
}
if(!Array.prototype.append){
Array.prototype.append=function(a){
for(var i=0;i<a.length;i++){
this.push(a[i]);
}
return this;
};
}
if(!Array.prototype.forEach){
Array.prototype.forEach=function(fn){
var len=this.length;
if(typeof fn!="function"){
throw new TypeError();
}
var _292=arguments[1];
for(var i=0;i<len;i++){
if(i in this){
fn.call(_292,this[i],i,this);
}
}
};
}
Xinha.getElementsByClassName=function(el,_295){
if(el.getElementsByClassName){
return Array.prototype.slice.call(el.getElementsByClassName(_295));
}else{
var els=el.getElementsByTagName("*");
var _297=[];
var _298;
for(var i=0;i<els.length;i++){
_298=els[i].className.split(" ");
if(_298.contains(_295)){
_297.push(els[i]);
}
}
return _297;
}
};
Xinha.arrayContainsArray=function(a1,a2){
var _29c=true;
for(var x=0;x<a2.length;x++){
var _29e=false;
for(var i=0;i<a1.length;i++){
if(a1[i]==a2[x]){
_29e=true;
break;
}
}
if(!_29e){
_29c=false;
break;
}
}
return _29c;
};
Xinha.arrayFilter=function(a1,_2a1){
var _2a2=[];
for(var x=0;x<a1.length;x++){
if(_2a1(a1[x])){
_2a2[_2a2.length]=a1[x];
}
}
return _2a2;
};
Xinha.collectionToArray=function(_2a4){
try{
return _2a4.length?Array.prototype.slice.call(_2a4):[];
}
catch(e){
}
var _2a5=[];
for(var i=0;i<_2a4.length;i++){
_2a5.push(_2a4.item(i));
}
return _2a5;
};
Xinha.uniq_count=0;
Xinha.uniq=function(_2a7){
return _2a7+Xinha.uniq_count++;
};
Xinha._loadlang=function(_2a8,url){
var lang;
if(typeof _editor_lcbackend=="string"){
url=_editor_lcbackend;
url=url.replace(/%lang%/,_editor_lang);
url=url.replace(/%context%/,_2a8);
}else{
if(!url){
if(_2a8!="Xinha"){
url=Xinha.getPluginDir(_2a8)+"/lang/"+_editor_lang+".js";
}else{
Xinha.setLoadingMessage("Loading language");
url=_editor_url+"lang/"+_editor_lang+".js";
}
}
}
var _2ab=Xinha._geturlcontent(url);
if(_2ab!==""){
try{
eval("lang = "+_2ab);
}
catch(ex){
alert("Error reading Language-File ("+url+"):\n"+Error.toString());
lang={};
}
}else{
lang={};
}
return lang;
};
Xinha._lc=function(_2ac,_2ad,_2ae){
var url,ret;
if(typeof _2ad=="object"&&_2ad.url&&_2ad.context){
url=_2ad.url+_editor_lang+".js";
_2ad=_2ad.context;
}
var m=null;
if(typeof _2ac=="string"){
m=_2ac.match(/\$(.*?)=(.*?)\$/g);
}
if(m){
if(!_2ae){
_2ae={};
}
for(var i=0;i<m.length;i++){
var n=m[i].match(/\$(.*?)=(.*?)\$/);
_2ae[n[1]]=n[2];
_2ac=_2ac.replace(n[0],"$"+n[1]);
}
}
if(_editor_lang=="en"){
if(typeof _2ac=="object"&&_2ac.string){
ret=_2ac.string;
}else{
ret=_2ac;
}
}else{
if(typeof Xinha._lc_catalog=="undefined"){
Xinha._lc_catalog=[];
}
if(typeof _2ad=="undefined"){
_2ad="Xinha";
}
if(typeof Xinha._lc_catalog[_2ad]=="undefined"){
Xinha._lc_catalog[_2ad]=Xinha._loadlang(_2ad,url);
}
var key;
if(typeof _2ac=="object"&&_2ac.key){
key=_2ac.key;
}else{
if(typeof _2ac=="object"&&_2ac.string){
key=_2ac.string;
}else{
key=_2ac;
}
}
if(typeof Xinha._lc_catalog[_2ad][key]=="undefined"){
if(_2ad=="Xinha"){
if(typeof _2ac=="object"&&_2ac.string){
ret=_2ac.string;
}else{
ret=_2ac;
}
}else{
return Xinha._lc(_2ac,"Xinha",_2ae);
}
}else{
ret=Xinha._lc_catalog[_2ad][key];
}
}
if(typeof _2ac=="object"&&_2ac.replace){
_2ae=_2ac.replace;
}
if(typeof _2ae!="undefined"){
for(i in _2ae){
ret=ret.replace("$"+i,_2ae[i]);
}
}
return ret;
};
Xinha.hasDisplayedChildren=function(el){
var _2b6=el.childNodes;
for(var i=0;i<_2b6.length;i++){
if(_2b6[i].tagName){
if(_2b6[i].style.display!="none"){
return true;
}
}
}
return false;
};
Xinha._loadback=function(url,_2b9,_2ba,_2bb){
if(document.getElementById(url)){
return true;
}
var t=!Xinha.is_ie?"onload":"onreadystatechange";
var s=document.createElement("script");
s.type="text/javascript";
s.src=url;
s.id=url;
if(_2b9){
s[t]=function(){
if(Xinha.is_ie&&(!/loaded|complete/.test(window.event.srcElement.readyState))){
return;
}
_2b9.call(_2ba?_2ba:this,_2bb);
s[t]=null;
};
}
document.getElementsByTagName("head")[0].appendChild(s);
return false;
};
Xinha.makeEditors=function(_2be,_2bf,_2c0){
if(!Xinha.isSupportedBrowser){
return;
}
if(typeof _2bf=="function"){
_2bf=_2bf();
}
var _2c1={};
var _2c2;
for(var x=0;x<_2be.length;x++){
if(typeof _2be[x]=="string"){
_2c2=Xinha.getElementById("textarea",_2be[x]);
if(!_2c2){
_2be[x]=null;
continue;
}
}else{
if(typeof _2be[x]=="object"&&_2be[x].tagName&&_2be[x].tagName.toLowerCase()=="textarea"){
_2c2=_2be[x];
if(!_2c2.id){
_2c2.id="xinha_id_"+x;
}
}
}
var _2c4=new Xinha(_2c2,Xinha.cloneObject(_2bf));
_2c4.registerPlugins(_2c0);
_2c1[_2c2.id]=_2c4;
}
return _2c1;
};
Xinha.startEditors=function(_2c5){
if(!Xinha.isSupportedBrowser){
return;
}
for(var i in _2c5){
if(_2c5[i].generate){
_2c5[i].generate();
}
}
};
Xinha.prototype.registerPlugins=function(_2c7){
if(!Xinha.isSupportedBrowser){
return;
}
if(_2c7){
for(var i=0;i<_2c7.length;i++){
this.setLoadingMessage(Xinha._lc("Register plugin $plugin","Xinha",{"plugin":_2c7[i]}));
this.registerPlugin(_2c7[i]);
}
}
};
Xinha.base64_encode=function(_2c9){
var _2ca="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
var _2cb="";
var chr1,chr2,chr3;
var enc1,enc2,enc3,enc4;
var i=0;
do{
chr1=_2c9.charCodeAt(i++);
chr2=_2c9.charCodeAt(i++);
chr3=_2c9.charCodeAt(i++);
enc1=chr1>>2;
enc2=((chr1&3)<<4)|(chr2>>4);
enc3=((chr2&15)<<2)|(chr3>>6);
enc4=chr3&63;
if(isNaN(chr2)){
enc3=enc4=64;
}else{
if(isNaN(chr3)){
enc4=64;
}
}
_2cb=_2cb+_2ca.charAt(enc1)+_2ca.charAt(enc2)+_2ca.charAt(enc3)+_2ca.charAt(enc4);
}while(i<_2c9.length);
return _2cb;
};
Xinha.base64_decode=function(_2d4){
var _2d5="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
var _2d6="";
var chr1,chr2,chr3;
var enc1,enc2,enc3,enc4;
var i=0;
_2d4=_2d4.replace(/[^A-Za-z0-9\+\/\=]/g,"");
do{
enc1=_2d5.indexOf(_2d4.charAt(i++));
enc2=_2d5.indexOf(_2d4.charAt(i++));
enc3=_2d5.indexOf(_2d4.charAt(i++));
enc4=_2d5.indexOf(_2d4.charAt(i++));
chr1=(enc1<<2)|(enc2>>4);
chr2=((enc2&15)<<4)|(enc3>>2);
chr3=((enc3&3)<<6)|enc4;
_2d6=_2d6+String.fromCharCode(chr1);
if(enc3!=64){
_2d6=_2d6+String.fromCharCode(chr2);
}
if(enc4!=64){
_2d6=_2d6+String.fromCharCode(chr3);
}
}while(i<_2d4.length);
return _2d6;
};
Xinha.removeFromParent=function(el){
if(!el.parentNode){
return;
}
var pN=el.parentNode;
return pN.removeChild(el);
};
Xinha.hasParentNode=function(el){
if(el.parentNode){
if(el.parentNode.nodeType==11){
return false;
}
return true;
}
return false;
};
Xinha.viewportSize=function(_2e2){
_2e2=(_2e2)?_2e2:window;
var x,y;
if(_2e2.innerHeight){
x=_2e2.innerWidth;
y=_2e2.innerHeight;
}else{
if(_2e2.document.documentElement&&_2e2.document.documentElement.clientHeight){
x=_2e2.document.documentElement.clientWidth;
y=_2e2.document.documentElement.clientHeight;
}else{
if(_2e2.document.body){
x=_2e2.document.body.clientWidth;
y=_2e2.document.body.clientHeight;
}
}
}
return {"x":x,"y":y};
};
Xinha.pageSize=function(_2e5){
_2e5=(_2e5)?_2e5:window;
var x,y;
var _2e8=_2e5.document.body.scrollHeight;
var _2e9=_2e5.document.documentElement.scrollHeight;
if(_2e8>_2e9){
x=_2e5.document.body.scrollWidth;
y=_2e5.document.body.scrollHeight;
}else{
x=_2e5.document.documentElement.scrollWidth;
y=_2e5.document.documentElement.scrollHeight;
}
return {"x":x,"y":y};
};
Xinha.prototype.scrollPos=function(_2ea){
_2ea=(_2ea)?_2ea:window;
var x,y;
if(typeof _2ea.pageYOffset!="undefined"){
x=_2ea.pageXOffset;
y=_2ea.pageYOffset;
}else{
if(_2ea.document.documentElement&&typeof document.documentElement.scrollTop!="undefined"){
x=_2ea.document.documentElement.scrollLeft;
y=_2ea.document.documentElement.scrollTop;
}else{
if(_2ea.document.body){
x=_2ea.document.body.scrollLeft;
y=_2ea.document.body.scrollTop;
}
}
}
return {"x":x,"y":y};
};
Xinha.getElementTopLeft=function(_2ed){
var _2ee=0;
var _2ef=0;
if(_2ed.offsetParent){
_2ee=_2ed.offsetLeft;
_2ef=_2ed.offsetTop;
while(_2ed=_2ed.offsetParent){
_2ee+=_2ed.offsetLeft;
_2ef+=_2ed.offsetTop;
}
}
return {top:_2ef,left:_2ee};
};
Xinha.findPosX=function(obj){
var _2f1=0;
if(obj.offsetParent){
return Xinha.getElementTopLeft(obj).left;
}else{
if(obj.x){
_2f1+=obj.x;
}
}
return _2f1;
};
Xinha.findPosY=function(obj){
var _2f3=0;
if(obj.offsetParent){
return Xinha.getElementTopLeft(obj).top;
}else{
if(obj.y){
_2f3+=obj.y;
}
}
return _2f3;
};
Xinha.createLoadingMessages=function(_2f4){
if(Xinha.loadingMessages||!Xinha.isSupportedBrowser){
return;
}
Xinha.loadingMessages=[];
for(var i=0;i<_2f4.length;i++){
if(!document.getElementById(_2f4[i])){
continue;
}
Xinha.loadingMessages.push(Xinha.createLoadingMessage(Xinha.getElementById("textarea",_2f4[i])));
}
};
Xinha.createLoadingMessage=function(_2f6,text){
if(document.getElementById("loading_"+_2f6.id)||!Xinha.isSupportedBrowser){
return;
}
var _2f8=document.createElement("div");
_2f8.id="loading_"+_2f6.id;
_2f8.className="loading";
_2f8.style.left=(Xinha.findPosX(_2f6)+_2f6.offsetWidth/2)-106+"px";
_2f8.style.top=(Xinha.findPosY(_2f6)+_2f6.offsetHeight/2)-50+"px";
var _2f9=document.createElement("div");
_2f9.className="loading_main";
_2f9.id="loading_main_"+_2f6.id;
_2f9.appendChild(document.createTextNode(Xinha._lc("Loading in progress. Please wait!")));
var _2fa=document.createElement("div");
_2fa.className="loading_sub";
_2fa.id="loading_sub_"+_2f6.id;
text=text?text:Xinha._lc("Loading Core");
_2fa.appendChild(document.createTextNode(text));
_2f8.appendChild(_2f9);
_2f8.appendChild(_2fa);
document.body.appendChild(_2f8);
Xinha.freeLater(_2f8);
Xinha.freeLater(_2f9);
Xinha.freeLater(_2fa);
return _2fa;
};
Xinha.prototype.setLoadingMessage=function(_2fb,_2fc){
if(!document.getElementById("loading_sub_"+this._textArea.id)){
return;
}
document.getElementById("loading_main_"+this._textArea.id).innerHTML=_2fc?_2fc:Xinha._lc("Loading in progress. Please wait!");
document.getElementById("loading_sub_"+this._textArea.id).innerHTML=_2fb;
};
Xinha.setLoadingMessage=function(_2fd){
if(!Xinha.loadingMessages){
return;
}
for(var i=0;i<Xinha.loadingMessages.length;i++){
Xinha.loadingMessages[i].innerHTML=_2fd;
}
};
Xinha.prototype.removeLoadingMessage=function(){
if(document.getElementById("loading_"+this._textArea.id)){
document.body.removeChild(document.getElementById("loading_"+this._textArea.id));
}
};
Xinha.removeLoadingMessages=function(_2ff){
for(var i=0;i<_2ff.length;i++){
if(!document.getElementById(_2ff[i])){
continue;
}
var main=document.getElementById("loading_"+document.getElementById(_2ff[i]).id);
main.parentNode.removeChild(main);
}
Xinha.loadingMessages=null;
};
Xinha.toFree=[];
Xinha.freeLater=function(obj,prop){
Xinha.toFree.push({o:obj,p:prop});
};
Xinha.free=function(obj,prop){
if(obj&&!prop){
for(var p in obj){
Xinha.free(obj,p);
}
}else{
if(obj){
if(prop.indexOf("src")==-1){
try{
obj[prop]=null;
}
catch(x){
}
}
}
}
};
Xinha.collectGarbageForIE=function(){
Xinha.flushEvents();
for(var x=0;x<Xinha.toFree.length;x++){
Xinha.free(Xinha.toFree[x].o,Xinha.toFree[x].p);
Xinha.toFree[x].o=null;
}
};
Xinha.prototype.insertNodeAtSelection=function(_308){
Xinha.notImplemented("insertNodeAtSelection");
};
Xinha.prototype.getParentElement=function(sel){
Xinha.notImplemented("getParentElement");
};
Xinha.prototype.activeElement=function(sel){
Xinha.notImplemented("activeElement");
};
Xinha.prototype.selectionEmpty=function(sel){
Xinha.notImplemented("selectionEmpty");
};
Xinha.prototype.saveSelection=function(){
Xinha.notImplemented("saveSelection");
};
Xinha.prototype.restoreSelection=function(_30c){
Xinha.notImplemented("restoreSelection");
};
Xinha.prototype.selectNodeContents=function(node,pos){
Xinha.notImplemented("selectNodeContents");
};
Xinha.prototype.insertHTML=function(html){
Xinha.notImplemented("insertHTML");
};
Xinha.prototype.getSelectedHTML=function(){
Xinha.notImplemented("getSelectedHTML");
};
Xinha.prototype.getSelection=function(){
Xinha.notImplemented("getSelection");
};
Xinha.prototype.createRange=function(sel){
Xinha.notImplemented("createRange");
};
Xinha.prototype.isKeyEvent=function(_311){
Xinha.notImplemented("isKeyEvent");
};
Xinha.prototype.isShortCut=function(_312){
if(_312.ctrlKey&&!_312.altKey){
return true;
}
return false;
};
Xinha.prototype.getKey=function(_313){
Xinha.notImplemented("getKey");
};
Xinha.getOuterHTML=function(_314){
Xinha.notImplemented("getOuterHTML");
};
Xinha.getXMLHTTPRequestObject=function(){
try{
if(typeof XMLHttpRequest!="undefined"&&typeof XMLHttpRequest.constructor=="function"){
return new XMLHttpRequest();
}else{
if(typeof ActiveXObject=="function"){
return new ActiveXObject("Microsoft.XMLHTTP");
}
}
}
catch(e){
Xinha.notImplemented("getXMLHTTPRequestObject");
}
};
Xinha.prototype._activeElement=function(sel){
return this.activeElement(sel);
};
Xinha.prototype._selectionEmpty=function(sel){
return this.selectionEmpty(sel);
};
Xinha.prototype._getSelection=function(){
return this.getSelection();
};
Xinha.prototype._createRange=function(sel){
return this.createRange(sel);
};
HTMLArea=Xinha;
Xinha.init();
if(Xinha.ie_version<8){
Xinha.addDom0Event(window,"unload",Xinha.collectGarbageForIE);
}
Xinha.debugMsg=function(text,_319){
if(typeof console!="undefined"&&typeof console.log=="function"){
if(_319&&_319=="warn"&&typeof console.warn=="function"){
console.warn(text);
}else{
if(_319&&_319=="info"&&typeof console.info=="function"){
console.info(text);
}else{
console.log(text);
}
}
}else{
if(typeof opera!="undefined"&&typeof opera.postError=="function"){
opera.postError(text);
}
}
};
Xinha.notImplemented=function(_31a){
throw new Error("Method Not Implemented","Part of Xinha has tried to call the "+_31a+" method which has not been implemented.");
};

