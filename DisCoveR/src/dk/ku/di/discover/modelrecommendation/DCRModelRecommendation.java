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
package dk.ku.di.discover.modelrecommendation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.eclipse.persistence.jaxb.JAXBContextProperties;

import dk.ku.di.discover.algorithms.bitparnek.*;
import dk.ku.di.dcrgraphs.BitDCRGraph;

public class DCRModelRecommendation {

	// Refactor this later, quite a bit of code duplication...
	
	static public String GetRecommendationSimpleFormat(String input)
	{
		StringWriter sw = new StringWriter();
		JAXBContext jc;
		try {
			 Map<String, Object> properties = new HashMap<String, Object>(2);
			 properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			 properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			jc = JAXBContext.newInstance("dk.ku.di.discover.modelrecommendation", DCRModelAdvice.class.getClassLoader(), properties);
			Marshaller m = jc.createMarshaller();
			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			String sInput = input;						
			DCRModelRecommendation r = new DCRModelRecommendation();
			DCRModelAdvice a = r.recommend(sInput);
			System.out.println(a.toString());
			m.marshal(a, sw);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return sw.toString();
	}	
	
	
	static public String GetRecommendation(String input)
	{
		StringWriter sw = new StringWriter();
		JAXBContext jc;
		try {
			 Map<String, Object> properties = new HashMap<String, Object>(2);
			 properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
			 properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
			jc = JAXBContext.newInstance("dk.ku.di.discover.modelrecommendation", DCRModelAdvice.class.getClassLoader(), properties);
			Marshaller m = jc.createMarshaller();
			//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			String sInput = input;
			//File f = new File(sInput);
			InputStream f = new ByteArrayInputStream(input.getBytes());			
			DCRModelRecommendation r = new DCRModelRecommendation();
			DCRModelAdvice a = r.recommend(f);
			System.out.println(a.toString());
			m.marshal(a, sw);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return sw.toString();
	}
	
	
	
	public DCRModelAdvice recommend(File f)
	{
		XesXmlParser parser = new XesXmlParser();
		XLog log = null;
		if (parser.canParse(f)) {
			try {
				log = parser.parse(f).get(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return recommend(log);
		}
		return null;
	}
	
	public DCRModelAdvice recommend(InputStream f)
	{
		XesXmlParser parser = new XesXmlParser();
		XLog log = null;
		
			try {
				log = parser.parse(f).get(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return recommend(log);		
	}
	
	
	public DCRModelAdvice recommend(String s)
	{
		BitParNek disco = new BitParNek();		
		BitDCRGraph g = disco.mineForModelRecommendation(s);		
		return graphToRecommendation(g);		
	}
	
	public DCRModelAdvice recommend(XLog log)
	{
		BitParNek disco = new BitParNek();
		
		BitDCRGraph g = disco.mineForModelRecommendation(log);
		
		return graphToRecommendation(g);			
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
	
}
