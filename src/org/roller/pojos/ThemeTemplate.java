/*
 * ThemeTemplate.java
 *
 * Created on June 27, 2005, 12:14 PM
 */

package org.roller.pojos;

import java.io.Serializable;
import java.util.Date;
import org.roller.model.Template;


/**
 * A Theme based implementation of a Template.  A ThemeTemplate represents a
 * template which is part of a shared Theme.
 *
 * @author Allen Gilliland
 */
public class ThemeTemplate implements Template, Serializable {
    
    private String id;
    private String name;
    private String description;
    private String contents;
    private String link;
    private Date lastModified;
    
    
    public ThemeTemplate() {}
    
    public ThemeTemplate(String id, String name, 
                String desc, String contents, String link, Date date) {
        
        this.id = id;
        this.name = name;
        this.description = desc;
        this.contents = contents;
        this.link = link;
        this.lastModified = date;
    }
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
    
    public String toString() {
        return (id + "," + name + "," + description + "," + link + "," + 
                lastModified + "\n\n" + contents + "\n");
    }
    
}
