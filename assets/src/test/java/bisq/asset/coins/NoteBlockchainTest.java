/*
 * This file is part of Haveno.
 *
 * Haveno is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Haveno is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Haveno. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.asset.coins;

import bisq.asset.AbstractAssetTest;

import org.junit.Test;

public class NoteBlockchainTest extends AbstractAssetTest {

    public NoteBlockchainTest() {
        super(new NoteBlockchain());
    }

    @Test
    public void testValidAddresses() {
        assertValidAddress("NaeSp6oTDFiGBZejFyYJvuCaSqWMnMM44E");
        assertValidAddress("NPCz6bsSnksLUGbp11hbHFWqFuVweEgMWM");
        assertValidAddress("NMNA6oMBExWhYoVEcD2BbcL6qmQ6rs7GN2");
    }

    @Test
    public void testInvalidAddresses() {
        assertInvalidAddress("1NMNA6oMBExWhYoVEcD2BbcL6qmQ6rs7GN2");
        assertInvalidAddress("NMNA6oMBExyWhYoVEcD2BbcL6qmQ6rs7GN2");
        assertInvalidAddress("NMNA6oMBExWhYoVEcD2BbcL6qmQ6rs7GN2#");
    }
}
