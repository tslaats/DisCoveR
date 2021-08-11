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
package dk.ku.di.discover.compliance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.dcrgraphs.DCRGraph;

public class ComplianceChecker {

	public static ComplianceResult CheckCompliance(DCRGraph dcrGraph, DCRGraph dcrGraphFilter, XLog log, HashMap<String, Boolean> labelling) 
	{
		ComplianceResult result = new ComplianceResult();		
		//XLog log;
		
		//HashSet<String> unqiueEventNames = new HashSet<String>();
		//int countEvents = 0;
		//int countTraces = 0;
		
		
		try {
			long startTime = System.nanoTime();
			
			for (XTrace trace : log) {	
					String traceId = XESTools.xTraceID(trace);
					//countTraces++;
					
					boolean expectedResult = true;
					if (!(labelling == null))					
						expectedResult = labelling.get(traceId);
					
					LinkedList<String> t = new LinkedList<>();
					for (XEvent event : trace) {
						if (event != null) {
							try {
								String e = XESTools.xEventName(event);
								//unqiueEventNames.add(e);
								//countEvents++;
								// For Amine: here you want to add your code for mapping Log activities to DCR activities.
								
								t.add(e);
							} catch (LabelTypeException e) {
								e.printStackTrace();
							}
						}
					}
					
					if(dcrGraphFilter != null)
					{
						if (!dcrGraphFilter.canRunOpenWorld(t))
							continue;
					}
					
					
					boolean actualResult = dcrGraph.canRunOpenWorld(t);
					if (actualResult)
						result.AddResult(new TraceComplianceResult(traceId, expectedResult, actualResult));
					else
						result.AddResult(new TraceComplianceResult(traceId, expectedResult, actualResult, dcrGraph.whyInvalidRunOpenWorld(t)));
				}	
			  result.runTime = System.nanoTime() - startTime;
				
			}
		catch (LabelTypeException e1) {
			e1.printStackTrace();
		}
		
		//System.out.println("Unique labels:");
		//System.out.println(unqiueEventNames.toString());
		//System.out.print("Events:");
		//System.out.println(countEvents);
		//System.out.print("Traces:");
		//System.out.println(countTraces);		

		
		return result;	
		
	}

}
