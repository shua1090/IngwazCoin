/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network.Miner;

import Ingwaz.BlockChain.*;
import Ingwaz.Mining.Foreman;
import Ingwaz.Network.Server.Node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class MinerClient {
    BlockChain bc;
    Foreman f;
    Wallet w;
    ArrayList<String> serverAddresses = new ArrayList<>();

    boolean mine = true;

    public MinerClient(String directory, Wallet minersWallet) {
        bc = new BlockChain(directory);
        w = minersWallet;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Signature.InitializeProvider();
        Node n = new Node();

        Node.mp.tm.destroy();
        Node.mp.tm = new BalanceManager(new File("WasteBasket/Balances"));

//        Node.currentBlock = new Block();
//        Node.currentBlock.setBlockNumber(0);
//        Node.currentBlock.setPreviousHash("0".repeat(64));
//        Node.currentBlock.setTimeStamp(System.currentTimeMillis());
//        Node.currentBlock.setTarget(Node.bc.getTarget());
//
//        Listener ls = new Listener();
//        new Thread(ls::listen).start();
//
//        Wallet w = Wallet.createNewWallet("Mining");
//        Wallet recieverWallet = Wallet.createNewWallet("recieverWallet");
//
//        MinerClient mc = new MinerClient("WasteBasket/Miner", w);
//
//        Node.bc.clear();
//        mc.bc.clear();
//
//        mc.serverAddresses.add("192.168.86.34");
//        mc.sync();
//        Thread t = new Thread(mc::startMining);
//        t.start();
//
//        WalletClient wc = new WalletClient(w, mc.serverAddresses.get(0));
//        WalletClient receiverWC = new WalletClient(recieverWallet, mc.serverAddresses.get(0));
//
//        long start = System.currentTimeMillis() + 21_000;
//        while (true) {
//            String balance = wc.parseCommand("Balance", null);
//            String balanceOfReceiver = receiverWC.parseCommand("Balance", null);
//
//            System.out.println("Miner/Sender: " + balance);
//            System.out.println("Receiver: " + balanceOfReceiver);
//
//            Thread.sleep(10_000);
//            if (System.currentTimeMillis() >= start) {
//                wc.parseCommand("Transaction", new String[]{KeyPackager.packagePubkey(recieverWallet), "100"});
//                System.out.println("Transaction submitted.");
//                start = Long.MAX_VALUE - 50;
//            }
//        }
    }


    private void miner() {
        System.out.println("Starting Mining");
        Block b = f.waitOnBlock();
        try {
            Socket s = new Socket(serverAddresses.get(0), 4200);

            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            dos.writeUTF("FoundBlock");

            String[] partsOfBlock = b.fullBlockAsString().split("\n");
            dos.writeInt(partsOfBlock.length);
            for (int i = 0; i < partsOfBlock.length; i++) {
                dos.writeUTF(partsOfBlock[i]);
            }
            if (din.readInt() == 1) {
                this.bc.saveBlock(b);
            }
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMining() {
        while (mine) {
            try {
                Socket s = new Socket(serverAddresses.get(0), 4200);
                var dos = new DataOutputStream(s.getOutputStream());
                var din = new DataInputStream(s.getInputStream());

                dos.writeUTF("BlockToMine");

                int length = din.readInt();
                String blok = "";
                for (int i = 0; i < length; i++) {
                    blok = blok + din.readUTF() + "\n";
                }

                Block c = new Block(blok);
                s.close();
                c.setMinerTransaction(c.calculateMT(this.w));
                c.calculateMerkleRoot();

                f = new Foreman(c.copy());

                f.startMining();
                Thread t = new Thread(this::miner);
                t.start();
                long a = System.currentTimeMillis();
                while (true) {
                    if (System.currentTimeMillis() >= (a + 10_000)) {
                        Socket tenSecSock = new Socket(serverAddresses.get(0), 4200);
                        var tendout = new DataOutputStream(tenSecSock.getOutputStream());
                        var tendin = new DataInputStream(tenSecSock.getInputStream());

                        tendout.writeUTF("CurrentBlockNumber");
                        long blockNum = tendin.readLong();
                        tenSecSock.close();
                        if (blockNum > c.getBlockNumber()) break;
                        else a = System.currentTimeMillis();
                    }
                }

                if (t.isAlive()) t.interrupt();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void stopMining() {
        f.stopMining();
    }

    public void sync() {
        try {
            Socket sok = new Socket(serverAddresses.get(0), 4200);
            var dossy = new DataOutputStream(sok.getOutputStream());
            var dinny = new DataInputStream(sok.getInputStream());
            dossy.writeUTF("BlockCount");
            long end = dinny.readLong();
            sok.close();

            if (end == -1) return;

            if (this.bc.loadLatestBlock() == null) {
                Socket s = new Socket(serverAddresses.get(0), 4200);
                var dos = new DataOutputStream(s.getOutputStream());
                var din = new DataInputStream(s.getInputStream());
                dos.writeUTF("Request Block");
                dos.writeLong(0);

                int length = din.readInt();
                String blok = "";
                for (int i = 0; i < length; i++) {
                    blok = blok + din.readUTF() + "\n";
                }
                Block c = new Block(blok);
                System.out.println("Work: " + c.verifyWork());
                this.bc.saveBlock(c);
                s.close();
            }

            long start = this.bc.loadLatestBlock().getBlockNumber() + 1;

            for (; start <= end; start++) {
                System.out.println("Syncing: " + start);
                Socket s = new Socket(serverAddresses.get(0), 4200);
                var dos = new DataOutputStream(s.getOutputStream());
                var din = new DataInputStream(s.getInputStream());
                dos.writeUTF("Request Block");

                dos.writeLong(start);

                int length = din.readInt();
                String blok = "";
                for (int i = 0; i < length; i++) {
                    blok = blok + din.readUTF() + "\n";
                }
                Block c = new Block(blok);
                System.out.println("Work: " + c.verifyWork());
                this.bc.saveBlock(c);
                s.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
