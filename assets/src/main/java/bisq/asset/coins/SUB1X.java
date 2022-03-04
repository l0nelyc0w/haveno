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

import bisq.asset.AddressValidationResult;
import bisq.asset.Base58AddressValidator;
import bisq.asset.Coin;
import bisq.asset.NetworkParametersAdapter;

public class SUB1X extends Coin {

    public SUB1X() {
        super("SUB1X", "SUB1X", new SUB1XAddressValidator());
    }


    public static class SUB1XAddressValidator extends Base58AddressValidator {

        public SUB1XAddressValidator() {
            super(new SUB1XParams());
        }

        @Override
        public AddressValidationResult validate(String address) {
            if (!address.matches("^[Z][a-km-zA-HJ-NP-Z1-9]{24,33}$"))
                return AddressValidationResult.invalidStructure();

            return super.validate(address);
        }
    }


    public static class SUB1XParams extends NetworkParametersAdapter {

        public SUB1XParams() {
            addressHeader = 80;
            p2shHeader = 13;
        }
    }
}
