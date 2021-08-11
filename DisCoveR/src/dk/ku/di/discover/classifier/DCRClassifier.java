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
package dk.ku.di.discover.classifier;

import java.util.HashMap;
import java.util.LinkedList;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.dcrgraphs.DCRGraph;

public class DCRClassifier {	
	public static void classify(DCRGraph dcrGraph, XLog log, boolean openWorld) 
	{
		//System.out.println(dcrGraph.toString());
		
		try {
			long startTime = System.nanoTime();			
			for (XTrace trace : log) {	
					String traceId = XESTools.xTraceID(trace);	
					
					LinkedList<String> t = new LinkedList<>();
					for (XEvent event : trace) {
						if (event != null) {
							try {
								String e = XESTools.xEventName(event);								
								t.add(e);
							} catch (LabelTypeException e) {
								e.printStackTrace();
							}
						}
					}
					
					//System.out.println(t);
					boolean actualResult;
					if (openWorld)
						actualResult = dcrGraph.canRunOpenWorld(t);
					else
						actualResult = dcrGraph.canRunClosedWorld(t);
						
					
					XAttributeMap attrs = trace.getAttributes(); 

					/*
					System.out.println(actualResult);
					System.out.println(dcrGraph.canRunOpenWorld(t));
					System.out.println(dcrGraph.whyInvalidRunOpenWorld(t));
					System.out.println(dcrGraph.canRunClosedWorld(t));
					System.out.println(dcrGraph.whyInvalidRunClosedWorld(t));
					*/
					
					
					if (actualResult)						
						attrs.put("pdc:isPos", new XAttributeBooleanImpl("pdc:isPos", true));
					else
						attrs.put("pdc:isPos", new XAttributeBooleanImpl("pdc:isPos", false));						
				}	
			}
		catch (LabelTypeException e1) {
			e1.printStackTrace();
		}
		
	}

}
