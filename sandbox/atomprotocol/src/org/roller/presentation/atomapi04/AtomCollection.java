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
package org.roller.presentation.atomapi04;

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
 * appCollection = element
 *    app:collection { 
 *       attribute next { text } ?, 
 *       appMember* 
 *    }
 * 
 * Here is an example Atom collection:
 * 
 * <?xml version="1.0" encoding='utf-8'?> 
 * <collection xmlns="http://purl.org/atom/app#"> 
 * <member href="http://example.org/1"
 *    hrefreadonly="http://example.com/1/bar" 
 *    title="Sample 1"
 *    updated="2003-12-13T18:30:02Z" /> 
 * <member href="http://example.org/2"
 *    hrefreadonly="http://example.com/2/bar" 
 *    title="Sample 2"
 *    updated="2003-12-13T18:30:02Z" /> 
 * <member href="http://example.org/3"
 *    hrefreadonly="http://example.com/3/bar" 
 *    title="Sample 3"
 *    updated="2003-12-13T18:30:02Z" /> 
 * <member href="http://example.org/4"
 *    title="Sample 4" 
 *    updated="2003-12-13T18:30:02Z" /> 
 * </collection>
 */
public class AtomCollection
{
    public static final Namespace ns = 
        Namespace.getNamespace("http://purl.org/atom/app#");
    
    private static SimpleDateFormat df =
        new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
    private String next    = null;
    private List   members = new ArrayList();

    public AtomCollection()
    {
    }

    /** URI of collection containing member elements updated earlier in time */
    public String getNext()
    {
        return next;
    }

    public void setNext(String next)
    {
        this.next = next;
    }

    public List getMembers()
    {
        return members;
    }

    public void setMembers(List members)
    {
        this.members = members;
    }

    public void addMember(Member member)
    {
        members.add(member);
    }

    /** Models an Atom collection member */
    /*
     * appMember = element app:member { attribute title { text }, attribute href {
     * text }, attribute hrefreadonly { text } ?, attribute updated { text } }
     */
    public static class Member
    {
        private String title;
        private String href;
        private String hrefreadonly;
        private Date   updated;

        public Member()
        {
        }

        /** Human readable title */
        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        /** The URI used to edit the member source */
        public String getHref()
        {
            return href;
        }

        public void setHref(String href)
        {
            this.href = href;
        }

        /** The URI for readonly access to member source */
        public String getHrefreadonly()
        {
            return hrefreadonly;
        }

        public void setHrefreadonly(String hrefreadonly)
        {
            this.hrefreadonly = hrefreadonly;
        }

        /** Same as updated value of collection member */
        public Date getUpdated()
        {
            return updated;
        }

        public void setUpdated(Date updated)
        {
            this.updated = updated;
        }
    }

    /** Deserialize an Atom Collection XML document into an object */
    public static AtomCollection documentToCollection(Document document)
            throws Exception
    {
        AtomCollection collection = new AtomCollection();
        Element root = document.getRootElement();
        if (root.getAttribute("next") != null)
        {
            collection.setNext(root.getAttribute("next").getValue());
        }
        List mems = root.getChildren("member", ns);
        Iterator iter = mems.iterator();
        while (iter.hasNext())
        {
            Element e = (Element) iter.next();
            collection.addMember(AtomCollection.elementToMember(e));
        }
        return collection;
    }

    /** Serialize an AtomCollection object into an XML document */
    public static Document collectionToDocument(AtomCollection collection)
    {
        Document doc = new Document();
        Element root = new Element("collection", ns);
        doc.setRootElement(root);
        if (collection.getNext() != null)
        {
            root.setAttribute("next", collection.getNext());
        }
        Iterator iter = collection.getMembers().iterator();
        while (iter.hasNext())
        {
            Member member = (Member) iter.next();
            root.addContent(AtomCollection.memberToElement(member));
        }
        return doc;
    }

    /** Deserialize an Atom collection member XML element into an object */
    public static Member elementToMember(Element element) throws Exception
    {
        Member member = new Member();
        member.setTitle(element.getAttribute("title").getValue());
        member.setHref(element.getAttribute("href").getValue());
        if (element.getAttribute("href") != null)
        {
            member.setHref(element.getAttribute("href").getValue());
        }
        member.setUpdated(df.parse(element.getAttribute("updated").getValue()));
        return member;
    }

    /** Serialize a collection member into an XML element */
    public static Element memberToElement(Member member)
    {
        Element element = new Element("member", ns);
        element.setAttribute("title", member.getTitle()); // TODO: escape/strip HTML?
        element.setAttribute("href", member.getHref());
        if (member.getHrefreadonly() != null)
        {
            element.setAttribute("hrefreadonly", member.getHrefreadonly());
        }
        element.setAttribute("updated", df.format(member.getUpdated()));
        return element;
    }

    /** Start and end date range */
    public static class Range { Date start=null; Date end=null; }
    
    /** Parse HTTP Range header into a start and end date range */
    public static Range parseRange(String rangeString) throws ParseException 
    {
        // Range: updated=<isodate>/<isodate>   
        // Range: updated=<isodate>/ 
        // Range: updated=/<isodate>  

        Range range = new Range();
        String[] split = rangeString.split("=");
        if (split[1].startsWith("/")) 
        {
            // we have only end date
            range.end = df.parse(split[1].split("/")[1]);
        }
        else if (split[1].endsWith("/"))
        {
            // we have only start date
            range.start = df.parse(split[1].split("/")[0]);
        }
        else
        {
            // both dates present
            String[] dates = split[1].split("/");
            range.start = df.parse(dates[0]);
            range.end = df.parse(dates[1]);
        }
        return range;
    }
}
