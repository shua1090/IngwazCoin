/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network.Server;

import Ingwaz.BlockChain.*;
import Ingwaz.Mining.Hash;
import Ingwaz.Mining.Mempool;
import Ingwaz.Mining.TransactionStatus;
import Ingwaz.Network.AddressList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class Node {
    public static BlockChain bc;
    public static Mempool mp;

    public static Block currentBlock;

    public static ArrayList<Socket> minerSocks = new ArrayList<>();

    static {
        try {
            mp = new Mempool(new BalanceManager(new File("WasteBasket/Balances")));
            bc = new BlockChain("WasteBasket/BlockChain");
            currentBlock = bc.loadLatestBlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    AddressList AL = new AddressList();

    public Node() {
    }
}

class Handler implements Runnable {
    Socket sock;

    public Handler(Socket s) {
        this.sock = s;
    }

    @Override
    public void run() {
        try {
            DataOutputStream dos = new DataOutputStream(this.sock.getOutputStream());
            DataInputStream din = new DataInputStream(this.sock.getInputStream());

            String command = din.readUTF();
            switch (command) {
                case "Request Block": {
                    long blockNumber = din.readLong();
                    Block b = Node.bc.findBlock(blockNumber);
                    String[] partsOfBlock = b.fullBlockAsString().split("\n");
                    dos.writeInt(partsOfBlock.length);
                    for (int i = 0; i < partsOfBlock.length; i++) {
                        dos.writeUTF(partsOfBlock[i]);
                    }
                    break;
                    // TODO: Implement Balance Checking by Node
                }
                case "BlockCount": {
                    Block blockCount = Node.bc.loadLatestBlock();
                    if (blockCount == null) {
                        dos.writeLong(-1);
                    } else
                        dos.writeLong(blockCount.getBlockNumber());
                    break;
                }
                case "Exists": {
                    Block b = Node.bc.findBlock(din.readLong());
                    dos.writeBoolean(b != null);
                    break;
                }
                case "Balance": {
                    String address = din.readUTF();
                    try {
                        BigDecimal bd = Node.mp.tm.get(address);
                        if (bd == null) dos.writeUTF("0");
                        else dos.writeUTF(bd.toString());
                    } catch (AddressDNEException ade) {
                        dos.writeUTF("No such Address exists");
                    }
                    break;
                }
                case "FoundBlock": {
                    int length = din.readInt();
                    String theStringBlock = "";
                    for (int i = 0; i < length; i++) {
                        theStringBlock += din.readUTF() + "\n";
                    }
                    Block theHopefulBlock = new Block(theStringBlock);
                    if (!theHopefulBlock.calculateMTAmount().equals(Node.currentBlock.calculateMTAmount()) ||
                            !(theHopefulBlock.getTarget().equals(Node.bc.getTarget())) ||
                            !theHopefulBlock.verifyWork() ||
                            !theHopefulBlock.getTransactions().equals(Node.currentBlock.getTransactions()) ||
                            !(theHopefulBlock.getBlockNumber() == (Node.currentBlock.getBlockNumber()))
                    ) {
                        dos.writeInt(-1);
                        break;
                    } else {
                        dos.writeInt(1);
                        Node.mp.applyMT(theHopefulBlock.getMt());
                        Node.bc.saveBlock(theHopefulBlock);
                    }

                    // Establish next Block
                    Node.currentBlock = Node.mp.buildBlock(Node.bc.loadLatestBlock().getBlockNumber() + 1);
                    Node.currentBlock.setPreviousHash(Hash.hashToHex(Node.bc.loadLatestBlock().getHash()));
                    Node.currentBlock.setTimeStamp(System.currentTimeMillis());
                    Node.currentBlock.setTarget(Node.bc.getTarget());
                    break;
                }

                case "CurrentBlockNumber": {
                    dos.writeLong(Node.currentBlock.getBlockNumber());
                    break;
                }

                case "BlockToMine": {
                    String[] partsOfBlock = Node.currentBlock.fullBlockAsString().split("\n");
                    dos.writeInt(partsOfBlock.length);
                    for (int i = 0; i < partsOfBlock.length; i++) {
                        dos.writeUTF(partsOfBlock[i]);
                    }
                    break;
                }
                case "Apply Transaction": {
                    Transaction txToApprove = Transaction.fromString(din.readUTF());
                    System.out.println(txToApprove);
                    if (txToApprove.getTXID().equals("null")) {
                        txToApprove.setTXID(String.valueOf(System.currentTimeMillis()));
                        dos.writeUTF(txToApprove.toString());
                    } else {

                        try {
                            if (txToApprove.verify()) {
                                System.out.println("Verified");
                                TransactionStatus tp = Node.mp.addTransaction(
                                        txToApprove
                                );
                                dos.writeUTF(tp.toString());
                            } else {
                                System.out.println("Unverified");
                                dos.writeUTF("Invalid Transaction Signature");
                            }

                        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                }
                default: {
                    break;
                }
            }

            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
