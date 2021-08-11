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
package dk.ku.di.discover.client.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.discover.algorithms.bitparnek.*;
import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.dcrgraphs.DCRGraph;

class ICPM2020 {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@Test
	void ModelRecommendData() {
		int totalForbidden = 0;
		int classifiedForbidden = 0;
		int totalConstraints = 0;
		
		double perTraceTNR = 0.0;
		double perTraceAccuracy = 0.0;
		double perTracePrecision = 0.0;
		double perTraceF1 = 0.0;

		List<XLog> logs = new ArrayList<>();

		/*
		for (int i = 1; i <= 215; i++) {

			try {
				logs.add(XESTools.loadXES("D:\\Dropbox\\Development\\RejectionMiner\\logs\\DCRPortal individual\\log_" + i + ".xes", true));
			} catch (FileLoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		try {
			logs.add(XESTools.loadXES("D:\\Dropbox\\Development\\RejectionMiner\\logs\\dreyers\\dreyers.xes", true));
		} catch (FileLoadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		XFactory xFactory = new XFactoryBufferedImpl();

		for (XLog l : logs) {

			XLog requiredTraces = xFactory.createLog();
			XLog forbiddenTraces = xFactory.createLog();

			for (String key : l.getAttributes().keySet()) {
				System.out.println("LOG: " + l.getAttributes().get(key));
			}

			for (XTrace t : l) {

				String label = t.getAttributes().get("label").toString();

				if (label.equals("Required")) {
					requiredTraces.add(t);
				} else if (label.equals("Forbidden")) {
					forbiddenTraces.add(t);
				} else {
					System.out.println("ERROR - bad label: " + label);
					System.exit(1);
				}
			}

			//System.exit(1);
			
			// MINING HAPPENS HERE
			
			System.out.print("Mining required traces...");

			BitParNek disco = new BitParNek();
			BitParNekLogAbstractions helper = disco.helper(requiredTraces);
			BitDCRGraph g = disco.mine(helper);
			disco.findAdditionalConditions(helper, g);
			disco.clean(helper, g);
			//BitDCRGraph g = disco.mineForModelRecommendation(helper);
			//disco.clean(helper, g);

			System.out.println("done");

			int classifiedForbiddenInTrace = 0;
			
			for (XTrace t : forbiddenTraces) {

				//String name = XESTools.xTraceID(t);
				String name = t.toString();
				LinkedList<Integer> intTrace = new LinkedList<>();
				for (XEvent event : t) {
					if (event != null) {
						try {
							String e = XESTools.xEventName(event);
							if (!helper.getActivityIDMap().containsKey(e)) {
								helper.addActivity(e);
							}
							int j = helper.getActivityIDMap().get(e);
							intTrace.add(j);
						} catch (LabelTypeException e) {
							e.printStackTrace();
						}
					}
				}
				boolean accepts = DCRMetrics.acceptsTrace(g, intTrace);

				//System.out.println(name + "\t" + accepts);
				//System.out.println("# of constraints: " + g.numOfRelations());
				
				if (!accepts) {
					classifiedForbidden++;
					classifiedForbiddenInTrace++;
				}
				totalForbidden++;
			}
			
			totalConstraints += g.numOfRelations();
			System.out.println("# of constraints: " + g.numOfRelations());


			perTraceTNR += (double) classifiedForbiddenInTrace / forbiddenTraces.size();
			perTraceAccuracy += (double) (classifiedForbiddenInTrace + requiredTraces.size()) / (forbiddenTraces.size() + requiredTraces.size());
			
			if (requiredTraces.size() > 0)								
				perTracePrecision += (double) requiredTraces.size() / ((forbiddenTraces.size() - classifiedForbiddenInTrace) + requiredTraces.size());
			else
				perTracePrecision += (double) 1.0;
			
			
			double tp = (double) requiredTraces.size();
			
			double fp = (double) forbiddenTraces.size() - classifiedForbiddenInTrace;
			
			double fn = 0;
											
			if (tp > 0)
				perTraceF1 += (double) (2 * tp) / ((2 * tp) + fn + fp);
			else
				perTraceF1 += (double) 1.0;
			
			
			System.out.println("tp: " + tp);
			System.out.println("fp: " + fp);
			System.out.println("fn: " + fn);
			
			System.out.println("perTraceF1: " + perTraceF1);
			
			System.out.println("requiredTraces.size(): " + requiredTraces.size());
			System.out.println("forbiddenTraces.size(): " + forbiddenTraces.size());
			System.out.println("classifiedForbiddenInTrace: " + classifiedForbiddenInTrace);
			
			if (requiredTraces.size() > 0)							
				System.out.println("Precision: " + ((double) requiredTraces.size() / ((forbiddenTraces.size() - classifiedForbiddenInTrace) + requiredTraces.size())));
			
			System.out.println("");
		}
		
		double tnr = (double) classifiedForbidden  / totalForbidden;
		double tnrPerTrace = perTraceTNR / logs.size();
		double accuracyPerTrace = perTraceAccuracy / logs.size();
		double precisionPerTrace = perTracePrecision / logs.size();
		double F1PerTrace = perTraceF1 / logs.size();
		
		
		System.out.println("\n=== RESULTS ===\n");
		System.out.println("Classified as forbidden: " + classifiedForbidden);
		System.out.println("Total traces  forbidden: " + totalForbidden + "\n");
		System.out.println("True negative rate: " + String.format("%.3f", tnr)); 
		System.out.println("True negative rate (per trace mean): " + String.format("%.3f", tnrPerTrace));//MathTools.round(tnrPerTrace*100.0, 2));
		System.out.println("Accuracy (per trace mean): " + String.format("%.3f", accuracyPerTrace));//MathTools.round(tnrPerTrace*100.0, 2));
		System.out.println("Precision (per trace mean): " + String.format("%.3f", precisionPerTrace));//MathTools.round(tnrPerTrace*100.0, 2));
		System.out.println("F1 (per trace mean): " + String.format("%.3f", F1PerTrace));//MathTools.round(tnrPerTrace*100.0, 2));
		System.out.println("Mean # of constraints: " + String.format("%.3f", (totalConstraints*1.0)/(logs.size()*1.0)));//MathTools.round(totalConstraints/logs.size(), 2));
	}

}
