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

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.persistence.jaxb.JAXBContextProperties;

import dk.ku.di.discover.modelrecommendation.DCRModelAdvice;
import dk.ku.di.discover.modelrecommendation.DCRModelRecommendation;
import dk.ku.di.discover.modelrecommendation.RelationAdvice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javafx.util.Pair;

//
public class BitDCRGraph {

	String title;
	BitDCRMarking marking = new BitDCRMarking();	
	protected HashMap<Integer, String> idToEvent = new HashMap<>();
	protected HashMap<String, Integer> eventToId = new HashMap<>();
	public HashMap<Integer, BitSet> conditionsFor = new HashMap<>();
	public HashMap<Integer, BitSet> conditionsTo = new HashMap<>();
	private HashMap<Integer, BitSet> milestonesFor = new HashMap<>();
	public HashMap<Integer, BitSet> responsesTo = new HashMap<>();
	public HashMap<Integer, BitSet> excludesTo = new HashMap<>();
	public HashMap<Integer, BitSet> includesTo = new HashMap<>();

	public Integer addEvent(String l) {
		if (this.eventToId.keySet().contains(l)) {
			return this.eventToId.get(l);

		} else {

			Integer e = this.idToEvent.keySet().size();

			this.idToEvent.put(e, l);
			this.eventToId.put(l, e);

			this.conditionsFor.put(e, new BitSet());
			this.conditionsTo.put(e, new BitSet());
			this.milestonesFor.put(e, new BitSet());
			this.responsesTo.put(e, new BitSet());
			this.excludesTo.put(e, new BitSet());
			this.includesTo.put(e, new BitSet());

			return e;
		}
	}

	public void clearConditions() {
		for (BitSet b : this.conditionsFor.values()) {
			b.clear();
		}

		for (BitSet b : this.conditionsTo.values()) {
			b.clear();
		}
	}

	public void addCondition(int src, int trg) {
		this.conditionsFor.get(trg).set(src);
		this.conditionsTo.get(src).set(trg);
	}

	public void addMilestone(int src, int trg) {
		this.milestonesFor.get(trg).set(src);
	}

	public void addResponse(int src, int trg) {
		this.responsesTo.get(src).set(trg);
	}

	public void addExclude(int src, int trg) {
		this.excludesTo.get(src).set(trg);
	}

	public void addInclude(int src, int trg) {
		this.includesTo.get(src).set(trg);
	}

	public void removeExclude(int src, int trg) {
		this.excludesTo.get(src).clear(trg);
	}

	public boolean hasInclude(int src, int trg) {
		return this.includesTo.get(src).get(trg);
	}

	public Boolean enabled(final BitDCRMarking marking, final int event) {
		if (!this.idToEvent.keySet().contains(event)) {
			return false;
		} // throw new Exception("Trying to execute invalid event."); }
		
		// check included
		if (!marking.included.get(event)) {
			return false;
		}
		
		// check conditions
		if (this.conditionsFor.get(event).intersects(marking.blockCond())) {
			return false;
		}
		
		// check milestones
		if (this.milestonesFor.get(event).intersects(marking.blockMilestone())) {
			return false;
		}
		return true;
	}
	

	
	public String whyNotEnabled(final BitDCRMarking marking, final int event) {
		if (!this.idToEvent.keySet().contains(event)) {
			return "Event " + this.idToEvent.get(event) + " unkown.";
		} // throw new Exception("Trying to execute invalid event."); }
		
		// check included
		if (!marking.included.get(event)) {
			return "Event " + this.idToEvent.get(event) + " not included.";
		}
		
		// check conditions
		if (this.conditionsFor.get(event).intersects(marking.blockCond())) {
			String reason = "Event " + this.idToEvent.get(event) + " has at least one blocking condition: ";
			for (int i = marking.blockCond().nextSetBit(0); i != -1; i = marking.blockCond().nextSetBit(i + 1)) {
				reason += this.idToEvent.get(i) + " ";
			}							
			return  reason;
		}
		
		// check milestones
		if (this.milestonesFor.get(event).intersects(marking.blockMilestone())) {
			String reason = "Event " + this.idToEvent.get(event) + " has at least one blocking milestone: ";
			for (int i = marking.blockMilestone().nextSetBit(0); i != -1; i = marking.blockMilestone().nextSetBit(i + 1)) {
				reason += this.idToEvent.get(i) + " ";
			}				
			return  reason;
		}
		return "Event " + this.idToEvent.get(event) + " IS ENABLED.";
	}	

	public Set<Integer> getAllEnabled(BitDCRMarking m) {
		HashSet<Integer> result = new HashSet<>();
		// TODO: could probably do a cool matrix transformation here 
		for (int e : this.idToEvent.keySet()) {
			if (this.enabled(m, e)) {
				result.add(e);
			}
		}

		return result;
	}

