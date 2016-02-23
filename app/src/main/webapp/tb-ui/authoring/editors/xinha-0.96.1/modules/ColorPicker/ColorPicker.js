/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/ColorPicker/ColorPicker.js */
ColorPicker._pluginInfo={name:"colorPicker",version:"$LastChangedRevision: 1237 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"James Sleeman",developer_url:"http://www.gogo.co.nz/",c_owner:"Gogo Internet Services",license:"htmlArea",sponsor:"Gogo Internet Services",sponsor_url:"http://www.gogo.co.nz/"};
function ColorPicker(){
};
try{
if(window.opener&&window.opener.Xinha){
var openerColorPicker=window.opener.Xinha.colorPicker;
Xinha._addEvent(window,"unload",function(){
Xinha.colorPicker=openerColorPicker;
});
}
}
catch(e){
}
Xinha.colorPicker=function(_1){
if(Xinha.colorPicker.savedColors.length===0){
Xinha.colorPicker.loadColors();
}
this.is_ie_6=(Xinha.is_ie&&Xinha.ie_version<7);
var _2=this;
var _3=false;
var _4=false;
var _5=0;
var _6=0;
this.callback=_1.callback?_1.callback:function(_7){
alert("You picked "+_7);
};
this.websafe=_1.websafe?_1.websafe:false;
this.savecolors=_1.savecolors?_1.savecolors:20;
this.cellsize=parseInt(_1.cellsize?_1.cellsize:"10px",10);
this.side=_1.granularity?_1.granularity:18;
var _8=this.side+1;
var _9=this.side-1;
this.value=1;
this.saved_cells=null;
this.table=document.createElement("table");
this.table.className="dialog";
this.table.cellSpacing=this.table.cellPadding=0;
this.table.onmouseup=function(){
_3=false;
_4=false;
};
this.tbody=document.createElement("tbody");
this.table.appendChild(this.tbody);
this.table.style.border="1px solid WindowFrame";
this.table.style.zIndex="1050";
var tr=document.createElement("tr");
var td=document.createElement("td");
td.colSpan=this.side;
td.className="title";
td.style.fontFamily="small-caption,caption,sans-serif";
td.style.fontSize="x-small";
td.unselectable="on";
td.style.MozUserSelect="none";
td.style.cursor="default";
td.appendChild(document.createTextNode(Xinha._lc("Click a color...")));
td.style.borderBottom="1px solid WindowFrame";
tr.appendChild(td);
td=null;
var td=document.createElement("td");
td.className="title";
td.colSpan=2;
td.style.fontFamily="Tahoma,Verdana,sans-serif";
td.style.borderBottom="1px solid WindowFrame";
td.style.paddingRight="0";
tr.appendChild(td);
var _c=document.createElement("div");
_c.title=Xinha._lc("Close");
_c.className="buttonColor";
_c.style.height="11px";
_c.style.width="11px";
_c.style.cursor="pointer";
_c.onclick=function(){
_2.close();
};
_c.appendChild(document.createTextNode("×"));
_c.align="center";
_c.style.verticalAlign="top";
_c.style.position="relative";
_c.style.cssFloat="right";
_c.style.styleFloat="right";
_c.style.padding="0";
_c.style.margin="2px";
_c.style.backgroundColor="transparent";
_c.style.fontSize="11px";
if(!Xinha.is_ie){
_c.style.lineHeight="9px";
}
_c.style.letterSpacing="0";
td.appendChild(_c);
this.tbody.appendChild(tr);
_c=tr=td=null;
this.constrain_cb=document.createElement("input");
this.constrain_cb.type="checkbox";
this.chosenColor=document.createElement("input");
this.chosenColor.type="text";
this.chosenColor.maxLength=7;
this.chosenColor.style.width="50px";
this.chosenColor.style.fontSize="11px";
this.chosenColor.onchange=function(){
if(/#[0-9a-f]{6,6}/i.test(this.value)){
_2.backSample.style.backgroundColor=this.value;
_2.foreSample.style.color=this.value;
}
};
this.backSample=document.createElement("div");
this.backSample.appendChild(document.createTextNode(" "));
this.backSample.style.fontWeight="bold";
this.backSample.style.fontFamily="small-caption,caption,sans-serif";
this.backSample.fontSize="x-small";
this.foreSample=document.createElement("div");
this.foreSample.appendChild(document.createTextNode(Xinha._lc("Sample")));
this.foreSample.style.fontWeight="bold";
this.foreSample.style.fontFamily="small-caption,caption,sans-serif";
this.foreSample.fontSize="x-small";
function toHex(_d){
var h=_d.toString(16);
if(h.length<2){
h="0"+h;
}
return h;
};
function tupleToColor(_f){
return "#"+toHex(_f.red)+toHex(_f.green)+toHex(_f.blue);
};
function nearestPowerOf(num,_11){
return Math.round(Math.round(num/_11)*_11);
};
function doubleHexDec(dec){
return parseInt(dec.toString(16)+dec.toString(16),16);
};
function rgbToWebsafe(_13){
_13.red=doubleHexDec(nearestPowerOf(parseInt(toHex(_13.red).charAt(0),16),3));
_13.blue=doubleHexDec(nearestPowerOf(parseInt(toHex(_13.blue).charAt(0),16),3));
_13.green=doubleHexDec(nearestPowerOf(parseInt(toHex(_13.green).charAt(0),16),3));
return _13;
};
function hsvToRGB(h,s,v){
var _17;
if(s===0){
_17={red:v,green:v,blue:v};
}else{
h/=60;
var i=Math.floor(h);
var f=h-i;
var p=v*(1-s);
var q=v*(1-s*f);
var t=v*(1-s*(1-f));
switch(i){
case 0:
_17={red:v,green:t,blue:p};
break;
case 1:
_17={red:q,green:v,blue:p};
break;
case 2:
_17={red:p,green:v,blue:t};
break;
case 3:
_17={red:p,green:q,blue:v};
break;
case 4:
_17={red:t,green:p,blue:v};
break;
default:
_17={red:v,green:p,blue:q};
break;
}
}
_17.red=Math.ceil(_17.red*255);
_17.green=Math.ceil(_17.green*255);
_17.blue=Math.ceil(_17.blue*255);
return _17;
};
var _1d=this;
function closeOnBodyClick(ev){
ev=ev?ev:window.event;
el=ev.target?ev.target:ev.srcElement;
do{
if(el==_1d.table){
return;
}
}while(el=el.parentNode);
_1d.close();
};
this.open=function(_1f,_20,_21){
this.table.style.display="";
this.pick_color();
if(_21&&/#[0-9a-f]{6,6}/i.test(_21)){
this.chosenColor.value=_21;
this.backSample.style.backgroundColor=_21;
this.foreSample.style.color=_21;
}
Xinha._addEvent(document.body,"mousedown",closeOnBodyClick);
this.table.style.position="absolute";
var e=_20;
var top=0;
var _24=0;
do{
if(e.style.position=="fixed"){
this.table.style.position="fixed";
}
top+=e.offsetTop-e.scrollTop;
_24+=e.offsetLeft-e.scrollLeft;
e=e.offsetParent;
}while(e);
var x,y;
if(/top/.test(_1f)||(top+this.table.offsetHeight>document.body.offsetHeight)){
if(top-this.table.offsetHeight>0){
this.table.style.top=(top-this.table.offsetHeight)+"px";
}else{
this.table.style.top=0;
}
}else{
this.table.style.top=(top+_20.offsetHeight)+"px";
}
if(/left/.test(_1f)||(_24+this.table.offsetWidth>document.body.offsetWidth)){
if(_24-(this.table.offsetWidth-_20.offsetWidth)>0){
this.table.style.left=(_24-(this.table.offsetWidth-_20.offsetWidth))+"px";
}else{
this.table.style.left=0;
}
}else{
this.table.style.left=_24+"px";
}
if(this.is_ie_6){
this.iframe.style.top=this.table.style.top;
this.iframe.style.left=this.table.style.left;
}
};
function pickCell(_27){
_2.chosenColor.value=_27.colorCode;
_2.backSample.style.backgroundColor=_27.colorCode;
_2.foreSample.style.color=_27.colorCode;
if((_27.hue>=195&&_27.saturation>0.5)||(_27.hue===0&&_27.saturation===0&&_27.value<0.5)||(_27.hue!==0&&_2.value<0.75)){
_27.style.borderColor="#fff";
}else{
_27.style.borderColor="#000";
}
_5=_27.thisrow;
_6=_27.thiscol;
};
function pickValue(_28){
if(_2.value<0.5){
_28.style.borderColor="#fff";
}else{
_28.style.borderColor="#000";
}
_9=_28.thisrow;
_8=_28.thiscol;
_2.chosenColor.value=_2.saved_cells[_5][_6].colorCode;
_2.backSample.style.backgroundColor=_2.saved_cells[_5][_6].colorCode;
_2.foreSample.style.color=_2.saved_cells[_5][_6].colorCode;
};
function unpickCell(row,col){
_2.saved_cells[row][col].style.borderColor=_2.saved_cells[row][col].colorCode;
};
this.pick_color=function(){
var _2b,_2c;
var _2d=this;
var _2e=359/(this.side);
var _2f=1/(this.side-1);
var _30=1/(this.side-1);
var _31=this.constrain_cb.checked;
if(this.saved_cells===null){
this.saved_cells=[];
for(var row=0;row<this.side;row++){
var tr=document.createElement("tr");
this.saved_cells[row]=[];
for(var col=0;col<this.side;col++){
var td=document.createElement("td");
if(_31){
td.colorCode=tupleToColor(rgbToWebsafe(hsvToRGB(_2e*row,_2f*col,this.value)));
}else{
td.colorCode=tupleToColor(hsvToRGB(_2e*row,_2f*col,this.value));
}
this.saved_cells[row][col]=td;
td.style.height=this.cellsize+"px";
td.style.width=this.cellsize-2+"px";
td.style.borderWidth="1px";
td.style.borderStyle="solid";
td.style.borderColor=td.colorCode;
td.style.backgroundColor=td.colorCode;
if(row==_5&&col==_6){
td.style.borderColor="#000";
this.chosenColor.value=td.colorCode;
this.backSample.style.backgroundColor=td.colorCode;
this.foreSample.style.color=td.colorCode;
}
td.hue=_2e*row;
td.saturation=_2f*col;
td.thisrow=row;
td.thiscol=col;
td.onmousedown=function(){
_3=true;
_2d.saved_cells[_5][_6].style.borderColor=_2d.saved_cells[_5][_6].colorCode;
pickCell(this);
};
td.onmouseover=function(){
if(_3){
pickCell(this);
}
};
td.onmouseout=function(){
if(_3){
this.style.borderColor=this.colorCode;
}
};
td.ondblclick=function(){
Xinha.colorPicker.remember(this.colorCode,_2d.savecolors);
_2d.callback(this.colorCode);
_2d.close();
};
td.appendChild(document.createTextNode(" "));
td.style.cursor="pointer";
tr.appendChild(td);
td=null;
}
var td=document.createElement("td");
td.appendChild(document.createTextNode(" "));
td.style.width=this.cellsize+"px";
tr.appendChild(td);
td=null;
var td=document.createElement("td");
this.saved_cells[row][col+1]=td;
td.appendChild(document.createTextNode(" "));
td.style.width=this.cellsize-2+"px";
td.style.height=this.cellsize+"px";
td.constrainedColorCode=tupleToColor(rgbToWebsafe(hsvToRGB(0,0,_30*row)));
td.style.backgroundColor=td.colorCode=tupleToColor(hsvToRGB(0,0,_30*row));
td.style.borderWidth="1px";
td.style.borderStyle="solid";
td.style.borderColor=td.colorCode;
if(row==_9){
td.style.borderColor="black";
}
td.hue=_2e*row;
td.saturation=_2f*col;
td.hsv_value=_30*row;
td.thisrow=row;
td.thiscol=col+1;
td.onmousedown=function(){
_4=true;
_2d.saved_cells[_9][_8].style.borderColor=_2d.saved_cells[_9][_8].colorCode;
_2d.value=this.hsv_value;
_2d.pick_color();
pickValue(this);
};
td.onmouseover=function(){
if(_4){
_2d.value=this.hsv_value;
_2d.pick_color();
pickValue(this);
}
};
td.onmouseout=function(){
if(_4){
this.style.borderColor=this.colorCode;
}
};
td.style.cursor="pointer";
tr.appendChild(td);
td=null;
this.tbody.appendChild(tr);
tr=null;
}
var tr=document.createElement("tr");
this.saved_cells[row]=[];
for(var col=0;col<this.side;col++){
var td=document.createElement("td");
if(_31){
td.colorCode=tupleToColor(rgbToWebsafe(hsvToRGB(0,0,_30*(this.side-col-1))));
}else{
td.colorCode=tupleToColor(hsvToRGB(0,0,_30*(this.side-col-1)));
}
this.saved_cells[row][col]=td;
td.style.height=this.cellsize+"px";
td.style.width=this.cellsize-2+"px";
td.style.borderWidth="1px";
td.style.borderStyle="solid";
td.style.borderColor=td.colorCode;
td.style.backgroundColor=td.colorCode;
td.hue=0;
td.saturation=0;
td.value=_30*(this.side-col-1);
td.thisrow=row;
td.thiscol=col;
td.onmousedown=function(){
_3=true;
_2d.saved_cells[_5][_6].style.borderColor=_2d.saved_cells[_5][_6].colorCode;
pickCell(this);
};
td.onmouseover=function(){
if(_3){
pickCell(this);
}
};
td.onmouseout=function(){
if(_3){
this.style.borderColor=this.colorCode;
}
};
td.ondblclick=function(){
Xinha.colorPicker.remember(this.colorCode,_2d.savecolors);
_2d.callback(this.colorCode);
_2d.close();
};
td.appendChild(document.createTextNode(" "));
td.style.cursor="pointer";
tr.appendChild(td);
td=null;
}
this.tbody.appendChild(tr);
tr=null;
var tr=document.createElement("tr");
var td=document.createElement("td");
tr.appendChild(td);
td.colSpan=this.side+2;
td.style.padding="3px";
if(this.websafe){
var div=document.createElement("div");
var _37=document.createElement("label");
_37.appendChild(document.createTextNode(Xinha._lc("Web Safe: ")));
this.constrain_cb.onclick=function(){
_2d.pick_color();
};
_37.appendChild(this.constrain_cb);
_37.style.fontFamily="small-caption,caption,sans-serif";
_37.style.fontSize="x-small";
div.appendChild(_37);
td.appendChild(div);
div=null;
}
var div=document.createElement("div");
var _37=document.createElement("label");
_37.style.fontFamily="small-caption,caption,sans-serif";
_37.style.fontSize="x-small";
_37.appendChild(document.createTextNode(Xinha._lc("Color: ")));
_37.appendChild(this.chosenColor);
div.appendChild(_37);
var but=document.createElement("span");
but.className="buttonColor ";
but.style.fontSize="13px";
but.style.width="24px";
but.style.marginLeft="2px";
but.style.padding="0px 4px";
but.style.cursor="pointer";
but.onclick=function(){
Xinha.colorPicker.remember(_2d.chosenColor.value,_2d.savecolors);
_2d.callback(_2d.chosenColor.value);
_2d.close();
};
but.appendChild(document.createTextNode(Xinha._lc("OK")));
but.align="center";
div.appendChild(but);
td.appendChild(div);
var _39=document.createElement("table");
_39.style.width="100%";
var _3a=document.createElement("tbody");
_39.appendChild(_3a);
var _3b=document.createElement("tr");
_3a.appendChild(_3b);
var _3c=document.createElement("td");
_3b.appendChild(_3c);
_3c.appendChild(this.backSample);
_3c.style.width="50%";
var _3d=document.createElement("td");
_3b.appendChild(_3d);
_3d.appendChild(this.foreSample);
_3d.style.width="50%";
td.appendChild(_39);
var _3e=document.createElement("div");
_3e.style.clear="both";
function createSavedColors(_3f){
var _40=Xinha.is_ie;
var div=document.createElement("div");
div.style.width=_2d.cellsize+"px";
div.style.height=_2d.cellsize+"px";
div.style.margin="1px";
div.style.border="1px solid black";
div.style.cursor="pointer";
div.style.backgroundColor=_3f;
div.style[_40?"styleFloat":"cssFloat"]="left";
div.ondblclick=function(){
_2d.callback(_3f);
_2d.close();
};
div.onclick=function(){
_2d.chosenColor.value=_3f;
_2d.backSample.style.backgroundColor=_3f;
_2d.foreSample.style.color=_3f;
};
_3e.appendChild(div);
};
for(var _42=0;_42<Xinha.colorPicker.savedColors.length;_42++){
createSavedColors(Xinha.colorPicker.savedColors[_42]);
}
td.appendChild(_3e);
this.tbody.appendChild(tr);
document.body.appendChild(this.table);
if(this.is_ie_6){
if(!this.iframe){
this.iframe=document.createElement("iframe");
this.iframe.frameBorder=0;
this.iframe.src="javascript:;";
this.iframe.style.position="absolute";
this.iframe.style.width=this.table.offsetWidth;
this.iframe.style.height=this.table.offsetHeight;
this.iframe.style.zIndex="1049";
document.body.insertBefore(this.iframe,this.table);
}
this.iframe.style.display="";
}
}else{
for(var row=0;row<this.side;row++){
for(var col=0;col<this.side;col++){
if(_31){
this.saved_cells[row][col].colorCode=tupleToColor(rgbToWebsafe(hsvToRGB(_2e*row,_2f*col,this.value)));
}else{
this.saved_cells[row][col].colorCode=tupleToColor(hsvToRGB(_2e*row,_2f*col,this.value));
}
this.saved_cells[row][col].style.backgroundColor=this.saved_cells[row][col].colorCode;
this.saved_cells[row][col].style.borderColor=this.saved_cells[row][col].colorCode;
}
}
var _43=this.saved_cells[_5][_6];
this.chosenColor.value=_43.colorCode;
this.backSample.style.backgroundColor=_43.colorCode;
this.foreSample.style.color=_43.colorCode;
if((_43.hue>=195&&_43.saturation>0.5)||(_43.hue===0&&_43.saturation===0&&_43.value<0.5)||(_43.hue!==0&&_2d.value<0.75)){
_43.style.borderColor="#fff";
}else{
_43.style.borderColor="#000";
}
}
};
this.close=function(){
Xinha._removeEvent(document.body,"mousedown",closeOnBodyClick);
this.table.style.display="none";
if(this.is_ie_6){
if(this.iframe){
this.iframe.style.display="none";
}
}
};
};
Xinha.colorPicker.savedColors=[];
Xinha.colorPicker.remember=function(_44,_45){
for(var i=Xinha.colorPicker.savedColors.length;i--;){
if(Xinha.colorPicker.savedColors[i]==_44){
return false;
}
}
Xinha.colorPicker.savedColors.splice(0,0,_44);
Xinha.colorPicker.savedColors=Xinha.colorPicker.savedColors.slice(0,_45);
var _47=new Date();
_47.setMonth(_47.getMonth()+1);
document.cookie="XinhaColorPicker="+escape(Xinha.colorPicker.savedColors.join("-"))+";expires="+_47.toGMTString();
return true;
};
Xinha.colorPicker.loadColors=function(){
var _48=document.cookie.indexOf("XinhaColorPicker");
if(_48!=-1){
var _49=(document.cookie.indexOf("=",_48)+1);
var end=document.cookie.indexOf(";",_48);
if(end==-1){
end=document.cookie.length;
}
Xinha.colorPicker.savedColors=unescape(document.cookie.substring(_49,end)).split("-");
}
};
Xinha.colorPicker.InputBinding=function(_4b,_4c){
var doc=_4b.ownerDocument;
var _4e=doc.createElement("span");
_4e.className="buttonColor";
var _4f=this.chooser=doc.createElement("span");
_4f.className="chooser";
if(_4b.value){
_4f.style.backgroundColor=_4b.value;
}
_4f.onmouseover=function(){
_4f.className="chooser buttonColor-hilite";
};
_4f.onmouseout=function(){
_4f.className="chooser";
};
_4f.appendChild(doc.createTextNode(" "));
_4e.appendChild(_4f);
var _50=doc.createElement("span");
_50.className="nocolor";
_50.onmouseover=function(){
_50.className="nocolor buttonColor-hilite";
_50.style.color="#f00";
};
_50.onmouseout=function(){
_50.className="nocolor";
_50.style.color="#000";
};
_50.onclick=function(){
_4b.value="";
_4f.style.backgroundColor="";
};
_50.appendChild(doc.createTextNode("×"));
_4e.appendChild(_50);
_4b.parentNode.insertBefore(_4e,_4b.nextSibling);
Xinha._addEvent(_4b,"change",function(){
_4f.style.backgroundColor=this.value;
});
_4c=(_4c)?Xinha.cloneObject(_4c):{cellsize:"5px"};
_4c.callback=(_4c.callback)?_4c.callback:function(_51){
_4f.style.backgroundColor=_51;
_4b.value=_51;
};
_4f.onclick=function(){
var _52=new Xinha.colorPicker(_4c);
_52.open("",_4f,_4b.value);
};
Xinha.freeLater(this,"chooser");
};
Xinha.colorPicker.InputBinding.prototype.setColor=function(_53){
this.chooser.style.backgroundColor=_53;
};

