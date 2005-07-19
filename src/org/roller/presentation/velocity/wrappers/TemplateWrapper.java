/*
 * TemplateWrapper.java
 *
 * Created on July 2, 2005, 4:02 PM
 */

package org.roller.presentation.velocity.wrappers;

import java.util.Date;
import org.roller.pojos.Template;


/**
 * Wrapper class for org.roller.model.Template objects.
 *
 * @author Allen Gilliland
 */
public class TemplateWrapper {
    
    private Template template = null;
    
    
    public TemplateWrapper(Template template) {
        this.template = template;
    }
    
    
    public String getId() {
        return this.template.getId();
    }

    public String getName() {
        return this.template.getName();
    }

    public String getDescription() {
        return this.template.getDescription();
    }

    public String getContents() {
        return this.template.getContents();
    }

    public Date getLastModified() {
        return this.template.getLastModified();
    }

    public String getLink() {
        return this.template.getLink();
    }

}
