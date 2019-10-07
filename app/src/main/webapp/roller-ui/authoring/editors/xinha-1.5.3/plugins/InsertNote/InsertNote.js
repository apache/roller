/*------------------------------------------*\
InsertNote plugin for Xinha
___________________________
     
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License (at http://www.gnu.org/licenses/lgpl.html) 
for more details.

InsertNote
----------
Allows for the insertion of footnotes/endnotes. Supports:
  * Automatic numbering
  * Automatic linking to anchors
  * Links back to each specific citation
  * Saves previously referenced notes for easy reuse
  * Cleanup of unreferenced notes (or references to non-existent notes)
\*------------------------------------------*/

function InsertNote(editor)
{
  this.editor = editor;
  var cfg = editor.config;
  var self = this;

  cfg.registerButton({
    id       : "insert-note",
    tooltip  : this._lc("Insert footnote"),
    image    : editor.imgURL("insert-note.gif", "InsertNote"),
    textMode : false,
    action   : function() {
      self.show();
    }
  });
  cfg.addToolbarElement("insert-note", "createlink", 1);
};

InsertNote._pluginInfo = {
  name          : "InsertNote",
  version       : "0.4",
  developer     : "Nicholas Bergson-Shilcock",
  developer_url : "http://www.nicholasbs.com",
  c_owner       : "Nicholas Bergson-Shilcock",
  sponsor       : "The Open Planning Project",
  sponsor_url   : "http://topp.openplans.org",
  license       : "LGPL"
};

InsertNote.prototype.onGenerateOnce = function ()
{
  this.notes = {};
  this.noteNums = {};
  this.ID_PREFIX = "InsertNoteID_"; // This should suffice to be unique...
  this.MARKER_SUFFIX = "_marker";
  this.MARKER_CLASS = "InsertNoteMarker";
  this.LINK_BACK_SUFFIX = "_LinkBacks";
  this.NOTE_LIST_ID = "InsertNote_NoteList";

  this._prepareDialog();
};

InsertNote.prototype._prepareDialog = function()
{
  var self = this;
  var editor = this.editor;

  if (!this.html)
  {
    Xinha._getback(Xinha.getPluginDir("InsertNote") + "/dialog.html",
                   function(getback) { 
                     self.html = getback;
                     self._prepareDialog();
                   });
    return;
  }

  this.dialog = new Xinha.Dialog(editor, this.html, "InsertNote", 
                                 {width: 400,
                                  closeOnEscape: true,
                                  resizable: false,
                                  centered: true,
                                  modal: true
                                 });

  this.dialog.getElementById("ok").onclick = function() {self.apply();};
  this.dialog.getElementById("cancel").onclick = function() {self.dialog.hide();};
  this.dialog.getElementById("noteMenu").onchange = function() {
    var noteId = this.options[this.selectedIndex].value;
    var text = self.notes[noteId] || "";
    var textArea = self.dialog.getElementById("noteContent")
    textArea.value = text;
    if (text) {
      textArea.disabled = true;
    } else {
      textArea.disabled = false;
      textArea.focus();
    }
  };

  this.ready = true;
};

InsertNote.prototype.show = function()
{
  if (!this.ready)
  {
    var self = this;
    window.setTimeout(function() {self.show();}, 80);
    return;
  }
  
  var editor = this.editor;

  // All of the Xinha dialogs are part of the main document, not the editor
  // document, so we have to use the correct document reference to modify them.
  var doc = document;

  this.repairNotes();
  
  // Can't insert footnotes inside of other footnotes...
  if (this.cursorInFootnote()) 
  {
    alert("Footnotes cannot be inserted inside of other footnotes.");
    return
  }

  this.dialog.show();

  var popupMenu = this.dialog.getElementById("noteMenu");
  while (popupMenu.lastChild.value != "new")
  {
    // Remove all menu options except "New note"
    popupMenu.removeChild(popupMenu.lastChild);
  }
  var temp, displayName, n;
  var orderedIds = this._getOrderedNoteIds();
  for (var i=0; i<orderedIds.length; i++)
  {
    n = orderedIds[i];
    temp = doc.createElement("option");
    temp.setAttribute("value", n);
    displayName = this.notes[n];
    displayName = displayName.replace(/<[^>]*>/gi, ""); // XXX TODO: Strip HTML less naively.
    if (displayName.length > 50) // Truncate preview text to 50 chars
    {
      displayName = displayName.substr(0,50) + "...";
    }
    displayName = this._getNumFromNoteId(n) + ". " + displayName;
    temp.appendChild(doc.createTextNode(displayName));
    popupMenu.appendChild(temp);
  }

  var textArea = this.dialog.getElementById("noteContent");
  textArea.disabled = false;
  textArea.focus();
  textArea.value = "";
};

