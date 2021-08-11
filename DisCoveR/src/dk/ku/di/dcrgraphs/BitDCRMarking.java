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

import java.util.BitSet;

public class BitDCRMarking {

	public BitSet executed = new BitSet();
	public BitSet included = new BitSet();
	public BitSet pending = new BitSet();

	public BitSet blockCond() {
		BitSet result = (BitSet) included.clone();
		result.andNot(executed);
		return result;
	}

	public BitSet blockMilestone() {
		BitSet result = (BitSet) included.clone();
		result.and(pending);
		return result;
	}

	public boolean isAccepting() {
		return !included.intersects(pending);
	}

	@Override
	public BitDCRMarking clone() {
		BitDCRMarking newMarking = new BitDCRMarking();
		newMarking.executed = (BitSet) this.executed.clone();
		newMarking.included = (BitSet) this.included.clone();
		newMarking.pending = (BitSet) this.pending.clone();
		return newMarking;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executed == null) ? 0 : executed.hashCode());
		result = prime * result + ((included == null) ? 0 : included.hashCode());
		result = prime * result + ((pending == null) ? 0 : pending.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BitDCRMarking other = (BitDCRMarking) obj;
		if (executed == null) {
			if (other.executed != null)
				return false;
		} else if (!executed.equals(other.executed))
			return false;
		if (included == null) {
			if (other.included != null)
				return false;
		} else if (!included.equals(other.included))
			return false;
		if (pending == null) {
			if (other.pending != null)
				return false;
		} else if (!pending.equals(other.pending))
			return false;
		return true;
	}

	/*
	 * @Override public boolean equals(final Object obj) { if (this == obj) return
	 * true; if (obj == null) return false; if (getClass() != obj.getClass()) return
	 * false; final DCRMarking other = (DCRMarking) obj; return
	 * (this.executed.equals(other.executed) && this.included.equals(other.included)
	 * && this.pending.equals(other.pending)); }
	 * 
	 */

	/*
	 * @Override public String toString() { final StringBuilder result = new
	 * StringBuilder(); final String NEW_LINE =
	 * System.getProperty("line.separator");
	 * 
	 * result.append(this.getClass().getName() + " DCR marking {" + NEW_LINE);
	 * result.append(" Executed: "); for (final String e : executed) {
	 * result.append(e + "; "); } result.append(NEW_LINE);
	 * 
	 * result.append(" Pending: "); for (final String e : pending) { result.append(e
	 * + "; "); } result.append(NEW_LINE);
	 * 
	 * result.append(" Included: "); for (final String e : included) {
	 * result.append(e + "; "); } result.append(NEW_LINE);
	 * 
	 * return result.toString(); }
	 * 
	 * @Override public int hashCode() { return this.toString().hashCode(); }
	 * 
	 * @Override public DCRMarking clone() { DCRMarking newMarking = new
	 * DCRMarking();
	 * 
	 * newMarking.executed = new HashSet<String>(this.executed); newMarking.included
	 * = new HashSet<String>(this.included); newMarking.pending = new
	 * HashSet<String>(this.pending); return newMarking; }
	 */

}
