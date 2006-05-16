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
package org.apache.roller.ui.core.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/*
 * @author llavandowska
 *
 * Implementation of HttpServletResponseWrapper.
 */
public class ByteArrayResponseWrapper extends HttpServletResponseWrapper
{
    private PrintWriter tpWriter;
    private ByteArrayOutputStreamWrapper tpStream;

    public ByteArrayResponseWrapper(ServletResponse inResp)
    throws java.io.IOException
    {
        super((HttpServletResponse) inResp);
        tpStream = new ByteArrayOutputStreamWrapper(inResp.getOutputStream());
        tpWriter = new PrintWriter(new OutputStreamWriter(tpStream,"UTF-8"));
    }

    public ServletOutputStream getOutputStream()
    throws java.io.IOException
    {
        return tpStream;
    }

    public PrintWriter getWriter() throws java.io.IOException
    {
        return tpWriter;
    }
     
    /** Get a String representation of the entire buffer.
     */    
    public String toString()
    {
        return tpStream.getByteArrayStream().toString();
    }
    
    public ByteArrayOutputStream getByteArrayOutputStream()
    throws java.io.IOException
    {
        return tpStream.getByteArrayStream();
    }
}
