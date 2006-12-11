/*
 * Copyright 2005 David M Johnson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Simple Technorati wrapper for Java. @author David M Johnson */
public class Technorati {
    private String mKey = null;

    public Technorati(String key) {
        mKey = key;
    }
    
    public Technorati(String key, String proxy, int proxyPort) {
        this(key);
        System.setProperty("proxySet", "true");
        System.setProperty("http.proxyHost", proxy);
        System.setProperty("http.proxyPort", Integer.toString(proxyPort)); 
    }

    /** Looks for key in classpath using "/technorati.license" */
    public Technorati() throws IOException {
        InputStream is = getClass().getResourceAsStream("/technorati.license");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        mKey = br.readLine();       
    }

    public Technorati(String proxy, int proxyPort) throws Exception {
        this();
        System.setProperty("proxySet", "true");
        System.setProperty("http.proxyHost", proxy);
        System.setProperty("http.proxyPort", Integer.toString(proxyPort)); 
    }
    
    public Result getLinkCosmos(String url) throws Exception {
        return new Result("http://api.technorati.com/cosmos",url,"links");
    }

    public Result getWeblogCosmos(String url) throws Exception {
        return new Result("http://api.technorati.com/cosmos",url,"weblog");
    }

    public Result getBloginfo(String url) throws Exception {
        return new Result("http://api.technorati.com/bloginfo",url,null);
    }

    public Result getOutbound(String url) throws Exception {
        return new Result("http://api.technorati.com/outbound",url,null);
    }

    /** Technorati result with header info and collection of weblog items */
    public class Result {
        private Weblog mWeblog = null;
        private Collection mWeblogs = new ArrayList();

        protected Result(String apiUrl, String url, String type) throws Exception {
            Map args = new HashMap();
            args.put("url", url);
            args.put("type", type);
            args.put("format", "xml");
            args.put("key", mKey);

            int start = 0;
            boolean repeat = true;
            XPath itemsPath = XPath.newInstance("/tapi/document/item/weblog");            

            while (repeat) {
                 Document doc = getRawResults(apiUrl,args);               
                Element elem = doc.getRootElement();                          
                String error = getString(elem,"/tapi/document/result/error");
                if ( error != null ) throw new Exception(error);
                if (mWeblog == null) {
                    XPath p = XPath.newInstance("/tapi/document/result/weblog");
                    Element w = (Element) p.selectSingleNode(doc);
                    mWeblog = new Weblog(w);
                }
                int count=0;
                Iterator iter = itemsPath.selectNodes(doc).iterator();
                while (iter.hasNext()) {
                    Element element = (Element) iter.next();
                    Weblog w = new Weblog(element);
                    mWeblogs.add(w);
                    count++;
                }
                if ( count < 20 ) { 
                    repeat = false;
                }
                else {
                    start += 20;
                    args.put("start",new Integer(start));
                }
            }
        }

        public Weblog getWeblog() {return mWeblog;}
        public Collection getWeblogs() {return mWeblogs;}
    }

    /** Technorati weblog representation */
    public class Weblog {
        private String mName = null;
        private String mUrl = null;
        private String mRssurl = null;
        private String mLastupdate = null;
        private String mNearestpermalink = null;
        private String mExcerpt = null;
        private int mInboundlinks = 0;
        private int mInboundblogs = 0;

        public Weblog(Element elem) throws Exception {
            mName = getString(elem,"name");
            mUrl = getString(elem,"url");
            mRssurl = getString(elem,"rssurl");
            mLastupdate = getString(elem,"lastupdate");
            mNearestpermalink = getString(elem,"nearestpermalink");
            mExcerpt = getString(elem,"excerpt");
            try { 
                mInboundlinks = getInt(elem,"inboundlinks");
            } catch (Exception ignored) {}
            try { 
                mInboundblogs = getInt(elem,"inboundblogs");
            } catch (Exception ignored) {}
        }

        public String getName() {return mName;}
        public String getUrl() {return mUrl;}
        public String getRssurl() {return mRssurl;}
        public int getInboundblogs() {return mInboundblogs;}
        public int getInboundlinks() {return mInboundlinks;}
        public String getLastupdate() {return mLastupdate;}
        public String getNearestpermalink() {return mNearestpermalink;}
        public String getExcerpt() {return mExcerpt;}
    }
    
    protected Document getRawResults(String urlString, Map args) throws Exception {
        int count = 0;
        Iterator keys = args.keySet().iterator();
        while (keys.hasNext()) {
            String sep = count++==0 ? "?" : "&";
            String name = (String)keys.next();
            if ( args.get(name) != null ) {
                urlString += sep + name + "=" + args.get(name);                         
            }
        }
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.connect();
        SAXBuilder builder = new SAXBuilder();
        return builder.build(conn.getInputStream());
    }
    
    protected String getString(Element elem, String path) throws Exception {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        return e!=null ? e.getText() : null;
    }
    
    protected int getInt(Element elem, String path) throws Exception {
        XPath xpath = XPath.newInstance(path);
        Element e = (Element)xpath.selectSingleNode(elem);
        return e!=null ? Integer.parseInt(e.getText()) : 0;
    }
}
