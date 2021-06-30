package Ingwaz.BlockChain;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * Contains the ECDSA Public and Private Key info, with a tool to save and load
 * said keys so a wallet can be reused.
 * <p>
 * Will include balance-checking in the future (Communicates with the future BlockChain class
 * to calculate current balance).
 */
public class Wallet {
    private PrivateKey priKey;
    private PublicKey pubKey;
    private String walletMemo;

    private Wallet() {
    }

    /**
     * A builder-method that returns a new Wallet
     *
     * @param memo A memo to add to the wallet when creating it
     * @return A Wallet Object
     */
    public static Wallet createNewWallet(String memo) {
        Wallet w = new Wallet();
        w.walletMemo = memo;

        KeyPair kp = Signature.GenerateKeys();
        w.priKey = kp.getPrivate();
        w.pubKey = kp.getPublic();

        return w;
    }

    /**
     * A method that loads a Wallet object from a File
     *
     * @param f The filename that represents the location of the wallet
     * @return A Wallet Object that is constructed from the given file
     */
    public static Wallet loadWalletInfo(String f) {
        File k = new File(f);
        if (!k.exists()) {
            System.out.println("The passed in File doesn't exist.");
            System.exit(-1);
        }

        Wallet w = new Wallet();

        try (BufferedReader reader = new BufferedReader(new FileReader(k))) {
            String sanity = reader.readLine();
            if (!sanity.substring(0, 5).equals("#####")) {
                System.out.println("This is not a recognized Ingwaz Wallet. Halting.");
                System.exit(-1);
            }
            String rawMemo = reader.readLine();
            if (rawMemo.charAt(0) == '#') {
                w.walletMemo = rawMemo.substring(8);
                reader.readLine();
            }
            w.priKey = KeyPackager.unpackagePrikey(reader.readLine());
            reader.readLine();
            w.pubKey = KeyPackager.unpackagePubkey(reader.readLine());
            reader.close();

            return w;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Signature.InitializeProvider();
        Wallet w = Wallet.loadWalletInfo("WasteBasket/MyWallet");

        Wallet f = Wallet.loadWalletInfo("WasteBasket/2ndWallet");

        Transaction t = w.createTransaction(KeyPackager.packagePubkey(f.getPubKey()), BigDecimal.ONE);

        t.setTXID("1-" + "1622846674559");
        w.signTransaction(t);

        System.out.println(t);
        System.out.println(Signature.verifySignature(
                Base64.getDecoder().decode(t.getSignature()),
                t.getTXID().getBytes(StandardCharsets.UTF_8),
                w.getPubKey()
        ));
        System.out.println("\n\n\n" + w.priKey);
    }

    public String getWalletMemo() {
        return walletMemo;
    }

    public void setWalletMemo(String walletMemo) {
        this.walletMemo = walletMemo;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

    public PrivateKey getPriKey() {
        return priKey;
    }

    /**
     * A method that saves the current Wallet into the Client's hard-disk
     *
     * @param filename A String that represents the relative or absolute path for the Wallet to be saved at
     */
    public void saveWalletInfo(String filename) {
        File f = new File(filename);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write("#".repeat(5) + " Ingwaz Wallet " + "#".repeat(5) + "\n");
            if (this.walletMemo != null)
                writer.write("#" + " Memo: " + this.walletMemo + "\n");
            writer.write("-".repeat(3) + " Private Key " + "-".repeat(3) + "\n");
            writer.write(KeyPackager.packagePrikey(priKey) + "\n");
            writer.write("-".repeat(3) + " Public Key " + "-".repeat(3) + "\n");
            writer.write(KeyPackager.packagePubkey(pubKey) + "\n");
            writer.write("#".repeat(5) + " END WALLET " + "#".repeat(5));
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * createTransaction Creates a Transaction by the wallet to send to the Blockchain
     *
     * @param receiver The ECDSA public Key of the receiver
     * @param amount   The amount of coins (positive) that is intended to be sent
     * @return A Transaction object representing the Transaction, without the TXID or Signature filled out
     */
    public Transaction createTransaction(String receiver, BigDecimal amount) {
        assert (amount.compareTo(BigDecimal.ZERO) > 0);
        return new Transaction(KeyPackager.packagePubkey(this.pubKey), amount, receiver);
    }

    /**
     * Signs the given Transaction with the Wallet's ECDSA private key
     * Will throw an assertion error if the public key of this wallet does not match the one in the Transaction
     *
     * @param t The Transaction to be signed
     */
    public void signTransaction(Transaction t) {

        try {
            if (!KeyPackager.unpackagePubkey(t.getSenderAddr()).equals(this.pubKey)) throw new AssertionError("This " +
                    "wallet's public key does not match the public key of the sender in the Transaction");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            e.printStackTrace();
            return;
        }

        t.addSignature(
                Base64.getEncoder().encodeToString(
                        Signature.sign(
                                t.getTXID().getBytes(StandardCharsets.UTF_8),
                                this.priKey
                        )
                )
        );

    }

}
