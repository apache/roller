package org.apache.roller.weblogger.ui.core.util;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author Dave Johnson
 */
public class SplitPrintWriter extends PrintWriter 
{
    private PrintWriter captureWriter = null;
    private PrintWriter passThroughWriter = null;
    
    public SplitPrintWriter(PrintWriter captureWriter, PrintWriter passThroughWriter) 
    {
        super(passThroughWriter);
        this.captureWriter = captureWriter;
        this.passThroughWriter = passThroughWriter;
    }  
      
    public void write(char[] cbuf)
    {
        captureWriter.write(cbuf);
        passThroughWriter.write(cbuf);
    }
    public void write(char[] cbuf, int off, int len)
    {       
        captureWriter.write(cbuf,off,len);
        passThroughWriter.write(cbuf,off,len);
    }
    public void write(int c)
    {       
        captureWriter.write(c);
        passThroughWriter.write(c);
    }
    public void write(String str)
    {       
        captureWriter.write(str);
        passThroughWriter.write(str);
    }
    public void write(String str, int off, int len)
    {       
        captureWriter.write(str,off,len);
        passThroughWriter.write(str,off,len);
    }
    public void flush() 
    {
        captureWriter.flush();
        passThroughWriter.flush();
    }    
    public void close() 
    {
        captureWriter.close();
        passThroughWriter.close();
    }
}
