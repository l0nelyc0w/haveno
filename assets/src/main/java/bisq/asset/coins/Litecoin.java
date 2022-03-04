/*
 * This file is part of Penumbra.
 *
 * Penumbra is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Penumbra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Penumbra. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.asset.coins;

import bisq.asset.Base58AddressValidator;
import bisq.asset.Coin;
import bisq.asset.NetworkParametersAdapter;

public class Litecoin extends Coin {
    public Litecoin() {
        super("Litecoin", "LTC", new Base58AddressValidator(new LitecoinMainNetParams()), Network.MAINNET);
    }

    public static class LitecoinMainNetParams extends NetworkParametersAdapter {
        public LitecoinMainNetParams() {
            this.addressHeader = 48;
            this.p2shHeader = 5;
        }
    }
}
