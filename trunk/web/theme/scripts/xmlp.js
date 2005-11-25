/*
 * xmlp.js, version 1.0
 * An XML parser in JavaScript
 *
 * Revision history:
 *    1.0, 23 Nov 2001 : Initial version
 *
 * Copyright (C) 2001 David A. Lindquist (dave@gazingus.org)
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/// Node ///////////////////////////////////////////////////////////////////////

Node.ELEMENT_NODE                =  1;
Node.ATTRIBUTE_NODE              =  2;
Node.TEXT_NODE                   =  3;
Node.CDATA_SECTION_NODE          =  4;
Node.ENTITY_REFERENCE_NODE       =  5;
Node.ENTITY_NODE                 =  6; // not used
Node.PROCESSING_INSTRUCTION_NODE =  7;
Node.COMMENT_NODE                =  8;
Node.DOCUMENT_NODE               =  9;
Node.DOCUMENT_TYPE_NODE          = 10;
Node.DOCUMENT_FRAGMENT_NODE      = 11;
Node.NOTATION_NODE               = 12; // not used

function Node() {
  this.attributes = null;
  this.childNodes = new NodeList();
  this.firstChild = null
  this.lastChild = null;
  this.nextSibling = null;
  this.nodeName = null;
  this.nodeType = null;
  this.nodeValue = null;
  this.ownerDocument = null;
  this.parentNode = null;
  this.previousSibling = null;
}

Node.prototype.getAttributes = function() { return this.attributes; }
Node.prototype.getChildNodes = function() { return this.childNodes; }
Node.prototype.getFirstChild = function() { return this.firstChild; }
Node.prototype.getLastChild = function() { return this.lastChild; }
Node.prototype.getNextSibling = function() { return this.nextSibling; }
Node.prototype.getNodeName = function() { return this.nodeName; }
Node.prototype.getNodeType = function() { return this.nodeType; }
Node.prototype.getNodeValue = function() { return this.nodeValue; }
Node.prototype.getOwnerDocument = function() { return this.ownerDocument; }
Node.prototype.getParentNode = function() { return this.parentNode; }
Node.prototype.getPreviousSibling = function() { return this.previousSibling; }

Node.prototype.setNodeValue =
function() {
  // Default behavior is to do nothing;
  // overridden in some subclasses
}

Node.prototype.appendChild =
function( childNode ) {
  if ( this.nodeType == Node.ELEMENT_NODE ||
       this.nodeType == Node.ATTRIBUTE_NODE ||
       this.nodeType == Node.DOCUMENT_NODE ||
       this.nodeType == Node.DOCUMENT_FRAGMENT_NODE ) {
    this.childNodes.add( childNode );
  }
  else {
    // :REVISIT: change to DOMException
    throw new ParseError( "Cannot append child node" );
  }

  if ( this.ownerDocument != childNode.ownerDocument ) {
    // :REVISIT: change to DOMException
    throw new ParseError( "Cannot append child to this document" );
  }
  
  if ( this.childNodes.length == 1 ) {
    this.firstChild = childNode;
  }

  this.lastChild = childNode;
  childNode.parentNode = this;
  
  var prevSibling = this.childNodes.item( -2 );
  childNode.previousSibling = prevSibling;
  
  if ( prevSibling != null ) {
    prevSibling.nextSibling = childNode;
  }
}

Node.prototype.cloneNode =
function( deep ) {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}

Node.prototype.hasChildNodes =
function() {
  return ( this.childNodes.length > 0 );
}

Node.prototype.insertBefore =
function( newChild, refChild ) {
  var currentChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < currentChildren.length; ) {
    var child = currentChildren.item(i);
    if ( child == refChild && refChild != null ) {
      this.appendChild( newChild );
      refChild = null;
    }
    else {
      this.appendChild( child );
      i++;
    }
  }
}

Node.prototype.removeChild =
function( oldChild ) {
  var currentChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < currentChildren.length; i++ ) {
    var child = currentChildren.item(i)
    if ( child != oldChild ) {
      this.appendChild( child );
    }
  }
}

Node.prototype.replaceChild =
function( newChild, oldChild ) {
  var oldChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < oldChildren.length; i++ ) {
    if ( oldChildren.item(i) == oldChild ) {
      this.appendChild( newChild );
    }
    else {
      this.appendChild( oldChild );
    }
  }
}

/// Element ////////////////////////////////////////////////////////////////////

Element.prototype = new Node();
Element.prototype.constructor = Element;
  // Element : Node

function Element( ownerDoc, name ) {
  this.tagName = name;
  
  // inherited from Node
  this.attributes = new NamedNodeMap();
  this.childNodes = new NodeList();
  this.nodeType = Node.ELEMENT_NODE;
  this.nodeName = name;
  this.ownerDocument = ownerDoc;
}

Element.prototype.getAttribute =
function( name ) {
  var attr = this.attributes.getNamedItem( name );
  return ( attr == null ) ? "" : attr.getValue();
}

Element.prototype.getAttributeNode =
function( name ) {
  return this.attributes.getNamedItem( name );
}

Element.prototype.getElementsByTagName =
function( tagName ) {
  return new DeepNodeList( this, tagName );
}

Element.prototype.getTagName =
function() {
  return this.tagName;
}

Element.prototype.normalize =
function() {
  var child, next;
  for ( child = this.getFirstChild(); child != null; child = next ) {
    next = child.getNextSibling();
    if ( child.getNodeType() == Node.TEXT_NODE ) {
      if ( next != null && next.getNodeType() == Node.TEXT_NODE ) {
        child.appendData( next.getNodeValue() );
        this.removeChild( next );
        next = child;
      }
      else {
        if ( child.getNodeValue().length == 0 ) {
          this.removeChild( child );
        }
      }
    }
    else if ( child.getNodeType() == Node.ELEMENT_NODE ) {
      child.normalize();
    }
  }
}

Element.prototype.removeAttribute = 
function( name ) {
  this.attributes.removeNamedItem( name );
}

Element.prototype.removeAttributeNode =
function( attr ) {
  return this.attributes.removeNamedItem( attr.nodeName );
}

Element.prototype.setAttribute =
function( name, value ) {
  var attr = this.ownerDocument.createAttribute( name );
  arrt.setValue( value );
  this.attributes.setNamedItem( attr );
}

Element.prototype.setAttributeNode =
function( attr ) {
  return this.attributes.setNamedItem( attr );
}

/// Attr ///////////////////////////////////////////////////////////////////////

Attr.prototype = new Node();
Attr.prototype.constructor = Attr;
  // Attr : Node

function Attr( ownerDoc, name ) {
  this.name = name;
  this.specified = true;
  this.value = null;
  
  // inherited from Node
  this.childNodes = new NodeList();
  this.nodeName = name;
  this.nodeType = Node.ATTRIBUTE_NODE;
  this.nodeValue = null;
  this.ownerDocument = ownerDoc;
}

Attr.prototype.getName =
function() {
  return this.name;
}

Attr.prototype.getNodeValue =
function() {
  return this.getValue();
}

Attr.prototype.getSpecified =
function() {
  return this.specified;
}

Attr.prototype.getValue =
function() {
  // :REVISIT:
  var value = "";
  for ( var i = 0; i < this.childNodes.length; i++ ) {
    value += this.childNodes.item(i).getNodeValue();
  }
  return value;
}

Attr.prototype.setValue =
function( value ) {
  // :REVISIT:
  this.childNodes = new NodeList();
  this.firstChild = null;
  this.lastChild = null;
  
  if ( value != null ) {
    this.appendChild( this.ownerDocument.createTextNode( value ) );
  }
}

/// CharacterData //////////////////////////////////////////////////////////////

CharacterData.prototype = new Node();
CharacterData.prototype.constructor = CharacterData;
  // CharacterData : Node

function CharacterData( data ) {
  this.data = data;
  
  // inherited from Node
  this.nodeValue = data;
}

CharacterData.prototype.appendData =
function( data ) {
  this.setData( this.getData() + data );
}

CharacterData.prototype.deleteData =
function( offset, count ) {
  var begin = this.getData().substring( 0, offset );
  var end = this.getData().substring( offset + count );
  this.setData( begin + end );
}

CharacterData.prototype.getData =
function() {
  return this.data;
}

CharacterData.prototype.getLength =
function() {
  return ( this.data ) ? this.data.length : 0;
}

CharacterData.prototype.insetData =
function( offset, data ) {
  var begin = this.getData().substring( 0, offset );
  var end = this.getData().substring( offset, this.getLength );
  this.setData( begin + data + end );
}

CharacterData.prototype.replaceData =
function( offset, count, data ) {
  this.deleteData( offset, count );
  this.insertData( offset, data );
}

CharacterData.prototype.setData =
function( data ) {
  this.setNodeValue( data );
}

CharacterData.prototype.setNodeValue =
function( value ) {
  this.data = value;
  this.nodeValue = value;
}

CharacterData.prototype.substringData =
function( offset, count ) {
  return this.getData().substring( offset, offset + count );
}

/// Text ///////////////////////////////////////////////////////////////////////

Text.prototype = new CharacterData();
Text.prototype.constructor = Text;
  // Text : CharacterData

function Text( ownerDoc, data ) {
  // inherited from CharacterData
  this.data = data;
  
  // inherited from CharacterData : Node
  this.nodeName = "#text";
  this.nodeType = Node.TEXT_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

Text.prototype.splitText =
function( offset ) {
  // :REVISIT:
  // check for index out of bounds condition
  
  var newText =
    this.getOwnerDocument().createTextNode( this.data.substring( offset ) );
  
  var parentNode = this.getParentNode();
  if ( parentNode != null ) {
    parentNode.insetBefore( newText, this.nextSibling );
  }
  
  return newText;
}

/// CDATASection ///////////////////////////////////////////////////////////////

CDATASection.prototype = new Text();
CDATASection.prototype.constructor = CDATASection;
  // CDATASection : Text

function CDATASection( ownerDoc, data ) {
  
  // inherited from Text : CharacterData
  this.data = data;
  
  // inherited from Text : CharacterData : Node
  this.nodeName = "#cdata-section";
  this.nodeType = Node.CDATA_SECTION_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

/// EntityReference ////////////////////////////////////////////////////////////

EntityReference.prototype = new Node();
EntityReference.prototype.constructor = EntityReference;
  // EntityReference : Node

function EntityReference( ownerDoc, name ) {
  
  // inherited from Node
  this.nodeName = name;
  this.nodeType = Node.ENTITY_REFERENCE_NODE;
  this.ownerDocument = ownerDoc;
}

/// ProcessingInstruction //////////////////////////////////////////////////////

ProcessingInstruction.prototype = new Node();
ProcessingInstruction.prototype.constructor = ProcessingInstruction;
  // ProcessingInstruction : Node

function ProcessingInstruction( ownerDoc, target, data ) {
  this.target = target;
  this.data = data;
  
  // inherited from Node
  this.nodeName = target;
  this.nodeType = Node.PROCESSING_INSTRUCTION_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

ProcessingInstruction.prototype.getData =
function() {
  return this.data;
}

ProcessingInstruction.prototype.getTarget =
function() {
  return this.target;
}

ProcessingInstruction.prototype.setData =
function( data ) {
  this.setNodeValue( data );
}

ProcessingInstruction.prototype.setNodeValue =
function( value ) {
  this.data = data;
  this.nodeValue = data;
}

/// Comment ////////////////////////////////////////////////////////////////////

Comment.prototype = new CharacterData();
Comment.prototype.constructor = Comment;
  // Comment : CharacterData

function Comment( ownerDoc, data ) {
  // inherited from CharacterData
  this.data = data;
  
  // inherited from CharacterData : Node
  this.nodeName = "#comment";
  this.nodeType = Node.COMMENT_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

/// Document ///////////////////////////////////////////////////////////////////

Document.prototype = new Node();
Document.prototype.constructor = Document;
  // Document : Node

function Document() {
  this.doctype = null;
  this.implementation = null;
  this.documentElement = null;

  // inherited from Node
  this.childNodes = new NodeList();
  this.nodeName = "#document";
  this.nodeType = Node.DOCUMENT_NODE;
  this.ownerDocument = this;
}

Document.prototype.createAttribute =
function( name, value ) {
  return new Attr( this, name, value );
}

Document.prototype.createCDATASection =
function( data ) {
  return new CDATASection( this, data );
}

Document.prototype.createComment =
function( data ) {
  return new Comment( this, data );
}

Document.prototype.createDocumentFragment =
function() {
  return new DocumentFragment( this );
}

Document.prototype.createElement =
function( tagName ) {
  return new Element( this, tagName );
}

Document.prototype.createEntityReference =
function( name ) {
  return new EntityReference( this, name );
}

Document.prototype.createProcessingInstruction =
function( target, data ) {
  return new ProcessingInstruction( this, target, data );
}

Document.prototype.createTextNode =
function( data ) {
  return new Text( this, data );
}

Document.prototype.getDoctype =
function() {
  return this.doctype;
}

Document.prototype.getDocumentElement =
function() {
  return this.documentElement;
}

Document.prototype.getElementsByTagName =
function( tagName ) {
  return new DeepNodeList( this, tagName );
}

Document.prototype.getImplementation =
function() {
  // :REVISIT:
  return this.implementation;
}

/// DocumentType ///////////////////////////////////////////////////////////////

DocumentType.prototype = new Node();
DocumentType.prototype.constructor = DocumentType;
  // DocumentType : Node

function DocumentType( ownderDoc, name ) {
  this.name = name;
  this.entities = null;
  this.notations = null;
  
  // inherited from Node
  this.nodeName = name;
  this.nodeType = Node.DOCUMENT_TYPE_NODE;
  this.ownerDocument = ownderDoc;
}

DocumentType.prototype.getEntities = 
function() {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}

DocumentType.prototype.getName = 
function() {
  return this.name;
}

DocumentType.prototype.getNotations = 
function() {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}

/// DocumentFragment ///////////////////////////////////////////////////////////

DocumentFragment.prototype = new Node();
DocumentFragment.prototype.constructor = DocumentFragment;
  // DocumentFragment : Node

function DocumentFragment( ownerDoc ) {
  
  // inherited from Node
  this.childNodes = new NodeList();
  this.nodeName = "#document-fragment";
  this.nodeType = Node.DOCUMENT_FRAGMENT_NODE;
  this.ownerDocument = ownerDoc;
}

/// NodeList ///////////////////////////////////////////////////////////////////

function NodeList() {
  this.length = 0;
}

NodeList.prototype.getLength =
function() {
  return this.length;
}

NodeList.prototype.item =
function( index ) {
  var item;
  item = ( index < 0 ) ? this[ this.length + index ]
                       : this[ index ];
  return ( item || null );
}

NodeList.prototype.add =
function( node ) {
  this[ this.length++ ] = node;
}

/// DeepNodeList ///////////////////////////////////////////////////////////////

DeepNodeList.prototype = new NodeList();
DeepNodeList.prototype.constructor = DeepNodeList;
  // DeepNodeList : NodeList

function DeepNodeList( rootNode, tagName ) {
  this.rootNode = rootNode;
  this.tagName = tagName;
  this.getElementsByTagName( rootNode );
}

DeepNodeList.prototype.getElementsByTagName =
function( contextNode ) {
  var nextNode;
  while ( contextNode != null ) {
    if ( contextNode.hasChildNodes() ) {
      contextNode = contextNode.firstChild;
    }
    else if ( contextNode != this.rootNode &&
              null != ( next = contextNode.nextSibling ) ) {
      contextNode = next;
    }
    else {
      next = null;
      for ( ; contextNode != this.rootNode;
           contextNode = contextNode.parentNode ) {
        next = contextNode.nextSibling;
        if ( next != null ) {
          break;
        }
      }
      contextNode = next;
    }
    if ( contextNode != this.rootNode &&
         contextNode != null &&
         contextNode.nodeType == Node.ELEMENT_NODE ) {
      if ( this.tagName == "*" || contextNode.tagName == this.tagName ) {
        this.add( contextNode );
      }
    }
  }
  return null;
}

/// NamedNodeMap ///////////////////////////////////////////////////////////////

function NamedNodeMap() {
  this.length = 0;
}

NamedNodeMap.prototype.getLength =
function() {
  return this.length;
}

NamedNodeMap.prototype.getNamedItem =
function( name ) {
  return ( this[ name ] || null );
}

NamedNodeMap.prototype.item =
function( index ) {
  var item;
  item = ( index < 0 ) ? this[ this.length + index ]
                       : this[ index ];
  return ( item || null );
}

NamedNodeMap.prototype.removeNamedItem =
function( name ) {
  var removed = this[ name ];

  if ( !removed ) {
    return null;
  }

  delete this[ name ];
  for ( var i = 0; i < this.length - 1; i++ ) {
    if ( !this[i] ) {
      this[i] = this[ i + 1 ];
      delete this[ i + 1 ];
    }
  }
  this.length--;
  return removed;
}

NamedNodeMap.prototype.setNamedItem =
function( node ) {
  var nodeName = node.getNodeName();
  var item = this.getNamedItem( nodeName );
  this[ nodeName ] = node;
  
  if ( item == null ) {
    this[ this.length++ ] = node;
  }
  
  return item;
}

/// ParseError /////////////////////////////////////////////////////////////////

ParseError.prototype = new Error();
ParseError.prototype.constructor = ParseError;
  // ParseError : Error

function ParseError( message ) {
  this.message = message;
}

/// DOMException ///////////////////////////////////////////////////////////////

// :REVISIT:
// not currently used

DOMException.prototype = new Error();
DOMException.prototype.constructor = DOMException;
  // DOMException : Error

DOMException.INDEX_SIZE_ERR              = 1;
DOMException.DOMSTRING_SIZE_ERR          = 2;
DOMException.HIERARCHY_REQUEST_ERR       = 3;
DOMException.WRONG_DOCUMENT_ERR          = 4;
DOMException.INVALID_CHARACTER_ERR       = 5;
DOMException.NO_DATA_ALLOWED_ERR         = 6;
DOMException.NO_MODIFICATION_ALLOWED_ERR = 7;
DOMException.NOT_FOUND_ERR               = 8;
DOMException.NOT_SUPPORTED_ERR           = 9;
DOMException.INUSE_ATTRIBUTE_ERR         = 10;

function DOMException( code, message ) {
  this.code = code;
  this.message = message;
}

/// XMLParser //////////////////////////////////////////////////////////////////

XMLParser.VERSION = 1.0;

function XMLParser() {
  this.doc = new Document();
  this.xml = null;

  this.openedTags = new Array();
  this.contextNodes = new Array( this.doc );
}

XMLParser.prototype.parse =
function( xml ) {
  this.xml = xml.trim();
  this.processProlog();
  this.processRootElement();
  this.processMisc();

  if ( this.xml.length != 0 ) {
    throw( new ParseError( "Illegal construct in XML document" ) );
  }
}

XMLParser.prototype.processProlog =
function() {
  this.processXmlDecl();
  this.processMisc();
  this.processDoctypeDecl();
  this.processMisc();
}

XMLParser.prototype.processRootElement =
function() {
  var matches;
  
  if ( matches = this.xml.match( RegExp.$STag ) ) {
    this.processSTag( matches );
    this.xml = this.xml.substring( matches[0].length );
    this.processContent();
  }
  else if ( matches = this.xml.match( RegExp.$EmptyElemTag ) ) {
    this.processEmptyElemTag( matches );
    this.xml = this.xml.substring( matches[0].length );
  }
  else {
    throw( new ParseError( "Root element not found" ) );
  }
}

XMLParser.prototype.processMisc =
function() {
  var matches;
  
  while ( RegExp.$Misc.test( this.xml ) ) {
    
      // white space
    if ( matches = this.xml.match( RegExp.$S ) ) {
      ;
    }
      // comment
    else if ( matches = this.xml.match( RegExp.$Comment ) ) {
      var comment = this.doc.createComment( matches[1] );
      
      this.doc.appendChild( comment );
    }
      // processing instruction
    else if ( matches = this.xml.match( RegExp.$PI ) ) {
      var target = matches[1];
      var data = matches[3];
      var pi = this.doc.createProcessingInstruction( target, data );
      
      this.doc.appendChild( pi );
    }
      // unknown construct
    else {
      throw( new ParseError( "Illegal construct in XML document" ) );
    }
    
    this.xml = this.xml.substring( matches[0].length );
  }
}

XMLParser.prototype.processContent =
function() {
  var matches;

  while ( this.openedTags.length != 0  ) {

      // start tag
    if ( matches = this.xml.match( RegExp.$STag ) ) {
      this.processSTag( matches );
    }
      // end tag
    else if ( matches = this.xml.match( RegExp.$ETag ) ) {
      this.processETag( matches );
    }
      // empty element
    else if ( matches = this.xml.match( RegExp.$EmptyElemTag ) ) {
      this.processEmptyElemTag( matches );
    }
      // character data
    else if ( matches = this.xml.match( RegExp.$CharData ) ) {
      this.processCharData( matches );
    }
      // entity reference
    else if ( matches = this.xml.match( RegExp.$Reference ) ) {
      this.processReference( matches );
    }
      // CDATA section
    else if ( matches = this.xml.match( RegExp.$CDSect ) ) {
      this.processCDSect( matches );
    }
      // processing instruction
    else if ( matches = this.xml.match( RegExp.$PI ) ) {
      this.processPI( matches );
    }
      // comment
    else if ( matches = this.xml.match( RegExp.$Comment ) ) {
      this.processComment( matches );
    }
      // unknown construct
    else {
      throw( new ParseError( "Illegal construct in XML document" ) );
    }

    this.xml = this.xml.substring( matches[0].length );
  }
}

XMLParser.prototype.processAttributes =
function( attString ) {
  var matches;

  while ( matches = attString.match( RegExp.$Attribute ) ) {
    var name = matches[1];
    var value = matches[2].removeQuotes();
    var attr = this.doc.createAttribute( name );
    var currentContext = this.contextNodes.lastItem();

    attr.setValue( value );
    
    attString = attString.substring( matches[0].length ).trim();
    currentContext.attributes.setNamedItem( attr );
  }
}

XMLParser.prototype.processXmlDecl =
function() {
  var matches;

  if ( matches = this.xml.match( RegExp.$XMLDecl ) ) {
    this.xml = this.xml.substring( matches[0].length );
  }
}

XMLParser.prototype.processDoctypeDecl =
function() {
  var matches;
  
  if ( matches = this.xml.match( RegExp.$doctypedecl ) ) {
    var name = matches[1];
    
    var doctype = new DocumentType( this.doc, name );
    this.doc.appendChild( doctype );
    this.doc.doctype = doctype;
    
    this.xml = this.xml.substring( matches[0].length );
  }
}

XMLParser.prototype.processSTag =
function( matches ) {
  var element = this.doc.createElement( matches[1] );
  var attString = matches[2];
  
  this.openedTags.push( matches[1] );
  this.addNode( element );
  
  if ( attString ) {
    this.processAttributes( attString.trim() );
  }
}

XMLParser.prototype.processETag =
function( matches ) {
  this.contextNodes.pop();
  
  if ( this.openedTags.pop() != matches[1] ) {
    throw( new ParseError( "End tag does not match opening tag" ) );
  }
}

XMLParser.prototype.processEmptyElemTag =
function( matches ) {
  var element = this.doc.createElement( matches[1] );
  var attString = matches[2];
  
  this.addNode( element );
  
  if ( attString ) {
    this.processAttributes( attString.trim() );
  }
  
  this.contextNodes.pop();
}

XMLParser.prototype.processCharData =
function( matches ) {
  if ( matches[0].trim() != "" ) {
    var text = this.doc.createTextNode( matches[0] );
    this.addNode( text );
  }
}

XMLParser.prototype.processReference =
function( matches ) {
  var reference = this.doc.createEntityReference( matches[0] );
  this.addNode( reference );
}

XMLParser.prototype.processCDSect =
function( matches ) {
  var cdsect = this.doc.createCDATASection( matches[1] );
  this.addNode( cdsect );
}

XMLParser.prototype.processPI =
function( matches ) {
  var target = matches[1];
  var data = matches[3];
  var pi = this.doc.createProcessingInstruction( target, data );
  this.addNode( pi );
}

XMLParser.prototype.processComment =
function( matches ) {
  var comment = this.doc.createComment( matches[1] );
  this.addNode( comment );
}

XMLParser.prototype.addNode =
function( node ) {
  var currentContext = this.contextNodes.lastItem();
  currentContext.appendChild( node );
  
  if ( node.nodeType == Node.ELEMENT_NODE ) {
    var contextNode = currentContext.childNodes.item( -1 );
    this.contextNodes.push( contextNode );
    
    if ( !this.doc.documentElement ) {
      this.doc.documentElement = this.contextNodes.lastItem();
    }
  }
}

/// Utilities //////////////////////////////////////////////////////////////////

/*
 * Utility. Removes leading and trailing white space
 * from a string
 */
