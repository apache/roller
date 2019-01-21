
  /*--------------------------------------:noTabs=true:tabSize=2:indentSize=2:--
    --  Xinha (is not htmlArea) - http://xinha.gogo.co.nz/
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    --
    --  This is the implementation of the standard popup dialog
    --
    --   Though "Dialog" looks like an object, it isn't really an object.  Instead
    --   it's just namespace for protecting global symbols.
    --
    --
    --  $HeadURL: http://svn.xinha.org/trunk/modules/Dialogs/dialog.js $
    --  $LastChangedDate: 2008-10-13 06:42:42 +1300 (Mon, 13 Oct 2008) $
    --  $LastChangedRevision: 1084 $
    --  $LastChangedBy: ray $
    --------------------------------------------------------------------------*/


function Dialog(url, action, init) {
	if (typeof init == "undefined") {
		init = window;	// pass this window object by default
	}
	if (typeof window.showModalDialog == 'function' && !Xinha.is_webkit) // webkit easily looses the selection with window.showModalDialog
	{
		Dialog._return = function(retVal) {
			if (typeof action == 'function') action (retVal);
		}
		var r = window.showModalDialog(url, init, "dialogheight=300;dialogwidth=400;resizable=yes");
	}
	else
	{
		Dialog._geckoOpenModal(url, action, init);
	}
}

Dialog._parentEvent = function(ev) {
	setTimeout( function() { if (Dialog._modal && !Dialog._modal.closed) { Dialog._modal.focus() } }, 50);
	try {
		if (Dialog._modal && !Dialog._modal.closed) {
			Xinha._stopEvent(ev);
		} 
	} catch (e) {
		//after closing the popup in IE the events are not released and trying to access Dialog._modal.closed causes an error
	}
};


// should be a function, the return handler of the currently opened dialog.
Dialog._return = null;

// constant, the currently opened dialog
Dialog._modal = null;

// the dialog will read it's args from this variable
Dialog._arguments = null;

Dialog._selection = null;

Dialog._geckoOpenModal = function(url, action, init) {
	var dlg = window.open(url, "hadialog",
			      "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
			      "scrollbars=no,resizable=yes,modal=yes,dependable=yes");
	Dialog._modal = dlg;
	Dialog._arguments = init;

	// capture some window's events
	function capwin(w) {
		Xinha._addEvent(w, "click", Dialog._parentEvent);
		Xinha._addEvent(w, "mousedown", Dialog._parentEvent);
		Xinha._addEvent(w, "focus", Dialog._parentEvent);
	}
	// release the captured events
	function relwin(w) {
		Xinha._removeEvent(w, "click", Dialog._parentEvent);
		Xinha._removeEvent(w, "mousedown", Dialog._parentEvent);
		Xinha._removeEvent(w, "focus", Dialog._parentEvent);
	}
	capwin(window);
	// capture other frames, note the exception trapping, this is because
  // we are not permitted to add events to frames outside of the current
  // window's domain.
	for (var i = 0; i < window.frames.length; i++) {try { capwin(window.frames[i]); } catch(e) { } };
	// make up a function to be called when the Dialog ends.
	Dialog._return = function (val) {
		if (val && action) {
			action(val);
		}
		relwin(window);
		// capture other frames
		for (var i = 0; i < window.frames.length; i++) { try { relwin(window.frames[i]); } catch(e) { } };
		Dialog._modal = null;
	};
  Dialog._modal.focus();
};