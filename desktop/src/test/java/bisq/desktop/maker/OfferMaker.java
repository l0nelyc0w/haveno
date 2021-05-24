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

package bisq.desktop.maker;

import bisq.core.offer.Offer;
import bisq.core.offer.OfferPayload;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Maker;
import com.natpryce.makeiteasy.Property;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;

public class OfferMaker {

    public static final Property<Offer, Long> price = new Property<>();
    public static final Property<Offer, Long> minAmount = new Property<>();
    public static final Property<Offer, Long> amount = new Property<>();
    public static final Property<Offer, String> baseCurrencyCode = new Property<>();
    public static final Property<Offer, String> counterCurrencyCode = new Property<>();
    public static final Property<Offer, OfferPayload.Direction> direction = new Property<>();
    public static final Property<Offer, Boolean> useMarketBasedPrice = new Property<>();
    public static final Property<Offer, Double> marketPriceMargin = new Property<>();
    public static final Property<Offer, String> id = new Property<>();

    public static final Instantiator<Offer> Offer = lookup -> new Offer(
            new OfferPayload(lookup.valueOf(id, "1234"),                     //id
                    0L,                                                      //date
                    null,                                                    //OwnerNodeAddress
                    null,                                                    //PubkeyRing
                    lookup.valueOf(direction, OfferPayload.Direction.BUY),   //Direction
                    lookup.valueOf(price, 100000L),                          //Price
                    lookup.valueOf(marketPriceMargin, 0.0),                  //MarketPriceMargin
                    lookup.valueOf(useMarketBasedPrice, false),              //MarketBasedPrice
                    lookup.valueOf(amount, 100000L),                         //Amount
                    lookup.valueOf(minAmount, 100000L),                      //MinAmount
                    lookup.valueOf(baseCurrencyCode, "XMR"),                 //BaseCurrencyCode
                    lookup.valueOf(counterCurrencyCode, "USD"),              //CounterCurrencyCode
                    null,           //ArbitratorNodeAddress
		    null,           //MediatorNodeAddress
                    "SEPA",         //PaymentMethodID
                    "",             //MakerPaymentAccountID
                    null,           //OfferFeePaymentTxID
                    null,           //CountryCode
                    null,          //AcceptedCountryCodes 
                    null,          //BankID 
                    null,           //AcceptedBankIds
                    "",             //VersionNr
                    0L,             //BlockHeightAtOfferCreation
                    0L,             //TxFee
                    0L,             //MakerFee
                    0L,             //BuyerSecurityDeposit
                    0L,             //SellerSecurityDeposit
                    0L,             //MaxTradeLimit
                    0L,             //MaxTradePeriod
                    false,          //UseAutoClose
                    false,          //UseReOpenAfterAutoClose
                    0L,            //LowerClosePrice 
                    0L,            //UpperClosePrice 
                    false, //IsPrivateOffer         
                    null,//HashOfChallenge
                    null,//ExtraDataMapMap
                    0));//ProtocolVersion

    public static final Maker<Offer> btcUsdOffer = a(Offer);
    public static final Maker<Offer> btcBCHCOffer = a(Offer).but(with(counterCurrencyCode, "BCHC"));
}
