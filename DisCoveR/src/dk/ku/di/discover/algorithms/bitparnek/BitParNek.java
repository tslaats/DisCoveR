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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
//import org.qmpm.logtrie.exceptions.FileLoadException;
//import org.qmpm.logtrie.tools.XESTools;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.dcrgraphs.BitDCRMarking;

public class BitParNek {

	public BitDCRGraph mine(XLog xLog)
	{
		// Building the log abstractions used by the miner 
		BitParNekLogAbstractions helper = new BitParNekLogAbstractions();
		
		for (XTrace t : xLog) {
			helper.addTrace(t);
		}
		helper.finish();		
		
		// Actual mining 
		return this.mine(helper);
	}
	
	// Method for instantiating a log abstraction object that uses the same 
	// name -> id assignment for activities as another. Helpful for comparing results.  
	public BitParNekLogAbstractions helper(BitParNekLogAbstractions inspiration, XLog xLog) {
		BitParNekLogAbstractions helper = new BitParNekLogAbstractions();
		
		helper.borrowActivities(inspiration);

		for (XTrace t : xLog) {
			helper.addTrace(t);
		}

		helper.finish();

		return helper;

	}	
	
	// Method for directly getting a LogAbstraction
	public BitParNekLogAbstractions helper(XLog xLog) {
		BitParNekLogAbstractions helper = new BitParNekLogAbstractions();

		for (XTrace t : xLog) {
			helper.addTrace(t);
		}

		helper.finish();

		return helper;

	}

	// Main mining method
	public BitDCRGraph mine(BitParNekLogAbstractions helper) {
		BitDCRGraph g = new BitDCRGraph();
		BitSet selfExcludes = new BitSet();

		// Adding events
		for (Integer x = 0; x < helper.ActivityToID.size(); x++) {
			g.addEvent(helper.IDToActivity.get(x));
		}

		// Plain conditions based on precedence relation
		HashMap<Integer, BitSet> conditions = new HashMap<>();
		for (int i : helper.precedenceFor.keySet()) {
			conditions.put(i, helper.precedenceFor.get(i));
		}
		
		// Optimization of conditions based on transitive closure.
		this.optimize(conditions);
		
		// Adding conditions to the graph
		for (int i : conditions.keySet()) {
			for (int j : conditions.keySet()) {
				if (conditions.get(i).get(j)) {
					g.addCondition(j, i);
				}
			}
		}

		// Plain responses based on response relation
		HashMap<Integer, BitSet> responses = new HashMap<>();
		for (int i : helper.responseTo.keySet()) {
			responses.put(i, helper.responseTo.get(i));
		}
		
		// Optimization of responses based on transitive closure.
		this.optimize(responses);
		
		// Adding responses to the graph
		for (int i : responses.keySet()) {
			for (int j : responses.keySet()) {
				if (responses.get(i).get(j)) {
					g.addResponse(i, j);
				}
			}
		}

		// Adding self-exclude on any event that occurs at most once 
		// (iterating over the responseTo keySet out of lazy convenience 
		for (int i : helper.responseTo.keySet()) {
			if (helper.atMostOnce.get(i)) {
				g.addExclude(i, i);
				selfExcludes.set(i);
			}
		}

		// For each chainprecedence(j,i) we add: include(j,i) exclude(i,i) 
		// TODO: Change this to alternatePrecedence once the abstraction class supports these.
		for (int i : helper.chainPrecedenceFor.keySet()) {
			for (int j : helper.chainPrecedenceFor.keySet()) {
				if (helper.chainPrecedenceFor.get(i).get(j)) {
					g.addInclude(j, i);
					g.addExclude(i, i);
					selfExcludes.set(i);
				}
			}
		}

		HashMap<Integer, BitSet> excludes = new HashMap<>();

		// Adding additional excludes
		for (String s : helper.ActivityToID.keySet()) {
			int x = helper.ActivityToID.get(s);

			BitSet ex_x = new BitSet();

			// (x, y) in act_ex if y is never a successor for x
			BitSet act_ex = new BitSet();
			act_ex.set(0, helper.IDToActivity.size());
			act_ex.andNot(helper.successor.get(x));

			// TODO: check this, bit confused about the underlying intuition here...
			// x -->% y if: 
			// 1) y is never a successor for x
			// 2) y is never a predecessor for x
			// (The "mutual exclusions" as identified in the paper.)
			BitSet bs1 = (BitSet) act_ex.clone();
			bs1.andNot(helper.predecessor.get(x));
			ex_x.or(bs1);

			// x -->% y if: 
			// 1) y is never a successor for x
			// 2) and y is a predecessor for x
			// 3) and y does not exclude itself
			BitSet bs2 = (BitSet) act_ex.clone();
			bs2.and(helper.predecessor.get(x));
			bs2.andNot(selfExcludes);
			ex_x.or(bs2);

			ex_x.clear(x);

			excludes.put(x, ex_x);
		}


		// Removing redundant excludes 
		// Based on the observation that: 
		// if x --> % y, z -->% y and x is a predecessor for z, then it is 
		// unlikely that z -->% y is triggered
		//
		// Note:
		// Doesn't give exactly the same result as ParNek, seems to be caused by the order in which
		// constraints are considered.
		HashMap<Integer, BitSet> opt = new HashMap<>();

		for (String s : helper.ActivityToID.keySet()) {
			int x = helper.ActivityToID.get(s);
			BitSet ex_x = excludes.get(x);

			BitSet opt_x = new BitSet();

			for (String s2 : helper.ActivityToID.keySet()) {
				int y = helper.ActivityToID.get(s2);
				if (ex_x.get(y)) {					
					boolean excluded = false;
					BitSet pred_x = helper.predecessor.get(x);
					for (String s3 : helper.ActivityToID.keySet()) {
						int p = helper.ActivityToID.get(s3);

						if (pred_x.get(p)) {
							if (excludes.get(p).get(y) && x != p) {								
								excluded = true;
								break;
							}
						}
					}
					if (!excluded) {
						opt_x.set(y);
					}
				}
			}
			opt.put(x, opt_x);
		}


		// actually adding excludes to the graph
		for (int i : opt.keySet()) {
			for (int j : opt.keySet()) {
				if (opt.get(i).get(j)) {
					g.addExclude(i, j);
				}
			}
		}
		return g;

	}

