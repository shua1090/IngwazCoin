/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.BlockChain;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DBException;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.DbImpl;
import org.iq80.leveldb.impl.WriteBatchImpl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import static org.iq80.leveldb.impl.Iq80DBFactory.bytes;
import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

class BalanceBatch extends WriteBatchImpl {
    public BalanceBatch() {
        super();
    }

    public void put(String walletAddress, BigDecimal amount) {
        super.put(
                bytes(walletAddress),
                bytes(amount.toString())
        );
    }
}

public class BalanceManager extends DbImpl {
    private final File databaseDir;

    public BalanceManager(File databaseDir) throws IOException {
        super(getProperOptions(), databaseDir);
        this.databaseDir = databaseDir;
    }

    private static Options getProperOptions() {
        Options opt = new Options();
        opt.cacheSize(100 * 1048576);
        opt.compressionType(CompressionType.SNAPPY);
        return opt;
    }

    public void put(String walletAddress, BigDecimal amount) throws DBException {
        this.put(
                bytes(walletAddress),
                bytes(amount.toString())
        );
    }

    public void put(Wallet w, BigDecimal amount) throws DBException {
        this.put(KeyPackager.packagePubkey(w.getPubKey()), amount);
    }

    public BigDecimal get(String walletAddress) {
        try {
            return new BigDecimal(new String(super.get(bytes(walletAddress))));
        } catch (DBException | NullPointerException dbe) {
            return null;
        }
    }

    public BigDecimal get(Wallet w) {
        return get(KeyPackager.packagePubkey(w.getPubKey()));
    }

    public void applyTransaction(Transaction t) {
        String sender = t.getSenderAddr();
        String receiver = t.getReceiverAddr();
        // Receiver gets amount 'coins', while sender loses said amount

        if (this.get(receiver) == null) {
            this.put(receiver, BigDecimal.ZERO);
            applyTransaction(t);
        } else {
            this.put(sender, this.get(sender).subtract(t.getFullPaid()));
            this.put(receiver, this.get(receiver).add(t.getAmountWithoutFees()));
        }

    }

    public void applyMinerTransaction(MinerTransaction mt) {
        String receiver = mt.getMinerAddress();
        if (this.get(receiver) == null) {
            this.put(receiver, BigDecimal.ZERO);
            this.applyMinerTransaction(mt);
        } else {
            this.put(receiver, this.get(receiver).add(mt.amount));
        }
    }

    public void applyBlock(Block b) {
        for (Transaction t : b.getTransactions()) {
            applyTransaction(t);
        }
        MinerTransaction m = b.getMt();
        applyMinerTransaction(m);
    }


    public void destroy() throws IOException {
        this.close();
        factory.destroy(this.databaseDir, new Options());
    }

    @Override
    public byte[] get(byte[] key) throws DBException {
        return super.get(key);
    }
}
