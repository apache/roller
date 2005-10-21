package org.roller.presentation.filters;

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
