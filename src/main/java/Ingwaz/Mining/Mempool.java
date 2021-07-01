/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Mining;

import Ingwaz.BlockChain.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Stack;

public class Mempool {
    public BalanceManager tm;
    Stack<Transaction> txList = new Stack<>();
    HashMap<String, BigDecimal> tempMap = new HashMap<>();

    public Mempool(BalanceManager tb) {
        tm = tb;
    }

    public boolean verifySufficientFunds(Transaction t) {
        String temp = t.getSenderAddr();
        if (tm.get(temp) == null) {
            return false;
        } else {
            return tm.get(temp).compareTo(t.getFullPaid()) >= 0;
        }
    }

    public TransactionStatus addTransaction(Transaction t) {

        // Check if there already is a Transaction submitted by the sender wallet
        for (int i = 0; i < this.txList.size(); i++) {
            if (txList.get(i).getSenderAddr().equals(t.getSenderAddr())) {
                return TransactionStatus.ANOTHER_TRANSACTION_SUBMITTED;
            }
        }

        // Verify Signature present within the Transaction
        try {
            if (
                    !Signature.verifySignature(
                            Base64.getDecoder().decode(t.getSignature()),
                            t.getTXID().getBytes(StandardCharsets.UTF_8),
                            KeyPackager.unpackagePubkey(t.getSenderAddr())
                    )
            ) return TransactionStatus.INVALID_SIGNATURE;
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            System.out.println("Be sure that Signature.Initialize has already been called to setup Bouncy Castle");
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return TransactionStatus.INVALID_KEY;
        }

        this.txList.add(t);
        System.out.println("Added to List");
        return TransactionStatus.ACCEPTED;
    }

    public Block buildBlock(long blockNumber) {
        Block b = new Block();

        b.setBlockNumber(blockNumber);
        b.setTimeStamp(System.currentTimeMillis());

        int i = 0;
        while (
                (i < this.txList.size()) &&
                        (b.getTransactions().size() <= b.getTransactionAmount())
        ) {

            Transaction t = this.txList.pop();

            if (verifySufficientFunds(t)) {
                tm.applyTransaction(t);
                b.addTransaction(t);
            }
            i++;
        }

        return b.copy();
    }

    public void applyMT(MinerTransaction mt) {
        this.tm.applyMinerTransaction(mt);
    }

}
