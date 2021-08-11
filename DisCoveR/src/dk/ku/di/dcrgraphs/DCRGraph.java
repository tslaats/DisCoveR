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
package dk.ku.di.dcrgraphs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

public class DCRGraph {
	protected HashSet<String> events = new HashSet<String>();
	private HashMap<String, HashSet<String>> labels = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> conditionsFor = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> milestonesFor = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> responsesTo = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> excludesTo = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> includesTo = new HashMap<String, HashSet<String>>();
	private HashMap<String, String> parents = new HashMap<String, String>();
	HashMap<String, HashSet<String>> children = new HashMap<String, HashSet<String>>();
	public DCRMarking marking;

	private HashMap<String, HashSet<String>> conditionsTo = new HashMap<String, HashSet<String>>();

	public void addUniqueEvent(String e) {
		addEvent(e);
		addLabel(e, e);
	}
	
	
	public void addEvent(String e) {
		events.add(e);
		conditionsFor.put(e, new HashSet<String>());
		conditionsTo.put(e, new HashSet<String>());
		milestonesFor.put(e, new HashSet<String>());
		responsesTo.put(e, new HashSet<String>());
		excludesTo.put(e, new HashSet<String>());
		includesTo.put(e, new HashSet<String>());
		children.put(e, new HashSet<String>());
	}
	
	public void addLabel(String event, String label) {
		if (!labels.containsKey(label))
			labels.put(label, new HashSet<String>());
		labels.get(label).add(event);
	}

	public void removeLabel(String event, String label) {
		if (labels.containsKey(label))
			labels.get(label).remove(event);
	}
	
	public String getLabel(String event) {
		for (String l : labels.keySet())
		{
			if (labels.get(l).contains(event))
				return l;
		}
		return "";
	}
	

	protected String randomAlphabeticString(int length) {
		char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		return sb.toString();
	}

	public String newEvent() {
		String newName = "g" + randomAlphabeticString(10);
		while (events.contains(newName))
			newName = "g" + randomAlphabeticString(10);
		addEvent(newName);
		return newName;
	}

	public void addCondition(String src, String trg) {
		conditionsFor.get(trg).add(src);
		conditionsTo.get(src).add(trg);
	}

	public void addMilestone(String src, String trg) {
		milestonesFor.get(trg).add(src);
	}

	public void addResponse(String src, String trg) {
		responsesTo.get(src).add(trg);
	}

	public void addExclude(String src, String trg) {
		excludesTo.get(src).add(trg);
	}

	public void addInclude(String src, String trg) {
		includesTo.get(src).add(trg);
	}

	public String name(String e) {
		return e;
	}

	public void addNesting(String child, String parent) {
		parents.put(child, parent);
		children.get(parent).add(child);
	}
	
	private HashSet<String> leavesOf(String parent)
	{
		HashSet<String> result = new HashSet<String>();
		
		if (children.get(parent).isEmpty())
		{
			result.add(parent);
		}
		else
		{
			for (String e : children.get(parent))
			result.addAll(leavesOf(e));
		}				
		return result;		
	}	
	
	
	// TODO: Implement a method for generating the semantic bitvector class.
	// Could do this on the fly, but it will be more work, and I don't see when we would need it yet.
	// I imagine normal usage is that one first builds the abstract class to represent their DCR Grpah, then compiles to the semantic class after.
	// TODO: Add flattening....
	public BitDCRGraph semantics()
	{
		BitDCRGraph result = new BitDCRGraph();		
		
		for (final String e : events) {
			if (children.get(e).isEmpty())
				result.addEvent(e);
		}
		
		for (Entry<String, HashSet<String>> r : conditionsFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())		
				for (String src2 : leavesOf(src))
					for (String trg2 : leavesOf(trg))
					{
						int s = result.getEvent(src2);
						int t = result.getEvent(trg2);
						result.addCondition(s, t);
					}				
		}
		
