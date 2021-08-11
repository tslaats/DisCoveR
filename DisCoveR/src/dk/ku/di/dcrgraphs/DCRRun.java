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
package dk.ku.di.dcrgraphs;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.qmpm.logtrie.exceptions.LabelTypeException;
import org.qmpm.logtrie.tools.XESTools;

public class DCRRun {
	static public boolean accepts(BitDCRGraph g, XLog log) {

		for (XTrace trace : log) {
			BitDCRMarking m = g.defaultInitialMarking();
			for (XEvent event : trace) {
				if (event != null) {
					try {
						String e = XESTools.xEventName(event);
						if (!g.eventToId.containsKey(e))
							return false;
						int i = g.eventToId.get(e);
						if (!g.enabled(m, i))
							return false;
						m = g.execute(m, i);
					} catch (LabelTypeException e) {
						return false;
					}
				}
			}
			if (!m.isAccepting()) {
				System.out.println("Non accepting trace: ");
				for (XEvent event : trace) {
					try {
						String e = XESTools.xEventName(event);
						int i = g.eventToId.get(e);
						System.out.print(i + "; ");
					} catch (LabelTypeException e) {
						return false;
					}
				}
				System.out.println();
				return false;
			}
		}

		return true;
	}
}
