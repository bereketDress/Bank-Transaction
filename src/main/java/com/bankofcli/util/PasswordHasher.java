package com.bankofcli.util;

import com.bankofcli.exception.BankException;

import org.mindrot.jbcrypt.BCrypt;
/*
Bcrypt is a class from external library
 */

public final class PasswordHasher {
    private static final int WORK_FACTOR = 12;

    public String hash(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new BankException("Secret cannot be blank.");
        }
        //gensalt: generate random data even two user have same password
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    public boolean matches(String plainText, String hash) {
        if (plainText == null || hash == null || hash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainText, hash);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
