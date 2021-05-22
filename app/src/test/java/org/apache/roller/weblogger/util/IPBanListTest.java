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
    Path ipBanList;
    IPBanList sut;

    @BeforeEach
    void setUp() throws IOException {
        ipBanList = tmpDir.resolve("ipbanlist.txt");
        Files.createFile(ipBanList);
        sut = new IPBanList(() -> ipBanList.toAbsolutePath().toString());
    }

    @Test
    @DisplayName("addBanned() adds the given IP address to the file")
    void addBannedAddsToFile() {
        sut.addBannedIp("10.0.0.1");

        List<String> ipBanList = readIpBanList();
        assertTrue(ipBanList.contains("10.0.0.1"));
        assertEquals(1, ipBanList.size());
    }

    @Test
    @DisplayName("addBanned() ignores nulls")
    void addBannedIgnoresNulls() {
        sut.addBannedIp(null);

        assertTrue(readIpBanList().isEmpty());
    }

    @Test
    @DisplayName("isBanned() returns true if the given IP address is banned")
    void isBanned() {
        sut.addBannedIp("10.0.0.1");

        assertTrue(sut.isBanned("10.0.0.1"));
    }

    @Test
    @DisplayName("isBanned() returns false if the given IP address it not banned")
    void isBanned2() {
        assertFalse(sut.isBanned("10.0.0.1"));
    }

    @Test
    @DisplayName("isBanned() returns false if the given IP address is null")
    void isBanned3() {
        assertFalse(sut.isBanned(null));
    }

    @Test
    @DisplayName("isBanned() reads the file if needed")
    void isBanned4() {
        writeIpBanList("10.0.0.1");

        assertTrue(sut.isBanned("10.0.0.1"));
    }

    private void writeIpBanList(String ipAddress) {
        try {
            Files.writeString(ipBanList, ipAddress);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> readIpBanList() {
        try {
            return Files.readAllLines(ipBanList);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
