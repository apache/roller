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
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/*
 * @author llavandowska
 *
 * Implementation of ServletOutputStream that allows the filter to hold the
 * Response content for insertion into the cache.
 */
public class ByteArrayOutputStreamWrapper extends ServletOutputStream
{
    protected OutputStream intStream;
    protected ByteArrayOutputStream baStream;
    protected boolean finallized = false;
    protected boolean flushOnFinalizeOnly = true;

    public ByteArrayOutputStreamWrapper(OutputStream outStream)
    {
        intStream = outStream;
        baStream = new ByteArrayOutputStream();
    }

    public ByteArrayOutputStreamWrapper()
    {
        intStream = System.out;
        baStream = new ByteArrayOutputStream();
    }

    public ByteArrayOutputStream getByteArrayStream()
    {
        return baStream;
    }

    public void setFinallized()
    {
        finallized = true;
    }

    public boolean isFinallized()
    {
        return finallized;
    }


    public void write(int i) throws java.io.IOException
    {
        baStream.write(i);
    }

    public void close() throws java.io.IOException
    {
        if (finallized) {
            processStream();
            intStream.close();
        }
    }

    public void flush() throws java.io.IOException
    {
        if (baStream.size() != 0) {
            if (!flushOnFinalizeOnly || finallized) {
                processStream();
                baStream = new ByteArrayOutputStream();
            }
        }
    }

    protected void processStream() throws java.io.IOException
    {
        intStream.write(baStream.toByteArray());
        intStream.flush();
    }
    
    public void clear()
    {
        baStream = new ByteArrayOutputStream();
    }
}
