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
import java.util.Map;
import java.util.Map.Entry;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;

public class BitParNekLogAbstractions {
	protected boolean extendedVersion = true;
	
	protected HashMap<String, Integer> ActivityToID = new HashMap<>();
	protected HashMap<Integer, String> IDToActivity = new HashMap<>();
	
	public boolean containsActivity(String a)
	{
		return ActivityToID.containsKey(a);
	}
	
	public Integer getActivityID(String a)
	{
		return ActivityToID.get(a);
	}

	protected HashMap<Integer, BitSet> chainPrecedenceFor = new HashMap<>();
	protected HashMap<Integer, BitSet> alternatePrecedence = new HashMap<>();
	protected HashMap<Integer, BitSet> precedenceFor = new HashMap<>();
	protected HashMap<Integer, BitSet> responseTo = new HashMap<>();
	protected HashMap<Integer, BitSet> occursWith = new HashMap<>();
	protected HashMap<Integer, BitSet> predecessor = new HashMap<>();
	protected HashMap<Integer, BitSet> directPredecessor = new HashMap<>();
	protected HashMap<Integer, BitSet> successor = new HashMap<>();
	protected HashMap<Integer, BitSet> directSuccessor = new HashMap<>();
	// protected HashMap<Integer, BitSet> occursTogether = new HashMap<Integer,
	// BitSet>();
	protected BitSet atMostOnce = new BitSet();
	protected BitSet atLeastOnce = new BitSet();
	protected BitSet notRepeating = new BitSet();

	protected HashMap<List<Integer>, Integer> traces = new HashMap<>();

