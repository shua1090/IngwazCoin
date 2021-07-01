/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.BlockChain;

import Ingwaz.Mining.Hash;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;


public class BlockChain {
    public static int miningTime = 10; // ~ 10 minutes per Block
    public static BigDecimal minersReward = new BigDecimal("100"); // The Initial Miner's Reward
    public static long halvingBlockAmount = 100_000; // The amount of blocks before a halving is applied
    public static long targetResetBlockCount = 100; // Amount of blocks before each target reset
    String directoryName;

    public BlockChain(String dirName) {
        this.directoryName = dirName;
        syncData();
    }

    public void syncData() {
        if (!Files.exists(Path.of(this.directoryName + "/metadata"))) {
            try {
                Files.createFile(Path.of(this.directoryName + "/metadata"));
                BufferedWriter bw = new BufferedWriter(new FileWriter(this.directoryName + "/metadata"));
                bw.write("-".repeat(5) + " INGWAZ CHAIN METADATA " + "-".repeat(5) + "\n");
                bw.write(BlockChain.miningTime + "\n");
                bw.write(BlockChain.minersReward.toString() + "\n");
                bw.write(BlockChain.halvingBlockAmount + "\n");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(this.directoryName + "/metadata"));
                br.readLine();
                BlockChain.miningTime = Integer.parseInt(br.readLine());
                BlockChain.minersReward = new BigDecimal(br.readLine());
                BlockChain.halvingBlockAmount = Long.valueOf(br.readLine());
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void clear() throws IOException {
        if (loadLatestBlock() == null) return;
        else {
            long a = loadLatestBlock().blockNumber;
            for (long start = 0; start <= a; start++) {
                Files.delete(Path.of(this.directoryName + "/" + start + ".blk"));
            }
        }
    }

    public synchronized void saveBlock(Block b) throws IOException {
        File f = new File(this.directoryName + "/" + b.getBlockNumber() + ".blk");
        if (!f.exists()) {
            f.createNewFile();
        }
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(b.fullBlockAsString());

        bw.flush();
        fw.flush();

        bw.close();
        fw.close();
    }

    private Block loadBlock(BufferedReader br) throws IOException {
        br.readLine();
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            header.append(br.readLine());
        }
        Block b = Block.loadHeader(Block.loadSpacedHex(header.toString()));
        br.readLine();
        String trans = "";
        while (!(trans = br.readLine()).equals("==END TRANSACTIONS==")) {
            b.addTransaction(Transaction.fromString(trans));
        }
        b.setMinerTransaction(MinerTransaction.fromString(br.readLine()));
        b.setTimeStamp(new BigInteger(br.readLine()).longValue());
        b.setTarget(new BigInteger(br.readLine().substring(2), 16));
        return b;
    }

    public synchronized Block findBlock(long blockNumber) {
        File f = new File(this.directoryName + "/" + blockNumber + ".blk");
        return getBlock(f);
    }

    public synchronized Block findBlock(Path f) {
        File file = new File(String.valueOf(f));
        return getBlock(file);
    }

    private Block getBlock(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return loadBlock(br);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
//            throw new RuntimeException("Block does not seem to exist in the given directory");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("See above IOException");
        }
    }

    public BigInteger getTarget() {
        Block b = loadLatestBlock();
        if (b == null || b.getBlockNumber() == 0) {
            return new BigInteger("0000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
        }

        BigDecimal a = new BigDecimal(b.getTarget());
        BigInteger newTarget;

        if ((b.getBlockNumber()) % targetResetBlockCount == 0) {
            // 600,000 = 10 minutes
            newTarget = a.multiply(new BigDecimal(b.getTimeStamp() - findBlock(b.getBlockNumber() - targetsAreValid()).getTimeStamp()).divide(new BigDecimal(miningTime * 6_000_000), 5, RoundingMode.CEILING)).toBigInteger();
        } else newTarget = b.getTarget();
        return newTarget;
    }

    public void removeBlock(long blockNumber) {
        try {
            Files.delete(Path.of(this.directoryName + "/" + blockNumber + ".blk"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Block loadEarliestBlock() {
        Block firstBlock = null;
        try {
            firstBlock = Files.list(Path.of(this.directoryName)).filter(p -> p.toString().endsWith(".blk")).map(this::findBlock).min(new Comparator<Block>() {
                @Override
                public int compare(Block block, Block t1) {
                    return block.compareBlockNums(t1);
                }
            }).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return firstBlock;
    }

    public synchronized Block loadLatestBlock() {
        Block firstBlock = null;
        try {
            firstBlock = Files.list(Path.of(this.directoryName)).filter(p -> p.toString().endsWith(".blk")).map(this::findBlock).min(new Comparator<Block>() {
                @Override
                public int compare(Block block, Block t1) {
                    return -block.compareBlockNums(t1);
                }
            }).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return firstBlock;
    }

    private long targetsAreValid() {
        BigInteger target = null;
        long blockNum = loadLatestBlock().blockNumber;
        for (long a = 0; a <= blockNum; a++) {
            if (a == 0) {
                target = new BigInteger("0000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
            } else if ((a) % targetResetBlockCount == 1 && a != 1) {
                target = new BigDecimal(target).multiply(new BigDecimal(findBlock(a - 1).getTimeStamp() - findBlock(a - (targetResetBlockCount + 1)).getTimeStamp()).divide(new BigDecimal(miningTime * 60 * 1000), 5, RoundingMode.CEILING)).toBigInteger();
            }
            if (!target.toString(16).equals(findBlock(a).getTarget().toString(16))) {
                return a;
            }
        }
        return -1;
    }

    private long hashBelowTarget() {
        long blockNum = loadLatestBlock().blockNumber;

        for (long a = 0; a <= blockNum; a++) {
            if (new BigInteger(Hash.hashToHex(findBlock(a).getHash()), 16).compareTo(findBlock(a).getTarget()) <= 0) {
            } else return a;
        }
        return -1;
    }

    private long validMinersFees() {
        long blocknum = loadLatestBlock().blockNumber;
        for (long a = 0; a <= blocknum; a++) {
            Block temp = findBlock(a);
            BigDecimal val = BigDecimal.ZERO;
            for (Transaction t : temp.getTransactions()) {
                val = val.add(t.getTransactionFees());
            }
            val = val.add(
                    BlockChain.minersReward.multiply(BigDecimal.ONE.divide(BigDecimal.valueOf(2).pow((int) (a / halvingBlockAmount)), 3, RoundingMode.FLOOR))
            );
            if (!val.equals(temp.getMt().amount)) return a;
        }
        return -1;
    }

    private long validLinks() {
        long blocknum = (loadLatestBlock() == null) ? -1 : loadLatestBlock().blockNumber;
        for (; blocknum >= 0; blocknum--) {
            if (blocknum == 0) {
                return (findBlock(blocknum).getPreviousHash().equals("0".repeat(64))) ? -1 : 0;
            } else {
                if (!findBlock(blocknum).getPreviousHash().equals(Hash.hashToHex(findBlock(blocknum - 1).getHash())))
                    return blocknum;
            }
        }
        return -1;
    }

    public long verifyChain() {
        long a = targetsAreValid();
        System.out.println("Valid Targets");
        if (a != -1)
            return a;


        a = hashBelowTarget();
        System.out.println("Hash Below Target");
        if (a != -1) return a;
        a = validMinersFees();
        System.out.println("Valid Miners Fee");
        if (a != -1) return a;
        a = validLinks();
        System.out.println("Valid Links");
        return a;
    }

}

