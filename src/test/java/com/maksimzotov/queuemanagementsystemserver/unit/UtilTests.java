package com.maksimzotov.queuemanagementsystemserver.unit;

import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.PhoneHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTests {

    @Test
    void testGenerateCodeForEmail() {
        for (int i = 0; i < 5; i++) {
            assertEquals(4, CodeGenerator.generateCodeForEmail().toString().length());
        }
    }

    @Test
    void testGenerateCodeInLocation() {
        assertEquals(11, CodeGenerator.generateCodeInLocation(List.of(1, 5, 10)));
        assertEquals(1, CodeGenerator.generateCodeInLocation(List.of(2, 5, 10)));
    }

    @Test
    void testGenerateAccessKey() {
        for (int i = 0; i < 5; i++) {
            assertEquals(4, CodeGenerator.generateAccessKey().toString().length());
        }
    }

    @Test
    void testEmailMatches() {
        assertFalse(EmailChecker.emailMatches(""));
        assertFalse(EmailChecker.emailMatches("zotovm256gmail.com"));
        assertFalse(EmailChecker.emailMatches("zotovm256@gmailcom"));
        assertTrue(EmailChecker.emailMatches("zotovm256@gmail.com"));
    }

    @Test
    void testPhoneMatches() {
        assertFalse(PhoneHelper.phoneMatches(""));
        assertFalse(PhoneHelper.phoneMatches("+81234567890"));
        assertFalse(PhoneHelper.phoneMatches("+812345678901"));
        assertFalse(PhoneHelper.phoneMatches("812345678901"));
        assertFalse(PhoneHelper.phoneMatches("+8123456789"));
        assertFalse(PhoneHelper.phoneMatches("8123456789"));
        assertFalse(PhoneHelper.phoneMatches("+712345678901"));
        assertFalse(PhoneHelper.phoneMatches("712345678901"));
        assertFalse(PhoneHelper.phoneMatches("+7123456789"));
        assertFalse(PhoneHelper.phoneMatches("7123456789"));
        assertTrue(PhoneHelper.phoneMatches("+71234567890"));
        assertTrue(PhoneHelper.phoneMatches("71234567890"));
        assertTrue(PhoneHelper.phoneMatches("81234567890"));
    }

    @Test
    void testNormalizePhoneForDatabase() {
        assertEquals("+71234567890", PhoneHelper.normalizePhoneForDatabase("81234567890"));
        assertEquals("+71234567890", PhoneHelper.normalizePhoneForDatabase("+71234567890"));
        assertEquals("+71234567890", PhoneHelper.normalizePhoneForDatabase("71234567890"));
    }
}