	// From XTrace to int list.
	public void addTrace(final XTrace trace) {
		LinkedList<Integer> t = new LinkedList<>();
		for (XEvent event : trace) {
			if (event != null) {
				try {
					String e = XESTools.xEventName(event);
					if (!this.ActivityToID.containsKey(e)) {
						this.addActivity(e);
					}
					int i = this.ActivityToID.get(e);
					t.add(i);
				} catch (LabelTypeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.addTrace(t);
	}

	// From string list to int list.
	public void addStringList(List<String> trace) {
		LinkedList<Integer> t = new LinkedList<>();
		for (String e : trace) {
			if (!this.ActivityToID.containsKey(e)) {
				this.addActivity(e);
			}
			int i = this.ActivityToID.get(e);
			t.add(i);
		}
		this.addTrace(t);
	}

	public void addTrace(List<Integer> t)

	{
		if (this.traces.containsKey(t)) {
			this.traces.put(t, this.traces.get(t) + 1);
		} else {
			this.traces.put(t, 1);
		}
	}

	public void parse() {
		this.initActivities();
		for (List<Integer> t : this.traces.keySet()) {
			this.parseTrace(t);
		}
	}

	public void addActivity(String a) {
		int i = this.ActivityToID.size();
		this.ActivityToID.put(a, i);
		this.IDToActivity.put(i, a);
	}
	
	public void borrowActivities(BitParNekLogAbstractions inspiration)
	{
		for (int i : inspiration.IDToActivity.keySet())
		{
			String a = inspiration.IDToActivity.get(i);
			this.ActivityToID.put(a, i);
			this.IDToActivity.put(i, a);		
		}
	}
	

	public void initActivities() {

		for (int i : this.IDToActivity.keySet()) {
			// TODO: not implemented yet (also not used by the miner at the moment, but useful for planned future improvements.)
			this.alternatePrecedence.put(i, new BitSet());
			this.occursWith.put(i, new BitSet());

			// implemented:

			this.atMostOnce.set(i);
			this.notRepeating.set(i);

			this.precedenceFor.put(i, new BitSet());
			this.precedenceFor.get(i).set(0, this.IDToActivity.size());
			this.precedenceFor.get(i).clear(i);

			this.chainPrecedenceFor.put(i, new BitSet());
			this.chainPrecedenceFor.get(i).set(0, this.IDToActivity.size());
			this.chainPrecedenceFor.get(i).clear(i);

			this.responseTo.put(i, new BitSet());
			this.responseTo.get(i).set(0, this.IDToActivity.size());
			this.responseTo.get(i).clear(i);

			this.predecessor.put(i, new BitSet());
			this.successor.put(i, new BitSet());
			this.directPredecessor.put(i, new BitSet());
			this.directSuccessor.put(i, new BitSet());
		}

	}

	// I'm not sure if the string -> integer transformation will just take more time
	// or not. May want to change this later.
	public void parseTrace(List<Integer> t) {
		// note: not saving traces may save significant time if there is not much
		// overlap

		BitSet localAtLeastOnce = new BitSet();

		HashMap<Integer, BitSet> seenOnlyBefore = new HashMap<>();

		int last_i = -1;
		for (int i : t) {
			// Note: this always needs to be placed before we update localAtLeastOnce
			this.predecessor.get(i).or(localAtLeastOnce);

			// At most once
			if (localAtLeastOnce.get(i)) {
				this.atMostOnce.clear(i);
			}
			localAtLeastOnce.set(i);

			// Precedence
			this.precedenceFor.get(i).and(localAtLeastOnce);

			// chainPrecedence
			if (last_i != -1) {
				BitSet bs = new BitSet();
				bs.set(last_i);
				this.chainPrecedenceFor.get(i).and(bs);
			} else {
				this.chainPrecedenceFor.get(i).and(new BitSet());
			}

			// response
			if (this.responseTo.get(i).cardinality() > 0) {
				seenOnlyBefore.put(i, (BitSet) localAtLeastOnce.clone());
			}
			for (int j : seenOnlyBefore.keySet()) {
				seenOnlyBefore.get(j).clear(i);
			}

			if (this.extendedVersion) {
				// notrepeating
				if (last_i == i) {
					this.notRepeating.clear(i);
				}
				if (last_i != -1) {
					this.directPredecessor.get(i).set(last_i);
					this.directSuccessor.get(last_i).set(i);
				}
			}

			last_i = i;
		}

		// responses
		for (int j : seenOnlyBefore.keySet()) {
			this.responseTo.get(j).and(localAtLeastOnce);
			this.responseTo.get(j).andNot(seenOnlyBefore.get(j));
		}

		this.atLeastOnce.or(localAtLeastOnce);
		// TODO: use localAtLeastOnce to fill occursWith
	}

	public void finish() {
		this.parse();

		for (int i : this.predecessor.keySet()) {
			for (int j : this.predecessor.keySet()) {
				if (this.predecessor.get(i).get(j)) {
					this.successor.get(j).set(i);
				}
			}
		}

	}


	public Map<Integer, String> getIDActivityMap() {
		return this.IDToActivity;
	}

	public HashMap<String, Integer> getActivityIDMap() {
		return this.ActivityToID;
	}

	public Map<List<Integer>, Integer> getTraces() {
		return this.traces;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR Discovery Helper {" + NEW_LINE);
		result.append(" Precedence: ");
		for (final Entry<Integer, BitSet> entry : this.precedenceFor.entrySet()) {
			int key = entry.getKey();
			BitSet value = entry.getValue();
			result.append(key + ": " + value.toString());
			result.append(NEW_LINE);
		}
		result.append(NEW_LINE);

		result.append("Chain Precedence: ");
		for (final Entry<Integer, BitSet> entry : this.chainPrecedenceFor.entrySet()) {
			int key = entry.getKey();
			BitSet value = entry.getValue();
			result.append(key + ": " + value.toString());
			result.append(NEW_LINE);
		}
		result.append(NEW_LINE);

		result.append("Responses: ");
		for (final Entry<Integer, BitSet> entry : this.responseTo.entrySet()) {
			int key = entry.getKey();
			BitSet value = entry.getValue();
			result.append(key + ": " + value.toString());
			result.append(NEW_LINE);
		}
		result.append(NEW_LINE);

		result.append("At least once: ");
		result.append(this.atLeastOnce.toString());
		result.append(NEW_LINE);

		result.append("At most once: ");
		result.append(this.atMostOnce.toString());
		result.append(NEW_LINE);

		result.append(NEW_LINE);

		result.append("Traces: ");
		result.append(NEW_LINE);
		for (final Entry<List<Integer>, Integer> entry : this.traces.entrySet()) {
			List<Integer> key = entry.getKey();
			Integer value = entry.getValue();
			result.append(value + ": " + key.toString());
			result.append(NEW_LINE);
		}
		result.append(NEW_LINE);

		return result.toString();
	}

}