		for (Entry<String, HashSet<String>> r : responsesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				for (String src2 : leavesOf(src))
					for (String trg2 : leavesOf(trg))
					{
						int s = result.getEvent(src2);
						int t = result.getEvent(trg2);
						result.addResponse(s, t);
					}				
		}		
		
		for (Entry<String, HashSet<String>> r : excludesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				for (String src2 : leavesOf(src))
					for (String trg2 : leavesOf(trg))
					{
						int s = result.getEvent(src2);
						int t = result.getEvent(trg2);
						result.addExclude(s, t);
					}				
		}

		for (Entry<String, HashSet<String>> r : includesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				for (String src2 : leavesOf(src))
					for (String trg2 : leavesOf(trg))
					{
						int s = result.getEvent(src2);
						int t = result.getEvent(trg2);
						result.addInclude(s, t);
					}				
		}
		


		for (Entry<String, HashSet<String>> r : milestonesFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				for (String src2 : leavesOf(src))
					for (String trg2 : leavesOf(trg))
					{
						int s = result.getEvent(src2);
						int t = result.getEvent(trg2);
						result.addMilestone(s, t);
					}				
		}
		
		result.marking = this.marking.toBitMarking(result);
		
		return result;
	}
	
	public boolean canRunOpenWorld(List<String> trace)
	{
		BitDCRGraph g = semantics();
		LinkedList<Integer> t = new LinkedList<Integer>();
		
		//For testing only
		//HashSet<String> foundLabels = new HashSet<String>();  
		
		for (String l : trace)
		{
			if (this.labels.containsKey(l))
			{
				//foundLabels.add(l);
				String e = this.labels.get(l).iterator().next();
				t.add(g.getEvent(e));
			}
		}
		//System.out.print("Found Labels: ");
		//for (String s : foundLabels)
		//	System.out.print(s);
		//System.out.println();
		return (g.run(marking.toBitMarking(g), t) != null && g.run(marking.toBitMarking(g), t).isAccepting());
	}
	
	public boolean canRunClosedWorld(List<String> trace)
	{
		BitDCRGraph g = semantics();
		LinkedList<Integer> t = new LinkedList<Integer>();
		for (String l : trace)
		{
			if (!this.labels.containsKey(l))
				return false;
			String e = this.labels.get(l).iterator().next();
			t.add(g.getEvent(e));			
		}
		return (g.run(marking.toBitMarking(g), t) != null && g.run(marking.toBitMarking(g), t).isAccepting());
	}	
	
	
	public String whyInvalidRunOpenWorld(List<String> trace)
	{
		BitDCRGraph g = semantics();
		LinkedList<Integer> t = new LinkedList<Integer>();
		for (String l : trace)
		{
			if (this.labels.containsKey(l))
			{			
				String e = this.labels.get(l).iterator().next();
				t.add(g.getEvent(e));
			}
		}
		return g.whyInvalidRun(marking.toBitMarking(g), t);
	}	

	public String whyInvalidRunClosedWorld(List<String> trace)
	{
		BitDCRGraph g = semantics();
		LinkedList<Integer> t = new LinkedList<Integer>();
		for (String l : trace)
		{
			if (!this.labels.containsKey(l))
				return "Event '" + l + "' not found.";
			String e = this.labels.get(l).iterator().next();
			t.add(g.getEvent(e));
		}
		return g.whyInvalidRun(marking.toBitMarking(g), t);
	}		
	

	public DCRMarking defaultInitialMarking() {
		final DCRMarking result = new DCRMarking();
		for (final String e : events) {
			if (children.get(e).isEmpty())
				result.included.add(e);
		}
		return result;
	}
	
	public DCRMarking emptyInitialMarking() {
		final DCRMarking result = new DCRMarking();
		return result;
	}	

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR Graph {" + NEW_LINE);
		result.append(" Events: ");
		for (final String e : events) {
			result.append("'" + e + "'; ");
		}
		result.append(NEW_LINE);

		result.append(" Conditions: ");
		for (Entry<String, HashSet<String>> r : conditionsFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(src + " ->* " + trg + ";");
		}
		result.append(NEW_LINE);

		result.append(" Repsonses: ");
		for (Entry<String, HashSet<String>> r : responsesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " *-> " + trg + ";");
		}
		result.append(NEW_LINE);

		result.append(" Exclusions: ");
		for (Entry<String, HashSet<String>> r : excludesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " ->% " + trg + ";");
		}
		result.append(NEW_LINE);

		result.append(" Inclusions: ");
		for (Entry<String, HashSet<String>> r : includesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(src + " ->+ " + trg + ";");
		}
		result.append(NEW_LINE);

		result.append(" Milestones: ");
		for (Entry<String, HashSet<String>> r : milestonesFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(src + " -><> " + trg + ";");
		}
		result.append(NEW_LINE);

		if (marking != null) {
			result.append(" Marking: " + NEW_LINE);
			result.append(marking.toString());
		}

		// Note that Collections and Maps also override toString
		// result.append(" RelationID: " + relationID.toString() + NEW_LINE);
		result.append("}");

		return result.toString();
	}
	
	
	public String toSimpleString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
	
		for (final String e : events) {
			if (!this.marking.included.contains(e))
				result.append("%");
			if (this.marking.pending.contains(e))
				result.append("!");
			if (this.marking.executed.contains(e))
				result.append("�");			
			
			result.append(getLabel(e) + "; ");
		}
		
		for (Entry<String, HashSet<String>> r : conditionsFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(getLabel(src) + " ->* " + getLabel(trg) + ";");
		}
		
		for (Entry<String, HashSet<String>> r : responsesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(getLabel(src) + " *-> " + getLabel(trg) + ";");
		}
		
		for (Entry<String, HashSet<String>> r : excludesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(getLabel(src) + "getLabel( ->% " + getLabel(trg) + ";");
		}	
		
		for (Entry<String, HashSet<String>> r : includesTo.entrySet()) {
			String src = r.getKey();
			for (String trg : r.getValue())
				result.append(getLabel(src) + " ->+ " + getLabel(trg) + ";");
		}		
		
		for (Entry<String, HashSet<String>> r : milestonesFor.entrySet()) {
			String trg = r.getKey();
			for (String src : r.getValue())
				result.append(getLabel(src) + " -><> " + getLabel(trg) + ";");
		}

		return result.toString();
	}
	

	public Set<String> names() {
		return events;
	}

	public String labelsToEventIDs() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		
		BitDCRGraph g = semantics();
		
		for (String l : labels.keySet())
		{
			result.append(l + ": ");
			for (String e : labels.get(l))
			{
				int i = g.getEvent(e);
				if (i == -1)
					result.append("[" + e + " => NOT ATOMIC] ");		
				else
					result.append("[" + e + " => " + i + "] ");				
			}
			result.append(NEW_LINE);
		}	

		return result.toString();
	}
	
	static public DCRGraph fromCSVFormat(String fileName)
	{
		DCRGraph g = new DCRGraph();
		
	      
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = ",";

	        try {

	            br = new BufferedReader(new FileReader(fileName));
	            while ((line = br.readLine()) != null) {

	                // use comma as separator
	                String[] fields = line.split(cvsSplitBy);
	                String command = fields[0];
	                switch(command) {
	                case "EVENT":
	                {
	                	String e = fields[1].trim();
	                	g.addUniqueEvent(e);	                
	                	break;
	                }
	                case "CONDITION":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		g.addCondition(e1, e2);	                	
		                break;
	                }
	                case "RESPONSE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		g.addResponse(e1, e2);	                	
		                break;
	                }
	                case "INCLUDE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		g.addInclude(e1, e2);	                	
		                break;
	                }
	                case "EXCLUDE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		g.addExclude(e1, e2);	                	
		                break;
	                }	                
	              } 
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
		
		g.marking = g.defaultInitialMarking();
		return g;
	}
	

}
