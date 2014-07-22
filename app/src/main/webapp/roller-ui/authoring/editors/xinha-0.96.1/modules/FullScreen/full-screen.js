/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/FullScreen/full-screen.js */
function FullScreen(_1,_2){
this.editor=_1;
this.originalSizes=null;
_1._superclean_on=false;
var _3=_1.config;
_3.registerIcon("fullscreen",[_editor_url+_3.imgURL+"ed_buttons_main.png",8,0]);
_3.registerIcon("fullscreenrestore",[_editor_url+_3.imgURL+"ed_buttons_main.png",9,0]);
_3.registerButton("fullscreen",this._lc("Maximize/Minimize Editor"),_3.iconList.fullscreen,true,function(e,_5,_6){
e._fullScreen();
});
_3.addToolbarElement("fullscreen","popupeditor",0);
};
FullScreen._pluginInfo={name:"FullScreen",version:"1.0",developer:"James Sleeman",developer_url:"http://www.gogo.co.nz/",c_owner:"Gogo Internet Services",license:"htmlArea",sponsor:"Gogo Internet Services",sponsor_url:"http://www.gogo.co.nz/"};
FullScreen.prototype._lc=function(_7){
return Xinha._lc(_7,{url:_editor_url+"modules/FullScreen/lang/",context:"FullScreen"});
};
Xinha.prototype._fullScreen=function(){
var e=this;
var _9=e.config;
function sizeItUp(){
if(!e._isFullScreen||e._sizing){
return false;
}
e._sizing=true;
var _a=Xinha.viewportSize();
if(e.config.fullScreenSizeDownMethod=="restore"){
e.originalSizes={x:parseInt(e._htmlArea.style.width),y:parseInt(e._htmlArea.style.height),dim:_a};
}
var h=_a.y-e.config.fullScreenMargins[0]-e.config.fullScreenMargins[2];
var w=_a.x-e.config.fullScreenMargins[1]-e.config.fullScreenMargins[3];
e.sizeEditor(w+"px",h+"px",true,true);
e._sizing=false;
if(e._toolbarObjects.fullscreen){
e._toolbarObjects.fullscreen.swapImage(_9.iconList.fullscreenrestore);
}
};
function sizeItDown(){
if(e._isFullScreen||e._sizing){
return false;
}
e._sizing=true;
if(e.originalSizes!=null){
var os=e.originalSizes;
var _e=Xinha.viewportSize();
var nW=os.x+(_e.x-os.dim.x);
var nH=os.y+(_e.y-os.dim.y);
e.sizeEditor(nW+"px",nH+"px",e.config.sizeIncludesBars,e.config.sizeIncludesPanels);
e.originalSizes=null;
}else{
e.initSize();
}
e._sizing=false;
if(e._toolbarObjects.fullscreen){
e._toolbarObjects.fullscreen.swapImage(_9.iconList.fullscreen);
}
};
function resetScroll(){
if(e._isFullScreen){
window.scroll(0,0);
window.setTimeout(resetScroll,150);
}
};
if(typeof this._isFullScreen=="undefined"){
this._isFullScreen=false;
if(e.target!=e._iframe){
Xinha._addEvent(window,"resize",sizeItUp);
}
}
if(Xinha.is_gecko){
this.deactivateEditor();
}
if(this._isFullScreen){
this._htmlArea.style.position="";
if(!Xinha.is_ie){
this._htmlArea.style.border="";
}
try{
if(Xinha.is_ie&&document.compatMode=="CSS1Compat"){
var bod=document.getElementsByTagName("html");
}else{
var bod=document.getElementsByTagName("body");
}
bod[0].style.overflow="";
}
catch(e){
}
this._isFullScreen=false;
sizeItDown();
var _12=this._htmlArea;
while((_12=_12.parentNode)&&_12.style){
_12.style.position=_12._xinha_fullScreenOldPosition;
_12._xinha_fullScreenOldPosition=null;
}
if(Xinha.ie_version<7){
var _13=document.getElementsByTagName("select");
for(var i=0;i<_13.length;++i){
_13[i].style.visibility="visible";
}
}
window.scroll(this._unScroll.x,this._unScroll.y);
}else{
this._unScroll={x:(window.pageXOffset)?(window.pageXOffset):(document.documentElement)?document.documentElement.scrollLeft:document.body.scrollLeft,y:(window.pageYOffset)?(window.pageYOffset):(document.documentElement)?document.documentElement.scrollTop:document.body.scrollTop};
var _12=this._htmlArea;
while((_12=_12.parentNode)&&_12.style){
_12._xinha_fullScreenOldPosition=_12.style.position;
_12.style.position="static";
}
if(Xinha.ie_version<7){
var _13=document.getElementsByTagName("select");
var s,_16;
for(var i=0;i<_13.length;++i){
s=_13[i];
_16=false;
while(s=s.parentNode){
if(s==this._htmlArea){
_16=true;
break;
}
}
if(!_16&&_13[i].style.visibility!="hidden"){
_13[i].style.visibility="hidden";
}
}
}
window.scroll(0,0);
this._htmlArea.style.position="absolute";
this._htmlArea.style.zIndex=999;
this._htmlArea.style.left=e.config.fullScreenMargins[3]+"px";
this._htmlArea.style.top=e.config.fullScreenMargins[0]+"px";
if(!Xinha.is_ie&&!Xinha.is_webkit){
this._htmlArea.style.border="none";
}
this._isFullScreen=true;
resetScroll();
try{
if(Xinha.is_ie&&document.compatMode=="CSS1Compat"){
var bod=document.getElementsByTagName("html");
}else{
var bod=document.getElementsByTagName("body");
}
bod[0].style.overflow="hidden";
}
catch(e){
}
sizeItUp();
}
if(Xinha.is_gecko){
this.activateEditor();
}
this.focusEditor();
};

