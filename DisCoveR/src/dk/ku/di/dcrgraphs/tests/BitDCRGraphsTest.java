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
 * Class description: 
 * Efficient implementation of the ParNek algorithm based on bitvector operations.
 * Contains a number of minor deviations from the original thesis work.
 *
 */
package dk.ku.di.dcrgraphs.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.dcrgraphs.BitDCRMarking;
import dk.ku.di.dcrgraphs.DCRGraph;
import dk.ku.di.discover.algorithms.bitparnek.BitParNek;
import junit.framework.Assert;

class BitDCRGraphsTest {

	@Test
	void testAddEvent() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("A"), 0);
	}

	@Test
	void testAddCondition() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		g.addCondition(0, 1);
	}

	@Test
	void testAddMilestone() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		g.addMilestone(0, 1);
	}

	@Test
	void testAddResponse() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		g.addResponse(0, 1);
	}

	@Test
	void testAddExclude() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		g.addExclude(0, 1);
	}

	@Test
	void testAddInclude() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		g.addInclude(0, 1);
	}

	@Test
	void testEnabled() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		BitDCRMarking m = g.defaultInitialMarking();
		g.addCondition(0, 1);
		assertTrue(g.enabled(m, 0));
		assertFalse(g.enabled(m, 1));
	}

	@Test
	void testGetAllEnabled() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		BitDCRMarking m = g.defaultInitialMarking();
		g.addCondition(0, 1);
		assertTrue(g.getAllEnabled(m).contains(0));
		assertTrue(g.getAllEnabled(m).contains(2));
		assertFalse(g.getAllEnabled(m).contains(1));
	}

	@Test
	void testExecute() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		assertEquals((int) g.addEvent("D"), 3);
		assertEquals((int) g.addEvent("E"), 4);
		g.addCondition(0, 1);
		g.addExclude(3, 0);
		g.addInclude(4, 0);
		g.addResponse(2, 0);
		g.addMilestone(0, 2);
		BitDCRMarking m = g.defaultInitialMarking();
		assertTrue(g.enabled(m, 0));
		assertTrue(g.enabled(m, 2));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
		assertFalse(g.enabled(m, 1));

		m = g.execute(m, 3);
		assertTrue(g.enabled(m, 1));
		assertTrue(g.enabled(m, 2));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
		assertFalse(g.enabled(m, 0));

		m = g.execute(m, 4);
		assertTrue(g.enabled(m, 0));
		assertTrue(g.enabled(m, 2));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
		assertFalse(g.enabled(m, 1));

		m = g.execute(m, 2);
		assertTrue(g.enabled(m, 0));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
		assertFalse(g.enabled(m, 1));
		assertFalse(g.enabled(m, 2));

		m = g.execute(m, 0);
		assertTrue(g.enabled(m, 0));
		assertTrue(g.enabled(m, 1));
		assertTrue(g.enabled(m, 2));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
	}

	@Test
	void testRun() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		assertEquals((int) g.addEvent("D"), 3);
		assertEquals((int) g.addEvent("E"), 4);

		g.addCondition(0, 1);
		g.addExclude(3, 0);
		g.addInclude(4, 0);
		g.addResponse(2, 0);
		g.addMilestone(0, 2);
		ArrayList<Integer> t = new ArrayList<Integer>(Arrays.asList(3, 4, 2, 0));

		BitDCRMarking m = g.defaultInitialMarking();

		m = g.run(m, t);
		assertTrue(g.enabled(m, 0));
		assertTrue(g.enabled(m, 1));
		assertTrue(g.enabled(m, 2));
		assertTrue(g.enabled(m, 3));
		assertTrue(g.enabled(m, 4));
	}

	@Test
	void testDefaultInitialMarking() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		// DCRMarking m = g.defaultInitialMarking();
	}

	@Test
	void testSpeedConstruct() throws Exception {
		BitDCRGraph g = new BitDCRGraph();

		for (Integer x = 0; x <= 10000; x++)
			g.addEvent("Event_" + x.toString());

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addCondition(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addExclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addInclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addResponse(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addMilestone(e1, e2);
		}

	}

	@Test
	void testSpeedEnabled() throws Exception {
		BitDCRGraph g = new BitDCRGraph();

		for (Integer x = 0; x <= 10000; x++)
			g.addEvent("Event_" + x.toString());

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addCondition(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addExclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addInclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addResponse(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addMilestone(e1, e2);
		}

		/*
		 * DCRMarking m = g.defaultInitialMarking();
		 * 
		 * int y = 0; for (Integer x = 0; x <= 1000000; x++) { int e =
		 * ThreadLocalRandom.current().nextInt(0, 10000 + 1); if (g.enabled(m, e)) y++;
		 * } //System.out.println(y);
		 */
	}

	@Test
	void testSpeedExecute() throws Exception {
		BitDCRGraph g = new BitDCRGraph();

		for (Integer x = 0; x <= 10000; x++)
			g.addEvent("Event_" + x.toString());

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addCondition(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addExclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addInclude(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addResponse(e1, e2);
		}

		for (Integer x = 0; x <= 2000; x++) {
			int e1 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			int e2 = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			g.addMilestone(e1, e2);
		}

		BitDCRMarking m = g.defaultInitialMarking();

		Integer y = 0;
		for (Integer x = 0; x <= 1000000; x++) {
			int e = ThreadLocalRandom.current().nextInt(0, 10000 + 1);
			if (g.enabled(m, e))
				y++;
			m = g.execute(m, e);
		}
		System.out.println("Executed: " + y.toString());
	}

	@Test
	void toStringTest() {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		assertEquals((int) g.addEvent("D"), 3);
		assertEquals((int) g.addEvent("E"), 4);

		g.addCondition(0, 1);
		g.addExclude(3, 0);
		g.addInclude(4, 0);
		g.addResponse(2, 0);
		g.addMilestone(0, 2);
		// ArrayList<Integer> t = new ArrayList<Integer>(Arrays.asList(3, 4, 2, 0));

		// DCRMarking m = g.defaultInitialMarking();

		System.out.println(g.toString());
	}
	
	
	
	
	@Test
	void testToDCRLanguage() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		assertEquals((int) g.addEvent("D"), 3);
		assertEquals((int) g.addEvent("E"), 4);
		g.addCondition(0, 1);
		g.addExclude(3, 0);
		g.addInclude(4, 0);
		g.addResponse(2, 0);
		g.addMilestone(0, 2);
		
		//g.toXML("D:\\test\\DisCoveR\\test.xml");
		System.out.println(g.toDCRLanguage());
	}
	
	
	@Test
	void testToDCRLanguageFile() throws Exception {
		BitDCRGraph g = new BitDCRGraph();
		assertEquals((int) g.addEvent("A"), 0);
		assertEquals((int) g.addEvent("B"), 1);
		assertEquals((int) g.addEvent("C"), 2);
		assertEquals((int) g.addEvent("D"), 3);
		assertEquals((int) g.addEvent("E"), 4);
		g.addCondition(0, 1);
		g.addExclude(3, 0);
		g.addInclude(4, 0);
		g.addResponse(2, 0);
		g.addMilestone(0, 2);
		
		//g.toXML("D:\\test\\DisCoveR\\test.xml");
		g.toDCRLanguage("D:\\test\\DisCoveR\\test.dcr");
	}	

	@Test
	void testToDCRLanguage2() throws Exception {
		
		BitParNek disco = new BitParNek();
		BitDCRGraph g = disco.mine("C:\\Users\\jfr820\\git\\dikudcr\\logs\\BPI2012.xes");
		//C:\Users\jfr820\git\dikudcr\logs\BPI2012.xes

		System.out.println(g.toDCRLanguage2());	
		
		//g.toXML("D:\\test\\DisCoveR\\test.xml");
		//g.toDCRLanguage("D:\\test\\DisCoveR\\SE.dcr");
	}	
	
	@Test
	void testToDCRCSV() throws Exception {
		
		BitParNek disco = new BitParNek();
		BitDCRGraph g = disco.mine("C:\\Users\\jfr820\\git\\dikudcr\\logs\\BPI2012.xes");
		//C:\Users\jfr820\git\dikudcr\logs\BPI2012.xes

		System.out.println(g.toCSVFormat());	
		
		//g.toXML("D:\\test\\DisCoveR\\test.xml");
		//g.toDCRLanguage("D:\\test\\DisCoveR\\SE.dcr");
	}
	
	
	
	@Test
	void testFromDCRCSV() throws Exception {
		
		BitParNek disco = new BitParNek();
		BitDCRGraph g = disco.mine("C:\\Users\\jfr820\\git\\dikudcr\\logs\\BPI2012.xes");
		//C:\Users\jfr820\git\dikudcr\logs\BPI2012.xes

		
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		BitDCRGraph g2 = BitDCRGraph.fromCSVFormat("./dcrcsv/test.csv");
		
		System.out.println(g.toCSVFormat());	
		System.out.println(g2.toCSVFormat());
		assertEquals(g.toCSVFormat(), g2.toCSVFormat());
		
		//g.toXML("D:\\test\\DisCoveR\\test.xml");
		//g.toDCRLanguage("D:\\test\\DisCoveR\\SE.dcr");
	}	
	

	

}
