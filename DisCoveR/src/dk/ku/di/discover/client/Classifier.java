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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;
import org.qmpm.logtrie.exceptions.FileLoadException;
import org.qmpm.logtrie.tools.XESTools;

import dk.ku.di.dcrgraphs.BitDCRGraph;
import dk.ku.di.dcrgraphs.DCRGraph;
import dk.ku.di.discover.classifier.DCRClassifier;

public class Classifier {
	public static void classify(String logPath, String modelPath, String newLogPath, boolean openWorld) throws FileLoadException, IOException {
		System.out.println("Classifying log: " + logPath + ", DCR Graph: " + modelPath + ", new log:" + newLogPath + ".");
		XLog log = XESTools.loadXES(logPath, true);

		DCRGraph g = DCRGraph.fromCSVFormat(modelPath);
				
		DCRClassifier.classify(g, log, openWorld);
		
		saveFile(log, newLogPath);		
	}
	
	
	public static void saveFile(XLog log, String path) throws FileNotFoundException, IOException {

		File f = new File(path);

		if (f.getParentFile() != null)
			f.getParentFile().mkdirs();
		f.createNewFile();

		XesXmlSerializer xesSerial = new XesXmlSerializer();
		xesSerial.serialize(log, new FileOutputStream(f));
	}	
}
