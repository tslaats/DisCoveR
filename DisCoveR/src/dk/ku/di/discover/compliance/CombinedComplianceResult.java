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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeMap;

public class CombinedComplianceResult {

	public HashMap<String, ComplianceResult> results =  new HashMap<>();
	
	public CombinedComplianceResult() {
		// TODO Auto-generated constructor stub
	}
	
	public void add(String name , ComplianceResult res)
	{
		results.put(name, res);
	}
	
	
	@Override
	public String toString() {
		final String NEW_LINE = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();
		
		
		//for (ComplianceResult r : results.values())
		for (String n :  new TreeMap<String, ComplianceResult>(results).keySet())			
		{				
			ComplianceResult r = results.get(n);
			sb.append(r.name);
			sb.append("\t");
			sb.append(r.truePositive);
			sb.append("\t");
			sb.append(r.falseNegative);
			sb.append("\t");
			if (r.filter != null)				
				sb.append(r.filter.toSimpleString());				
			sb.append("\t");
			sb.append(r.pattern.toSimpleString());
			sb.append("\t");
			sb.append(r.runTime);
			sb.append(NEW_LINE);
		}

		
		return sb.toString();		
		
	}
	
	
	public void toFile(String fileName)
	{
		try (PrintWriter out = new PrintWriter(fileName)) {
		    out.println(toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	

}
