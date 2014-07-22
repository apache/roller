/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/CreateLink/link.js */
function CreateLink(_1){
this.editor=_1;
var _2=_1.config;
var _3=this;
_1.config.btnList.createlink[3]=function(){
_3.show(_3._getSelectedAnchor());
};
};
CreateLink._pluginInfo={name:"CreateLink",origin:"Xinha Core",version:"$LastChangedRevision: 1084 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/CreateLink/link.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
CreateLink.prototype._lc=function(_4){
return Xinha._lc(_4,"Xinha");
};
CreateLink.prototype.onGenerateOnce=function(){
CreateLink.loadAssets();
};
CreateLink.loadAssets=function(){
var _5=CreateLink;
if(_5.loading){
return;
}
_5.loading=true;
Xinha._getback(_editor_url+"modules/CreateLink/dialog.html",function(_6){
_5.html=_6;
_5.dialogReady=true;
});
Xinha._getback(_editor_url+"modules/CreateLink/pluginMethods.js",function(_7){
eval(_7);
_5.methodsReady=true;
});
};
CreateLink.prototype.onUpdateToolbar=function(){
if(!(CreateLink.dialogReady&&CreateLink.methodsReady)){
this.editor._toolbarObjects.createlink.state("enabled",false);
}else{
this.onUpdateToolbar=null;
}
};
CreateLink.prototype.prepareDialog=function(){
var _8=this;
var _9=this.editor;
var _a=this.dialog=new Xinha.Dialog(_9,CreateLink.html,"Xinha",{width:400});
_a.getElementById("ok").onclick=function(){
_8.apply();
};
_a.getElementById("cancel").onclick=function(){
_8.dialog.hide();
};
if(!_9.config.makeLinkShowsTarget){
_a.getElementById("f_target_label").style.visibility="hidden";
_a.getElementById("f_target").style.visibility="hidden";
_a.getElementById("f_other_target").style.visibility="hidden";
}
_a.getElementById("f_target").onchange=function(){
var f=_a.getElementById("f_other_target");
if(this.value=="_other"){
f.style.visibility="visible";
f.select();
f.focus();
}else{
f.style.visibility="hidden";
}
};
this.dialogReady=true;
};

