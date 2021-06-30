/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Peer {
    public static volatile AddressList addressList;
    ThreadPoolExecutor tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    ServerSocket serverSocket;


    public Peer(int port) {
        addressList = new AddressList();

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Socket could not be established. Report the error.");
        }
    }

    synchronized static boolean alreadyHasAddress(String address, int port) {
        return addressList.contains(new Address(address, port));
    }

    synchronized static void addAddress(String address, int port) {
        Peer.addressList.add(new Address(address, port));
    }

    public static void main(String[] args) {
        Peer serve = new Peer(4040);
        Peer client = new Peer(9090);

        addressList.add(new Address("192.168.86.30", 9090));

        addressList.saveAddresses();
        new Thread(client::listen).start();
        new Thread(() -> {
            serve.send("Transact");
        }).start();

    }

    void listen() {
        while (true) {

            try {
                Socket s = serverSocket.accept();
                Thread t = new SocketHandler(s);
                t.start();
                break;
//                new Thread(new SocketHandler(s)).start();
//                tpe.execute(new SocketHandler(s));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Peer.addressList.saveAddresses();
    }

    void send(String message) {
        Address[] list = addressList.addressList.toArray(new Address[0]);
        for (int i = 0; i < list.length; i++) {
            try {
                Socket s = new Socket(list[i].getIpAddress(), list[i].getPort());
                System.out.println("Socket established");
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                dos.writeUTF(message);

                System.out.println("=" + s.isClosed());
                System.out.println(s);
                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}

class SocketHandler extends Thread {

    private final Socket sock;

    public SocketHandler(Socket sock) {
        this.sock = sock;
        System.out.println("Socket created");
        System.out.println("/" + sock.isClosed());
        if (!Peer.alreadyHasAddress(((InetSocketAddress) sock.getRemoteSocketAddress()).getAddress().toString(), sock.getPort())) {
            System.out.println("best");
            Peer.addAddress(((InetSocketAddress) sock.getRemoteSocketAddress()).getAddress().toString(), sock.getPort());
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("-" + this.sock.isClosed());
            System.out.println(this.sock);
            DataInputStream in = new DataInputStream(sock.getInputStream());
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
            String operatingBusiness = in.readUTF();
            System.out.println(operatingBusiness);
            sock.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}

