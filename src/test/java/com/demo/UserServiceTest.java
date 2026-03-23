package com.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService service;
    private UserService.User alice;
    private UserService.User bob;

    @BeforeEach
    void setUp() {
        service = new UserService();
        alice = new UserService.User(1, "Alice", "alice@example.com");
        alice.setAge(30);
        bob = new UserService.User(2, "Bob", "bob@example.com");
        bob.setAge(25);
    }

    // ----- isAdminUser -----

    @Test
    void isAdminUser_withAdminRole_returnsTrue() {
        // NOTE: this test passes only because string literals are interned.
        // The bug (==) would fail for dynamically-built strings like new String("ADMIN").
        assertTrue(service.isAdminUser("ADMIN"));
    }

    @Test
    void isAdminUser_withNonAdminRole_returnsFalse() {
        assertFalse(service.isAdminUser("USER"));
    }

    @Test
    void isAdminUser_bugDemo_failsForNonInternedString() {
        // This test demonstrates the == bug:
        // new String("ADMIN") is NOT the same reference as the literal "ADMIN"
        String dynamicRole = new String("ADMIN");
        assertFalse(service.isAdminUser(dynamicRole),
                "== comparison fails for non-interned strings – this is the bug!");
    }

    // ----- getUserEmail -----

    @Test
    void getUserEmail_validUser_returnsEmail() {
        assertEquals("alice@example.com", service.getUserEmail(alice));
    }

    @Test
    void getUserEmail_nullUser_throwsNPE() {
        // Demonstrates the missing null-check bug
        assertThrows(NullPointerException.class, () -> service.getUserEmail(null));
    }

    // ----- buildUserReport -----

    @Test
    void buildUserReport_returnsFormattedReport() {
        String report = service.buildUserReport(List.of(alice, bob));
        assertTrue(report.contains("Alice"));
        assertTrue(report.contains("bob@example.com"));
    }

    // ----- isValidAge -----

    @Test
    void isValidAge_validAge_returnsTrue() {
        assertTrue(service.isValidAge(25));
    }

    @Test
    void isValidAge_tooYoung_returnsFalse() {
        assertFalse(service.isValidAge(10));
    }

    // ----- calculateAverageAge -----

    @Test
    void calculateAverageAge_returnsCorrectAverage() {
        // Actual average = (30 + 25) / 2 = 27.5
        // Due to integer division bug, the method returns 27.0
        double avg = service.calculateAverageAge(List.of(alice, bob));
        // This assertion exposes the integer division bug
        assertNotEquals(27.5, avg, "Bug: integer division truncates the decimal part");
        assertEquals(27.0, avg, "Actual (buggy) result due to integer division");
    }

    // ----- getActiveUsers -----

    @Test
    void getActiveUsers_emptyList_returnsNull() {
        // Demonstrates the bad practice of returning null instead of empty list
        assertNull(service.getActiveUsers());
    }

    // ----- addUser -----

    @Test
    void addUser_newUser_addsSuccessfully() {
        service.addUser(alice);
        assertNotNull(service.getActiveUsers());
        assertEquals(1, service.getActiveUsers().size());
    }

    @Test
    void addUser_duplicate_notAdded() {
        service.addUser(alice);
        service.addUser(alice);
        assertEquals(1, service.getActiveUsers().size());
    }
}
