package org.roller.presentation.tags; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author David M Johnson, Gregory Gerard
 * @version 1.0
 */
public abstract class VelocityTag extends HybridTag // TagSupport 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(VelocityTag.class);

    private String              title;
    private String              subtitle;
    private List                images;
    private Map                 ingredients;


	/** Return path to Velocity template to render this tag */
	public abstract String getTemplateClasspath();

	/** Prepare context for execution */
	public abstract void prepareContext( VelocityContext ctx );


    public VelocityTag()
    {
    }

    /**
     * Release any resources we might have acquired.
     */
    public void release()
    {
        super.release();
        title       = null;
        subtitle    = null;
        images      = null;
        ingredients = null;
    }

    /**
     * Evaluate any tags inside us.  This will also allow us to have child tags
     * send us messages.
     * @return
     * @throws JspException
     */
    public int doStartTag(java.io.PrintWriter pw)
        throws JspException
    {
        return TagSupport.EVAL_BODY_INCLUDE;
    }

    /**
     * Check all all the public properties that must be set by now, either by
     * tag arguments or tag children.
     * @return
     * @throws JspException
     */
    public int doEndTag(java.io.PrintWriter pw) throws JspException
    {
        String myResource= getVelocityClasspathResource(getTemplateClasspath());

        try
        {
            VelocityContext myVelocityContext = getVelocityContext();

			// ask concrete class to prepare context 
			prepareContext( myVelocityContext );

            StringWriter myStringWriter = new StringWriter();

            Velocity.mergeTemplate(myResource,
            	org.apache.velocity.runtime.RuntimeSingleton.getString(
					Velocity.INPUT_ENCODING, Velocity.ENCODING_DEFAULT),
                 myVelocityContext, myStringWriter);

            pw.println(myStringWriter);

            return EVAL_PAGE;
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception",e);

            throw new JspException(
				"Exception; TEMPLATE_CLASSPATH=" + getTemplateClasspath() 
				+ "; exception=" + e.getMessage());
        }
    }

    public String doStandaloneTest(Map inMap)
        throws Exception
    {
        String          resource = getVelocityClasspathResource(
										getTemplateClasspath());
        VelocityContext context  = getVelocityContext();
        StringWriter    w        = new StringWriter();
        Iterator        iter     = inMap.keySet().iterator();

        while (iter.hasNext())
        {
            Object o = iter.next();

            context.put(o.toString(), inMap.get(o));
        }

        Velocity.mergeTemplate(resource, 
			org.apache.velocity.runtime.RuntimeSingleton.getString(
				Velocity.INPUT_ENCODING, Velocity.ENCODING_DEFAULT),
            context, w);

        return w.toString();
    }

    protected static VelocityContext getVelocityContext()
        throws java.lang.Exception
    {
        Velocity.addProperty(Velocity.RESOURCE_LOADER, "classpath");
        Velocity.addProperty("classpath." + Velocity.RESOURCE_LOADER + ".class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.setProperty(
			VelocityEngine.COUNTER_INITIAL_VALUE, new Integer(0));
        Velocity.init();

        return new VelocityContext();
    }

    /**
     * Velocity wants classpath resources to not have a leading slash.  This is
     * contrary to how one would normally load a resource off the classpath
     * (e.g. SampleTag.class.getResourceAsStream("/extag/resources/test.html")
     * @param inClasspathString
     * @return
     */
    protected static String getVelocityClasspathResource(
		String inClasspathString)
    {
        if (inClasspathString.length() < 1)
        {
            return inClasspathString;
        }
        else
        {
            if (inClasspathString.startsWith("/"))
            {
                return inClasspathString.substring(1);
            }
            else
            {
                return inClasspathString;
            }
        }
    }
}