	// Alternative optimization method for excludes, not used at the moment
	public void alternativeOptimizeExcludes(BitParNekLogAbstractions helper, HashMap<Integer, BitSet> excludes) {
		HashMap<Integer, BitSet> usedExcludes = new HashMap<>();
		for (int i : excludes.keySet()) {
			usedExcludes.put(i, new BitSet());
		}

		for (List<Integer> t : helper.traces.keySet()) {
			BitSet excluded = new BitSet();
			for (int e : t) {
				BitSet bs = (BitSet) excludes.get(e).clone();
				bs.andNot(excluded);
				usedExcludes.get(e).or(bs);
				excluded.or(excludes.get(e));
			}
		}

		for (int i : excludes.keySet()) {
			excludes.put(i, usedExcludes.get(i));
		}
	}


	// Method for finding additional conditions 
	// based on the interaction between conditions and excludes.
	public void findAdditionalConditions(BitParNekLogAbstractions h, BitDCRGraph g) {
		// setup possible conditions: predecessors - current conditions
		HashMap<Integer, BitSet> possibleConditions = new HashMap<>();
		for (final Entry<Integer, BitSet> kvp : h.predecessor.entrySet()) {
			BitSet pc = (BitSet) kvp.getValue().clone();
			pc.andNot(g.conditionsFor.get(kvp.getKey()));
			possibleConditions.put(kvp.getKey(), pc);
		}

		for (final Entry<List<Integer>, Integer> kvp : h.traces.entrySet()) {
			List<Integer> trace = kvp.getKey();

			BitDCRMarking m = g.defaultInitialMarking();
			BitSet seen = new BitSet();
			for (int event : trace) {
				BitSet ok = new BitSet();
				ok.set(0, h.ActivityToID.size());
				ok.andNot(m.included);
				ok.or(seen);
				possibleConditions.get(event).and(ok);
				m = g.execute(m, event);
				seen.set(event);
			}
		}

		for (final Entry<Integer, BitSet> kvp : g.conditionsFor.entrySet()) {
			possibleConditions.get(kvp.getKey()).or(kvp.getValue());
		}
		g.clearConditions();

		this.optimize(possibleConditions);

		for (final Entry<Integer, BitSet> kvp : possibleConditions.entrySet()) {
			Integer t = kvp.getKey();
			for (Integer s : h.ActivityToID.values()) {
				if (kvp.getValue().get(s)) {
					g.addCondition(s, t);
				}
			}
		}
		this.cleanConditions(h, g);
	}

