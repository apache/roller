/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.planet.business.fetcher;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Feeds that are too large, or that arrive too slowly, fail with a clear
 * error instead of tying up the feed updater.
 */
class RomeFeedFetcherLimitsTest {

    private static final int MAX_BYTES = 64 * 1024;
    private static final Duration MAX_TIME = Duration.ofSeconds(2);

    private static final String HEAD = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
              <title>Limits</title>
              <id>urn:example:limits</id>
              <updated>2026-10-12T08:30:00Z</updated>
              <!--""";
    private static final String TAIL = """
              -->
            </feed>
            """;

    private static HttpServer server;
    private static ExecutorService executor;
    private static volatile boolean stopping;

    private final RomeFeedFetcher fetcher =
            new RomeFeedFetcher(MAX_BYTES, MAX_TIME);

    // a valid feed padded with a comment to the given size
    private static byte[] feed(int size) {
        int padding = Math.max(0, size - HEAD.length() - TAIL.length());
        return (HEAD + "x".repeat(padding) + TAIL)
                .getBytes(StandardCharsets.UTF_8);
    }

    @BeforeAll
    static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(
                InetAddress.getLoopbackAddress(), 0), 0);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
        server.createContext("/small", exchange -> send(exchange, feed(1024), true));
        server.createContext("/large", exchange -> send(exchange, feed(MAX_BYTES * 16), true));
        server.createContext("/large-chunked", exchange -> send(exchange, feed(MAX_BYTES * 16), false));
        server.createContext("/drip", RomeFeedFetcherLimitsTest::drip);
        server.createContext("/stall", RomeFeedFetcherLimitsTest::stall);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        stopping = true;
        server.stop(0);
        executor.shutdownNow();
    }

    private static void send(HttpExchange exchange, byte[] body,
            boolean contentLength) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/atom+xml");
        exchange.sendResponseHeaders(200, contentLength ? body.length : 0);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        } catch (IOException e) {
            // client gave up
        }
    }

    // sends a small valid feed one byte every 100 ms
    private static void drip(HttpExchange exchange) throws IOException {
        byte[] body = feed(1024);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            for (int i = 0; i < body.length && !stopping; i++) {
                out.write(body[i]);
                out.flush();
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            // client gave up or server stopping
        }
    }

    // sends headers, then nothing
    private static void stall(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, 0);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(HEAD.getBytes(StandardCharsets.UTF_8));
            out.flush();
            while (!stopping) {
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            // client gave up or server stopping
        }
    }

    private static String url(String path) {
        return "http://" + server.getAddress().getHostString() + ":"
                + server.getAddress().getPort() + path;
    }

    // fetching fails within a few seconds, with the cause's message
    // containing the expected text
    private void assertFailsFast(String path, String message) {
        long start = System.nanoTime();
        FetcherException ex = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> assertThrows(FetcherException.class,
                        () -> fetcher.fetchSubscription(url(path))));
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
        assertTrue(elapsed.compareTo(MAX_TIME.plusSeconds(2)) < 0,
                "took " + elapsed);
        Throwable cause = ex.getRootCause() != null ? ex.getRootCause() : ex;
        assertTrue(cause.getMessage() != null
                && cause.getMessage().contains(message), cause.toString());
    }

    @Test
    void feedWithinLimitsIsFetched() throws Exception {
        assertNotNull(fetcher.fetchSubscription(url("/small")));
    }

    @Test
    void oversizedFeedFails() {
        assertFailsFast("/large", "larger than " + MAX_BYTES + " bytes");
    }

    @Test
    void oversizedChunkedFeedFails() {
        assertFailsFast("/large-chunked", "larger than " + MAX_BYTES + " bytes");
    }

    @Test
    void slowFeedFails() {
        assertFailsFast("/drip", "not received within 2 seconds");
    }

    @Test
    void stalledFeedFails() {
        assertFailsFast("/stall", "not received within 2 seconds");
    }

    @Test
    void onlyHttpAndHttpsUrlsAreFetched() {
        for (String url : new String[] {"file:///etc/hosts", "ftp://localhost/feed",
                "jar:file:///tmp/x.jar!/feed.xml"}) {
            FetcherException ex = assertThrows(FetcherException.class,
                    () -> fetcher.fetchSubscription(url));
            assertTrue(ex.getMessage().contains("http"), ex.getMessage());
        }
    }
}
