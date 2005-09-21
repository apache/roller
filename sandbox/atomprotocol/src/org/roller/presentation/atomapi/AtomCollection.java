/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roller.presentation.atomapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Models an Atom collection.
 * 
 * @author Dave Johnson
 */
/*
 * Based on: draft-ietf-atompub-protocol-04.txt 
 * 
 * appCollection =
 *    element app:collection {
 *       ( appMemberType* 
 *         appSearchTemplate
 *         & anyElement* )
 * }
 * 
 * Here is an example Atom collection:
 * 
 * <?xml version="1.0" encoding='utf-8'?> 
 * <collection xmlns="http://purl.org/atom/app">
 *    <member-type>text</member-type>
 *    <member-type>html</member-type>
 *    <member-type>xhtml</member-type>
 *    <member-type>src</member-type>
 *    <member-type>generic</member-type>
 *    <search-template>http://example.org/{index}</search-template>
 *    <search-template>http://example.org/d/{daterange}</search-template>
 * </collection>
 */
public class AtomCollection
{
    public static final Namespace ns = 
        Namespace.getNamespace("http://purl.org/atom/app#");
    
    private List memberTypes = new ArrayList(); // array of strings
    private String dateRangeTemplate = null;
    private String indexTemplate = null;

    public AtomCollection()
    {
    }
    
    public List getMemberTypes()
    {
        return memberTypes;
    }

    public void setMemberTypes(List memberTypes)
    {
        this.memberTypes = memberTypes;
    }

    public void addMemberType(String memberType)
    {
        memberTypes.add(memberType);
    }

    /** Deserialize an Atom Collection XML document into an object */
    public static AtomCollection documentToCollection(Document document)
            throws Exception
    {
        AtomCollection collection = new AtomCollection();
        Element root = document.getRootElement();
//        if (root.getAttribute("next") != null)
//        {
//            collection.setNext(root.getAttribute("next").getValue());
//        }
//        List mems = root.getChildren("member", ns);
//        Iterator iter = mems.iterator();
//        while (iter.hasNext())
//        {
//            Element e = (Element) iter.next();
//            collection.addMember(AtomCollection.elementToMember(e));
//        }
        return collection;
    }

    /** Serialize an AtomCollection object into an XML document */
    public static Document collectionToDocument(AtomCollection collection)
    {
        Document doc = new Document();
//        Element root = new Element("collection", ns);
//        doc.setRootElement(root);
//        if (collection.getNext() != null)
//        {
//            root.setAttribute("next", collection.getNext());
//        }
//        Iterator iter = collection.getMembers().iterator();
//        while (iter.hasNext())
//        {
//            Member member = (Member) iter.next();
//            root.addContent(AtomCollection.memberToElement(member));
//        }
        return doc;
    }

}
