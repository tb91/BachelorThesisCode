package projects.reactiveSpanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sinalgo.nodes.Position;

public class PositionTriple {

	private Position x;
	private Position y;
	private Position z;
	private boolean redundant;
	
	public boolean isRedundant() {
		return redundant;
	}

	public void setRedundant(boolean redundant) {
		this.redundant = redundant;
	}

	public PositionTriple() {
		this.x = new Position(0, 0, 0);
		this.y = new Position(0, 0, 0);
		this.z = new Position(0, 0, 0);
		this.redundant = false;
	}
	
	public PositionTriple(Position x, Position y, Position z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.redundant = false;

		this.sort();
	}
	
	public boolean isEmpty() {
		if (this.x.xCoord == 0
				& this.y.xCoord == 0
				& this.z.xCoord == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sortiert die Positionen des PositionenTriple nach ihrer x-Koordinate, wobei x die kleinste
	 * und z die größte x-Koodrinate haben soll.
	 */
	public void sort() {

		if (this.getX().equals(this.getY()) || this.getY().equals(this.getZ())
				|| this.getX().equals(this.getZ())) {

			this.redundant = true;
			
		}
		
		
		boolean swap = true;
		Position temp;
		while (swap) {
			swap = false;

			temp = this.z;
		
			if (this.y.xCoord > temp.xCoord) {
				this.z = this.y;
				this.y = temp;
				swap = true;
			}

			temp = this.y;

			if (this.x.xCoord > temp.xCoord) {
				this.y = this.x;
				this.x = temp;
				swap = true;
			}
		}
	}
	
	public static boolean pointOnEdge (Position p, PositionTriple t) {
		
		// Code entnommen aus Methode Algorithms.positionDisk

		double det1 =  (p.xCoord - t.getX().xCoord) * (t.getX().yCoord - t.getY().yCoord) 
	    		- (t.getX().xCoord - t.getY().xCoord) * (p.yCoord - t.getX().yCoord);
		double det2 =  (p.xCoord - t.getY().xCoord) * (t.getY().yCoord - t.getZ().yCoord) 
	    		- (t.getY().xCoord - t.getZ().xCoord) * (p.yCoord - t.getY().yCoord);
		double det3 =  (p.xCoord - t.getZ().xCoord) * (t.getZ().yCoord - t.getX().yCoord) 
	    		- (t.getZ().xCoord - t.getX().xCoord) * (p.yCoord - t.getZ().yCoord);
		
		return (((Math.abs(det1)) < projects.reactiveSpanner.Algorithms.TOL)
				|| ((Math.abs(det2)) < projects.reactiveSpanner.Algorithms.TOL) || ((Math
				.abs(det3)) < projects.reactiveSpanner.Algorithms.TOL));
		
	}
	
	public boolean equals(PositionTriple t) {
		if ( comparePosition(t.getX(), this.getX()) & comparePosition(t.getY(), this.getY()) & comparePosition(t.getZ(), this.getZ()) ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gibt alle PositionTriple eines Set von PositionTriple tripleSet zurück,
	 * die p als Attribut haben.
	 * 
	 * @param p
	 * @param tripleSet
	 * @return
	 */
	public static Set<PositionTriple> findTriple (Position p, Set<PositionTriple> tripleSet) {
		
		Set<PositionTriple> result = new HashSet<PositionTriple>();
		
		for (PositionTriple triangle : tripleSet) {
			if (comparePosition(p, triangle.getX()) || comparePosition(p, triangle.getY()) || comparePosition(p, triangle.getZ())) {
					result.add(triangle);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Gibt alle PositionTriple eines Set von PositionTriple tripleSet zurück,
	 * die p1 und p2 als Attribute haben.
	 * 
	 * @param p1
	 * @param p2
	 * @param tripleSet
	 * @return
	 */
	public static Set<PositionTriple> findTriple (Position p1, Position p2, Set<PositionTriple> tripleSet) {
		
		Set<PositionTriple> result = new HashSet<PositionTriple>();
		
		for (PositionTriple triangle : tripleSet) {
			if (comparePosition(p1, triangle.getX())) {
				if (comparePosition(p2, triangle.getY())
						|| comparePosition(p2, triangle.getZ())) {
					result.add(triangle);
					continue;
				}
			}

			if (comparePosition(p1, triangle.getY())) {
				if (comparePosition(p2, triangle.getX())
						|| comparePosition(p2, triangle.getZ())) {
					result.add(triangle);
					continue;
				}
			}

			if (comparePosition(p1, triangle.getZ())) {
				if (comparePosition(p2, triangle.getX())
						|| comparePosition(p2, triangle.getY())) {
					result.add(triangle);
					continue;
				}
			}

		}
		
		return result;
		
	}
	
	/**
	 * Gibt alle PositionTriple eines Set von PositionTriple tripleSet zurück,
	 * die p1, p2 und p3 als Attribute haben.
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param tripleSet
	 * @return
	 */
	public static PositionTriple findTriple (Position p1, Position p2, Position p3, Set<PositionTriple> tripleSet) {
		
		PositionTriple searchedTriple = new PositionTriple(p1, p2, p3);
		
		for (PositionTriple triangle : tripleSet) {
			triangle.sort();
			if (searchedTriple.equals(triangle)) { return triangle; }
		}
		
		return new PositionTriple();
	}
	
	public static boolean compareDouble (double d1, double d2) {
		boolean equal = false;
		
		double zero = d1 - d2;
		zero = Math.abs(zero);
		
		if (zero < Math.pow(10, -5)) {equal = true;}
		
		return equal;
	}
	
	public static boolean comparePosition (Position p1, Position p2) {
		
		ArrayList<Double> pos1List = new ArrayList<Double>();
		ArrayList<Double> pos2List = new ArrayList<Double>();
		
		pos1List.add(p1.xCoord);
		pos1List.add(p1.yCoord);
		pos1List.add(p1.zCoord);
		Collections.sort(pos1List);
		
		pos2List.add(p2.xCoord);
		pos2List.add(p2.yCoord);
		pos2List.add(p2.zCoord);
		Collections.sort(pos2List);
		
		Iterator<Double> list1Iterator = pos1List.iterator();
		Iterator<Double> list2Iterator = pos2List.iterator();
		
		for (int i = 0; i < 3; i++) {
			if ( !compareDouble(list1Iterator.next(), list2Iterator.next()) ) {return false;}
		}
		
		return true;
	}
	
	public boolean contains (Position p) {
		
		boolean result = false;
		
		if ( comparePosition(this.x, p) || comparePosition(this.y, p) || comparePosition(this.z, p) ) { result = true; }
		
		return result;
	
	}
	
	public static ArrayList<PositionTriple> potenzmenge(Iterable<Position> workingSet) {

		// convert workingSet to Array
		int count = 0;

		for (@SuppressWarnings("unused") Position p : workingSet) {
			count++;
		}

		Position[] in = new Position[count];
		count = 0;

		for (Position p : workingSet) {
			in[count] = p;
			count++;
		}

		ArrayList<PositionTriple> out = new ArrayList<PositionTriple>();

		Position x = new Position();
		Position y = new Position();
		Position z = new Position();

		for (int i = 0; i < in.length; i++) {
			x = in[i];

			for (int j = 0; j < in.length; j++) {
				y = in[j];

				for (int k = 0; k < in.length; k++) {
					z = in[k];

					PositionTriple temp = new PositionTriple(x, y, z);

					// unmögliche Elemente ausschließen, d.h. alle Elemente
					// überspringen, die zwei identische Attribute haben
					// UND keine doppelten Elemente hinzufügen

					// TODO Ich würde hier gerne !out.contains(temp) benutzen,
					// aber ich habe NodeTriple.equals(NodeTriple) nicht zum
					// Laufen gekriegt.

					if (!temp.isRedundant()) {

						boolean contains = false;

						Iterator<PositionTriple> iterator2 = out.iterator();

						while (iterator2.hasNext()) {
							if (temp.equals(iterator2.next()))
								contains = true;
						}

						if (!contains) {
							out.add(temp);
						}
					}
				}
			}
		}

		return out;
	}
	
	public String toString() {
		return this.x.toString() + " | " + this.y.toString() + " | " + this.z.toString();
	}
	
	public Position getX() {
		return x;
	}
	public void setX(Position x) {
		this.x = x;
		this.sort();
	}
	public Position getY() {
		return y;
	}
	public void setY(Position y) {
		this.y = y;
		this.sort();
	}
	public Position getZ() {
		return z;
	}
	public void setZ(Position z) {
		this.z = z;
		this.sort();
	}
	
}