String.prototype.trim =
function() {
  return this.replace( /^[ \n\r\t]+|[ \n\r\t]+$/g, "" );
}

/*
 * Utility. Like pop(), but does not affect the length
 * of the array
 */
Array.prototype.lastItem =
function() {
  return this[ this.length - 1 ];
}

String.prototype.removeQuotes =
function() {
  return this.replace( /^['"]|['"]$/g, "" );
}

/*
 * Utility. Translates a string into a regex. Replaces identifiers
 * (beginning with "$") with corresponding regex fragment
 */
String.prototype.resolve =
function() {
  var resolved = this;
  var regex = /(\$[a-zA-Z0-9]+)/;
  
  while ( regex.test( resolved ) ) {
    resolved = resolved.replace( RegExp.$1, String[ RegExp.$1 ] );
  }
  
  return resolved.replace( / /g,"" );
}

/// Productions ////////////////////////////////////////////////////////////////

// Character Range
String.$Char          = "(?: \\u0009 | \\u000A | \\u000D | " +
                        "[\\u0020-\\uD7FF] | [\\uE000-\\uFFFD] | " +
                        "[\\u10000-\\u10FFFF] )";

// White Space
String.$S             = "(?: (?: \\u0020 | \\u0009 | \\u000D | \\u000A )+ )";

// Names and matches
String.$NameChar      = "(?: $Letter | $Digit | \\. | \\- | _ | : | " +
                        "$CombiningChar | $Extender )";
String.$Name          = "(?: $Letter | _ | : ) $NameChar*";

// Literals
String.$AttValue      = "(?: \" (?: [^<&\"] | $Reference )* \" ) | " + 
                        "(?: ' (?: [^<&'] | $Reference )* ' )";
String.$SystemLiteral = "(?: (?: \" [^\"]* \") | (?: ' [^']* '))";
String.$PubidLiteral  = "(?: (?: \" $PubidChar* \") | " +
                        "(?: ' (?: (?!')$PubidChar)* '))";
String.$PubidChar     = "(?: \\u0020 | \\u000D | \\u000A | [a-zA-Z0-9] | " +
                        "[-'()+,./:=?;!*#@$_%])";

// Character Data
//String.$CharData    = "(?![^<&]*]]>[^<&]*)[^<&]*"; // :REVISIT:
String.$CharData      = "[^<&]+";

// Comments
String.$Comment       = "<!-- ( (?: (?: (?!- ) $Char ) | " +
                        "(?: - (?: (?!- ) $Char ) ) )* ) -->";

// Processing Instructions
String.$PI            = "<\\? ( $PITarget ) ( $S $Char*? )? \\?>";
String.$PITarget      = "(?: (?: \\b( $Letter | _ | : ) " +
                        "(?: $NameChar ){0,1}\\b ) | " +
                        "(?: (?! [Xx][Mm][Ll] ) (?: $Letter | _ | : ) " +
                        "(?: $NameChar ){2} | (?: $Letter | _ | : ) " +
                        "(?: $NameChar ){3,} ) )";

// CDATA Sections
String.$CDSect        = "<!\\[CDATA\\[ ( $Char*? ) ]]>";

// Prolog
String.$prolog        = "(?: $XMLDecl? $Misc* (?: $doctypedecl $Misc* )? )";
String.$XMLDecl       = "<\\?xml $VersionInfo $EncodingDecl? " +
                        "$SDDecl? $S? \\?>";
String.$VersionInfo   = "(?: $S version $Eq ( ' $VersionNum ' | " +
                        "\" $VersionNum \" ) )";
String.$Eq            = "(?: $S? = $S? )";
String.$VersionNum    = "(?: (?: [a-zA-Z0-9_.:] | - )+ )";
String.$Misc          = "(?: $Comment | $PI | $S )";

// Document Type Definition
String.$doctypedecl   = "<!DOCTYPE $S ( $Name ) (?: $S $ExternalID)? $S? " +
                        "(?: \\[ [^]]* \\] )? $S? >";

// Standalone Document Declaration
String.$SDDecl        = "(?: $S standalone $Eq ( (?: \"(?: yes|no )\" ) | " +
                        "(?: '(?: yes|no)' ) ) )";

// Start-tag
String.$STag          = "< ( $Name ) ( (?: $S $Attribute )* ) $S? >";
String.$Attribute     = "( $Name ) $Eq ( $AttValue )";

// End-tag
String.$ETag          = "</ ( $Name ) $S? >";

// Tags for Empty Elements
String.$EmptyElemTag  = "< ( $Name ) ( (?: $S $Attribute )* ) $S? />";

// Character Reference
String.$CharRef       = "(?: &#[0-9]+; | &#x[0-9a-fA-F]+; )";

// Entity Reference
String.$Reference     = "(?: $EntityRef | $CharRef )";
String.$EntityRef     = "& $Name ;";

// External Entity Declaration
String.$ExternalID    = "(?: (?: SYSTEM $S ( $SystemLiteral ) ) | " +
                        "(?: PUBLIC $S ( $PubidLiteral ) $S " +
                        "( $SystemLiteral ) ) )";

// Encoding Declaration
String.$EncodingDecl  = "(?: $S encoding $Eq ( ' $EncName ' | " +
                        "\" $EncName \" ) )";
String.$EncName       = "[A-Za-z](?: [A-Za-z0-9._]|- )*";

// Characters
String.$Letter        = "(?: $BaseChar | $Ideographic )";
  if (Boolean.$I18N != true) { // if i18n.js is included
String.$BaseChar      = "(?: [\\u0041-\\u005A] | [\\u0061-\\u007A] )";
String.$Ideographic   = "\\u0000";
String.$CombiningChar = "\\u0000";
String.$Digit         = "[\\u0030-\\u0039]";
String.$Extender      = "\\u0000";
  }

// compile regular expressions
RegExp.$prolog       = new RegExp( "^" + String.$prolog.resolve() );
RegExp.$XMLDecl      = new RegExp( "^" + String.$XMLDecl.resolve() );
RegExp.$doctypedecl  = new RegExp( "^" + String.$doctypedecl.resolve() );
RegExp.$STag         = new RegExp( "^" + String.$STag.resolve() );
RegExp.$ETag         = new RegExp( "^" + String.$ETag.resolve() );
RegExp.$EmptyElemTag = new RegExp( "^" + String.$EmptyElemTag.resolve() );
RegExp.$Attribute    = new RegExp( "^" + String.$Attribute.resolve() );
RegExp.$CDSect       = new RegExp( "^" + String.$CDSect.resolve() );
RegExp.$CharData     = new RegExp( "^" + String.$CharData.resolve() );
RegExp.$Reference    = new RegExp( "^" + String.$Reference.resolve() );
RegExp.$PI           = new RegExp( "^" + String.$PI.resolve() );
RegExp.$Comment      = new RegExp( "^" + String.$Comment.resolve() );
RegExp.$Misc         = new RegExp( "^" + String.$Misc.resolve() );
RegExp.$S            = new RegExp( "^" + String.$S.resolve() );