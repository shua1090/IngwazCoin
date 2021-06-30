/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.BlockChain;

import Ingwaz.Mining.Hash;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class MinerTransaction {


    BigDecimal amount;
    String minerAddress;


    public MinerTransaction(BigDecimal amount, String receiverAddr) {
        this.amount = amount;
        minerAddress = receiverAddr;
    }

    public MinerTransaction(BigDecimal amount, Wallet receiverAddr) {
        this(amount, KeyPackager.packagePubkey(receiverAddr.getPubKey()));
    }

    public static MinerTransaction fromString(String s) {
        if (s.equals("null")) return null;
        String[] a = s.split("->");
        return new MinerTransaction(new BigDecimal(a[0]), a[1]);
    }

    public String getHash() {
        return Hash.hashToHex(Hash.hash(Hash.hash(this.toString().getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public String toString() {
        return this.amount + "->" + minerAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }
}
