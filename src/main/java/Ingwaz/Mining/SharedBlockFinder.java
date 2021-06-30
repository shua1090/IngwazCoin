package Ingwaz.Mining;

import Ingwaz.BlockChain.Block;

import java.math.BigInteger;

public class SharedBlockFinder {
    static volatile Block theBlock;
    static volatile boolean found;

    public static boolean isFound() {
        return found;
    }

    public static synchronized boolean setBlock(Block b) {
        if (new BigInteger(b.getHash()).compareTo(b.getTarget()) <= 0) {
            found = true;
            theBlock = b;
            return true;
        } else return false;
    }

    public synchronized static Block getBlock() {
        if (isFound()) return theBlock;
        else return null;
    }

    public static void reset() {
        found = false;
        theBlock = null;
    }
}
