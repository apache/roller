/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.roller.presentation.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.taglib.logic.IterateTag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;


/**
 * <p>This tag generates table rows (i.e. &lt;tr&gt;....&lt;/tr&gt; elements) 
 * with the background color set differently for alternating odd and even 
 * rows. This tag only operates properly if embedded in an IterateTag.</p>
 *
 * <p>The following parameters can be specified for this Tag:</p>
 * <ul>
 * <li><code>oddColor </code> - The color for Odd numbered rows
 * <li><code>evenColor</code> - The color for Even numbered rows
 * <li><code>oddStyleClass</code> - The style class for Odd numbered rows
 * <li><code>evenStyleClass</code> - The style class for Even numbered rows
 * <li><code>align</code> - The alignment for the table row
 * <li><code>valign</code> - The vertical alignment for the table row
 * </ul>
 *
 * <p>Additionally this tag inherits the Event Handler and Style attributes
 * from the BaseHandlerTag which can also be specified</p>
 * 
 * TODO Make RowTag work with STL's forEach tag too.
 * 
 * @jsp.tag name="row"
 *
 * @author Amarda Business Systems Ltd
 * @version 1.0
 */
public class RowTag extends org.apache.struts.taglib.html.BaseHandlerTag
{
    // ----------------------------------------------------- Instance Variables
    private static Log mLogger = LogFactory.getFactory()
                                           .getInstance(RowTag.class);
    protected final static String QUOTE = "\"";

    /**
     * Color of Odd rows in a table
     */
    protected String oddColor = null;

    /**
     *  Color of Even rows in a table
     */
    protected String evenColor = null;

    /**
     *  StyleClass of Odd rows in a table
     */
    protected String oddStyleClass = null;

    /**
     *  Style Class of Even rows in a table
     */
    protected String evenStyleClass = null;

    /**
     *  Alignment of the table row
     */
    protected String align = null;

    /**
     *  Vertical Alignment of the table row
     */
    protected String valign = null;

    /**
     * Return the color of Odd rows
     * @jsp.attribute
     */
    public String getOddColor()
    {
        return (this.oddColor);
    }

    /**
     * Set the color of Odd rows
     *
     * @param color HTML bgcolor value for Odd rows
     */
    public void setOddColor(String color)
    {
        this.oddColor = color;
    }

    /**
     *  Return the color of Even rows
     * @jsp.attribute
     */
    public String getEvenColor()
    {
        return (this.evenColor);
    }

    /**
     * Set the color of Even rows
     *
     * @param color HTML bgcolor value for Even rows
     */
    public void setEvenColor(String color)
    {
        this.evenColor = color;
    }

    /**
     * Return the Style Class of Odd rows
     * @jsp.attribute
     */
    public String getOddStyleClass()
    {
        return (this.oddStyleClass);
    }

    /**
     * Set the Style Class of Odd rows
     *
     * @param styleClass HTML Style Class value for Odd rows
     */
    public void setOddStyleClass(String styleClass)
    {
        this.oddStyleClass = styleClass;
    }

    /**
     *  Return the Style Class of Even rows
     * @jsp.attribute
     */
    public String getEvenStyleClass()
    {
        return (this.evenStyleClass);
    }

    /**
     * Set the styleClass of Even rows
     *
     * @param styleClass HTML Style Class value for Even rows
     */
    public void setEvenStyleClass(String styleClass)
    {
        this.evenStyleClass = styleClass;
    }

    /**
     *  Return the Alignment
     * @jsp.attribute
     */
    public String getAlign()
    {
        return (this.align);
    }

    /**
     * Set the Alignment
     *
     * @param Value for Alignment
     */
    public void setAlign(String align)
    {
        this.align = align;
    }

    /**
     *  Return the Vertical Alignment
     * @jsp.attribute
     */
    public String getValign()
    {
        return (this.valign);
    }

    /**
     * Set the Vertical Alignment
     *
     * @param Value for Vertical Alignment
     */
    public void setValign(String valign)
    {
        this.valign = valign;
    }

    // ----------------------------------------------------- Public Methods

    /**
     * Start of Tag processing
     *
     * @exception JspException if a JSP exception occurs
     */
    public int doStartTag() throws JspException
    {
        // Continue processing this page
        return EVAL_BODY_BUFFERED;
    }

    /**
     * End of Tag Processing
     *
     * @exception JspException if a JSP exception occurs
     */
    public int doEndTag() throws JspException
    {
        StringBuffer buffer = new StringBuffer();


        // Create a <tr> element based on the parameters
        buffer.append("<tr");


        // Prepare this HTML elements attributes
        prepareAttributes(buffer);

        buffer.append(">");

        // Add Body Content
        if (bodyContent != null)
        {
            buffer.append(bodyContent.getString().trim());
        }

        buffer.append("</tr>");

        // Render this element to our writer
        JspWriter writer = pageContext.getOut();

        try
        {
            writer.print(buffer.toString());
        }
        catch (IOException e)
        {
            mLogger.error("ERROR in tag", e);
            throw new JspException("Exception in RowTag doEndTag():" + 
                                   e.toString());
        }

        return EVAL_PAGE;
    }

    /**
     * Prepare the attributes of the HTML element
     */
    protected void prepareAttributes(StringBuffer buffer)
    {
        // Determine if it is an "Odd" or "Even" row
        boolean evenNumber = ((getRowNumber() % 2) == 0)                     
                             ? true : false;


        // Append bgcolor parameter
        buffer.append(prepareBgcolor(evenNumber));


        // Append CSS class parameter
        buffer.append(prepareClass(evenNumber));


        // Append "align" parameter
        buffer.append(prepareAttribute("align", align));


        // Append "valign" parameter
        buffer.append(prepareAttribute("valign", valign));


        // Append Event Handler details
        buffer.append(prepareEventHandlers());

        try
        {
            // Append Style details
            buffer.append(prepareStyles());
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception", e);
        }
    }

    /**
     * Format attribute="value" from the specified attribute & value
     */
    protected String prepareAttribute(String attribute, String value)
    {
        return (value == null)       
               ? "" : " " + attribute + "=" + QUOTE + value + QUOTE;
    }

    /**
     * Format the bgcolor attribute depending on whether
     * the row is odd or even.
     *
     * @param evenNumber Boolean set to true if an even numbered row
     *
     */
    protected String prepareBgcolor(boolean evenNumber)
    {
        if (evenNumber)
        {
            return prepareAttribute("bgcolor", evenColor);
        }
        else
        {
            return prepareAttribute("bgcolor", oddColor);
        }
    }

    /**
     * Format the Style sheet class attribute depending on whether
     * the row is odd or even.
     *
     * @param evenNumber Boolean set to true if an even numbered row
     *
     */
    protected String prepareClass(boolean evenNumber)
    {
        if (evenNumber)
        {
            return prepareAttribute("class", evenStyleClass);
        }
        else
        {
            return prepareAttribute("class", oddStyleClass);
        }
    }

    /**
     * Determine the Row Number - from the IterateTag
     */
    protected int getRowNumber()
    {
        // Determine if embedded in an IterateTag
        Tag tag = findAncestorWithClass(this, IterateTag.class);

        if (tag == null)
        {
            return 1;
        }

        // Determine the current row number
        IterateTag iterator = (IterateTag) tag;

        //        return iterator.getLengthCount() + 1;
        return iterator.getIndex() + 1;
    }

    /**
     * Release resources after Tag processing has finished.
     */
    public void release()
    {
        super.release();

        oddColor = null;
        evenColor = null;
        oddStyleClass = null;
        evenStyleClass = null;
        align = null;
        valign = null;
    }
}