/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/Dialogs/div-dialog.js */
Xinha.DivDialog=function(_1,_2,_3){
this.id={};
this.r_id={};
this.document=document;
this.rootElem=_1;
this.rootElem.className+=" dialog";
this.rootElem.style.display="none";
this.width=this.rootElem.offsetWidth+"px";
this.height=this.rootElem.offsetHeight+"px";
this.setLocalizer(_3);
this.rootElem.innerHTML=this.translateHtml(_2);
};
Xinha.extend(Xinha.DivDialog,Xinha.Dialog);
Xinha.DivDialog.prototype.show=function(_4){
if(typeof _4!="undefined"){
this.setValues(_4);
}
this.rootElem.style.display="";
};
Xinha.DivDialog.prototype.hide=function(){
this.rootElem.style.display="none";
return this.getValues();
};

