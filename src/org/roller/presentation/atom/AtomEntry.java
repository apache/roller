/*
 * Created on Apr 2, 2004
 */
package org.roller.presentation.atom;

import org.osjava.atom4j.Atom4J;
import org.osjava.atom4j.pojo.Entry;
import org.osjava.atom4j.pojo.Link;
import org.osjava.atom4j.pojo.Person;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lance.lavandowska
 */
public class AtomEntry extends Entry
{
    private String annotation = null;
    private List categories = null;
    
    /**
     * @return Returns the annotation.
     */
    public String getAnnotation()
    {
        return this.annotation;
    }
    
    /**
     * @param annotation The annotation to set.
     */
    public void setAnnotation(String annotation)
    {
        this.annotation = annotation;
    }
    
    /**
     * @return Returns the categories.
     */
    public List getCategories()
    {
        return this.categories;
    }
    
    /**
     * @param categories The categories to set.
     */
    public void setCategories(List categories)
    {
        this.categories = categories;
    }
    
    public void addCategory(String cat) 
    {
        if (categories == null) categories = new ArrayList();
        categories.add(cat);
    }

    /**
     * Pretty  output.
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer("<entry xmlns=\"" + Atom4J.xmlns + "\" >\n");

        if (getId() != null) buf.append("    ").append(Atom4J.simpleTag(getId(), "id"));
        if (getTitle() != null) buf.append("    ").append(getTitle().toString("title"));
        if (getSummary() != null) buf.append("    ").append(getSummary().toString("summary"));
        
        // generate Author and Contributor tags
        if (getAuthor() != null) 
        {
            buf.append("    ").append(getAuthor().toString("author"));
        }
        if (getContributors() != null)
        {
            java.util.Iterator it = getContributors().iterator();
            while (it.hasNext())
            {
                Person _person = (Person)it.next();
                buf.append("    ").append(_person.toString("contributor"));
            }
        }

        if (getLinks() != null)
        {
            java.util.Iterator it = getLinks().iterator();
            while (it.hasNext())
            {
                Link _link = (Link)it.next();
                buf.append("    ").append(_link.toString());
            }
        }
        
        // generate date tags
        if (getIssued() != null)
            buf.append("    ").append(Atom4J.dateTag(getIssued(), "issued"));
        if (getCreated() != null)
            buf.append("    ").append(Atom4J.dateTag(getCreated(), "created"));
        if (getModified() != null)
            buf.append("    ").append(Atom4J.dateTag(getModified(), "modified"));
        
        // generate Content
        buf.append("    ").append(getContent().toString("content"));

        // custom fields
        if (annotation != null) buf.append("    ").append(Atom4J.simpleTag(annotation, "annotation"));
        if (categories != null)
        {
            java.util.Iterator it = categories.iterator();
            while (it.hasNext())
            {
                buf.append("    ").append(Atom4J.simpleTag(it.next(), "subject"));
            }
        }
        
        // close entry
        buf.append("</entry>\n");
        
        return buf.toString();
    }
}
