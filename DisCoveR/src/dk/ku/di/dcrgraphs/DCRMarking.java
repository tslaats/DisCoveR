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

import java.util.HashSet;

public class DCRMarking {
	public HashSet<String> executed = new HashSet<String>();
	public HashSet<String> included = new HashSet<String>();
	public HashSet<String> pending = new HashSet<String>();

	public boolean IsAccepting() {
		for (String e : included)
			if (pending.contains(e))
				return false;

		return true;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		final String NEW_LINE = System.getProperty("line.separator");

		result.append(this.getClass().getName() + " DCR marking {" + NEW_LINE);
		result.append(" Executed: ");
		for (final String e : executed) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Pending: ");
		for (final String e : pending) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		result.append(" Included: ");
		for (final String e : included) {
			result.append(e + "; ");
		}
		result.append(NEW_LINE);

		return result.toString();
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public DCRMarking clone() {
		DCRMarking newMarking = new DCRMarking();

		newMarking.executed = new HashSet<String>(this.executed);
		newMarking.included = new HashSet<String>(this.included);
		newMarking.pending = new HashSet<String>(this.pending);
		/*
		 * newMarking.executed = (HashSet<String>) this.executed.clone();
		 * newMarking.included = (HashSet<String>) this.included.clone();
		 * newMarking.pending = (HashSet<String>) this.pending.clone();
		 */
		return newMarking;
	}

	public BitDCRMarking toBitMarking(BitDCRGraph g) {
		// TODO Auto-generated method stub
		BitDCRMarking result = new BitDCRMarking();
		
		for (final String e : executed) {
			result.executed.set(g.getEvent(e));
		}
		
		for (final String e : pending) {
			result.pending.set(g.getEvent(e));
		}		
		
		for (final String e : included) {
			result.included.set(g.getEvent(e));
		} 
		
		return result;
	}

}