	public BitDCRMarking execute(final BitDCRMarking marking, final int event) {
		if (!this.idToEvent.keySet().contains(event)) {
			return marking;
		}

		if (!this.enabled(marking, event)) {
			return marking;
		}

		BitDCRMarking result = marking.clone();

		if (!this.conditionsTo.get(event).isEmpty()) {
			result.executed.set(event);
		}

		result.pending.clear(event);
		result.pending.or(this.responsesTo.get(event));
		result.included.andNot(this.excludesTo.get(event));
		result.included.or(this.includesTo.get(event));

		return result;
	}

	public BitDCRMarking run(final BitDCRMarking marking, List<Integer> trace) {
		BitDCRMarking m = marking.clone();
		for (int e : trace) {
			if (!this.enabled(m, e)) {
				return null;
			} else {
				m = this.execute(m, e);
			}
		}
		return m;
	}
	
	public String whyInvalidRun(final BitDCRMarking marking, List<Integer> trace) {
		BitDCRMarking m = marking.clone();
		for (int e : trace) {
			if (!this.enabled(m, e)) {
				return "Event: " + this.idToEvent.get(e) + " not enabled because: " + whyNotEnabled(m, e);
			} else {
				m = this.execute(m, e);
			}
		}
		if (!m.isAccepting())
		{
			String reason = "Run does not end in accepting marking, the following are included pending responses: ";
			for (int i = m.blockMilestone().nextSetBit(0); i != -1; i = m.blockMilestone().nextSetBit(i + 1)) {
				reason += this.idToEvent.get(i) + " ";
			}		
			
			return reason;
			//return "Run does not end in accepting marking, the following are included pending responses: " + m.blockMilestone().toString();
		}
		
		return "VALID RUN";
	}	
	
	

	public BitDCRMarking defaultInitialMarking() {
		final BitDCRMarking result = new BitDCRMarking();
		result.included.set(0, this.idToEvent.keySet().size());
		return result;
	}

	public Set<Pair<Integer, Integer>> relationAsPairs(HashMap<Integer, BitSet> rel) {
		HashSet<Pair<Integer, Integer>> result = new HashSet<>();

		for (int i : rel.keySet()) {
			for (int j : rel.keySet()) {
				if (rel.get(i).get(j)) {
					result.add(new Pair<>(i, j));
				}
			}
		}
		return result;
	}

