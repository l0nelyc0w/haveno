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

package bisq.price.spot.providers;

import bisq.price.spot.ExchangeRate;
import bisq.price.spot.ExchangeRateProvider;

import org.knowm.xchange.bitbay.BitbayExchange;

import org.springframework.stereotype.Component;

import java.time.Duration;

import java.util.Set;

@Component
class Bitbay extends ExchangeRateProvider {

    public Bitbay() {
        super("BITBAY", "bitbay", Duration.ofMinutes(1));
    }

    @Override
    public Set<ExchangeRate> doGet() {
        // Supported fiat: EUR, GBP, PLN, USD
        // Supported alts: DASH, ETH, LTC
        return doGet(BitbayExchange.class);
    }
}
