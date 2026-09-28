package com.bankofcli.util;

import com.bankofcli.exception.BankException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    private final PasswordHasher hasher = new PasswordHasher();

    @Test
    void hashesWithUniqueSaltsAndVerifiesOnlyTheCorrectSecret() {
        String first = hasher.hash("test-password");
        String second = hasher.hash("test-password");
        assertNotEquals("test-password", first);
        assertNotEquals(first, second);
        assertTrue(hasher.matches("test-password", first));
        assertTrue(hasher.matches("test-password", second));
        assertFalse(hasher.matches("wrong-password", first));
        assertFalse(hasher.matches(null, first));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsBlankSecrets(String secret) {
        assertThrows(BankException.class, () -> hasher.hash(secret));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "not-a-bcrypt-hash"})
    void rejectsMissingOrMalformedHashes(String hash) {
        assertFalse(hasher.matches("test-password", hash));
    }
}
