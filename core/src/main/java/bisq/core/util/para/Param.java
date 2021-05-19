/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

// l0nelyc0w: trasfered from dao.governance 
package bisq.core.util.param;

import bisq.core.locale.Res;

import bisq.common.config.Config;
import bisq.common.proto.ProtoUtil;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public enum Param {
    UNDEFINED("N/A", ParamType.UNDEFINED),

    // Fee in BTC for a 1 BTC trade. 0.001 is 0.1%. @5000 USD/BTC price 0.1% fee is 5 USD.
    DEFAULT_MAKER_FEE_BTC("0.001", ParamType.BTC, 5, 5),
    DEFAULT_TAKER_FEE_BTC("0.003", ParamType.BTC, 5, 5),       // 0.2% of trade amount
    MIN_MAKER_FEE_BTC("0.00005", ParamType.BTC, 5, 5),         // 0.005% of trade amount
    MIN_TAKER_FEE_BTC("0.00005", ParamType.BTC, 5, 5),


    // BTC address as recipient for BTC trade fee once the arbitration system is replaced as well as destination for
    // the time locked payout tx in case the traders do not cooperate. Will be likely a donation address (Bisq, Tor,...)
    // but can be also a burner address if we prefer to burn the BTC
    
    // l0nelyc0w: what should be done with those addresses?
    @SuppressWarnings("SpellCheckingInspection")
    RECIPIENT_BTC_ADDRESS( "1BVxNn3T12veSK6DgqwU4Hdn7QHcDDRag7", ParamType.ADDRESS ); // mainnet

    @Getter
    private final String defaultValue;
    @Getter
    private final ParamType paramType;
    // If 0 we ignore check for max decrease
    @Getter
    private final double maxDecrease;
    // If 0 we ignore check for max increase
    @Getter
    private final double maxIncrease;

    Param(String defaultValue, ParamType paramType) {
        this(defaultValue, paramType, 0, 0);
    }

    Param(String defaultValue, ParamType paramType, double maxDecrease, double maxIncrease) {
        this.defaultValue = defaultValue;
        this.paramType = paramType;
        this.maxDecrease = maxDecrease;
        this.maxIncrease = maxIncrease;
    }

}
