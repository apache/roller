/*
 * Theme.java
 *
 * Created on June 27, 2005, 12:55 PM
 */

package org.roller.pojos;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * The Theme object encapsulates all elements of a single weblog theme.  It
 * is used mostly to contain all the templates for a theme, but does contain
 * other theme related attributes such as name, last modifed date, etc.
 *
 * @author Allen Gilliland
 */
public class Theme implements Serializable {
    
    // this is the name that will be used to identify a user customized theme
    public static final String CUSTOM = "custom";
    
    private String id;
    private String name;
    private String description;
    private String author;
    private String lastEditor; // user id value of last editor
    private Date lastModified;
    private boolean enabled;
    
    // we keep templates in a Map for faster lookups by name
    // the Map contains ... (template name, ThemeTemplate)
    private Map templates;
    
    
    public Theme() {
        this.id = null;
        this.name = null;
        this.description = null;
        this.author = null;
        this.lastEditor = null;
        this.lastModified = null;
        this.enabled = false;
        this.templates = new HashMap();
    }

    
    /**
     * Get the collection of all templates associated with this Theme.
     */
    public Collection getTemplates() {
        return this.templates.values();
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplate(String name) {
        return (ThemeTemplate) this.templates.get(name);
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     *
     * NOTE: for themes we enforce the rule that 
     *          Theme.link == Theme.name
     *
     * So this lookup is basically the same as lookup by name.
     */
    public ThemeTemplate getTemplateByLink(String link) {
        return (ThemeTemplate) this.templates.get(link);
    }
    
    
    /**
     * Set the value for a given template name.
     */
    public void setTemplate(String name, ThemeTemplate template) {
        this.templates.put(name, template);
    }
    
    
    /**
     * Check if this Theme contains the named template.
     * Returns true if the template exists, false otherwise.
     */
    public boolean hasTemplate(String name) {
        return this.templates.containsKey(name);
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLastEditor() {
        return lastEditor;
    }

    public void setLastEditor(String lastEditor) {
        this.lastEditor = lastEditor;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("\n");
        
        Iterator it = this.templates.values().iterator();
        while(it.hasNext()) {
            sb.append(it.next());
            sb.append("\n");
        }
        
        return sb.toString();
        
    }

}