	public String relationAsString(HashMap<Integer, BitSet> rel) {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		for (int i : rel.keySet()) {
			result.append(i);
			result.append(" : {");
			for (int j : rel.keySet()) {
				if (rel.get(i).get(j)) {
					result.append(j);
					result.append(" ");
				}
			}
			result.append("}");
			result.append(NEW_LINE);
		}

		return result.toString();
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR Graph {" + NEW_LINE);
		result.append(" Title: " + this.title + NEW_LINE);
		result.append(" Stats: ");
		result.append(NEW_LINE);
		result.append(" #Events: " + this.idToEvent.size() + NEW_LINE);
		result.append(" #Conditions: " + this.relationAsPairs(this.conditionsFor).size() + NEW_LINE);
		result.append(" #Responses: " + this.relationAsPairs(this.responsesTo).size() + NEW_LINE);
		result.append(" #Includes: " + this.relationAsPairs(this.includesTo).size() + NEW_LINE);
		result.append(" #Excludes: " + this.relationAsPairs(this.excludesTo).size() + NEW_LINE);
		result.append(" #Relations: " + (this.relationAsPairs(this.conditionsFor).size() + this.relationAsPairs(this.responsesTo).size() + this.relationAsPairs(this.includesTo).size() + this.relationAsPairs(this.excludesTo).size()) + NEW_LINE);
		result.append(" Event IDs: " + this.idToEvent.keySet() + NEW_LINE);
		result.append(" IdToEvents: ");
		for (final int e : this.idToEvent.keySet()) {
			result.append(e + ": " + this.idToEvent.get(e) + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Marking:" + NEW_LINE);
		result.append("   Executed: " + this.marking.executed + NEW_LINE);
		result.append("   Included: " + this.marking.included + NEW_LINE);
		result.append("   Pending:  " + this.marking.pending + NEW_LINE);

		result.append(" Conditions: " + NEW_LINE);
		result.append(this.relationAsString(this.conditionsTo));
		result.append(NEW_LINE);

		result.append(" Responses: " + NEW_LINE);
		result.append(this.relationAsString(this.responsesTo));
		result.append(NEW_LINE);

		result.append(" Excludes: " + NEW_LINE);
		result.append(this.relationAsString(this.excludesTo));
		result.append(NEW_LINE);

		result.append(" Includes: " + NEW_LINE);
		result.append(this.relationAsString(this.includesTo));
		result.append(NEW_LINE);

		result.append("}");

		return result.toString();
	}

	public Set<Integer> getEvents() {
		return this.idToEvent.keySet();
	}

	public int numOfRelations() {
		return this.relationAsPairs(this.conditionsFor).size() + this.relationAsPairs(this.responsesTo).size() + this.relationAsPairs(this.includesTo).size() + this.relationAsPairs(this.excludesTo).size();
	}
	
	public String getLabel(int event)
	{
		return idToEvent.get(event);
	}
	
	public int getEvent(String event)
	{
		if(!eventToId.containsKey(event))
			return -1;
		return eventToId.get(event);
	}	
	
	
	public void toDCRLanguage(String fileName)
	{
		/*
		try (PrintWriter out = new PrintWriter(fileName)) {
		    out.println(toDCRLanguage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(
		    new FileOutputStream(fileName), "UTF-8"));
			out.write(toDCRLanguage());
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void toModelRecommendationFormat(String fileName)
	{
	   JAXBContext jc;
	   try {
			 Map<String, Object> properties = new HashMap<String, Object>(2);
			 properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			 properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			jc = JAXBContext.newInstance("dk.ku.di.discover.modelrecommendation", DCRModelAdvice.class.getClassLoader(), properties);
			Marshaller m = jc.createMarshaller();
		   File f = new File(fileName);
		   DCRModelAdvice a = graphToRecommendation(this);
		   m.marshal(a, f);
	   } catch (JAXBException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }		
	 }

	private DCRModelAdvice graphToRecommendation(BitDCRGraph g) {
		DCRModelAdvice result = new DCRModelAdvice();
		
		for (final Entry<Integer, BitSet> kvp : g.conditionsTo.entrySet()) {
			int source = kvp.getKey(); 
			BitSet targets = kvp.getValue();
			for (Integer i : g.getEvents())
				if (targets.get(i))
				{
					result.relations.add(new RelationAdvice("condition", g.getLabel(source), g.getLabel(i)));
				}			
		}
		
		for (final Entry<Integer, BitSet> kvp : g.responsesTo.entrySet()) {
			int source = kvp.getKey(); 
			BitSet targets = kvp.getValue();
			for (Integer i : g.getEvents())
				if (targets.get(i))
				{
					result.relations.add(new RelationAdvice("response", g.getLabel(source), g.getLabel(i)));
				}			
		}
		
		
		for (final Entry<Integer, BitSet> kvp : g.includesTo.entrySet()) {
			int source = kvp.getKey(); 
			BitSet targets = kvp.getValue();
			for (Integer i : g.getEvents())
				if (targets.get(i))
				{
					result.relations.add(new RelationAdvice("include", g.getLabel(source), g.getLabel(i)));
				}			
		}				
		
		
		for (final Entry<Integer, BitSet> kvp : g.excludesTo.entrySet()) {
			int source = kvp.getKey(); 
			BitSet targets = kvp.getValue();
			for (Integer i : g.getEvents())
				if (targets.get(i))
				{
					result.relations.add(new RelationAdvice("exclude", g.getLabel(source), g.getLabel(i)));
				}			
		}						
		
		return result;
	}	
	
	static public BitDCRGraph fromCSVFormat(String fileName)
	{
		BitDCRGraph g = new BitDCRGraph();
		
	      
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
	                	g.addEvent(e);	                
	                	break;
	                }
	                case "CONDITION":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		int src = g.getEvent(e1);
	            		int trg = g.getEvent(e2);
	            		g.addCondition(src, trg);	                	
		                break;
	                }
	                case "RESPONSE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		int src = g.getEvent(e1);
	            		int trg = g.getEvent(e2);
	            		g.addResponse(src, trg);	                	
		                break;
	                }
	                case "INCLUDE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		int src = g.getEvent(e1);
	            		int trg = g.getEvent(e2);
	            		g.addInclude(src, trg);	                	
		                break;
	                }
	                case "EXCLUDE":
	                {
	            		String e1 = fields[1].trim();
	            		String e2 = fields[2].trim();
	            		int src = g.getEvent(e1);
	            		int trg = g.getEvent(e2);
	            		g.addExclude(src, trg);	                	
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
	
	public void toCSVFormat(String fileName)
	{
		try (PrintWriter out = new PrintWriter(fileName)) {
		    out.println(toCSVFormat());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
	
	public String toCSVFormat()
	{
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		
		
		for (int i : idToEvent.keySet())
		{
			String e = idToEvent.get(i);
			result.append("EVENT");
			result.append(",");
			//result.append(this.marking.executed.get(i) + ",");
			//result.append(this.marking.included.get(i) + ",");
			//result.append(this.marking.pending.get(i) + "");
			result.append(e);
			result.append(NEW_LINE);							
		}
		
		
		for (int i : idToEvent.keySet())
			for (int j : idToEvent.keySet())				
				if (conditionsFor.get(i).get(j))
					result.append("CONDITION," + idToEvent.get(j) + "," + idToEvent.get(i) + NEW_LINE);
		
		for (int i : idToEvent.keySet())
			for (int j : idToEvent.keySet())				
				if (responsesTo.get(i).get(j))
					result.append("RESPONSE," + idToEvent.get(i) + "," + idToEvent.get(j) + NEW_LINE);

		for (int i : idToEvent.keySet())
			for (int j : idToEvent.keySet())				
				if (includesTo.get(i).get(j))
					result.append("INCLUDE," + idToEvent.get(i) + "," + idToEvent.get(j) + NEW_LINE);			

		for (int i : idToEvent.keySet())
			for (int j : idToEvent.keySet())				
				if (excludesTo.get(i).get(j))
					result.append("EXCLUDE," + idToEvent.get(i) + "," + idToEvent.get(j) + NEW_LINE);
		

		return result.toString();
		
	}
	
	
	public String toDCRLanguage2()
	{
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		
		
		for (int i : idToEvent.keySet())
		{
			String e = idToEvent.get(i);
			result.append(e);
			result.append("(");
			//result.append(this.marking.executed.get(i) + ",");
			//result.append(this.marking.included.get(i) + ",");
			//result.append(this.marking.pending.get(i) + "");
			result.append("0,1,0");
			
			result.append(")");		
			result.append("{" + NEW_LINE);	

			
			for (int j : idToEvent.keySet())				
				if (conditionsFor.get(i).get(j))
					result.append("  " + idToEvent.get(j) + " -->* " + e + NEW_LINE);
			
			for (int j : idToEvent.keySet())				
				if (responsesTo.get(i).get(j))
					result.append("  " + e + " *--> " + idToEvent.get(j) + NEW_LINE);
			
			for (int j : idToEvent.keySet())				
				if (includesTo.get(i).get(j))
					result.append("  " + e + " -->+ " + idToEvent.get(j) + NEW_LINE);			

			for (int j : idToEvent.keySet())				
				if (excludesTo.get(i).get(j))
					result.append("  " + e + " -->% " + idToEvent.get(j) + NEW_LINE);
			
			result.append("}," + NEW_LINE);			
		}
		
		return result.toString();
	}	
	
	
	public String toDCRLanguage()
	{
		
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
	
		addRelationToLanguage(result, conditionsTo, "-->*");
		addRelationToLanguage(result, responsesTo, "*-->");
		addRelationToLanguage(result, includesTo, "-->+");
		addRelationToLanguage(result, excludesTo, "-->%");		
		
		return result.toString();
	}
	
	
	private void addRelationToLanguage(StringBuilder result, HashMap<Integer, BitSet> rel, String symbol) {

		final String NEW_LINE = System.getProperty("line.separator");
		for (int i : rel.keySet()) {	
			boolean first = true;
			for (int j : rel.keySet()) {				
				if (rel.get(i).get(j)) {
					if (first)
					{
						first = false;
						result.append("\"" + idToEvent.get(i) + "\"");
						result.append(" " + symbol + " (");
						result.append("\"" + idToEvent.get(j) + "\"");
					}					
					else
					{
						result.append(" ");
						result.append("\"" + idToEvent.get(j) + "\"");
					}
					
				}
			}
			if (!first)
			{
				result.append(")");
				result.append(NEW_LINE);
			}
		}
	}
	
	
	public void toDCRLanguageLabels(String fileName)
	{
		try (PrintWriter out = new PrintWriter(fileName)) {
		    out.println(toDCRLanguageLabels());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String toDCRLanguageLabels()
	{
		
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		for (final int e : this.idToEvent.keySet()) { 
			result.append(e);
			result.append("[\"" + idToEvent.get(e) + "\"]");
			result.append(NEW_LINE);            
		}			
		
		addRelationToLanguageLabels(result, conditionsTo, "-->*");
		addRelationToLanguageLabels(result, responsesTo, "*-->");
		addRelationToLanguageLabels(result, includesTo, "-->+");
		addRelationToLanguageLabels(result, excludesTo, "-->%");		
		
		return result.toString();
	}
	
	
	private void addRelationToLanguageLabels(StringBuilder result, HashMap<Integer, BitSet> rel, String symbol) {
		final String NEW_LINE = System.getProperty("line.separator");
		for (int i : rel.keySet()) {	
			boolean first = true;
			for (int j : rel.keySet()) {				
				if (rel.get(i).get(j)) {
					if (first)
					{
						first = false;
						result.append(i);
						result.append(" " + symbol + " (");
						result.append(j);
					}					
					else
					{
						result.append(" ");
						result.append(j);
					}
					
				}
			}
			if (!first)
			{
				result.append(")");
				result.append(NEW_LINE);
			}
		}
	}
}
