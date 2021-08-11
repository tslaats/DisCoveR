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
package dk.ku.di.discover.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.logtrie.tools.XESTools;


import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.exceptions.LabelTypeException;

import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.discover.algorithms.bitparnek.BitParNek;
import dk.ku.di.discover.algorithms.bitparnek.BitParNekLogAbstractions;
import dk.ku.di.discover.algorithms.bitparnek.DCRMetrics;

public class DisCoveR {

	public static Boolean results[][] = new Boolean[90][10];
	
	public static void main(String[] args) throws FileLoadException {
		// TODO Auto-generated method stub
		if (args.length == 0)
		{
			System.out.println("Usage: java -jar DisCoveR.jar <xesfile>");
			System.out.println("");
			System.out.println("Or: java -jar DisCoveR.jar <inputFolder> <outputFolder>");
			System.out.println("");
			System.out.println("Or: java -jar DisCoveR.jar <inputFolder> <outputFolder> [testFolders...]");
			System.out.println("");
			System.out.println("When using the third option, test logs should have exactly the same name as the training logs, and each test folder should contain a log for each log in the input folder.");
			System.out.println("Only XES is supported at the moment.");
		}
		else if (args[0].equals("-PDC"))
		{		
			if (args[1].equals("-L"))
				minePDC(args[2], args[3], true);
			else 
				minePDC(args[1], args[2], false);
		}
		else if (args[0].equals("-classifyPDC"))
		{			
			try {
				Classifier.classify(args[1], args[2], args[3], Boolean.parseBoolean(args[4]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (args[0].equals("-comliance"))
		{
			System.out.println("Checking compliance: ");
			try {
				Compliance.checkCompliance(args[1], args[2], null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//Compliance.checkCompliance("",args[1]);
		}
		else if (args.length == 1)
		{
			String location = args[0];
			
			mine(location, location.replace(".xes", ".txt"));
			
		    for(String s : args){	    	
		    	if (s == "")
		    	{
		    		
		    	}
		    }
		    
    
			
		}
		else if (args.length == 2)
		{
			String sInputFolder = args[0];
			String sOutputFolder = args[1];
			
			File fInputFolder = new File(sInputFolder);
			File fOutputFolder = new File(sOutputFolder);
			
		    for (final File fileEntry : fInputFolder.listFiles()) {
		    	mine(sInputFolder + "/" + fileEntry.getName(), sOutputFolder + "/" + fileEntry.getName().replace(".xes", ".txt"));		    	
		    }
		}
		
		else if (args.length > 2)
		{
			String sInputFolder = args[0];
			String sOutputFolder = args[1];
			
			File fInputFolder = new File(sInputFolder);
			File fOutputFolder = new File(sOutputFolder);
			
			
			//int i = 0;
			String[] sVerficiationFolders = Arrays.copyOfRange(args, 2, args.length);
			
		    for (final File fileEntry : fInputFolder.listFiles()) {
		    	mineAndTest(sInputFolder + "/" + fileEntry.getName(), sOutputFolder + "/" + fileEntry.getName().replace(".xes", ".txt"), sVerficiationFolders, fileEntry.getName(), sOutputFolder);		    	
		    }

		    
		    
		    final StringBuilder result = new StringBuilder();
			final String NEW_LINE = System.getProperty("line.separator");		    
		    for (int i = 0; i < 90; i++) {
		    	
		    	for (int j = 0; j < 10; j++) { 
		    		result.append(results[i][j]);
		    		result.append(",");
		    	}
		    	result.append(NEW_LINE);
		    }
		    
			try (PrintWriter out = new PrintWriter(sOutputFolder + "/all.csv")) {
			    out.println(result.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}		    
		    
		    
		}		
		
		
	    
	}
	
	// almost a complete copy, merge these again sometime later.
	public static void minePDC(String location, String output, boolean light) throws FileLoadException {		
		System.out.println("=== " + location + " => " + output + " ===");
	    long startTime = System.currentTimeMillis();
	    System.out.print("Loading XES... ");
	    XLog log = XESTools.loadXES(location, true);
	    long endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    

	    startTime = System.currentTimeMillis();
	    System.out.print("Parsing log... ");		    
		BitParNek disco = new BitParNek();
		BitParNekLogAbstractions helper = disco.helper(log);
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
		
	    BitDCRGraph g;  
	    
	    if (!light)
	    {
		    startTime = System.currentTimeMillis();
		    System.out.print("Mining log... ");
			g = disco.mine(helper);		//
			disco.findAdditionalConditions(helper, g);
			disco.clean(helper, g);	    
		    endTime = System.currentTimeMillis();
		    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
	    }
	    else	    	
	    {
		    startTime = System.currentTimeMillis();
		    System.out.print("Mining log (light)... ");
	    	g = disco.mineForModelRecommendation(helper);
	    	disco.clean(helper, g);
		    endTime = System.currentTimeMillis();
		    System.out.println(" execution time: " + (endTime - startTime) + "ms");	    	
	    }
	    

	    startTime = System.currentTimeMillis();
	    System.out.print("Writing model... ");
	    //g.toCSVFormat(output);
	    g.toDCRLanguage(output);
		//g.toModelRecommendationFormat(output + ".JSON");
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");				
	}
	
	
	
	public static void mine(String location, String output) throws FileLoadException {		
		System.out.println("=== " + location + " => " + output + " ===");
	    long startTime = System.currentTimeMillis();
	    System.out.print("Loading XES... ");
	    XLog log = XESTools.loadXES(location, true);
	    long endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    

	    startTime = System.currentTimeMillis();
	    System.out.print("Parsing log... ");		    
		BitParNek disco = new BitParNek();
		BitParNekLogAbstractions helper = disco.helper(log);
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
		
	    startTime = System.currentTimeMillis();
	    System.out.print("Mining log... ");
		BitDCRGraph g  = disco.mine(helper);		//
		disco.findAdditionalConditions(helper, g);
		disco.clean(helper, g);	    
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
		

	    startTime = System.currentTimeMillis();
	    System.out.print("Writing model... ");		    
		g.toDCRLanguage(output);
		g.toModelRecommendationFormat(output + ".JSON");
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");				
	}
	
	
	public static void mineAndTest(String location, String output, String[] verification, String logName, String outPutFolder) throws FileLoadException {		
		System.out.println("=== " + location + " => " + output + " ===");
	    long startTime = System.currentTimeMillis();
	    System.out.print("Loading XES... ");
	    XLog log = XESTools.loadXES(location, true);
	    long endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    

	    startTime = System.currentTimeMillis();
	    System.out.print("Parsing log... ");		    
		BitParNek disco = new BitParNek();
		BitParNekLogAbstractions helper = disco.helper(log);
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
		
	    startTime = System.currentTimeMillis();
	    System.out.print("Mining log... ");
		BitDCRGraph g  = disco.mine(helper);		//
		disco.findAdditionalConditions(helper, g);
		disco.clean(helper, g);	    
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");		    
		

	    startTime = System.currentTimeMillis();
	    System.out.print("Writing model... ");		    
		g.toDCRLanguage(output);
		g.toModelRecommendationFormat(output + ".JSON");
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");
	    

	    startTime = System.currentTimeMillis();
	    System.out.print("Testing model... ");
	    for(String s : verification){
	    	test(helper, g, s + "/" + logName, outPutFolder + "/" + s + "_" + logName.replace(".xes", ".csv"));
	    }
	    endTime = System.currentTimeMillis();
	    System.out.println(" execution time: " + (endTime - startTime) + "ms");	    
	    
	}


	private static void test(BitParNekLogAbstractions helper, BitDCRGraph g, String s, String output) {

		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");
		
		XLog log;
		try {
			log = XESTools.loadXES(s, true);
			
			BitParNek disco = new BitParNek();			
			
			for (XTrace trace : log) {	
					String name = XESTools.xTraceID(trace);					
					LinkedList<Integer> t = new LinkedList<>();
					for (XEvent event : trace) {
						if (event != null) {
							try {
								String e = XESTools.xEventName(event);
								if (!helper.containsActivity(e)) {
									helper.addActivity(e);
								}
								int j = helper.getActivityID(e);
								t.add(j);
							} catch (LabelTypeException e) {
								e.printStackTrace();
							}
						}
					}
					result.append(name + "," + DCRMetrics.acceptsTrace(g, t));
					result.append(NEW_LINE);
					
					int model = Integer.parseInt(output.replaceAll("\\D+",""));
					int tracenr = Integer.parseInt(name);
					results[tracenr-1][model-1] = DCRMetrics.acceptsTrace(g, t);
				}					
				
			}
		 catch (FileLoadException e) {
			e.printStackTrace();			
		} catch (LabelTypeException e1) {
			e1.printStackTrace();
		}		
	
		
		try (PrintWriter out = new PrintWriter(output)) {
		    out.println(result.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		
	}
	
}