// Checks to see whether or not the cursor is placed inside of a footnote
InsertNote.prototype.cursorInFootnote = function(ignoreMarkers)
{
  var ancestors = this.editor.getAllAncestors();
  for (var i=0; i<ancestors.length; i++) 
  {
    if (ancestors[i].id == this.NOTE_LIST_ID)
      return true;
    if (ancestors[i].className == this.MARKER_CLASS && !ignoreMarkers)
      return true;
  }
  
  return false;
}

/*  This function makes sure all of the notes are consistent.
    Specifically:
       * If a note exists but is never referred to, that note is removed
       * Conversely, if a note is referred to but does not exist, the 
         references are removed
       * If the markup for a note marker exists but contains no visible
         text, the markup is removed
       * All numbering is updated as follows:
          - the first note is numbered 1
          - each note thereafter is numbered sequentially, unless it has
            already been referenced, in which case it's given the same
            number as it was the first time it was referenced.
       * The note text stored in the this.notes hash is updated to reflect
         any changes made by the user (so if the user changes the 
         citation body in Xinha we don't overwrite these changes next
         time we update things)
       * The noteNums array is regenerated so as to be up to date

*/
InsertNote.prototype.repairNotes = function() 
{
  var self = this;
  var note;
  var markers;
  var marker;
  var isEmpty;
  var temp;
  var doc = this.editor._doc;

  this.repairScheduled = false;

  // First, we remove any markers if they reference a note that doesn't exist.
  // If the note does exist, we make sure we have a reference to it by
  // calling this._saveNote()
  markers = this._getMarkers();
  for (var i=0; i<markers.length; i++)
  {
    marker = markers[i];

    // Remove empty markers
    isEmpty = false;
    if (marker) // check if anchor is empty
      isEmpty = marker.innerHTML.match(/^(<span[^>]*>)?(<sup>)?(<a[^>]*>)?[\s]*(<\/a>)?(<\/sup>)?(<\/span>)?$/m);
    if (isEmpty)
      marker.parentNode.removeChild(marker);


    var noteId = this._getIdFromMarkerId(marker.id);
    if (!this.notes[noteId]) 
    {
      // We don't have this note stored, let's see if 
      // it actually exists...
      note = doc.getElementById(noteId);
      if (note)
      {
        this._saveNote(note);
      } else
      {
        if (marker)
          marker.parentNode.removeChild(marker);
      }
    }
  }

  // Now we iterate through all the note ids we have stored...
  for (var id in this.notes)
  {
    // Get reference to note
    note = doc.getElementById(id);
    
    markers = this._getMarkers(id);
    if (note)
    {
      if (markers.length == 0) // Remove note if it's not referenced anywhere
      {
        // remove the note
        note.parentNode.removeChild(note);
        delete this.notes[id];
      } else 
      {
        // The note exists and *is* referenced, so we save its contents
        this._saveNote(note);
      }
    } else
    {
      // Note no longer exists. Remove any references to it.
      for (var i=0; i<markers.length; i++)
      {
        markers[i].parentNode.removeChild(markers[i]);
      }
      delete this.notes[id];
    }
  }

  // Correct marker numbering
  this._updateRefNums();

  // At this point we now have all of the note markers correctly
  // numbered, an up-to-date hash (this.noteNums) of note numbers 
  // keyed to note ids, and no stray markers or unreferenced notes.

  // Final step: Correct the numbering/order of the actual notes
  var noteList = doc.getElementById(this.NOTE_LIST_ID);
  var newNoteList = this._createNoteList();
  if (newNoteList)
  {
    noteList.parentNode.replaceChild(newNoteList, noteList);
  } else
  {
    if (noteList)
      noteList.parentNode.removeChild(noteList);
  }
};

