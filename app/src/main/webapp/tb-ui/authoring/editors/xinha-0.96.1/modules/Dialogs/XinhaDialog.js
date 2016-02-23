/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/Dialogs/XinhaDialog.js */
Xinha.Dialog=function(_1,_2,_3,_4,_5){
var _6=this;
this.id={};
this.r_id={};
this.editor=_1;
this.document=document;
this.size=_4;
this.modal=(_5&&_5.modal===false)?false:true;
this.closable=(_5&&_5.closable===false)?false:true;
this.resizable=(_5&&_5.resizable===false)?false:true;
this.layer=(_5&&_5.layer)?_5.layer:0;
this.centered=(_5&&_5.centered===true)?true:false;
this.closeOnEscape=(_5&&_5.closeOnEscape===true)?true:false;
this.rootElem=null;
this.captionBar=null;
this.main=null;
this.background=null;
this.centered=null;
this.greyout=null;
this.buttons=null;
this.closer=null;
this.icon=null;
this.resizer=null;
this.initialZ=null;
var _7=_1.config.dialogOptions;
if(_7){
if(typeof _7.centered!="undefined"){
this.centered=_7.centered;
}
if(typeof _7.resizable!="undefined"){
this.resizable=_7.resizable;
}
if(typeof _7.closable!="undefined"){
this.closable=_7.closable;
}
if(typeof _7.greyout!="undefined"){
this.greyout=_7.greyout;
}
if(typeof _7.closeOnEscape!="undefined"){
this.closeOnEscape=_7.closeOnEscape;
}
}
var _8;
if(Xinha.is_ie){
_8=document.createElement("iframe");
_8.src="about:blank";
_8.onreadystatechange=function(){
var _9=window.event.srcElement.contentWindow.document;
if(this.readyState=="complete"&&_9&&_9.body){
var _a=_9.createElement("div");
var _b,_c=document.styleSheets;
for(var i=0;i<_c.length;i++){
if(_c[i].id.indexOf("Xinha")!=-1&&_c[i].cssText){
_b+=_c[i].cssText;
}
}
_a.innerHTML="<br><style type=\"text/css\">\n"+_b+"\n</style>";
_9.getElementsByTagName("body")[0].appendChild(_a);
_9.body.className="xinha_dialog_background";
if(_6.modal){
_9.body.className+="_modal";
}
if(_6.greyout){
_9.body.className+="_greyout";
}
}
};
}else{
_8=document.createElement("div");
}
_8.className="xinha_dialog_background";
if(this.modal){
_8.className+="_modal";
}
if(this.greyout){
_8.className+="_greyout";
}
var z=1000;
if(!Xinha.Dialog.initialZ){
var p=_1._htmlArea;
while(p){
if(p.style&&parseInt(p.style.zIndex,10)>z){
z=parseInt(p.style.zIndex,10);
}
p=p.parentNode;
}
Xinha.Dialog.initialZ=z;
}
z=Xinha.Dialog.initialZ;
var s=_8.style;
s.position="absolute";
s.top=0;
s.left=0;
s.border="none";
s.overflow="hidden";
s.display="none";
s.zIndex=(this.modal?z+25:z+1)+this.layer;
document.body.appendChild(_8);
this.background=_8;
_8=null;
Xinha.freeLater(this,"background");
var _11=document.createElement("div");
_11.style.position=(Xinha.is_ie||!this.modal)?"absolute":"fixed";
_11.style.zIndex=(this.modal?z+27:z+3)+this.layer;
_11.style.display="none";
if(!this.modal){
Xinha._addEvent(_11,"mousedown",function(){
Xinha.Dialog.activateModeless(_6);
});
}
_11.className="dialog"+(this.modal?"":" modeless");
if(Xinha.is_chrome){
_11.className+=" chrome";
}
document.body.appendChild(_11);
_11.style.paddingBottom="10px";
_11.style.width=(_4&&_4.width)?_4.width+"px":"";
if(_4&&_4.height){
if(Xinha.ie_version<7){
_11.style.height=_4.height+"px";
}else{
_11.style.minHeight=_4.height+"px";
}
}
_2=this.translateHtml(_2,_3);
var _12=document.createElement("div");
_11.appendChild(_12);
_12.innerHTML=_2;
this.fixupDOM(_12,_3);
var _13=_12.removeChild(_12.getElementsByTagName("h1")[0]);
_11.insertBefore(_13,_12);
Xinha._addEvent(_13,"mousedown",function(ev){
_6.dragStart(ev);
});
_13.style.MozUserSelect="none";
_13.style.WebkitUserSelect="none";
_13.unselectable="on";
_13.onselectstart=function(){
return false;
};
this.buttons=document.createElement("div");
s=this.buttons.style;
s.position="absolute";
s.top="0";
s.right="2px";
_11.appendChild(this.buttons);
if(this.closable&&this.closeOnEscape){
Xinha._addEvent(document,"keypress",function(ev){
if(ev.keyCode==27){
if(Xinha.Dialog.activeModeless==_6||_6.modal){
_6.hide();
return true;
}
}
});
}
this.closer=null;
if(this.closable){
this.closer=document.createElement("div");
this.closer.className="closeButton";
this.closer.onmousedown=function(ev){
this.className="closeButton buttonClick";
Xinha._stopEvent(Xinha.getEvent(ev));
return false;
};
this.closer.onmouseout=function(ev){
this.className="closeButton";
Xinha._stopEvent(Xinha.getEvent(ev));
return false;
};
this.closer.onmouseup=function(){
this.className="closeButton";
_6.hide();
return false;
};
this.buttons.appendChild(this.closer);
var _18=document.createElement("span");
_18.className="innerX";
_18.style.position="relative";
_18.style.top="-3px";
_18.appendChild(document.createTextNode("×"));
this.closer.appendChild(_18);
_18=null;
}
this.icon=document.createElement("img");
var _19=this.icon;
_19.className="icon";
_19.src=_1.config.iconList.dialogCaption;
_19.style.position="absolute";
_19.style.top="3px";
_19.style.left="2px";
_19.ondrag=function(){
return false;
};
_11.appendChild(this.icon);
var all=_11.getElementsByTagName("*");
for(var i=0;i<all.length;i++){
var el=all[i];
if(el.tagName.toLowerCase()=="textarea"||el.tagName.toLowerCase()=="input"){
}else{
el.unselectable="on";
}
}
this.resizer=null;
if(this.resizable){
this.resizer=document.createElement("div");
this.resizer.className="resizeHandle";
s=this.resizer.style;
s.position="absolute";
s.bottom="0px";
s.right="0px";
s.MozUserSelect="none";
Xinha._addEvent(this.resizer,"mousedown",function(ev){
_6.resizeStart(ev);
});
_11.appendChild(this.resizer);
}
this.rootElem=_11;
this.captionBar=_13;
this.main=_12;
_13=null;
_11=null;
_12=null;
Xinha.freeLater(this,"rootElem");
Xinha.freeLater(this,"captionBar");
Xinha.freeLater(this,"main");
Xinha.freeLater(this,"buttons");
Xinha.freeLater(this,"closer");
Xinha.freeLater(this,"icon");
Xinha.freeLater(this,"resizer");
Xinha.freeLater(this,"document");
this.size={};
};
Xinha.Dialog.prototype.onresize=function(){
return true;
};
Xinha.Dialog.prototype.show=function(_1e){
var _1f=this.rootElem;
var _20=_1f.style;
var _21=this.modal;
var _22=this.editor.scrollPos();
this.scrollPos=_22;
var _23=this;
if(this.attached){
this.editor.showPanel(_1f);
}
if(Xinha._someEditorHasBeenActivated){
this._lastRange=this.editor.saveSelection();
if(Xinha.is_ie&&!_21){
_23.saveSelection=function(){
_23._lastRange=_23.editor.saveSelection();
};
Xinha._addEvent(this.editor._doc,"mouseup",_23.saveSelection);
}
}
if(_21){
this.editor.deactivateEditor();
this.editor.suspendUpdateToolbar=true;
this.editor.currentModal=_23;
}
if(Xinha.is_ff2&&_21){
this._restoreTo=[this.editor._textArea.style.display,this.editor._iframe.style.visibility,this.editor.hidePanels()];
this.editor._textArea.style.display="none";
this.editor._iframe.style.visibility="hidden";
}
if(!this.attached){
if(_21){
this.showBackground();
this.posBackground({top:0,left:0});
this.resizeBackground(Xinha.Dialog.calcFullBgSize());
}else{
this.background.style.display="";
}
Xinha.Dialog.fadeIn(this.rootElem,100,function(){
if(_21){
var _24=_23.rootElem.getElementsByTagName("input");
for(var i=0;i<_24.length;i++){
if(_24[i].type=="text"){
try{
_24[i].focus();
break;
}
catch(e){
}
}
}
}
});
var _26=_1f.offsetHeight;
var _27=_1f.offsetWidth;
var _28=Xinha.viewportSize();
var _29=_28.y;
var _2a=_28.x;
if(_26>_29){
_20.height=_29+"px";
if(_1f.scrollHeight>_26){
_23.main.style.overflowY="auto";
}
}
if(this.size.top&&this.size.left){
_20.top=parseInt(this.size.top,10)+"px";
_20.left=parseInt(this.size.left,10)+"px";
}else{
if(this.editor.btnClickEvent&&!this.centered){
var _2b=this.editor.btnClickEvent;
if(_20.position=="absolute"){
_20.top=_2b.clientY+this.scrollPos.y+"px";
}else{
_20.top=_2b.clientY+"px";
}
if(_26+_1f.offsetTop>_29){
_20.top=(_20.position=="absolute"?this.scrollPos.y:0)+"px";
}
if(_20.position=="absolute"){
_20.left=_2b.clientX+this.scrollPos.x+"px";
}else{
_20.left=_2b.clientX+"px";
}
if(_27+_1f.offsetLeft>_2a){
_20.left=_2b.clientX-_27+"px";
if(_1f.offsetLeft<0){
_20.left=0;
}
}
this.editor.btnClickEvent=null;
}else{
var top=(_29-_26)/2;
var _2d=(_2a-_27)/2;
_20.top=((top>0)?top:0)+"px";
_20.left=((_2d>0)?_2d:0)+"px";
}
}
}
this.width=_27;
this.height=_26;
if(!_21){
this.resizeBackground({width:_27+"px",height:_26+"px"});
this.posBackground({top:_20.top,left:_20.left});
}
if(typeof _1e!="undefined"){
this.setValues(_1e);
}
this.dialogShown=true;
};
Xinha.Dialog.prototype.hide=function(){
if(this.attached){
this.editor.hidePanel(this.rootElem);
}else{
Xinha.Dialog.fadeOut(this.rootElem);
this.hideBackground();
var _2e=this;
if(Xinha.is_ff2&&this.modal){
this.editor._textArea.style.display=this._restoreTo[0];
this.editor._iframe.style.visibility=this._restoreTo[1];
this.editor.showPanels(this._restoreTo[2]);
}
if(!this.editor._isFullScreen&&this.modal){
window.scroll(this.scrollPos.x,this.scrollPos.y);
}
if(Xinha.is_ie&&!this.modal){
Xinha._removeEvent(this.editor._doc,"mouseup",_2e.saveSelection);
}
if(this.modal){
this.editor.suspendUpdateToolbar=false;
this.editor.currentModal=null;
this.editor.activateEditor();
}
}
if(this.modal){
this.editor.restoreSelection(this._lastRange);
}
this.dialogShown=false;
this.editor.updateToolbar();
this.editor.focusEditor();
return this.getValues();
};
Xinha.Dialog.prototype.toggle=function(){
if(this.rootElem.style.display=="none"){
this.show();
}else{
this.hide();
}
};
Xinha.Dialog.prototype.collapse=function(){
if(this.collapsed){
this.collapsed=false;
this.show();
}else{
this.main.style.height=0;
this.collapsed=true;
}
};
Xinha.Dialog.prototype.getElementById=function(id){
if(!this.rootElem.parentNode){
this.document.body.appendChild(this.rootElem);
}
return this.document.getElementById(this.id[id]?this.id[id]:id);
};
Xinha.Dialog.prototype.getElementsByName=function(_30){
if(!this.rootElem.parentNode){
this.document.body.appendChild(this.rootElem);
}
var els=this.document.getElementsByName(this.id[_30]?this.id[_30]:_30);
return Xinha.collectionToArray(els);
};
Xinha.Dialog.prototype.getElementsByClassName=function(_32){
return Xinha.getElementsByClassName(this.rootElem,_32);
};
Xinha.Dialog.prototype.dragStart=function(ev){
if(this.attached||this.dragging){
return;
}
if(!this.modal){
this.posBackground({top:0,left:0});
this.resizeBackground(Xinha.Dialog.calcFullBgSize());
this.editor.suspendUpdateToolbar=true;
}
ev=Xinha.getEvent(ev);
var _34=this;
_34.dragging=true;
_34.scrollPos=_34.editor.scrollPos();
var st=_34.rootElem.style;
_34.xOffs=ev.offsetX||ev.layerX;
_34.yOffs=ev.offsetY||ev.layerY;
_34.mouseMove=function(ev){
_34.dragIt(ev);
};
Xinha._addEvent(document,"mousemove",_34.mouseMove);
if(Xinha.is_ie){
Xinha._addEvent(this.background.contentWindow.document,"mousemove",_34.mouseMove);
}
_34.mouseUp=function(ev){
_34.dragEnd(ev);
};
Xinha._addEvent(document,"mouseup",_34.mouseUp);
if(Xinha.is_ie){
Xinha._addEvent(this.background.contentWindow.document,"mouseup",_34.mouseUp);
}
};
Xinha.Dialog.prototype.dragIt=function(ev){
var _39=this;
if(!_39.dragging){
return false;
}
var _3a,_3b,_3c;
if(_39.rootElem.style.position=="absolute"){
_3a=(ev.clientY+this.scrollPos.y)-_39.yOffs+"px";
_3b=(ev.clientX+this.scrollPos.x)-_39.xOffs+"px";
_3c={top:_3a,left:_3b};
}else{
if(_39.rootElem.style.position=="fixed"){
_3a=ev.clientY-_39.yOffs+"px";
_3b=ev.clientX-_39.xOffs+"px";
_3c={top:_3a,left:_3b};
}
}
_39.posDialog(_3c);
};
Xinha.Dialog.prototype.dragEnd=function(ev){
var _3e=this;
if(!this.modal){
this.editor.suspendUpdateToolbar=false;
}
if(!_3e.dragging){
return false;
}
_3e.dragging=false;
Xinha._removeEvent(document,"mousemove",_3e.mouseMove);
if(Xinha.is_ie){
Xinha._removeEvent(this.background.contentWindow.document,"mousemove",_3e.mouseMove);
}
Xinha._removeEvent(document,"mouseup",_3e.mouseUp);
if(Xinha.is_ie){
Xinha._removeEvent(this.background.contentWindow.document,"mouseup",_3e.mouseUp);
}
var _3f=_3e.rootElem.style;
_3e.size.top=_3f.top;
_3e.size.left=_3f.left;
if(!this.modal){
this.sizeBgToDialog();
}
};
Xinha.Dialog.prototype.resizeStart=function(ev){
var _41=this;
if(_41.resizing){
return;
}
_41.resizing=true;
if(!this.modal){
this.editor.suspendUpdateToolbar=true;
this.posBackground({top:0,left:0});
this.resizeBackground(Xinha.Dialog.calcFullBgSize());
}
_41.scrollPos=_41.editor.scrollPos();
var st=_41.rootElem.style;
st.minHeight="";
st.overflow="hidden";
_41.xOffs=parseInt(st.left,10);
_41.yOffs=parseInt(st.top,10);
_41.mouseMove=function(ev){
_41.resizeIt(ev);
};
Xinha._addEvent(document,"mousemove",_41.mouseMove);
if(Xinha.is_ie){
Xinha._addEvent(this.background.contentWindow.document,"mousemove",_41.mouseMove);
}
_41.mouseUp=function(ev){
_41.resizeEnd(ev);
};
Xinha._addEvent(document,"mouseup",_41.mouseUp);
if(Xinha.is_ie){
Xinha._addEvent(this.background.contentWindow.document,"mouseup",_41.mouseUp);
}
};
Xinha.Dialog.prototype.resizeIt=function(ev){
var _46=this;
if(!_46.resizing){
return false;
}
var _47,_48;
if(_46.rootElem.style.position=="absolute"){
_47=ev.clientY+_46.scrollPos.y;
_48=ev.clientX+_46.scrollPos.x;
}else{
_47=ev.clientY;
_48=ev.clientX;
}
_48-=_46.xOffs;
_47-=_46.yOffs;
var _49={};
_49.width=((_48>10)?_48:10)+8+"px";
_49.height=((_47>10)?_47:10)+"px";
_46.sizeDialog(_49);
_46.width=_46.rootElem.offsetWidth;
_46.height=_46.rootElem.offsetHeight;
_46.onresize();
};
Xinha.Dialog.prototype.resizeEnd=function(ev){
var _4b=this;
_4b.resizing=false;
if(!this.modal){
this.editor.suspendUpdateToolbar=false;
}
Xinha._removeEvent(document,"mousemove",_4b.mouseMove);
if(Xinha.is_ie){
Xinha._removeEvent(this.background.contentWindow.document,"mouseup",_4b.mouseUp);
}
Xinha._removeEvent(document,"mouseup",_4b.mouseUp);
if(Xinha.is_ie){
Xinha._removeEvent(this.background.contentWindow.document,"mouseup",_4b.mouseUp);
}
_4b.size.width=_4b.rootElem.offsetWidth;
_4b.size.height=_4b.rootElem.offsetHeight;
if(!this.modal){
this.sizeBgToDialog();
}
};
Xinha.Dialog.prototype.attachToPanel=function(_4c){
var _4d=this;
var _4e=this.rootElem;
var _4f=this.editor;
this.attached=true;
this.rootElem.side=_4c;
this.captionBar.ondblclick=function(ev){
_4d.detachFromPanel(Xinha.getEvent(ev));
};
_4e.style.position="static";
_4e.parentNode.removeChild(_4e);
this.background.style.display="none";
this.captionBar.style.paddingLeft="3px";
this.resizer.style.display="none";
if(this.closable){
this.closer.style.display="none";
}
this.icon.style.display="none";
if(_4c=="left"||_4c=="right"){
_4e.style.width=_4f.config.panel_dimensions[_4c];
}else{
_4e.style.width="";
}
Xinha.addClasses(_4e,"panel");
_4f._panels[_4c].panels.push(_4e);
_4f._panels[_4c].div.appendChild(_4e);
_4f.notifyOf("panel_change",{"action":"add","panel":_4e});
};
Xinha.Dialog.prototype.detachFromPanel=function(){
var _51=this;
var _52=_51.rootElem;
var _53=_52.style;
var _54=_51.editor;
_51.attached=false;
var pos=Xinha.getElementTopLeft(_52);
_53.position="absolute";
_53.top=pos.top+"px";
_53.left=pos.left+"px";
_51.resizer.style.display="";
if(_51.closable){
_51.closer.style.display="";
}
_51.icon.style.display="";
if(_51.size.width){
_52.style.width=_51.size.width+"px";
}
Xinha.removeClasses(_52,"panel");
_54.removePanel(_52);
document.body.appendChild(_52);
_51.captionBar.ondblclick=function(){
_51.attachToPanel(_52.side);
};
this.background.style.display="";
this.sizeBgToDialog();
};
Xinha.Dialog.calcFullBgSize=function(){
var _56=Xinha.pageSize();
var _57=Xinha.viewportSize();
return {width:(_56.x>_57.x?_56.x:_57.x)+"px",height:(_56.x>_57.y?_56.y:_57.y)+"px"};
};
Xinha.Dialog.prototype.sizeBgToDialog=function(){
var _58=this.rootElem.style;
var _59=this.background.style;
_59.top=_58.top;
_59.left=_58.left;
_59.width=_58.width;
_59.height=_58.height;
};
Xinha.Dialog.prototype.hideBackground=function(){
Xinha.Dialog.fadeOut(this.background);
};
Xinha.Dialog.prototype.showBackground=function(){
Xinha.Dialog.fadeIn(this.background,70);
};
Xinha.Dialog.prototype.posBackground=function(pos){
if(this.background.style.display!="none"){
this.background.style.top=pos.top;
this.background.style.left=pos.left;
}
};
Xinha.Dialog.prototype.resizeBackground=function(_5b){
if(this.background.style.display!="none"){
this.background.style.width=_5b.width;
this.background.style.height=_5b.height;
}
};
Xinha.Dialog.prototype.posDialog=function(pos){
var st=this.rootElem.style;
st.left=pos.left;
st.top=pos.top;
};
Xinha.Dialog.prototype.sizeDialog=function(_5e){
var st=this.rootElem.style;
st.height=_5e.height;
st.width=_5e.width;
var _60=parseInt(_5e.width,10);
var _61=parseInt(_5e.height,10)-this.captionBar.offsetHeight;
this.main.style.height=(_61>20)?_61:20+"px";
this.main.style.width=(_60>10)?_60:10+"px";
};
Xinha.Dialog.prototype.setValues=function(_62){
for(var i in _62){
if(typeof i=="string"){
var _64=this.getElementsByName(i);
if(!_64){
continue;
}
for(var x=0;x<_64.length;x++){
var e=_64[x];
switch(e.tagName.toLowerCase()){
case "select":
for(var j=0;j<e.options.length;j++){
if(typeof _62[i]=="object"){
for(var k=0;k<_62[i].length;k++){
if(_62[i][k]==e.options[j].value){
e.options[j].selected=true;
}
}
}else{
if(_62[i]==e.options[j].value){
e.options[j].selected=true;
}
}
}
break;
case "textarea":
case "input":
switch(e.getAttribute("type")){
case "radio":
if(e.value==_62[i]){
e.checked=true;
}
break;
case "checkbox":
if(typeof _62[i]=="object"){
for(j in _62[i]){
if(_62[i][j]==e.value){
e.checked=true;
}
}
}else{
if(_62[i]==e.value){
e.checked=true;
}
}
break;
default:
e.value=_62[i];
break;
}
}
}
}
}
};
Xinha.Dialog.prototype.getValues=function(){
var _69=[];
var _6a=Xinha.collectionToArray(this.rootElem.getElementsByTagName("input")).append(Xinha.collectionToArray(this.rootElem.getElementsByTagName("textarea"))).append(Xinha.collectionToArray(this.rootElem.getElementsByTagName("select")));
for(var x=0;x<_6a.length;x++){
var i=_6a[x];
if(!(i.name&&this.r_id[i.name])){
continue;
}
if(typeof _69[this.r_id[i.name]]=="undefined"){
_69[this.r_id[i.name]]=null;
}
var v=_69[this.r_id[i.name]];
switch(i.tagName.toLowerCase()){
case "select":
if(i.multiple){
if(!v.push){
if(v!==null){
v=[v];
}else{
v=[];
}
}
for(var j=0;j<i.options.length;j++){
if(i.options[j].selected){
v.push(i.options[j].value);
}
}
}else{
if(i.selectedIndex>=0){
v=i.options[i.selectedIndex];
}
}
break;
default:
switch(i.type.toLowerCase()){
case "radio":
if(i.checked){
v=i.value;
}
break;
case "checkbox":
if(v===null){
if(this.getElementsByName(this.r_id[i.name]).length>1){
v=[];
}
}
if(i.checked){
if(v!==null&&typeof v=="object"&&v.push){
v.push(i.value);
}else{
v=i.value;
}
}
break;
default:
v=i.value;
break;
}
}
_69[this.r_id[i.name]]=v;
}
return _69;
};
Xinha.Dialog.prototype.setLocalizer=function(_6f){
var _70=this;
if(typeof _6f=="function"){
_70._lc=_6f;
}else{
if(_6f){
this._lc=function(_71){
return Xinha._lc(_71,_6f);
};
}else{
this._lc=function(_72){
return _72;
};
}
}
};
Xinha.Dialog.prototype.translateHtml=function(_73,_74){
var _75=this;
if(_74){
this.setLocalizer(_74);
}
_73=_73.replace(/((?:name)|(?:id))=(['"])\[([a-z0-9_]+)\]\2/ig,function(_76,_77,_78,id){
return _77+"="+_78+_75.createId(id)+_78;
}).replace(/<l10n>(.*?)<\/l10n>/ig,function(_7a,_7b){
return _75._lc(_7b);
}).replace(/\="_\((.*?)\)"/g,function(_7c,_7d){
return "=\""+_75._lc(_7d)+"\"";
});
return _73;
};
Xinha.Dialog.prototype.fixupDOM=function(_7e,_7f){
var _80=this;
if(typeof _7f!="string"){
_7f="GenericPlugin";
}
var _81=function(_82,_83){
switch(_83){
case "editor":
return _editor_url;
case "plugin":
return Xinha.getPluginDir(_7f);
case "images":
return _80.editor.imgURL("images");
}
};
var _84=Xinha.collectionToArray(_7e.getElementsByTagName("img"));
for(var _85=0;_85<_84.length;++_85){
var _86=_84[_85];
var _87=_86.getAttribute("src");
if(_87){
var _88=_87.replace(/^\[(editor|plugin|images)\]/,_81);
if(_88!=_87){
_86.setAttribute("src",_88);
}
}
}
var _89=Xinha.collectionToArray(_7e.getElementsByTagName("a"));
for(var _85=0;_85<_89.length;++_85){
var _8a=_89[_85];
var _87=_8a.getAttribute("href");
if(_87){
var _88=_87.replace(/^\[(editor|plugin|images)\]/,_81);
if(_88!=_87){
_8a.setAttribute("href",_88);
}
}
}
};
Xinha.Dialog.prototype.createId=function(id){
var _8c=this;
if(typeof _8c.id[id]=="undefined"){
_8c.id[id]=Xinha.uniq("Dialog");
_8c.r_id[_8c.id[id]]=id;
}
return _8c.id[id];
};
Xinha.Dialog.activateModeless=function(_8d){
if(Xinha.Dialog.activeModeless==_8d||_8d.attached){
return;
}
if(Xinha.Dialog.activeModeless){
Xinha.Dialog.activeModeless.rootElem.style.zIndex=parseInt(Xinha.Dialog.activeModeless.rootElem.style.zIndex,10)-10;
}
Xinha.Dialog.activeModeless=_8d;
Xinha.Dialog.activeModeless.rootElem.style.zIndex=parseInt(Xinha.Dialog.activeModeless.rootElem.style.zIndex,10)+10;
};
Xinha.Dialog.setOpacity=function(el,_8f){
if(typeof el.style.filter!="undefined"){
el.style.filter=(_8f<100)?"alpha(opacity="+_8f+")":"";
}else{
el.style.opacity=_8f/100;
}
};
Xinha.Dialog.fadeIn=function(el,_91,_92,_93,_94){
_93=_93||1;
_94=_94||25;
_91=_91||100;
el.op=el.op||0;
var op=el.op;
if(el.style.display=="none"){
Xinha.Dialog.setOpacity(el,0);
el.style.display="";
}
if(op<_91){
el.op+=_94;
Xinha.Dialog.setOpacity(el,op);
el.timeOut=setTimeout(function(){
Xinha.Dialog.fadeIn(el,_91,_92,_93,_94);
},_93);
}else{
Xinha.Dialog.setOpacity(el,_91);
el.op=_91;
el.timeOut=null;
if(typeof _92=="function"){
_92.call();
}
}
};
Xinha.Dialog.fadeOut=function(el,_97,_98){
_97=_97||1;
_98=_98||30;
if(typeof el.op=="undefined"){
el.op=100;
}
var op=el.op;
if(op>=0){
el.op-=_98;
Xinha.Dialog.setOpacity(el,op);
el.timeOut=setTimeout(function(){
Xinha.Dialog.fadeOut(el,_97,_98);
},_97);
}else{
Xinha.Dialog.setOpacity(el,0);
el.style.display="none";
el.op=0;
el.timeOut=null;
}
};

