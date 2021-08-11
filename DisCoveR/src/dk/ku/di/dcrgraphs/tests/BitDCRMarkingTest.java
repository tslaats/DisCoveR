/**
 * @author Tijs Slaats
 * 
 * DisCoveR: The DCR Graphs process miner.
 * Copyright (C) 2021 Tijs Slaats
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional terms under GNU AGPL version 3 section 7:
 * - By using the program, you provide the copyright holder with permission to mention, discuss, and describe your use of the program in academic publications and accept your responsibility to assist the copyright holder in such publications by providing relevant information.
 *  
 */
package dk.ku.di.dcrgraphs.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import dk.ku.di.dcrgraphs.BitDCRMarking;

class BitDCRMarkingTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void testHashEntries() {
		HashMap<BitDCRMarking, Integer> map = new HashMap<BitDCRMarking, Integer>();

		BitDCRMarking m1 = new BitDCRMarking();
		m1.executed.set(3);
		m1.included.set(4);
		m1.pending.set(5);

		BitDCRMarking m2 = new BitDCRMarking();
		m2.executed.set(3);
		m2.included.set(4);
		m2.pending.set(5);

		BitDCRMarking m3 = new BitDCRMarking();
		m3.executed.set(3);
		m3.included.set(7);
		m3.pending.set(5);

		assertEquals(0, map.size());
		map.put(m1, 1);
		assertEquals(1, map.size());
		map.put(m2, 2);
		assertEquals(1, map.size());
		map.put(m3, 3);
		assertEquals(2, map.size());
	}

	@Test
	void testBlockCond() {
		fail("Not yet implemented");
	}

	@Test
	void testBlockMilestone() {
		fail("Not yet implemented");
	}

	@Test
	void testIsAccepting() {
		fail("Not yet implemented");
	}

	@Test
	void testClone() {
		fail("Not yet implemented");
	}

}