InsertNote.prototype._saveNote = function(note)
{
  var doc = this.editor._doc;
  // Before saving a note's contents, we do two things:
  // 1) we remove the markup we inserted so we don't end up with duplicate links
  // 2) check to see if the note is empty, and if so, we remove it
  var temp = doc.getElementById(this._getLinkBackSpanId(note.id));
  if (temp)
    temp.parentNode.removeChild(temp);
  if (note.innerHTML)
  {
    this.notes[note.id] = note.innerHTML;
  } else
  {
    // If we're here then the note is empty,
    // so we delete it and any markers for it.
    delete this.notes[note.id];
    markers = this._getMarkers(note.id);
    for (var i=0; i<markers.length; i++)
      markers[i].parentNode.removeChild(markers[i]);
  }
};

InsertNote.prototype._updateRefNums = function()
{
  var runningCount = 1; // first reference is 1
  var num;
  var marker;
  var noteId;

  // Reset all note numbering...
  this.noteNums = {};
  
  var markers = this._getMarkers();

  for (var i=0; i<markers.length; i++)
  {
    marker = markers[i];
    noteId = this._getIdFromMarkerId(marker.id);
    if (this.noteNums[noteId])
    {
      num = this.noteNums[noteId];
    } else
    {
      this.noteNums[noteId] = runningCount;
      num = runningCount++;
    }
    // span -> sup -> anchor
    marker.firstChild.firstChild.innerHTML = num;
    // XXX TODO: don't use firstChild.firstChild?
  }
};

InsertNote.prototype.apply = function()
{
  var editor = this.editor;
  var param = this.dialog.hide();
  var noteId = param['noteMenu'].value;
  var newNote = (noteId == "new");
  var noteContent = param['noteContent'];
  var doc = editor._doc;

  if (newNote) // Inserting a new note
    noteId = this._getNextId(this.ID_PREFIX);

  this.notes[noteId] = noteContent;

  var markerTemplate = this._createNoteMarker(noteId)
  editor.insertNodeAtSelection(markerTemplate);
  var marker = doc.getElementById(markerTemplate.id);
  
  var currentNoteList = doc.getElementById(this.NOTE_LIST_ID);
  var newNoteList = this._createNoteList();
  var body = doc.body;
  if (currentNoteList)
  {
    body.replaceChild(newNoteList, currentNoteList);
  } else
  {
    body.appendChild(newNoteList);
  }

  var newel = doc.createTextNode('\u00a0');
  if (marker.nextSibling)
  {
    newel = marker.parentNode.insertBefore(newel, marker.nextSibling);
  } else
  {
    newel = marker.parentNode.appendChild(newel);
  }
  this.repairNotes();
  editor.selectNodeContents(newel,false);
};

InsertNote.prototype._createNoteList = function()
{
  var doc = this.editor._doc;
  var noteList = doc.createElement("ol");
  noteList.id = this.NOTE_LIST_ID;
  var orderedIds = this._getOrderedNoteIds();
  if (orderedIds.length == 0)
    return null;
  var note, id;
  for (var i=0; i<orderedIds.length; i++)
  {
    id = orderedIds[i];
    note = this._createNoteNode(id, this.notes[id]);
    noteList.appendChild(note);
  }
  return noteList;
};

InsertNote.prototype._createNoteMarker = function(noteId)
{
  // Create the note marker, i.e., the # superscript
  var doc = this.editor._doc;
  var noteNum = this._getNumFromNoteId(noteId);

  var link = doc.createElement("a");
  link.href = "#" + noteId;
  link.innerHTML = noteNum;
  
  var superscript = doc.createElement("sup");
  superscript.appendChild(link);

  var span = doc.createElement("span");
  span.id = this._getNextMarkerId(noteId);
  span.className = this.MARKER_CLASS;
  span.appendChild(superscript);

  return span
};

