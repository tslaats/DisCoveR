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

import dk.ku.di.dcrgraphs.DCRGraph;

public class ComplianceResult {	
	public String name;
	public DCRGraph filter;
	public DCRGraph pattern;
	
	int truePositive = 0;
	int falsePositive = 0;
	int trueNegative = 0;
	int falseNegative = 0;	
	public long runTime;
	
	public double accuracy()
	{
		if (results.size() == 0)
			return 1;
		else
			return (double)(truePositive + trueNegative) / results.size(); 
	}
	
	@Override
	public String toString() {
		final String NEW_LINE = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder();		
		StringBuilder sb_reasons = new StringBuilder();
				
		for (TraceComplianceResult r : results.values())
		{
			if (r.actualResult == true && r.expectedResult == false)
			{
				sb_reasons.append(r.traceId + "(false positive): " + " was accepted.");
				sb_reasons.append(NEW_LINE);
			}
			
			if (r.actualResult == false && r.expectedResult == true)
			{
				sb_reasons.append(r.traceId + "(false negative): " + r.reasonForFail);
				sb_reasons.append(NEW_LINE);
			}				
		}
		
		sb.append("Accuracy: " + accuracy() + NEW_LINE);
		
		sb.append("      | Positives | Negatives |" + NEW_LINE);
		sb.append("True  | " + String.format("%9d", truePositive) + " | " + String.format("%9d", trueNegative) +" |" + NEW_LINE);
		sb.append("False | " + String.format("%9d", falsePositive) + " | " + String.format("%9d", falseNegative) +" |" + NEW_LINE);
		sb.append(NEW_LINE);
		//sb.append(sb_reasons);
		
		return sb.toString();				
	}

	public HashMap<String, TraceComplianceResult> results =  new HashMap<>();

	public void AddResult(TraceComplianceResult r)
	{
		results.put(r.traceId, r);
		
		if (r.actualResult == true)
			if (r.expectedResult == true)
				truePositive++;
			else
				falsePositive++;
		else
			if (r.expectedResult == false)
				trueNegative++;
			else
				falseNegative++;		
	}
	
}