	// Method for finding additional excludes and includes.
	// Improves precision while significantly sacrificing simplicity.
	public void heavyExclude(BitParNekLogAbstractions h, BitDCRGraph g, double t) {
		HashMap<Integer, Double> act = DCRMetrics.escapingActivities(g, h);
		BitSet nr = (BitSet) h.notRepeating.clone();
		nr.andNot(h.atMostOnce);
		for (Entry<Integer, Double> k : act.entrySet()) {
			Integer e = k.getKey();
			Double p = k.getValue();
			if (p >= t)
			{
				boolean needInclude = false;
				for (Integer s : h.ActivityToID.values()) {
					if (!h.directSuccessor.get(s).get(e)) {
						if (!g.hasInclude(s, e)) {
							g.addExclude(s, e);
							needInclude = true;
						}
					}
				}

				if (needInclude) {
					for (Integer s : h.ActivityToID.values()) {
						if (h.directPredecessor.get(e).get(s) && e != s) {
							g.addInclude(s, e);
							g.removeExclude(s, e);
						}
					}
				}
			}
		}
		this.clean(h, g);
	}

	// Method that cleans up unused excludes and includes
	public void clean(BitParNekLogAbstractions helper, BitDCRGraph g) {
		this.cleanExcludes(helper, g);
		this.cleanIncludes(helper, g); 
	}

	// Method that cleans up unused excludes
	public void cleanExcludes(BitParNekLogAbstractions helper, BitDCRGraph g) {
		HashMap<Integer, BitSet> usedExcludes = new HashMap<>();
		HashMap<Integer, BitSet> excludes = g.excludesTo;
		for (int i : excludes.keySet()) {
			usedExcludes.put(i, new BitSet());
		}

		for (List<Integer> t : helper.traces.keySet()) {
			BitDCRMarking m = g.defaultInitialMarking();
			for (int e : t) {
				BitSet bs = (BitSet) excludes.get(e).clone();
				bs.and(m.included);
				usedExcludes.get(e).or(bs);
				m = g.execute(m, e);
			}
		}

		for (int i : excludes.keySet()) {
			excludes.put(i, usedExcludes.get(i));
		}
	}

	// Method that cleans up unused includes
	public void cleanIncludes(BitParNekLogAbstractions helper, BitDCRGraph g) {
		HashMap<Integer, BitSet> usedIncludes = new HashMap<>();
		HashMap<Integer, BitSet> includes = g.includesTo;
		for (int i : includes.keySet()) {
			usedIncludes.put(i, new BitSet());
		}

		for (List<Integer> t : helper.traces.keySet()) {
			BitDCRMarking m = g.defaultInitialMarking();
			for (int e : t) {
				BitSet bs = (BitSet) includes.get(e).clone();
				bs.andNot(m.included);
				usedIncludes.get(e).or(bs);
				m = g.execute(m, e);
			}
		}

		for (int i : includes.keySet()) {
			includes.put(i, usedIncludes.get(i));
		}
	}

	// Method that cleans up unused conditions
	public void cleanConditions(BitParNekLogAbstractions helper, BitDCRGraph g) {
		HashMap<Integer, BitSet> usedConditions = new HashMap<>();
		HashMap<Integer, BitSet> conditionsFor = g.conditionsFor;
		for (int i : conditionsFor.keySet()) {
			usedConditions.put(i, new BitSet());
		}

		for (List<Integer> t : helper.traces.keySet()) {
			BitDCRMarking m = g.defaultInitialMarking();
			for (int e : t) {
				for (int i : conditionsFor.keySet()) {
					BitSet bs = (BitSet) conditionsFor.get(i).clone();
					bs.andNot(m.executed);
					bs.and(m.included);
					usedConditions.get(i).or(bs);
				}
				m = g.execute(m, e);
			}
		}

		for (int i : conditionsFor.keySet()) {
			conditionsFor.put(i, usedConditions.get(i));
		}
	}
	
