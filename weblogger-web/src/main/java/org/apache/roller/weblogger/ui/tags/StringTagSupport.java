/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.tags;

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Abstract support class for the String Taglib.
 * It handles the JSP taglib side of things and calls abstract 
 * protected methods to delegate the String functionality.
 * <dl>
 * <dt>var</dt><dd>
 *             PageContext variable to put the 
 *             return result in instead of pushing
 *             out to the html page.
 * </dd>
 * </dl> 
 * 
 * @author bayard@generationjava.com
 */
abstract public class StringTagSupport extends BodyTagSupport {



    /**
     * PageContext attribute to store the result in.
     */
    private String var;

    /**
     * Empty constructor. Initialises the attributes.
     */
    public StringTagSupport() {
        initAttributes();
    }

    /**
     * Get the PageContext attribute to store the result in.
     */
    public String getVar() {
        return this.var;
    }

    /**
     * Set the PageContext attribute to store the result in.
     */
    public void setVar(String var) {
        this.var = var;
    }
       
    /**
     * Handles the manipulation of the String tag,
     * evaluating the body of the tag. The evaluation 
     * is delegated to the changeString(String) method 
     */
    public int doEndTag() throws JspException {

	/*
	 *  Although most of the tags that extends must have a body, some don't, like RandomStringTag
	 *  So I'm removing the code below...
     */
		 
//       if( (bodyContent == null) && (!canBeEmpty()) ) {
 //           return EVAL_PAGE;
 //      }
 
        String text = "";
        if(bodyContent != null) {
            StringWriter body = new StringWriter();
            try {
                bodyContent.writeOut(body);
                text = body.toString();
            } catch(IOException ioe) {
                ioe.printStackTrace();
            }
        }

        // first, try to evaluate the string and associated the result on var
        Object result = evaluateString( text );
        if ( result != null && this.var != null ) {
            pageContext.setAttribute(this.var, result);
        } else {
          // then, try to transform it
          text = changeString(text);
        
          // TODO: RandomString is not working if body is set...
          /*
            System.err.println("...."+text+"....");
            if ( text  != null  ) {
        	System.out.println( "length = " + text.length());
            }
          */
        
          if(this.var == null) {
            JspWriter writer = pageContext.getOut();
            try {
              writer.print(text);
            } catch (IOException e) {
              throw new JspException(e.toString());
            }
          } else {
            pageContext.setAttribute(this.var, text);
          }

        }

        return (EVAL_PAGE);
    }

    /** 
     * Perform an operation on the passed String.
     * The object returned by this operation (if not null) will be
     * associated to PageContext attribute represented by this.var.
     *
     * @param str String to be manipulated
     *
     * @return Object result of operation upon passed String
     */
  public Object evaluateString(String str) throws JspException {
    return null;
  }
  
    /** 
     * Perform a transformation on the passed in String.
     *
     * @param str String to be manipulated
     *
     * @return String result of operation upon passed in String
     */
    abstract public String changeString(String str) throws JspException;

    /**
     * Initialise any properties to default values.
     * This method is called upon construction, and 
     * after changeString(String) is called.
     * This is a default empty implementation.
     */
    public void initAttributes() {
        this.var = null;
    }

}
