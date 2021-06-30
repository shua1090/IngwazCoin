/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Network.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener {
    ServerSocket s;

    public Listener() {
        try {
            s = new ServerSocket(4200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {

        while (true) {
            try {
                Socket f = s.accept();
                Handler h = new Handler(f);
                new Thread(h).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
