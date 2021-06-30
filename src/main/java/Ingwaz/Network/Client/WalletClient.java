/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network.Client;

import Ingwaz.BlockChain.*;
import Ingwaz.Network.Server.Listener;
import Ingwaz.Network.Server.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Scanner;

public class WalletClient {
    private final String serverIPAddress;
    Wallet w;


    public WalletClient(Wallet w, String serverIPAddress) {
        this.w = w;
        this.serverIPAddress = serverIPAddress;
    }

    public static void main(String[] args) throws IOException {
        Signature.InitializeProvider();
        Node s = new Node();
        Listener ls = new Listener();
        new Thread(ls::listen).start();

        Wallet w = Wallet.createNewWallet("Test");

        Node.mp.tm.destroy();
        Node.mp.tm = new BalanceManager(new File("WasteBasket/Balances"));

        Node.mp.tm.put(w, new BigDecimal("100"));

        WalletClient wc = new WalletClient(
                w,
                "192.168.86.34"
        );

        Scanner scan = new Scanner(System.in);

        while (!scan.nextLine().contains("exit")) {
            wc.parseCommand(scan.nextLine(), new String[]{KeyPackager.packagePubkey(w.getPubKey())});
            Node.mp.tm.put(w, Node.mp.tm.get(w).add(new BigDecimal("100")));
        }
    }

    // Receiver address, amount
    public String parseCommand(String cmd, String[] additionalData) throws IOException {
        Socket sock = new Socket(serverIPAddress, 4200);
        DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
        DataInputStream din = new DataInputStream(sock.getInputStream());
        String commandResult = "";
        switch (cmd) {
            case "Transaction":
                String receiverAddress = additionalData[0];
                BigDecimal amount = new BigDecimal(additionalData[1]);

                dos.writeUTF("Apply Transaction");
                dos.writeUTF(this.w.createTransaction(receiverAddress, amount).toString());

                Transaction t = Transaction.fromString(din.readUTF());
                sock.close();
                sock = new Socket(serverIPAddress, 4200);
                dos = new DataOutputStream(sock.getOutputStream());
                din = new DataInputStream(sock.getInputStream());
                dos.writeUTF("Apply Transaction");
                w.signTransaction(t);
                dos.writeUTF(t.toString());
                commandResult = din.readUTF();
                break;
            case "Balance":
                dos.writeUTF("Balance");
                dos.writeUTF(KeyPackager.packagePubkey(this.w));
                commandResult = din.readUTF();
                break;
            default:
                break;
        }
        sock.close();
        return commandResult;
    }

}
