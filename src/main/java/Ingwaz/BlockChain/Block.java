/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.BlockChain;

import Ingwaz.Mining.Hash;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * The Block class contains data representing each block,
 * including Transactions, a Header containing the previousHash, blockNumber, the timeStamp,
 * the Root node of the MerkleHash, the Target, and when mined, the Nonce.
 * <p>
 * Will include methods to generate the root node of the MerkleHash and to verify the Nonce's validity
 */
public class Block {

    // Block Header (114 Bytes):
    long blockNumber;  // A number that represents the block number - 8 bytes
    String previousHash = null; // 32 Bytes (not Hex)
    long timeStamp;    // Current Time in Milliseconds since 1970 - 8 bytes

    String merkleRoot; // Merkle Root of the transactions - 32 bytes
    BigInteger nonce = BigInteger.ZERO;  // 64 Bytes (512 bit Integer)

    // Contained in the Block:
    BigInteger target; // 32 Byte

    int transactionAmount = 1000; // Max amount of Transactions
    ArrayList<Transaction> transactions = new ArrayList<>();
    MinerTransaction mt;

    public Block() {
    }

    public Block(Block b) {
        this.transactions = b.transactions;
        this.blockNumber = b.blockNumber;
        this.previousHash = b.previousHash;
        this.merkleRoot = b.merkleRoot;
        this.target = b.target;
        this.nonce = b.nonce;
        this.timeStamp = b.timeStamp;
        this.transactionAmount = b.transactionAmount;
        this.mt = b.mt;
    }

    public Block(String s) {
        this(recreateBlock(s));
    }

    private static Block recreateBlock(String s) {
        String[] theBlockInParts = s.split("\n");

        StringBuilder header = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            header.append(theBlockInParts[i]);
        }
        Block b = Block.loadHeader(Block.loadSpacedHex(header.toString()));