	// Method that as precisely as possible mimics the optimization employed in ParNek. 
	public void optimize(HashMap<Integer, BitSet> relation) {

		List<Entry<Integer, BitSet>> sortedList = new ArrayList<>(relation.entrySet());

		Collections.sort(sortedList, (o1, o2) -> o2.getValue().cardinality() - o1.getValue().cardinality());


		for (int i = 0; i < sortedList.size(); i++) {
			for (int j = i; j < sortedList.size(); j++) {
				BitSet bs1 = (BitSet) sortedList.get(i).getValue().clone();
				BitSet bs2 = (BitSet) sortedList.get(j).getValue().clone();
				BitSet bs3 = (BitSet) bs2.clone();
				bs3.set(sortedList.get(j).getKey());
				BitSet bs4 = (BitSet) bs3.clone();
				bs4.and(bs1);
				;
				if (bs4.cardinality() == bs3.cardinality()) {
					sortedList.get(i).getValue().andNot(sortedList.get(j).getValue());
				}
			}
		}
	}

	// Method that mines from a path
	public BitDCRGraph mine(String path) {
		try {
			System.out.println("loading");
			return this.mine(XESTools.loadXES(path, true));
		} catch (FileLoadException e) {
			e.printStackTrace();
		}
		return null;
	}

	
	// Method for mining model recommendations for the DCR Graphs portal. 
	// TODO: Heavy code duplication with the main mine method, refactor this later!!
	public BitDCRGraph mineForModelRecommendation(XLog log) {
		
		BitParNekLogAbstractions helper = new BitParNekLogAbstractions();
		
		for (XTrace t : log) {
			helper.addTrace(t);
		}
		
		helper.finish();
		
		return mineForModelRecommendationInternal(helper);
	}
	
	
	public BitDCRGraph mineForModelRecommendation(BitParNekLogAbstractions helper) {
		
		return mineForModelRecommendationInternal(helper);
	}	
	
	
	// Method for mining model recommendations for the DCR Graphs portal. (based on simpler input format than XES) 
	// TODO: Heavy code duplication with the main mine method, refactor this later!!
	public BitDCRGraph mineForModelRecommendation(String s) {
		
		BitParNekLogAbstractions helper = new BitParNekLogAbstractions();
		
		// Parsing the string: (I'm sure this could be done more principled, but looking for a quick fix...)
		// remove any whitespace
		s = s.replaceAll("\\s+","");		
		// remove outer parenthesis
		s = s.substring(1, s.length()-1);
		// remove open and close parenthesis of the first and last trace
		s = s.substring(1, s.length()-1);
		
		String[] traces = s.split("\\),\\(");
		
		for (String t : traces)
		{
			helper.addStringList(new ArrayList<String>(Arrays.asList(t.split(","))));
		}
		
		helper.finish();
		
		return mineForModelRecommendationInternal(helper);
	}	

	private BitDCRGraph mineForModelRecommendationInternal(BitParNekLogAbstractions helper) {
		BitDCRGraph g = new BitDCRGraph();
		BitSet selfExcludes = new BitSet();
		
		for (Integer x = 0; x < helper.ActivityToID.size(); x++)
		{
			g.addEvent(helper.IDToActivity.get(x));
		}
		
				
		HashMap<Integer, BitSet> conditions = new HashMap<Integer, BitSet>();		
		for (int i : helper.precedenceFor.keySet())
		{
			conditions.put(i, helper.precedenceFor.get(i));			
		}
				
		optimize(conditions);		
		for (int i : conditions.keySet())
		{
			for (int j : conditions.keySet())
				if (conditions.get(i).get(j)) g.addCondition(j, i);
		}		
				
		HashMap<Integer, BitSet> responses = new HashMap<Integer, BitSet>();		
		for (int i : helper.responseTo.keySet())
		{
			responses.put(i, helper.responseTo.get(i));			
		}		
		optimize(responses);
		for (int i : responses.keySet())
		{
			for (int j : responses.keySet())
				if (responses.get(i).get(j)) g.addResponse(i, j);
		}
		
		
		for (int i : helper.responseTo.keySet())
		{
			if (helper.atMostOnce.get(i)) 
			{
				g.addExclude(i, i);
				selfExcludes.set(i);				
			}
		}
		
		for (int i : helper.chainPrecedenceFor.keySet())
		{
			for (int j : helper.chainPrecedenceFor.keySet())
				if (helper.chainPrecedenceFor.get(i).get(j)) 
				{
					g.addInclude(j, i);
					g.addExclude(i, i);	
					selfExcludes.set(i);
				}
		}		
	
		return g;
	}
	

}
