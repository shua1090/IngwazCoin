package Ingwaz.Mining;

import Ingwaz.BlockChain.Block;

import java.math.BigInteger;


public class Miner implements Runnable {
    private final Block block;
    private final BigInteger actualStartingValue;
    private final boolean ascending;
    private BigInteger startingValue;

    public Miner(Block b, BigInteger startingValue, boolean ascending) {
        this.block = b;
        this.startingValue = startingValue;
        this.actualStartingValue = startingValue;
        this.ascending = ascending;
    }

    public BigInteger getStartingValue() {
        return startingValue;
    }

    @Override
    public void run() {
        long a = System.currentTimeMillis();
        while (!SharedBlockFinder.found) {
            try {
                if (ascending) {
                    block.setNonce(startingValue.add(BigInteger.ONE));
                    startingValue = startingValue.add(BigInteger.ONE);
                } else {
                    block.setNonce(startingValue.subtract(BigInteger.ONE));
                    startingValue = startingValue.subtract(BigInteger.ONE);
                }

//                System.out.println(block.getNonce().toString(16));

                if (new BigInteger(Hash.hashToHex(block.getHash()), 16).compareTo(block.getTarget()) <= 0) {
                    if (SharedBlockFinder.setBlock(block)) {
                        long b = System.currentTimeMillis();
//                        System.out.println("Hashing Rate: " + new BigDecimal(this.startingValue.subtract(this.actualStartingValue).abs()).divide(BigDecimal.valueOf((b-a)), 4, RoundingMode.CEILING).multiply(new BigDecimal("1000")) + " Hashes per second. For the one starting with Nonce: " + this.actualStartingValue);
//                        System.out.println("Hashes completed: " + this.startingValue.subtract(this.actualStartingValue).abs());
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        long b = System.currentTimeMillis();
//        System.out.println("Hashing Rate: " + new BigDecimal(this.startingValue.subtract(this.actualStartingValue).abs()).divide(BigDecimal.valueOf((b-a)), 4, RoundingMode.CEILING).multiply(new BigDecimal("1000")) + " Hashes per second. For the one starting with Nonce: " + this.actualStartingValue);
//        System.out.println("Milliseconds elapsed: " + (b-a));
//        System.out.println("-".repeat(5) + " " + this.block.getBlockNumber());
//        System.out.println(new BigInteger(Hash.hashToHex(block.getHash()), 16).compareTo(block.getTarget()));
//        System.out.println(this.block.getTarget());
//        System.out.println(new BigInteger(Hash.hashToHex(this.block.getHash()), 16));
//        System.out.println(SharedBlockFinder.getBlock().getTarget().compareTo(new BigInteger(Hash.hashToHex((SharedBlockFinder.getBlock().getHash())), 16)));
        return;
    }

    public BigInteger getActualStartingValue() {
        return actualStartingValue;
    }
}
