/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Mining;

import Ingwaz.BlockChain.Block;

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
