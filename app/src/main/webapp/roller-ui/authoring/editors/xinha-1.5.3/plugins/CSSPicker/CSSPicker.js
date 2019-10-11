/** CSS Picker Plugin by Justin Rovang
 *
 * For full documentation, please see 
 *  http://bitbucket.org/rovangju/xinha_csspicker/wiki/Home
 *
 * SAMPLE CONFIG:
 *
 * You can configure the appearance of the item/style list using the following CSS classes:
 *	.CSSPickerOption (Normal state)
 *	.CSSPickerOptionOver (Mouse-over state, typically border change)
 *	.CSSPickerOptionActive (Indicator for active classes under the selection/carat)
 *	
 * Keys are CSS Class names
 *	wrapper: tag to wrap selected text with
 *	name: friendly name to display in panel with that class style applied to it.
 *
 * Sample config: 
 * CSSPicker.cssList = {
 *		'xinhaDashedBox'	: { 'wrapper':'div',	'name':'Breakout box' }
 *		'xinhaMiniHeadline' : { 'wrapper':'div',	'name':'Sub-headline' }
 * }
 *
 */

function CSSPicker(editor, args) {
	this.editor = editor;
	var CSSPicker = this;
}

CSSPicker._pluginInfo = {
	name	: "CSSPicker",	
	version : "2008-12-01",
	author	: "Justin Rovang"
}

CSSPicker.prototype.onGenerateOnce = function() {
	var editor = this.editor;
	var CSSPicker = this;
	editor._cssPicker = editor.addPanel("right");
	
	this.main = document.createElement("div");
	editor._cssPicker.style.backgroundColor='#dee5f8';
	editor._cssPicker.appendChild(this.main);
	
	Xinha.freeLater(this,"main");
	editor.showPanel(editor._cssPicker);
}


CSSPicker.prototype.onUpdateToolbar = function() {
	if(this.editor._cssPicker) {
		if(this._timeoutID) window.clearTimeout(this._timeoutID);
		var e = this.editor;
		this._timeoutID = window.setTimeout(function() { e._gen(); }, 250); //1000 = 1sec / 500=.5sec / 250=.25sec
	}
}

Xinha.prototype.listStyles = function(s) {
	var editor = this;
	var mySel = this.getSelection();
	var myRange;
	if(Xinha.is_ie) {
		myRange = this.saveSelection();//mySel;
		mySel = this.createRange(mySel).text;
	}
	
	var d = document.createElement("div");
	
	d.className='CSSPickerOption';
	
	/* If our carat is within an active class, highlight it */
	var toggleState = editor.getStyleInfo(s);
	if(toggleState) Xinha._addClass(d, 'CSSPickerOptionActive');
	
	d.align='center';
              // v-- Avoid lc_parse_strings.php
	d.innerHTML='<'+'div class="'+s+'">'+CSSPicker.cssList[s].name+'</div>';
	d.onclick = function() {
		editor.wrapStyle(s, mySel, myRange, CSSPicker.cssList[s].wrapper);
		return false;
	};
	
	Xinha._addEvent(d, 'mouseover', function(ev) {
		Xinha._addClass(d, 'CSSPickerOptionOver');
	});
	
	Xinha._addEvent(d, 'mouseout', function(ev) {
		Xinha._removeClass(d, 'CSSPickerOptionOver');
	});
	
	return d;
}

Xinha.prototype._gen = function() {
	this.plugins.CSSPicker.instance.main.innerHTML='';
	for(var s in CSSPicker.cssList) {
		this.plugins.CSSPicker.instance.main.appendChild(this.listStyles(s));
	}
	return true;
}

/*
	(string) s: style name
	(string) sel: selection text
	(object) myRange: selection object
	(string) sWrapper: wrapper tag (e.g.: div, span)
*/
Xinha.prototype.wrapStyle = function(s, sel, myRange, sWrapper) {
	if(!sWrapper) sWrapper="div";
	sWrapper=sWrapper.toLowerCase();
	
	/* The reason for these next lines is that we want the user to be able to place
	 * their cursor below the new div element. Otherwise they can't which makes
	 * placing anything after a div wrapper difficult/almost impossible. */
	var divBreak='';
	if(sWrapper=="div") divBreak='<br/>';
	
	var editor=this;
	this.focusEditor();
	if(Xinha.is_ie) this.restoreSelection(myRange);

	/* 
	 * First - Get parent elements and see if the style is already applied.
	 */
	var toggleState = editor.getStyleInfo(s);
	if(!toggleState) {
		/* Create a new wrapper when:
		 * 1. Selected text has no 'snug' wrapper around it already.
		 * 2. If it does have a 'snug' wrapper, only append to the className if it's of the same type (span or div) 
		 */
		if(sel == '') sel = '&nbsp;'; //We insert this if the selection is empty, making it easier for carat placement via click
		
		this.insertHTML("<"+sWrapper+" class='"+s+"'>"+sel+"</"+sWrapper+">"+divBreak);
		/* Modify the 'snug' wrapper if the above conditions are not met for a new element: */		
	}
	else {
		/* 1. If the current ancestor has -just- this classname. It should be removed. 
		 * 2. If it has more than one class, it should be removed from the list of the parents
		 */
		Xinha._removeClass(toggleState, s);
	}
	
	return true;
}

Xinha.prototype.getStyleInfo = function(sClassToProbe) {
	var editor = this;
	var aList = this.getAllAncestors();
	var a,s;
	
	if(aList) aList.pop(); //We don't want the body element to show up in this list.
	if(aList.length > 0) {
		for(var o in aList){
			a = aList[o];
			/* Instead of break down and rebuild the array for this search, we're going
			 * to do some string trickery...
			 *  // NOTE: THIS MAY BE PRONE TO PARTIAL MATCHES. SOLUTION IS TO ADD A SPACE PREPEND 
			 */
			if(a.className) {
				s = a.className.trim()+' ';
				if(s.toLowerCase().match(sClassToProbe.toLowerCase()+' ')) {
					return a;
				}
			}
		}
	}
	return false;
}