        int i = 7;
        while (!theBlockInParts[i].equals("==END TRANSACTIONS==")) {
            b.addTransaction(Transaction.fromString(theBlockInParts[i]));
            i++;
        }
        i++;
        b.setMinerTransaction(
                (theBlockInParts[i].equals("null")) ? null : MinerTransaction.fromString(theBlockInParts[i])
        );
        i++;
        b.setTimeStamp(new BigInteger(theBlockInParts[i]).longValue());
        i++;
        b.setTarget(new BigInteger(theBlockInParts[i].substring(2), 16));
        return b;
    }

    /**
     * Generates a Random Block (without Miner's Transaction)
     * for Testing purposes
     *
     * @param blockNumber  The BlockNumber of the random Block
     * @param previousHash The Previous Hash for this Block
     * @param target       The Block's target
     * @return A Randomly Generated Block
     */
    public static Block randomBlock(long blockNumber, String previousHash, BigInteger target) {
        Signature.InitializeProvider();
        Block b = new Block();
        b.previousHash = previousHash;
        b.blockNumber = blockNumber;
        b.timeStamp = System.currentTimeMillis();

        for (int i = 0; i < b.transactionAmount; i++) {
            b.addTransaction(Transaction.createRandomTransaction());
        }

        b.calculateMerkleRoot();
        b.nonce = BigInteger.ZERO;
        b.target = target; // new BigInteger("00000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
//        System.out.println("This previous hash = " + b.previousHash);
        return b;
    }

    /**
     * Concatenates two byte arrays,
     * returning a byte array that contains the first
     * array, followed by the second
     *
     * @param first  The first byte-array to concatenate
     * @param second The second byte-array to concatenate
     * @return A byte array that encompasses both the first
     * and second byte arrays, in their respective order
     */
    private synchronized static byte[] concatenateByteArrays(byte[] first, byte[] second) {
        byte[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    /**
     * Add's Zeroes in front of BigInteger data
     * so that the resultant Header will always be 144 bytes in size
     *
     * @param data          The Data to pad with Zeroes
     * @param fullBytesSize The Size that the Data Should be, in bytes
     * @return A byte array containing the padded data
     */
    private synchronized static byte[] paddedBytes(BigInteger data, int fullBytesSize) {
        byte[] dataBytes = data.toByteArray();
        if (dataBytes.length > fullBytesSize) throw new RuntimeException("Data is already larger than fullSize");
        byte[] padding = new byte[fullBytesSize - dataBytes.length];
        return concatenateByteArrays(padding, dataBytes);
    }

    /**
     * Recreates a Block given the input header - can be used to
     * quickly verify PoW without requesting the entire block.
     *
     * @param header The byte array header to recreate a block
     * @return A Block from the Header, excluding Transactions
     */
    public synchronized static Block loadHeader(byte[] header) {
        Block b = new Block();

        b.blockNumber = new BigInteger(Arrays.copyOfRange(header, 0, 8)).longValue();
        b.previousHash = Hash.hashToHex(Arrays.copyOfRange(header, 8, 40));
        b.timeStamp = new BigInteger(Arrays.copyOfRange(header, 40, 48)).longValue();
        b.merkleRoot = Hash.hashToHex(Arrays.copyOfRange(header, 48, 80));
        b.nonce = new BigInteger(Arrays.copyOfRange(header, 80, 144));

        return b;
    }

    /**
     * Loads a formatted, two-byte-spaced,
     * HexString representing the Header of a block
     * into a byte array, which can be further used
     * to recreate a Block (loadHeader method).
     * Transactions must be loaded
     * separately, however.
     *
     * @param hex A HexString representing the
     *            formatted Header
     * @return A byte array representing the Header
     */
    public static synchronized byte[] loadSpacedHex(String hex) {
        String total = "";
        String[] spaced = hex.split(" ");
        for (String s : spaced) {
            total += s;
        }
        return Hex.decode(total);
    }

    /**
     * Recursively calculates the Merkle Tree Root of the transactions
     *
     * @param hashes The lower-most level of hashes
     * @return A String that represents the MerkleTree Root
     */
    private synchronized static String merkleRoot(String[] hashes) {
        if (hashes.length == 1) return hashes[0];

        if (hashes.length % 2 != 0) {
            hashes = Arrays.copyOf(hashes, hashes.length + 1);
            hashes[hashes.length - 1] = hashes[hashes.length - 2];
        }

        String[] newHashes = new String[hashes.length / 2];

        for (int i = 0; i < hashes.length; i += 2) {
            newHashes[i / 2] =
                    Hash.hashToHex(
                            Hash.hash(
                                    Hash.hash(
                                            concatenateByteArrays(
                                                    Hash.hexToHash(hashes[i]),
                                                    Hash.hexToHash(hashes[i + 1])
                                            )
                                    )
                            )
                    );
        }

        return merkleRoot(newHashes);
    }

    /**
     * Verifies the current block's hash
     * is below the Target, returning true
     * if it is, and false if the hash is
     * not below the Target. ProofOfWork verification, in short.
     *
     * @return A boolean value that represents if
     * some Work (Brute-Force Hashing) was put into this Block
     */
    public boolean verifyWork() {
        return new BigInteger(Hash.hashToHex(this.getHash()), 16).compareTo(this.target) <= 0;
    }

    /**
     * Returns a copy of the Block, with completely
     * new Allocations - Each copy is not linked with
     * the original, which is something that may happen otherwise
     * because of heap allocations in Java (See C++ Copy-Constructor
     * or contact Aditya Matam)
     *
     * @return A Block representing a copy of the current object
     */
    public Block copy() {
        return new Block(this);
    }

    /**
     * Appends the passed-in Transaction to this block
     *
     * @param t The Transaction to add to this block
     */
    public synchronized void addTransaction(Transaction t) {
        if (this.transactions.size() <= transactionAmount) {
            this.transactions.add(t);
        } else throw new IndexOutOfBoundsException("Size reached.");
    }


    /**
     * A method that gives a formatted
     * String reprsentation of the Header bytes
     * with spaces between every 2 bytes.
     *
     * @return A formatted String representing the header
     */
    public String headerSpacedHex() {
        byte[] bar = getHeaderAsBytes();
        String hexString = Hex.toHexString(bar);
        int size = 2;

        StringBuilder finalString = new StringBuilder();

        for (int start = 0; start < hexString.length(); start += size) {
            finalString.append(hexString, start, Math.min(hexString.length(), start + size)).append(" ");
        }
        return finalString.toString();
    }

    /**
     * Calculates the MerkleRoot from the
     * Transactions and the Miner's Fee, and setting
     * the result as the MerkleRoot of this block
     */
    public synchronized void calculateMerkleRoot() {
        if (this.mt == null) {
            this.merkleRoot = null;
        }

        ArrayList<String> transactionalHashes = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            transactionalHashes.add(transactions.get(i).getHash());
        }
        transactionalHashes.add(mt.getHash());
        merkleRoot = merkleRoot(transactionalHashes.toArray(new String[0]));
    }

    /**
     * A Method that Hashes the entire block -
     * Useful for Verification of PoW and
     * can be used in the Mining Process.
     *
     * @return The DoubleHash of this Block
     */
    public byte[] getHash() {
        return Hash.hash(Hash.hash(this.getHeaderAsBytes()));
    }

    /**
     * A method that calculates and returns the Block Header
     * as a byte array
     *
     * @return A byte array of the Header
     */
    public byte[] getHeaderAsBytes() {


        byte[] paddedBlockNum = paddedBytes(BigInteger.valueOf(blockNumber), 8); // Padded block number
        byte[] previousHash = Hash.hexToHash(this.previousHash); // Previous Hash bytes
        byte[] paddedTimeStamp = paddedBytes(BigInteger.valueOf(timeStamp), 8); // Padded TimeStamp
        byte[] merkles;
        if (this.merkleRoot == null)
            merkles = Hash.hexToHash("0".repeat(64));
        else
            merkles = Hash.hexToHash(this.merkleRoot);

        byte[] paddedNonce = paddedBytes(this.nonce, 64);

        byte[] header = concatenateByteArrays(paddedBlockNum, previousHash);
        header = concatenateByteArrays(header, paddedTimeStamp);
        header = concatenateByteArrays(header, merkles);
        header = concatenateByteArrays(header, paddedNonce);
        return header;
    }

    /**
     * Calculates the Miner's Fee with Transaction Fees
     *
     * @param address The String publicKey address of the miner
     * @return A MinerTransaction object that represents the
     * Fee the Miner receives for his services in Mining - including
     * a Standard fee of 100 IngwazCoin and a variable Transaction Fee
     * amount, which is 1% of each Transaction within the Block.
     */
    public MinerTransaction calculateMT(String address) {
        BigDecimal TXFees = BigDecimal.ZERO;
        for (int i = 0; i < this.transactions.size(); i++) {
            TXFees = TXFees.add(this.transactions.get(i).getTransactionFees());
        }
        TXFees = TXFees.add(BlockChain.minersReward.multiply(BigDecimal.ONE.divide(BigDecimal.valueOf(2).pow((int) (blockNumber / BlockChain.halvingBlockAmount)), 3, RoundingMode.FLOOR)));

        return new MinerTransaction(TXFees, address);
    }

    /**
     * Calculates the Miner's Fee with Transaction Fees
     *
     * @param address The Wallet who should receive the fees (Miner)
     * @return A MinerTransaction object that represents the
     * Fee the Miner receives for his services in Mining - including
     * a Standard fee of 100 IngwazCoin and a variable Transaction Fee
     * amount, which is 1% of each Transaction within the Block.
     */
    public MinerTransaction calculateMT(Wallet address) {
        return calculateMT(KeyPackager.packagePubkey(address.getPubKey()));
    }

    /*
     * GETTERS AND SETTERS
     */

    public int getTransactionAmount() {
        return transactionAmount;
    }

    public MinerTransaction getMt() {
        return mt;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    /**
     * Sets the Nonce for the Block, or throws an exception if
     * the Inputted Nonce is too large
     *
     * @param theNonce a BigInteger representing the nonce
     */
    public void setNonce(BigInteger theNonce) {
        if (!(theNonce.compareTo(BigInteger.ZERO) >= 0))
            throw new RuntimeException("Nonce: " + theNonce.toString(16) + " must be positive");
        if (!(theNonce.toByteArray().length < 64))
            throw new RuntimeException("Nonce must be less than 64 bytes in size");

        this.nonce = theNonce;
    }

    public BigInteger getTarget() {
        return target;
    }

    public void setTarget(BigInteger target) {
        this.target = target;
    }

    /**
     * A getter method that produces the Target of the Block as
     * a HexString
     *
     * @return A HexString representing the target BigInteger
     */
    public String getTargetAsHexString() {
        StringBuilder tar = new StringBuilder(target.toString(16));
        while (tar.length() < 64) {
            tar.insert(0, "0");
        }
        return "0x" + tar;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void setMinerTransaction(MinerTransaction t) {
        this.mt = t;
        if (this.mt != null)
            this.calculateMerkleRoot();
    }

    public BigDecimal calculateMTAmount() {
        BigDecimal TXFees = BigDecimal.ZERO;
        for (int i = 0; i < this.transactions.size(); i++) {
            TXFees = TXFees.add(this.transactions.get(i).getTransactionFees());
        }
        TXFees = TXFees.add(BlockChain.minersReward.multiply(BigDecimal.ONE.divide(BigDecimal.valueOf(2).pow((int) (blockNumber / BlockChain.halvingBlockAmount)), 3, RoundingMode.FLOOR)));
        return TXFees;
    }

    /**
     * A normal toString method for Printing,
     * for saving to Files, use Block.fullBlockAsString,
     * which represents the entire block, including Transactions  as a String
     *
     * @return A String that represents the base header data of the Block
     */
    @Override
    public String toString() {

        return "Block{" +
                "blockNumber=" + blockNumber +
                ", previousHash='" + previousHash + '\'' +
                ", timeStamp=" + timeStamp +
                ", merkleRoot='" + merkleRoot + '\'' +
                ", nonce=" + nonce +
                ", MinerT= " + mt +
                '}';
    }

    /**
     * Checks if this Block's header is equal
     * in all ways to the other Object passed in,
     * returning false otherwise
     *
     * @param o The other Object (Usually of Class Block)
     *          to check equality with
     * @return A Boolean asserting the equality, or lack
     * thereof, between this object and the paramter object
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return blockNumber == block.blockNumber && Objects.equals(previousHash, block.previousHash) &&
                Objects.equals(merkleRoot, block.merkleRoot) && Objects.equals(nonce, block.nonce) &&
                Objects.equals(target, block.target);
    }

    /**
     * Compares Blocks to find the Block that has
     * the Higher BlockNumber
     *
     * @param p2 The block to be compared to
     * @return 1, -1, or 0 depending on if the current
     * Block's blocknumber is larger, smaller, or equal
     */
    public int compareBlockNums(Block p2) {
        if (this.blockNumber > p2.blockNumber) {
            return 1;
        } else if (this.blockNumber < p2.blockNumber) {
            return -1;
        }
        return 0;
    }

    /**
     * Returns a full representation of the Block
     * as a String, with all the information within the block.
     *
     * @return a String containing all information inside the Block
     */
    public String fullBlockAsString() {
        StringBuilder result = new StringBuilder("======START BLOCK " + this.blockNumber + "======");

        String[] theHeader = headerSpacedHex().split(" ");

        for (int i = 0; i < 144; i++) {
            if (i % 30 == 0) {
                result.append("\n");
            }
            result.append(theHeader[i]).append(" ");
        }

        result.append("\n==START TRANSACTIONS==\n");
        for (int i = 0; i < transactions.size(); i++) {
            result.append(transactions.get(i)).append("\n");
        }
        result.append("==END TRANSACTIONS==\n");
        if (this.mt == null) result.append(null + "\n");
        else result.append(this.mt + "\n");
        result.append(this.timeStamp).append("\n");
        result.append(this.getTargetAsHexString()).append("\n");
        result.append("======END BLOCK======\n");
        return result.toString();
    }

}
