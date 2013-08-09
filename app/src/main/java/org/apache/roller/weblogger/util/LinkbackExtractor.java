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
package org.apache.roller.weblogger.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.Parser;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

/**
 * Parses HTML file for referring linkback title and excerpt.
 * 
 * @author David M Johnson
 */
public class LinkbackExtractor
{
    private static Log mLogger        = LogFactory.getFactory().getInstance(
                                              LinkbackExtractor.class);
    private boolean    mFound         = false;
    private String     mTitle         = "";
    private String     mRssLink       = null;
    private String     mExcerpt       = null;
    private String     mPermalink     = null;
    private int        mStart         = 0;
    private int        mEnd           = 0;
    private int        mMaxExcerpt    = 500;                           // characters
    private String     mRequestURL    = null;
    private String     mRequestURLWWW = null;
    private String     mRefererURL;

    //------------------------------------------------------------------------
    /**
     * Extract referring page title, excerpt, and permalink.
     * 
     * @param refererUrl
     * @param requestUrl
     */
    public LinkbackExtractor(String refererURL, String requestURL)
            throws MalformedURLException, IOException
    {
        try
        {
            extractByParsingHtml(refererURL, requestURL);
            if (mRssLink != null)
            {
                extractByParsingRss(mRssLink, requestURL);
            }
        }
        catch (Exception e)
        {
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("Extracting linkback", e);
            }
        }
    }

    //------------------------------------------------------------------------
    private void extractByParsingHtml(String refererURL, String requestURL)
            throws MalformedURLException, IOException
    {
        URL url = new URL(refererURL);
        InputStream is = url.openStream();

        mRefererURL = refererURL;

        if (requestURL.startsWith("http://www."))
        {
            mRequestURLWWW = requestURL;
            mRequestURL = "http://" + mRequestURLWWW.substring(11);
        }
        else
        {
            mRequestURL = requestURL;
            mRequestURLWWW = "http://www." + mRequestURL.substring(7);
        }

        // Trick gets Swing's HTML parser
        Parser parser = (new HTMLEditorKit() {
            public Parser getParser()
            {
                return super.getParser();
            }
        }).getParser();

        // Read HTML file into string
        StringBuffer sb = new StringBuffer();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        try
        {
            String line = null;
            while ((line = br.readLine()) != null)
            {
                sb.append(line);
            }
        }
        finally
        {
            br.close();
        }

        // Parse HTML string to find title and start and end position
        // of the referring excerpt.
        StringReader sr = new StringReader(sb.toString());
        parser.parse(sr, new LinkbackCallback(), true);

        if (mStart != 0 && mEnd != 0 && mEnd > mStart)
        {
            mExcerpt = sb.toString().substring(mStart, mEnd);
            mExcerpt = Utilities.removeHTML(mExcerpt);

            if (mExcerpt.length() > mMaxExcerpt)
            {
                mExcerpt = mExcerpt.substring(0, mMaxExcerpt) + "...";
            }
        }

        if (mTitle.startsWith(">") && mTitle.length() > 1)
        {
            mTitle = mTitle.substring(1);
        }
    }

    //------------------------------------------------------------------------
    private void extractByParsingRss(String rssLink, String requestURL)
            throws IllegalArgumentException, MalformedURLException, FeedException, IOException
    {
        SyndFeedInput feedInput = new SyndFeedInput();       
        SyndFeed feed = feedInput.build(
            new InputStreamReader(new URL(rssLink).openStream()));
        Iterator itemIter = feed.getEntries().iterator();
        String feedTitle = feed.getTitle();

        int count = 0;

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Feed parsed, title: " + feedTitle);
        }

        while (itemIter.hasNext())
        {
            count++;
            SyndEntry item = (SyndEntry) itemIter.next();
            if (item.getDescription().getValue().indexOf(requestURL) != -1)
            {
                mFound = true;
                mPermalink = item.getLink().toString();
                if (feedTitle != null && feedTitle.trim().length() > 0)
                {
                    mTitle = feedTitle + ": " + item.getTitle();
                }
                else
                {
                    mTitle = item.getTitle();
                }
                mExcerpt = item.getDescription().getValue();
                mExcerpt = Utilities.removeHTML(mExcerpt);
                if (mExcerpt.length() > mMaxExcerpt)
                {
                    mExcerpt = mExcerpt.substring(0, mMaxExcerpt) + "...";
                }
                break;
            }
        }

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Parsed " + count + " articles, found linkback="
                    + mFound);
        }
    }

    //------------------------------------------------------------------------
    /**
     * Returns the excerpt.
     * 
     * @return String
     */
    public String getExcerpt()
    {
        return mExcerpt;
    }

    //------------------------------------------------------------------------
    /**
     * Returns the title.
     * 
     * @return String
     */
    public String getTitle()
    {
        return mTitle;
    }

    //------------------------------------------------------------------------
    /**
     * Returns the permalink.
     * 
     * @return String
     */
    public String getPermalink()
    {
        return mPermalink;
    }

    //------------------------------------------------------------------------
    /**
     * Sets the permalink.
     * 
     * @param permalink
     *            The permalink to set
     */
    public void setPermalink(String permalink)
    {
        mPermalink = permalink;
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * Parser callback that finds title and excerpt. As we walk through the HTML
     * tags, we keep track of the most recently encountered divider tag in the
     * mStart field. Once we find the referring permalink, we set the mFound
     * flag. After that, we look for the next divider tag and save it's position
     * in the mEnd field.
     */
    private final class LinkbackCallback extends ParserCallback
    {
        // Dividers
        private Tag[] mDivTags    = { Tag.TD, Tag.DIV, Tag.SPAN,
                                          Tag.BLOCKQUOTE, Tag.P, Tag.LI,
                                          Tag.BR, Tag.HR, Tag.PRE, Tag.H1,
                                          Tag.H2, Tag.H3, Tag.H4, Tag.H5,
                                          Tag.H6 };

        private List  mList       = Arrays.asList(mDivTags);

        private Tag   mCurrentTag = null;

        /**
         * Look for divider tags and for the permalink.
         * 
         * @param tag
         *            HTML tag
         * @param atts
         *            Attributes of that tag
         * @param pos
         *            Tag's position in file
         */
        public void handleStartTag(Tag tag, MutableAttributeSet atts, int pos)
        {
            if (mList.contains(tag) && !mFound)
            {
                mStart = pos;
            }
            else if (mList.contains(tag) && mFound && mEnd == 0)
            {
                mEnd = pos;
            }
            else if (tag.equals(Tag.A))
            {
                String href = (String) atts.getAttribute(HTML.Attribute.HREF);
                if (href == null)
                    return;
                int hashPos = href.lastIndexOf('#');
                if (hashPos != -1)
                {
                    href = href.substring(0, hashPos);
                }
                if (href != null
                        && (href.equals(mRequestURL) || href
                                .equals(mRequestURLWWW)))
                {
                    mFound = true;
                }
                else
                {
                    /*
                     * if (mLogger.isDebugEnabled()) { mLogger.debug("No match:
                     * "+href); }
                     */
                }
            }
            mCurrentTag = tag;
        }

        /**
         * Needed to handle SPAN tag.
         */
        public void handleSimpleTag(Tag tag, MutableAttributeSet atts, int pos)
        {
            if (mList.contains(tag) && mFound && mEnd == 0)
            {
                mEnd = pos;
            }
            else if (tag.equals(Tag.LINK))
            {
                // Look out for RSS autodiscovery link
                String title = (String) atts.getAttribute(HTML.Attribute.TITLE);
                String type = (String) atts.getAttribute(HTML.Attribute.TYPE);
                if (title != null && type != null
                        && type.equals("application/rss+xml")
                        && title.equals("RSS"))
                {
                    mRssLink = (String) atts.getAttribute(HTML.Attribute.HREF);

                    if (mLogger.isDebugEnabled())
                    {
                        mLogger.debug("Found RSS link " + mRssLink);
                    }

                    if (mRssLink.startsWith("/") && mRssLink.length() > 1)
                    {
                        try
                        {
                            URL url = new URL(mRefererURL);
                            mRssLink = url.getProtocol() + "://"
                                    + url.getHost() + ":" + url.getPort()
                                    + mRssLink;
                        }
                        catch (MalformedURLException e)
                        {
                            mRssLink = null;
                            if (mLogger.isDebugEnabled())
                            {
                                mLogger.debug("Determining RSS URL", e);
                            }
                        }
                    }
                    else if (!mRssLink.startsWith("http"))
                    {
                        int slash = mRefererURL.lastIndexOf('/');
                        if (slash != -1)
                        {
                            mRssLink = mRefererURL.substring(0, slash) + "/"
                                    + mRssLink;
                        }
                    }
                    if (mLogger.isDebugEnabled())
                    {
                        mLogger.debug("Qualified RSS link is " + mRssLink);
                    }
                }
            }
        }

        /**
         * Stop at the very first divider tag after the permalink.
         * 
         * @param tag
         *            End tag
         * @param pos
         *            Position in HTML file
         */
        public void handleEndTag(Tag tag, int pos)
        {
            if (mList.contains(tag) && mFound && mEnd == 0)
            {
                mEnd = pos;
            }
            else if (mList.contains(tag) && !mFound)
            {
                mStart = pos;
            }
            else
            {
                mCurrentTag = null;
            }
        }

        /**
         * Get the page title
         */
        public void handleText(char[] data, int pos)
        {
            if (mCurrentTag != null && mCurrentTag.equals(Tag.TITLE))
            {
                String newText = new String(data);
                if (mTitle.length() < 50)
                {
                    mTitle += newText;
                }
            }
        }
    }
}