InsertNote.prototype._createNoteNode = function(noteId, noteContent)
{
  var doc = this.editor._doc;

  var anchor;
  var superscript = doc.createElement("sup");
  var markers = this._getMarkers(noteId);
  var temp;
  for (var i=0; i<markers.length; i++)
  {
    anchor = doc.createElement("a");    
    if (markers.length == 1)
    {
      anchor.innerHTML = "^";
    }
    else
    {
      anchor.innerHTML = this._letterNum(i);
    }
    anchor.href = "#" + markers[i].id;
    superscript.appendChild(anchor);
    if (i < markers.length-1)
    {
      temp = doc.createTextNode(", ");
      superscript.appendChild(temp);
    }
  }

  var span = doc.createElement("span");
  span.id = this._getLinkBackSpanId(noteId);
  span.appendChild(superscript);

  var li = doc.createElement("li");
  li.id = noteId;
  li.innerHTML = noteContent;
  li.appendChild(span);

  return li;
};

InsertNote.prototype._getNextId = function(prefix)
{
  // We can't just use Xinha.uniq here because it doesn't
  // work across editing sessions. E.g., it will break if 
  // the user copies the html from one editor to another.
  var id = Xinha.uniq(prefix);
  while (this.editor._doc.getElementById(id))
    id = Xinha.uniq(prefix);
  return id;
};

InsertNote.prototype._getOrderedNoteIds = function()
{
  var self = this;
  var orderedIds = new Array();
  for (var id in this.notes)
    orderedIds.push(id);
  
  orderedIds.sort(function(a,b) {
    var n1 = self._getNumFromNoteId(a);
    var n2 = self._getNumFromNoteId(b);

    return (n1 - n2);
  });

  return orderedIds;
};

InsertNote.prototype._getNumFromNoteId = function(noteId)
{
  if (!this.noteNums[noteId])
    return 0;
  return this.noteNums[noteId];
};


// Return array of all markers that reference the specified note.
// If noteId is not supplied, *all* markers are returned.
InsertNote.prototype._getMarkers = function(noteId)
{
  var doc = this.editor._doc;
  var markers = Xinha.getElementsByClassName(doc, this.MARKER_CLASS);
  
  if (!noteId)
    return markers;
  
  var els = new Array();
  for (var i=0; i<markers.length; i++)
  {
    if (this._getIdFromMarkerId(markers[i].id) == noteId)
    {
      els.push(markers[i]);
    }
  }
  return els;
};

InsertNote.prototype._getNextMarkerId = function(noteId)
{
  return this._getNextId(noteId + this.MARKER_SUFFIX);
};

InsertNote.prototype._getIdFromMarkerId = function(markerId)
{
  return markerId.substr(0, markerId.search(this.MARKER_SUFFIX));
};

InsertNote.prototype._getLinkBackSpanId = function(noteId)
{
  return noteId + this.LINK_BACK_SUFFIX;
};

InsertNote.prototype._lc = function(string)
{
  return Xinha._lc(string, "InsertNote");
};

// Probably overkill, but it does the job
InsertNote.prototype._letterNum = function(num)
{
  var letters = "abcdefghijklmnopqrstuvwxyz";
  var len = Math.floor(num/letters.length) + 1;
  var s = "";
  for (var i=0; i<len; i++)
    s += letters.substr(num % letters.length,1);
  return s;
};

InsertNote.prototype.inwardHtml = function(html)
{
  return html;
};

InsertNote.prototype.outwardHtml = function(html)
{
  this.repairNotes();
  return this.editor.getHTML();
};

InsertNote.prototype.onKeyPress = function (event)
{
  // This seems a bit hacky, but I don't presently see
  // a better way.
  //@NOTE: 8 = Backspace, 46 = Delete; this undocumented
  //  function apears to be for handling delete note references
  //  to have the note automatically deleted also
  if (event.keyCode == 8 || event.keyCode == 46)
  {
    var self = this;
    if (!this.repairScheduled && !this.cursorInFootnote(true)) // ignore if in a footnote
    {
      this.repairScheduled = true;
      window.setTimeout(function() { self.repairNotes(); }, 1000);
    }
  }
  return false;
};

/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
InsertNote.prototype.onMode = function (mode)
{
  return false;
};

/**
 * 
 * @param {String} mode either 'textmode' or 'wysiwyg'
 */
InsertNote.prototype.onBeforeMode = function (mode)
{
  return false;
};
