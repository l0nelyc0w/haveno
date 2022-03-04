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

package bisq.cli.opts;


import joptsimple.OptionSpec;

import static bisq.cli.opts.OptLabel.OPT_ACCOUNT_NAME;
import static bisq.cli.opts.OptLabel.OPT_ADDRESS;
import static bisq.cli.opts.OptLabel.OPT_CURRENCY_CODE;
import static bisq.cli.opts.OptLabel.OPT_TRADE_INSTANT;

public class CreateCryptoCurrencyPaymentAcctOptionParser extends AbstractMethodOptionParser implements MethodOpts {

    final OptionSpec<String> accountNameOpt = parser.accepts(OPT_ACCOUNT_NAME, "crypto currency account name")
            .withRequiredArg();

    final OptionSpec<String> currencyCodeOpt = parser.accepts(OPT_CURRENCY_CODE, "crypto currency code")
            .withRequiredArg();

    final OptionSpec<String> addressOpt = parser.accepts(OPT_ADDRESS, "address")
            .withRequiredArg();

    final OptionSpec<Boolean> tradeInstantOpt = parser.accepts(OPT_TRADE_INSTANT, "create trade instant account")
            .withOptionalArg()
            .ofType(boolean.class)
            .defaultsTo(Boolean.FALSE);

    public CreateCryptoCurrencyPaymentAcctOptionParser(String[] args) {
        super(args);
    }

    public CreateCryptoCurrencyPaymentAcctOptionParser parse() {
        super.parse();

        // Short circuit opt validation if user just wants help.
        if (options.has(helpOpt))
            return this;

        if (!options.has(accountNameOpt) || options.valueOf(accountNameOpt).isEmpty())
            throw new IllegalArgumentException("no payment account name specified");

        if (!options.has(currencyCodeOpt) || options.valueOf(currencyCodeOpt).isEmpty())
            throw new IllegalArgumentException("no currency code specified");
        return this;
    }

    public String getAccountName() {
        return options.valueOf(accountNameOpt);
    }

    public String getCurrencyCode() {
        return options.valueOf(currencyCodeOpt);
    }

    public String getAddress() {
        return options.valueOf(addressOpt);
    }

    public boolean getIsTradeInstant() {
        return options.valueOf(tradeInstantOpt);
    }
}
