/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/Dialogs/DetachedDialog.js */
Xinha.DetachedDialog=function(_1,_2,_3,_4){
var _5={"config":new Xinha.Config(),"scrollPos":Xinha.prototype.scrollPos,"_someEditorHasBeenActivated":false,"saveSelection":function(){
},"deactivateEditor":function(){
},"_textArea":document.createElement("textarea"),"_iframe":document.createElement("div"),"_doc":document,"hidePanels":function(){
},"showPanels":function(){
},"_isFullScreen":false,"activateEditor":function(){
},"restoreSelection":function(){
},"updateToolbar":function(){
},"focusEditor":function(){
}};
Xinha.Dialog.initialZ=100;
this.attached=false;
Xinha.DetachedDialog.parentConstructor.call(this,_5,_1,_2,_3,_4);
};
Xinha.extend(Xinha.DetachedDialog,Xinha.Dialog);
Xinha.DetachedDialog.prototype.attachToPanel=function(){
return false;
};
Xinha.DetachedDialog.prototype.detachFromToPanel=function(){
return false;
};

