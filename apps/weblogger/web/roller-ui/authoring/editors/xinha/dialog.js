// htmlArea v3.0 - Copyright (c) 2003-2004 interactivetools.com, inc.
// This copyright notice MUST stay intact for use (see license.txt).
//
// Portions (c) dynarch.com, 2003-2004
//
// A free WYSIWYG editor replacement for <textarea> fields.
// For full source code and docs, visit http://www.interactivetools.com/
//
// Version 3.0 developed by Mihai Bazon.
//   http://dynarch.com/mishoo
//
// $Id: dialog.js 594 2006-10-03 16:13:53Z ray $

// Though "Dialog" looks like an object, it isn't really an object.  Instead
// it's just namespace for protecting global symbols.

function Dialog(url, action, init) {
	if (typeof init == "undefined") {
		init = window;	// pass this window object by default
	}
	Dialog._geckoOpenModal(url, action, init);
}

Dialog._parentEvent = function(ev) {
	setTimeout( function() { if (Dialog._modal && !Dialog._modal.closed) { Dialog._modal.focus() } }, 50);
	try {
		if (Dialog._modal && !Dialog._modal.closed) {
			HTMLArea._stopEvent(ev);
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

Dialog._geckoOpenModal = function(url, action, init) {
	var dlg = window.open(url, "hadialog",
			      "toolbar=no,menubar=no,personalbar=no,width=10,height=10," +
			      "scrollbars=no,resizable=yes,modal=yes,dependable=yes");
	Dialog._modal = dlg;
	Dialog._arguments = init;

	// capture some window's events
	function capwin(w) {
		HTMLArea._addEvent(w, "click", Dialog._parentEvent);
		HTMLArea._addEvent(w, "mousedown", Dialog._parentEvent);
		HTMLArea._addEvent(w, "focus", Dialog._parentEvent);
	}
	// release the captured events
	function relwin(w) {
		HTMLArea._removeEvent(w, "click", Dialog._parentEvent);
		HTMLArea._removeEvent(w, "mousedown", Dialog._parentEvent);
		HTMLArea._removeEvent(w, "focus", Dialog._parentEvent);
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