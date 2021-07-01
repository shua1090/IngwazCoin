package Ingwaz.BlockChain;


import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.*;

public class Signature {

    public static void InitializeProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a KeyPair for the wallet and signing
     *
     * @return KeyPair for use in the wallet
     */
    public static KeyPair GenerateKeys() {
        try {
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("B-571");
            KeyPairGenerator g = KeyPairGenerator.getInstance("ECDSA", "BC");
            g.initialize(ecSpec, new SecureRandom());
            return g.generateKeyPair();
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            throw new RuntimeException("Error generating Key, see above^");
        }
    }

    /**
     * Signs the message
     *
     * @param message The message to sign
     * @param prikey  The EC private key to use in signing
     * @return A byte array representing the signed message
     */
    public static byte[] sign(byte[] message, PrivateKey prikey) {
        try {
            java.security.Signature ecdsaSign = java.security.Signature.getInstance("SHA256withECDSA", "BC");
            ecdsaSign.initSign(prikey);
            ecdsaSign.update(message);
            return ecdsaSign.sign();
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchProviderException | InvalidKeyException e) {
            e.printStackTrace();
            throw new RuntimeException("Error signing the message, see above ^");
        }
    }

    /**
     * @param signature    The signature to be verified
     * @param dataToVerify The data that the signature should be equal to
     * @param pubkey       The Public Key to use to verify
     * @return a boolean that represents if the signature and the data are the same (The signature is valid)
     */
    public static boolean verifySignature(byte[] signature, byte[] dataToVerify, PublicKey pubkey) {
        try {
            java.security.Signature ecdsaSign = java.security.Signature.getInstance("SHA256withECDSA", "BC");
            ecdsaSign.initVerify(pubkey);
            ecdsaSign.update(dataToVerify);
            return ecdsaSign.verify(signature);
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchProviderException | InvalidKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

}
