/*
 *     A Proof-of-work cryptocurrency with some amount of centralization
 *     Copyright (C) 2021 Shynn Lawrence
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package Ingwaz.Mining;

public enum TransactionStatus {
    INVALID_SIGNATURE,
    INVALID_KEY,
    ANOTHER_TRANSACTION_SUBMITTED,
    INSUFFICIENT_FUNDS,
    ACCEPTED
}
