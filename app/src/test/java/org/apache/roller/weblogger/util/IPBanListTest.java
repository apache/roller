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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IPBanListTest {

    @TempDir
    Path tmpDir;
    Path ipBanListPath;
    IPBanList ipBanList;

    @BeforeEach
    void setUp() throws IOException {
        ipBanListPath = tmpDir.resolve("ipbanlist.txt");
        Files.createFile(ipBanListPath);
        ipBanList = new IPBanList(() -> ipBanListPath.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("addBanned() adds the given IP address to the file")
    void addBannedAddsToFile() {
        ipBanList.addBannedIp("10.0.0.1");

        List<String> ipBanList = readIpBanList();
        assertTrue(ipBanList.contains("10.0.0.1"));
        assertEquals(1, ipBanList.size());
    }

    @Test
    @DisplayName("addBanned() ignores nulls")
    void addBannedIgnoresNulls() {
        ipBanList.addBannedIp(null);

        assertTrue(readIpBanList().isEmpty());
    }

    @Test
    @DisplayName("isBanned() returns true if the given IP address is banned")
    void isBanned() {
        ipBanList.addBannedIp("10.0.0.1");
        try { // work around for intermittently failing test
            Thread.sleep(500);
        } catch (InterruptedException ignored) {}
        assertTrue(ipBanList.isBanned("10.0.0.1"));
    }

    @Test
    @DisplayName("isBanned() returns false if the given IP address it not banned")
    void isBanned2() {
        assertFalse(ipBanList.isBanned("10.0.0.1"));
    }

    @Test
    @DisplayName("isBanned() returns false if the given IP address is null")
    void isBanned3() {
        assertFalse(ipBanList.isBanned(null));
    }

    @Test
    @DisplayName("isBanned() reads the file if needed")
    void isBanned4() {
        writeIpBanList("10.0.0.1");

        assertTrue(ipBanList.isBanned("10.0.0.1"));
    }

    private void writeIpBanList(String ipAddress) {
        try {
            Files.writeString(ipBanListPath, ipAddress);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> readIpBanList() {
        try {
            return Files.readAllLines(ipBanListPath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}