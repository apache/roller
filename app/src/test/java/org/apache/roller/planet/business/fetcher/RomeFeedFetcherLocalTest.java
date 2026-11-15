/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.roller.planet.business.fetcher;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.impl.Converters;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.impl.FeedParsers;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.util.rome.ContentModule;
import org.apache.roller.planet.util.rome.PlanetConverterForRSS091N;
import org.apache.roller.planet.util.rome.PlanetConverterForRSS091U;
import org.apache.roller.planet.util.rome.PlanetConverterForRSS20;
import org.apache.roller.planet.util.rome.PlanetRSS091UParser;
import org.apache.roller.weblogger.util.SafeSAXBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Fetches feeds from a loopback HTTP server, so the mapping from ROME's model
 * to Planet's is checked without reaching the network.
 */
class RomeFeedFetcherLocalTest {

    private static final String ATOM = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Atom Blog</title>
              <id>urn:example:atom</id>
              <link rel="self" type="application/atom+xml" href="https://atom.example.org/feed/atom"/>
              <link rel="alternate" type="text/html" href="https://atom.example.org/"/>
              <updated>2026-10-12T08:30:00Z</updated>
              <entry>
                <id>urn:example:atom:1</id>
                <title>Atom entry</title>
                <author><name>Entry Author</name></author>
                <link rel="alternate" type="text/html" href="https://atom.example.org/entry/1"/>
                <published>2026-10-10T20:00:00-04:00</published>
                <updated>2026-10-11T09:15:00+02:00</updated>
                <category term="java"/>
                <category term="feeds"/>
                <content type="html">&lt;p&gt;Atom body&lt;/p&gt;</content>
              </entry>
            </feed>
            """;

    private static final String RSS_20 = """
            <?xml version="1.0" encoding="utf-8"?>
            <rss version="2.0" xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:content="http://purl.org/rss/1.0/modules/content/"
                 xmlns:atom="http://www.w3.org/2005/Atom">
              <channel>
                <title>RSS Blog</title>
                <link>https://rss.example.org/</link>
                <atom:link rel="self" type="application/rss+xml" href="https://rss.example.org/feed.xml"/>
                <description>RSS 2.0 feed</description>
                <item>
                  <title>With pubDate</title>
                  <link>https://rss.example.org/1</link>
                  <pubDate>Sat, 10 Oct 2026 20:00:00 -0400</pubDate>
                  <dc:creator>Dee Creator</dc:creator>
                  <category>planet</category>
                  <description>Summary one</description>
                  <content:encoded><![CDATA[<p>Full text one</p>]]></content:encoded>
                </item>
                <item>
                  <title>With dc:date</title>
                  <link>https://rss.example.org/2</link>
                  <dc:date>2026-10-09T12:00:00+02:00</dc:date>
                  <description>Summary two</description>
                </item>
              </channel>
            </rss>
            """;

    private static final String RSS_091 = """
            <?xml version="1.0"?>
            <rss version="0.91">
              <channel>
                <title>Old Blog</title>
                <link>https://old.example.org/</link>
                <description>RSS 0.91 feed</description>
                <language>en-us</language>
                <item>
                  <title>Old item</title>
                  <link>https://old.example.org/1</link>
                  <description>Old text</description>
                  <pubDate>Fri, 09 Oct 2026 08:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
            """;

    private static final Map<String, String> FEEDS = new ConcurrentHashMap<>();

    private static HttpServer server;

    private final RomeFeedFetcher fetcher = new RomeFeedFetcher();

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            String body = FEEDS.get(exchange.getRequestURI().getPath());
            byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
            exchange.sendResponseHeaders(body == null ? 404 : 200, bytes.length == 0 ? -1 : bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
        server.start();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    @Test
    void atomFeedUsesAlternateLinkAsSiteUrl() throws Exception {
        String url = serve("/atom", ATOM);

        Subscription sub = fetcher.fetchSubscription(url);

        assertEquals(url, sub.getFeedURL());
        // ROME 2 reports the rel="self" link as the feed link; Planet wants the site
        assertEquals("https://atom.example.org/", sub.getSiteURL());
        assertEquals("Atom Blog", sub.getTitle());
        assertEquals(Instant.parse("2026-10-12T08:30:00Z"), sub.getLastUpdated().toInstant());

        SubscriptionEntry entry = entry(sub, "https://atom.example.org/entry/1");
        assertEquals("Atom entry", entry.getTitle());
        assertEquals("Entry Author", entry.getAuthor());
        assertEquals(timestamp("2026-10-11T00:00:00Z"), entry.getPubTime());
        assertEquals(timestamp("2026-10-11T07:15:00Z"), entry.getUpdateTime());
        assertEquals("<p>Atom body</p>", entry.getText());
        assertEquals("java,feeds", entry.getCategoriesString());
    }

    @Test
    void atomFeedPrefersHtmlAlternateOverOtherFormats() throws Exception {
        String url = serve("/atom-alternates", """
                <?xml version="1.0" encoding="utf-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>Many Links</title>
                  <id>urn:example:alternates</id>
                  <link rel="alternate" type="application/atom+xml" href="https://alt.example.org/fr/atom"/>
                  <link rel="alternate" type="application/feed+json" href="https://alt.example.org/feed.json"/>
                  <link rel="alternate" type="text/html" href="https://alt.example.org/"/>
                  <updated>2026-10-12T08:30:00Z</updated>
                </feed>
                """);
        assertEquals("https://alt.example.org/", fetcher.fetchSubscription(url).getSiteURL());

        String onlyFeeds = serve("/atom-feed-alternate", """
                <?xml version="1.0" encoding="utf-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>Feed Links Only</title>
                  <id>urn:example:feed-alternate</id>
                  <link rel="self" type="application/atom+xml" href="https://only.example.org/atom"/>
                  <link rel="alternate" type="application/rss+xml" href="https://only.example.org/rss"/>
                  <updated>2026-10-12T08:30:00Z</updated>
                </feed>
                """);
        assertEquals("https://only.example.org/rss", fetcher.fetchSubscription(onlyFeeds).getSiteURL());
    }

    @Test
    void unchangedFeedIsSkipped() throws Exception {
        String url = serve("/atom-unchanged", ATOM);

        Date updated = Date.from(Instant.parse("2026-10-12T08:30:00Z"));
        assertNull(fetcher.fetchSubscription(url, updated));
        assertNotNull(fetcher.fetchSubscription(url, new Date(updated.getTime() - 1)));
    }

    @Test
    void rss20FeedMapsDatesAuthorsAndContent() throws Exception {
        String url = serve("/rss20", RSS_20);

        Subscription sub = fetcher.fetchSubscription(url);

        assertEquals("https://rss.example.org/", sub.getSiteURL());
        assertEquals("RSS Blog", sub.getTitle());

        SubscriptionEntry first = entry(sub, "https://rss.example.org/1");
        assertEquals("Dee Creator", first.getAuthor());
        assertEquals(timestamp("2026-10-11T00:00:00Z"), first.getPubTime());
        assertEquals("<p>Full text one</p>", first.getText());
        assertEquals("planet", first.getCategoriesString());

        SubscriptionEntry second = entry(sub, "https://rss.example.org/2");
        assertEquals(timestamp("2026-10-09T10:00:00Z"), second.getPubTime());
        assertEquals("Summary two", second.getText());
    }

    @Test
    void rss091ItemPubDateIsUsed() throws Exception {
        String url = serve("/rss091", RSS_091);

        Subscription sub = fetcher.fetchSubscription(url);

        assertEquals("https://old.example.org/", sub.getSiteURL());
        SubscriptionEntry entry = entry(sub, "https://old.example.org/1");
        assertEquals(timestamp("2026-10-09T08:00:00Z"), entry.getPubTime());
        assertEquals("Old text", entry.getText());
    }

    @Test
    void romePropertiesRegistersPlanetPlugins() throws Exception {
        Converters converters = new Converters();
        assertInstanceOf(PlanetConverterForRSS20.class, converters.getConverter("rss_2.0"));
        assertInstanceOf(PlanetConverterForRSS091U.class, converters.getConverter("rss_0.91U"));
        assertInstanceOf(PlanetConverterForRSS091N.class, converters.getConverter("rss_0.91N"));

        assertInstanceOf(PlanetRSS091UParser.class, new FeedParsers()
                .getParserFor(new SafeSAXBuilder().build(new StringReader(RSS_091))));

        SyndEntry item = new SyndFeedInput().build(new StringReader(RSS_20)).getEntries().get(0);
        ContentModule content = assertInstanceOf(ContentModule.class, item.getModule(ContentModule.URI));
        assertEquals("<p>Full text one</p>", content.getEncoded());
    }

    @Test
    void feedWithDoctypeIsRefused(@TempDir Path dir) throws Exception {
        Path secret = dir.resolve("secret.txt");
        Files.writeString(secret, "SECRET-CONTENT");
        String url = serve("/doctype", "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE feed [<!ENTITY secret SYSTEM \"" + secret.toUri() + "\">]>"
                + "<feed xmlns=\"http://www.w3.org/2005/Atom\"><title>&secret;</title>"
                + "<link rel=\"alternate\" href=\"https://evil.example.org/\"/></feed>");

        FetcherException ex = assertThrows(FetcherException.class, () -> fetcher.fetchSubscription(url));
        ParsingFeedException refused = assertInstanceOf(ParsingFeedException.class, ex.getRootCause());
        assertTrue(refused.getMessage().contains("DOCTYPE"), refused.getMessage());
    }

    private static String serve(String path, String feed) {
        FEEDS.put(path, feed);
        return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort() + path;
    }

    private static SubscriptionEntry entry(Subscription sub, String permalink) {
        return sub.getEntries().stream()
                .filter(e -> permalink.equals(e.getPermalink()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No entry " + permalink + " in " + sub.getEntries()));
    }

    private static Timestamp timestamp(String instant) {
        return Timestamp.from(Instant.parse(instant));
    }
}
