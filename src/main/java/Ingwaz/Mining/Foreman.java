/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Mining;

import Ingwaz.BlockChain.Block;
import Ingwaz.BlockChain.BlockChain;
import Ingwaz.BlockChain.Signature;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

class ThreadCountSize extends StackOverflowError {
    public ThreadCountSize(String specifics) {
        super(specifics);
    }

    public ThreadCountSize() {
        super();
    }
}

/**
 * A Class that manages "Miners",
 * able to wait until they're finished
 * or halt mining prematurely.
 */
public class Foreman {
    List<Thread> theMiners = new ArrayList<>();

    Block theBlock;

    public Foreman(Block b) {
        this.theBlock = b;
        Miner m1 = new Miner(b.copy(), BigInteger.ZERO, true);
        Miner m2 = new Miner(b.copy(), new BigInteger("F".repeat(16), 16), true);

        theMiners.add(new Thread(m1));
        theMiners.add(new Thread(m2));
    }

    public static void main(String[] args) throws IOException {
        Signature.InitializeProvider();
        BlockChain bc = new BlockChain("WasteBasket/BlockChain");
        SharedBlockFinder.reset();

        System.out.println(bc.verifyChain());
//
//        long longStart = System.currentTimeMillis();
//
//        Wallet w = Wallet.createNewWallet("WasteTest");
//
//        for (int i = 0; i < 251; i++) {
//            Block temp = Block.randomBlock(
//                    i,
//                    (bc.loadLatestBlock() == null) ? "0".repeat(64) : Hash.hashToHex(bc.loadLatestBlock().getHash()),
//                    bc.getTarget(0.01)
//            );
//
//            temp.setMinerTransaction(temp.calculateMT(w));
//
//            Foreman f = new Foreman(temp.copy());
//            long a = System.currentTimeMillis();
//            f.startMining();
//            Block b = f.waitOnBlock();
//            long bTime = System.currentTimeMillis();
//            System.out.println("Time pass: " + (bTime - a));
//
//            if (i % 5 == 0)
//            System.out.println("Target of Block " + i + " is: " +
//                    ("0".repeat(64 - temp.getTarget().toString(16).length())) +
//                    temp.getTarget().toString(16));
//
//            if (i % 10 == 0 && i != 0){
//                System.out.println("10 Block TimePass: " + (System.currentTimeMillis() - longStart));
//                longStart = System.currentTimeMillis();
//            }
//
//            try {
//                bc.saveBlock(b);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            SharedBlockFinder.reset();
//
//        }
    }

    public void startMining() {
        for (int i = 0; i < theMiners.size(); i++) {
            theMiners.get(i).start();
        }
    }

    public void stopMining() {
        for (int i = 0; i < theMiners.size(); i++) {
            if (!theMiners.get(i).isInterrupted() && theMiners.get(i).isAlive()) theMiners.get(i).interrupt();
        }
    }

    public Block waitOnBlock() {
        try {
            this.theMiners.get(theMiners.size() - 1).join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Block b = SharedBlockFinder.getBlock();
        SharedBlockFinder.reset();
        return b;
    }
}
