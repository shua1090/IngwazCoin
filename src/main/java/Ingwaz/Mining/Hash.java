package Ingwaz.Mining;

import org.bouncycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {

    /**
     * Converts a byte array input into a HexString,
     * particularly useful as a toString method for the
     * resulting SHA256 hash output from the hash function
     *
     * @param args The byte array to convert to a Hex String
     * @return A HexString representing the input byte-array
     */
    public static synchronized String hashToHex(byte[] args) {
        return Hex.toHexString(args);
    }

    /**
     * Converts a 64-character HexString to a byte array,
     * particularly useful for a from-string method
     * for the SHA256 Hash, that recreates a byte-array hash from
     * a HexString
     *
     * @param hexString The hexString to revert to a Byte-Array
     * @return A byte array representing the bytes of the HexString
     */
    public static synchronized byte[] hexToHash(String hexString) {
        return Hex.decode(hexString);
    }

    /**
     * Applies SHA256 Once to the input byte array
     *
     * @param args The Byte Array to Hash
     * @return The SHA256 Hash of the input, in a Byte array Format
     */
    public static synchronized byte[] hash(byte[] args) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert digest != null;
        return digest.digest(args);
    }
}
