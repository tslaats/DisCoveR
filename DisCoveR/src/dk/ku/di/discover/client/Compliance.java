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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.deckfour.xes.model.XLog;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.dcrgraphs.DCRGraph;
import dk.ku.di.dcrgraphs.DCRXMLParser;
import dk.ku.di.discover.compliance.*;


public class Compliance {
	private static final String COMMA_DELIMITER = ";";
	
	private static CombinedComplianceResult results = new CombinedComplianceResult();

	public static void checkCompliance(String logPath, String sInputFolder, String labelPath) throws FileLoadException, IOException {
		System.out.println("Checking compliance for log: " + logPath + ", patterns: " + sInputFolder + ", and labels: " + labelPath);
		// load the compliance logs here and pass the helper along.		
		XLog log = XESTools.loadXES(logPath, true);
		File fInputFolder = new File(sInputFolder);
		
		HashMap<String, Boolean> labelling = null;
		if (labelPath != null)
		{
			labelling = new HashMap<String, Boolean>();
	
			try (BufferedReader br = new BufferedReader(new FileReader(labelPath))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			        String[] values = line.split(COMMA_DELIMITER);
			        labelling.put(values[0], Boolean.parseBoolean(values[1]));
			    }
			}
		}				
		
		check(log, fInputFolder, labelling, 0);
		
		results.toString();
		results.toFile("compliance_results.txt");
	}
	
	public static void check(XLog log, File fInputFolder, HashMap<String, Boolean> labelling, int ident) throws FileLoadException {
		//File fInputFolder = new File(sInputFolder);
	    for (final File fileEntry : fInputFolder.listFiles()) {	    	
	    	if (fileEntry.isDirectory())
	    	{
	    		//System.out.println(fileEntry.getPath());
	    		check(log, fileEntry, labelling, ident + 1);
	    	}
	    	else
	    	{	
	    		
	    		if (fileEntry.getName().contains("_f.xml"))
	    			continue;
	    		
	    		DCRGraph gFilter = null;
    			String path = fileEntry.getParent();
    			String name = fileEntry.getName();
    			name = name.replace(".xml", "_f.xml");
    			
    			
    			System.out.println("checking filter: " + path+ "\\" + name);
    			File fFilter = new File(path+"\\" + name);
    			
    			
    			
    			if (fFilter.exists())
    			{
    				gFilter = DCRXMLParser.load(fFilter);	    			
    				System.out.println("found filter: " + path+"\\" + name);
    			}
	    		
	    		DCRGraph g= DCRXMLParser.load(fileEntry);
	    		System.out.println("Path: " + fileEntry.getAbsolutePath());
	    		
	    		ComplianceResult res = null;	    		
	    		res = ComplianceChecker.CheckCompliance(g, gFilter, log, labelling);
	    		res.filter = gFilter;
	    		res.pattern = g;
	    		
	    		//if (res.accuracy() < 1)
	    			System.out.println(g.labelsToEventIDs());	
	    		System.out.println(res.toString());      		
	    	    
	    		res.name = fileEntry.getName();
	    		results.add(fileEntry.getName(), res);
	    		
	    		// possibly also write to a file...
	    	}	    		
    	}
	}
	
	
	public static void checkAmine(XLog log, File fInputFolder, HashMap<String, Boolean> labelling, int ident) throws FileLoadException {
		int max = 0;
		File max_rev = null;
		//File fInputFolder = new File(sInputFolder);
	    for (final File fileEntry : fInputFolder.listFiles()) {	    	
	    	if (fileEntry.isDirectory())
	    	{
	    		//System.out.println(fileEntry.getPath());
	    		check(log, fileEntry, labelling, ident + 1);
	    	}
	    	else
	    	{	  
	    		String str = fileEntry.getName().replaceAll("\\D+","");
	    		if (!str.equals(""))
	    		{
		    		int i  = Integer.parseInt(str);
		    		if (i > max)
		    		{
		    			max = i;
		    			max_rev = fileEntry;
		    		}	    		
	    		}
	    		else
	    			max_rev = fileEntry;
	    	}    
	    }
    	if (max_rev != null)
    	{
    		/*
    		String s = DCRXMLParser.analyse(max_rev);
    		if (!s.equals(""))
    		{
    			System.out.println(max_rev.getPath());
    			System.out.println(s);
    		}*/    		
    		
    		DCRGraph g= DCRXMLParser.load(max_rev);
    		System.out.println("Path: " + max_rev);
    		
    		ComplianceResult res = ComplianceChecker.CheckCompliance(g, null, log, labelling);
    		if (res.accuracy() < 1)
    			System.out.println(g.labelsToEventIDs());	
    		System.out.println(res.toString());      		
    		    		
    	}

	}	
	
	
}
