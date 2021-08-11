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
package dk.ku.di.discover.algorithms.bitparnek;

import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.dcrgraphs.BitDCRMarking;

public class DCRMetrics {

	// Method that checks if a graph accepts a certain trace.
	static public boolean acceptsTrace(BitDCRGraph g, List<Integer> t) { 
		BitDCRMarking m = g.defaultInitialMarking();
		for (int event : t) {			
			if (!g.enabled(m, event))
			{
				return false;
			}
			m = g.execute(m, event);
		}
		if (!m.isAccepting())
			return false;
		return true;
	}

	// Method that computes (trace-based) fitness for graph and log.
	static public double fitness(BitDCRGraph g, BitParNekLogAbstractions h) {
		int accepting = 0;
		int breaking = 0;

		for (final Entry<List<Integer>, Integer> kvp : h.traces.entrySet()) {
			List<Integer> trace = kvp.getKey();
			Integer multiplicity = kvp.getValue();
			if (acceptsTrace(g, trace))
				accepting += multiplicity;
			else
				breaking += multiplicity;
		}
		return ((double) accepting / (double) (accepting + breaking));
	}

	// Computes the precision of a flower model for a particular log
	static public double flowerPrecision(BitParNekLogAbstractions h) {
		BitDCRGraph g = new BitDCRGraph();
		for (String i : h.ActivityToID.keySet())
			g.addEvent(i);

		return precision(g, h);
	}

	// Computes escaping-edges based precision based on a dcr graph and log
	static public double precision(BitDCRGraph g, BitParNekLogAbstractions h) {

		HashMap<List<Integer>, Integer> enabledEdges = new HashMap<List<Integer>, Integer>();
		HashMap<List<Integer>, BitSet> visitedEdges = new HashMap<List<Integer>, BitSet>();
		HashMap<List<Integer>, Integer> visits = new HashMap<List<Integer>, Integer>();

		for (final Entry<List<Integer>, Integer> kvp : h.traces.entrySet()) {
			List<Integer> trace = kvp.getKey();
			Integer multiplicity = kvp.getValue();

			LinkedList<Integer> seen = new LinkedList<Integer>();
			BitDCRMarking m = g.defaultInitialMarking();
			for (int event : trace) {
				LinkedList<Integer> prefix = new LinkedList<Integer>(seen);
				if (!visitedEdges.containsKey(prefix)) {
					visitedEdges.put(prefix, new BitSet());
					enabledEdges.put(prefix, g.getAllEnabled(m).size());
					visits.put(prefix, 0);
				}
				visitedEdges.get(prefix).set(event);
				visits.put(prefix, visits.get(prefix) + multiplicity);
				// presume we can execute...
				m = g.execute(m, event);
				seen.add(event);
			}
		}
		long totalVisits = 0;
		double tally = 0;

		for (Entry<List<Integer>, BitSet> k : visitedEdges.entrySet()) {
			List<Integer> prefix = k.getKey();
			double vis;
			double seen;
			vis = k.getValue().cardinality();
			seen = enabledEdges.get(prefix);

			tally += vis / seen * visits.get(prefix);
			totalVisits += visits.get(prefix);
		}
		return (tally / totalVisits);
	}

	// Computes normalized precision for a graph and log
	static public double normalizedPrecision(BitDCRGraph g, BitParNekLogAbstractions h) {
		double fp = flowerPrecision(h);
		return (precision(g, h) - fp) / (1.0 - fp);
	}

	// Method that computes which activities contribute to what extend to escaping edges.
	static public HashMap<Integer, Double> escapingActivities(BitDCRGraph g, BitParNekLogAbstractions h) {

		HashMap<List<Integer>, Set<Integer>> enabledEdges = new HashMap<List<Integer>, Set<Integer>>();
		HashMap<List<Integer>, BitSet> visitedEdges = new HashMap<List<Integer>, BitSet>();
		HashMap<List<Integer>, Integer> visits = new HashMap<List<Integer>, Integer>();

		for (final Entry<List<Integer>, Integer> kvp : h.traces.entrySet()) {
			List<Integer> trace = kvp.getKey();
			Integer multiplicity = kvp.getValue();

			LinkedList<Integer> seen = new LinkedList<Integer>();
			BitDCRMarking m = g.defaultInitialMarking();
			for (int event : trace) {
				LinkedList<Integer> prefix = new LinkedList<Integer>(seen);
				if (!visitedEdges.containsKey(prefix)) {
					visitedEdges.put(prefix, new BitSet());
					enabledEdges.put(prefix, g.getAllEnabled(m));
					visits.put(prefix, 0);
				}
				visitedEdges.get(prefix).set(event);
				visits.put(prefix, visits.get(prefix) + multiplicity);
				// presume we can execute...
				m = g.execute(m, event);
				seen.add(event);
			}
		}

		double totalEnabled = 0;

		HashMap<Integer, Integer> tresult = new HashMap<Integer, Integer>();
		for (int i : g.getEvents()) {
			tresult.put(i, 0);
		}

		for (Entry<List<Integer>, BitSet> k : visitedEdges.entrySet()) {
			List<Integer> prefix = k.getKey();
			BitSet visited;
			Set<Integer> enabled;
			visited = k.getValue();
			enabled = enabledEdges.get(prefix);
			Integer v = visits.get(prefix);
			for (int i : enabled) {
				if (!visited.get(i))
					tresult.put(i, tresult.get(i) + v);
			}
			totalEnabled += (v * enabled.size());
		}

		HashMap<Integer, Double> result = new HashMap<Integer, Double>();

		for (Entry<Integer, Integer> k : tresult.entrySet()) {
			Integer e = k.getKey();
			double c = k.getValue();
			result.put(e, c / totalEnabled);
		}

		return result;
	}

}
