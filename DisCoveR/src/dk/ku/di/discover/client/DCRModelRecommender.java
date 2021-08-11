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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import dk.ku.di.discover.modelrecommendation.DCRModelAdvice;
import dk.ku.di.discover.modelrecommendation.DCRModelRecommendation;

public class DCRModelRecommender {

	public static void main(String[] args) throws FileLoadException {
		// TODO Auto-generated method stub
		if (args.length == 0)
		{
			System.out.println("Usage: java -jar DisCoveR.jar <xesfile>");
			System.out.println("");
		}
		else if (args.length == 1)
		{
	       JAXBContext jc;
	       try {
	    	   jc = JAXBContext.newInstance("dk.ku.di.discover.modelrecommendation", DCRModelAdvice.class.getClassLoader());
	    	   Marshaller m = jc.createMarshaller();
	    	   m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);    	   
				String sInput = args[0];
	    	   File f = new File(sInput);
	    	   DCRModelRecommendation r = new DCRModelRecommendation();
	    	   DCRModelAdvice a = r.recommend(f);
	    	   m.marshal(a, System.out);
	       } catch (JAXBException e) {
	    	   // TODO Auto-generated catch block
	    	   e.printStackTrace();
	       }		
		}
	}	
}
