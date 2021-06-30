/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network;

import java.io.*;
import java.util.HashSet;

class Address {
    private String ipAddress;
    private int port;

    public Address(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }


    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return ipAddress + ":" + port;
    }
}

public class AddressList {

    public HashSet<Address> addressList = new HashSet<>();

    public AddressList() {
    }

    public static void main(String[] args) {
        AddressList p = new AddressList();
//        p.addressList.add(0, new Node("192.168.86.110", 5050));
//        p.addressList.add(new Node("192.168.88.507", 6324));
        p.loadAddresses();

        for (Address n : p.addressList) {
            System.out.println(n);
        }
    }

    public void saveAddresses() {
        File addressFile = new File("addresses.txt");
        if (!addressFile.exists()) {
            try {
                addressFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter bwriter = new BufferedWriter(new FileWriter(addressFile))) {

            bwriter.write("==Addresses==\n");

            Address[] arrs = addressList.toArray(new Address[0]);

            for (int i = 0; i < addressList.size(); i++) {
                bwriter.write("-" + arrs[i].toString() + "\n");
            }

            bwriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAddresses() {
        File addressFile = new File("addresses.txt");
        if (addressFile.exists()) {
            addressList.clear();
            try (BufferedReader beader = new BufferedReader(new FileReader(addressFile))) {
                String line;

                while ((line = beader.readLine()) != null) {

                    if (line.charAt(0) == '-') {
                        line = line.substring(1);
                        String[] fullAddressArray = line.split(":");
                        addressList.add(new Address(fullAddressArray[0], Integer.parseInt(fullAddressArray[1])));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else throw new RuntimeException("Address container file (addresses.txt) not found in the " +
                "current directory.");
    }

    public synchronized boolean contains(Address node) {
        return this.addressList.contains(node);
    }

    public synchronized void add(Address node) {
        this.addressList.add(node);
    }

}
