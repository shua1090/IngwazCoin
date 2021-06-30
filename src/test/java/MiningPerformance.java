import Ingwaz.BlockChain.Block;
import Ingwaz.BlockChain.BlockChain;
import Ingwaz.Mining.Hash;
import Ingwaz.Mining.Miner;
import Ingwaz.Mining.SharedBlockFinder;
import Ingwaz.Values;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashPerformance {
    public BigInteger totalHashesCompleted;
    public long millisecondsPassed;
    public int conccurrentlyRunningThreadCount;

    @Override
    public String toString() {
        return "HashPerformance{" +
                "totalHashesCompleted=" + totalHashesCompleted +
                ", millisecondsPassed=" + millisecondsPassed +
                ", conccurrentlyRunningThreadCount=" + conccurrentlyRunningThreadCount +
                '}';
    }
}

public class MiningPerformance {

    static BigInteger zeroHex(int zeroCount) {
        return new BigInteger("0".repeat(zeroCount) + "F".repeat(64 - zeroCount), 16);
    }

    static HashPerformance miningPerformance(BigInteger target) throws InterruptedException {
        Block b = Block.randomBlock(1, Hash.hashToHex(new BlockChain("WasteBasket").loadLatestBlock().getHash()), Values.target);
        b.setNonce(BigInteger.ZERO);
        b.setTarget(target);

        HashPerformance hp = new HashPerformance();

        Miner m1 = new Miner(b.copy(), BigInteger.ZERO, true);
        Miner m2 = new Miner(b.copy(), new BigInteger("FFFFFFFFFFFFFFFF", 16), false);
        Miner m3 = new Miner(b.copy(), new BigInteger("FFFFFFFFFFFFFFFF", 16), true);

        Thread t1 = new Thread(m1);
        Thread t2 = new Thread(m2);
        Thread t3 = new Thread(m3);

        hp.conccurrentlyRunningThreadCount = 3;

        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t3.start();
        t3.join();
        long end = System.currentTimeMillis();
        hp.millisecondsPassed = end - start;
        BigInteger m1Hashes = m1.getStartingValue().subtract(m1.getActualStartingValue()).abs();
        BigInteger m2Hashes = m2.getStartingValue().subtract(m2.getActualStartingValue()).abs();
        BigInteger m3Hashes = m3.getStartingValue().subtract(m3.getActualStartingValue()).abs();
        hp.totalHashesCompleted = m1Hashes.add(m2Hashes).add(m3Hashes);
//        System.out.println(Hash.hashToHex(SharedBlockFinder.getBlock().getHash()));
        SharedBlockFinder.reset();
        return hp;
    }

    @Test
    void testTest() {
        try {
            long[] averageTimes = new long[7];
            for (int i = 1; i <= 7; i++) {
                long average = 0;
                for (int z = 0; z < 5; z++) {
                    average += miningPerformance(zeroHex(i)).millisecondsPassed;
                }
                averageTimes[i - 1] = average / 5;
                System.out.println("Finished " + i);
            }

            System.out.println("Array: \n" + Arrays.toString(averageTimes));
            for (int i = 1; i < 7; i++) {
                System.out.print((double) averageTimes[i] / averageTimes[i - 1] + ", ");
            }

            assertNotEquals(null, "This is a terribly written performance test in a place for Unit Tests");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
