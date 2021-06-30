package Ingwaz.BlockChain;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Simply Packages and Unpackages Public and Private ECDSA Keys
 */
public class KeyPackager {
    /**
     * @param theKey The Public Key to be packaged
     * @return A String representing the Public Key in Base64 format
     */
    public static String packagePubkey(PublicKey theKey) {
        return Base64.getEncoder().encodeToString(theKey.getEncoded());
    }

    /**
     * @param packagedKey The ECDSA Base64-Packaged Public Key
     * @return A ECDSA Public Key
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static PublicKey unpackagePubkey(String packagedKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        return KeyFactory.getInstance("ECDSA", "BC").generatePublic(
                new X509EncodedKeySpec(
                        Base64.getDecoder().decode(
                                packagedKey
                        )
                )
        );
    }


    /**
     * @param theKey The Private Key to be packaged
     * @return A String representing the Private Key in Base64 format
     */
    public static String packagePrikey(PrivateKey theKey) {
        return Base64.getEncoder().encodeToString(theKey.getEncoded());
    }


    /**
     * @param packagedKey The ECDSA Base64-Packaged Private Key
     * @return A ECDSA Private Key
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey unpackagePrikey(String packagedKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        return KeyFactory.getInstance("ECDSA", "BC").generatePrivate(
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(
                                packagedKey
                        )
                )
        );
    }

    public static String packagePubkey(Wallet w) {
        return packagePubkey(w.getPubKey());
    }

    public static String packagePrikey(Wallet w) {
        return packagePrikey(w.getPriKey());
    }
}
