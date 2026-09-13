package com.healthfirst.pims.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Small helper used everywhere the application needs to turn a plain-text
 * password into the SHA-256 hash that is actually stored in the "users"
 * table (never store plain-text passwords in a database).
 */
public class PasswordUtil {

    /** Hashes a plain-text password with SHA-256 and returns it as a hex string. */
    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // SHA-256 and UTF-8 are guaranteed to exist on every JVM, so this
            // should never happen in practice.
            throw new RuntimeException("Unable to hash password", e);
        }
    }

    /** Convenience command-line tool: prints the hash for any password you pass in. */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("admin123 -> " + hash("admin123"));
            System.out.println("cash123  -> " + hash("cash123"));
        } else {
            for (String pwd : args) {
                System.out.println(pwd + " -> " + hash(pwd));
            }
        }
    }
}
