package projects.reactiveSpanner;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import projects.reactiveSpanner.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.tools.Tuple;

public class NodeTriple implements Comparable<NodeTriple> {

	private Node x;
	private Node y;
	private Node z;

	// Wird auf true gesetzt, falls zwei der drei Knoten identische IDs haben.
	private boolean redundant = false;

	public NodeTriple() {
		this.x = null;
		this.y = null;
		this.z = null;
	}
	
	public NodeTriple(Node x, Node y, Node z) {
		this.x = x;
		this.y = y;
		this.z = z;

		this.sort();
	}

	/**
	 * Sortiert die Knoten des NodeTriple nach ihrer ID, wobei x die kleinste
	 * und z die größte ID haben soll.
	 */
	public void sort() {

		// Prüfen, ob Tupel existieren darf
		if (this.x.ID == this.y.ID || this.y.ID == this.z.ID
				|| this.x.ID == this.z.ID) {

			this.redundant = true;
			return;
		}

		boolean swap = true;
		Node temp;
		while (swap) {
			swap = false;

			temp = this.z;
			if (this.y.ID > temp.ID) {
				this.z = this.y;
				this.y = temp;
				swap = true;
			}

			temp = this.y;

			if (this.x.ID > temp.ID) {
				this.y = this.x;
				this.x = temp;
				swap = true;
			}
		}
	}
	
	public boolean containsNode(Node n) {
		if (this.x.equals(n) || this.y.equals(n) || this.z.equals(n)) { return true; }
		return false;
	}
	
	public boolean containsEdge(Node n1, Node n2) {
		
		if (this.x.equals(n1) || this.y.equals(n1) || this.z.equals(n1)) {
			if (this.x.equals(n2) || this.y.equals(n2) || this.z.equals(n2)) {return true;}
		}
		
		return false;
	}
	
	public static NodeTriple enclosingTriangle(Node n, Set<NodeTriple> triangles, Node[] symbolicalNodes) {
		
		for (NodeTriple triangle : triangles) {
			if (pointInTriangleTest(n.getPosition(), triangle, symbolicalNodes)) { return triangle; }
		}
		
		return null;
	}
	
	/**
	 * Checks whether a point is contained in a triangle.
	 * Code taken from StackOverflow.com
	 * Source: http://stackoverflow.com/questions/2049582/how-to-determine-a-point-in-a-triangle
	 */
	public static boolean pointInTriangleTest (Position point, NodeTriple triangle, Node[] symbolicalNodes) {
		
		boolean b1, b2, b3;

		Position a = triangle.getX().getPosition();
		Position b = triangle.getY().getPosition();
		Position c = triangle.getZ().getPosition();

		// prüfen, ob das Dreieck symbolicalNodes beinhält
		// und ggf. virtuelle Position anpassen
		
		if (symbolicalNodes.length > 0) {

			if (triangle.getX().equals(symbolicalNodes[0])) {
				
				a.yCoord = -1;
				a.xCoord = ((a.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord - 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

			if (triangle.getX().equals(symbolicalNodes[2])) {
				
				a.yCoord = -1;
				a.xCoord = ((a.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord + 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

			if (triangle.getY().equals(symbolicalNodes[0])) {
				
				b.yCoord = -1;
				b.xCoord = ((b.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord - 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

			if (triangle.getY().equals(symbolicalNodes[2])) {
				
				b.yCoord = -1;
				b.xCoord = ((b.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord + 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

			if (triangle.getZ().equals(symbolicalNodes[0])) {
				
				c.yCoord = -1;
				c.xCoord = ((c.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord - 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

			if (triangle.getZ().equals(symbolicalNodes[2])) {
				
				c.yCoord = -1;
				c.xCoord = ((c.yCoord - symbolicalNodes[1].getPosition().yCoord) / (point.yCoord - symbolicalNodes[1]
						.getPosition().yCoord))
						* ((point.xCoord + 1) - symbolicalNodes[1]
								.getPosition().xCoord)
						+ symbolicalNodes[1].getPosition().xCoord;
			}

		}
		
		b1 = sign(point, a, b) < 0.0f;
		b2 = sign(point, b, c) < 0.0f;
		b3 = sign(point, c, a) < 0.0f;

		return ((b1 == b2) && (b2 == b3));
	}
	
	/**
	 * Hilfsfunktion für pointInTriangleTest.
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return
	 */
	public static double sign(Position p1, Position p2, Position p3) {
		return (p1.xCoord - p3.xCoord) * (p2.yCoord - p3.yCoord) - (p2.xCoord - p3.xCoord) * (p1.yCoord - p3.yCoord);
	}
	
	public static NodeTriple getTriangle(Node p1, Node p2, Node p3, Set<NodeTriple> triangleSet) throws Exception {
		
		NodeTriple thisTriangle = new NodeTriple (p1, p2, p3);
		
		for (NodeTriple triangle : triangleSet) {
			if (thisTriangle.compareTo(triangle) == 0) { return triangle; }
		}
		
		throw new Exception("Triangle does not exist");
		
	}
	
	/**
	 * Punkt p, Kante (p1, p2). Dreieck (p, p1, p2) ist in tripleSet enthalten.
	 * Wir suchen das benachbarte Dreieck zur Kante (p1, p2).
	 * 
	 * @param p
	 * @param p1
	 * @param p2
	 * @param tripleSet
	 * @return
	 */
	public static NodeTriple adjacentTriangle (Node p, Node p1, Node p2, Set<NodeTriple> tripleSet) throws Exception {
		
		NodeTriple thisTriangle = new NodeTriple(p, p1, p2);
		
		for (NodeTriple triangle : tripleSet) {
			
			if (p1.equals(triangle.getX())) {
				if (p2.equals(triangle.getY())
						|| p2.equals(triangle.getZ())) {
					if ( (triangle.compareTo(thisTriangle)) != 0 ) {
						return triangle;
					}
				}
			}

			if (p1.equals(triangle.getY())) {
				if (p2.equals(triangle.getX())
						|| p2.equals(triangle.getZ())) {
					if ( (triangle.compareTo(thisTriangle)) != 0 ) {
						return triangle;
					}
				}
			}

			if (p1.equals(triangle.getZ())) {
				if (p2.equals(triangle.getX())
						|| p2.equals(triangle.getY())) {
					if ( (triangle.compareTo(thisTriangle)) != 0 ) {
						return triangle;
					}
				}
			}
			
		}
		
		throw new Exception("Couldn't find adjacentTriangle to triangle " + thisTriangle.toString() + " to edge [" + p1.ID + " " + p2.ID + "]");
		
	}
	
	/**
	 * Punkt p, Kante (p1, p2). Dreieck (p, p1, p2) ist in tripleSet enthalten.
	 * Wir suchen den unbekannten Knoten n des benachbarten Dreieck zur Kante (p1, p2).
	 * 
	 * @param p
	 * @param p1
	 * @param p2
	 * @param tripleSet
	 * @return
	 * @throws Exception
	 */
	public static Node getAdjacentTripleNode (Node p, Node p1, Node p2, Set<NodeTriple> tripleSet) throws Exception {
		
		NodeTriple adjacentTriangle = adjacentTriangle(p, p1, p2, tripleSet);
		
		if ( !p1.equals(adjacentTriangle.x) & !p2.equals(adjacentTriangle.x) ) {
			return adjacentTriangle.x;
		}
		
		if ( !p1.equals(adjacentTriangle.y) & !p2.equals(adjacentTriangle.y) ) {
			return adjacentTriangle.y;
		}
		
		if ( !p1.equals(adjacentTriangle.z) & !p2.equals(adjacentTriangle.z) ) {
			return adjacentTriangle.z;
		}
		
		throw new Exception("Couldn't find unknown Node of adjacentTriangle to triangle [" + (new NodeTriple(p, p1, p2)).toString() + "] to edge [" + p1.ID + " " + p2.ID + "]");
		
	}
	

	/**
	 * Berechnet alle möglichen Kombinationen dreier Knoten aus einer gegeben
	 * Liste von Knoten. In jeder Kombination sind keine doppelten Knoten
	 * enthalten und es existieren keine Kombinationen, die genau die drei
	 * gleichen Knoten beinhalten.
	 * 
	 * @param in
	 *            Array von mindestens drei Knoten
	 * @return ArrayList aller möglichen NodeTriple
	 */
	public static ArrayList<NodeTriple> potenzmenge(Iterable<Node> workingSet) {

		// convert workingSet to Array
		int count = 0;

		for (@SuppressWarnings("unused") Node v : workingSet) {
			count++;
		}

		Node[] in = new Node[count];
		count = 0;

		for (Node v : workingSet) {
			in[count] = v;
			count++;
		}

		ArrayList<NodeTriple> out = new ArrayList<NodeTriple>();

		Node x = new SimpleNode();
		Node y = new SimpleNode();
		Node z = new SimpleNode();

		for (int i = 0; i < in.length; i++) {
			x = in[i];

			for (int j = 0; j < in.length; j++) {
				y = in[j];

				for (int k = 0; k < in.length; k++) {
					z = in[k];

					NodeTriple temp = new NodeTriple(x, y, z);

					// unmögliche Elemente ausschließen, d.h. alle Elemente
					// überspringen, die zwei identische Attribute haben
					// UND keine doppelten Elemente hinzufügen

					// TODO Ich würde hier gerne !out.contains(temp) benutzen,
					// aber ich habe NodeTriple.equals(NodeTriple) nicht zum
					// Laufen gekriegt.

					if (!temp.isRedundant()) {

						boolean contains = false;

						Iterator<NodeTriple> iterator2 = out.iterator();

						while (iterator2.hasNext()) {
							if (temp.compareTo(iterator2.next()) == 0)
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

	/**
	 * Prüft, ob die Delaunay-Bedingung für das aktuelle Knotentripel gilt. Dies
	 * beinhaltet sowohl die Bedingung, dass keine weiteren Knoten im Umkreis
	 * der drei Knoten des Tripels liegen, sowie die Bedingung, dass keine
	 * bereits vorhandene Kante aus edgeSet von den neuen Kanten geschnitten
	 * wird.
	 * 
	 * @param workingSet
	 * @param edgeSet
	 * @return
	 */
	public boolean delaunayCondition(Iterable<Node> workingSet,
			Set<Tuple<Node, Node>> edgeSet) {

		// TODO Prüfen, ob drei Knoten auf einer Linie liegen
		// TODO Prüfen, ob vier Knoten auf einer Linie liegen
		
		// Prüfen, ob Knoten im Delaunay-Kreis sind
		Set<? extends Node> nodesInDisk = Algorithms.disk(this.x, this.y, this.z,
				workingSet).nodesInDisk;

		for (Node v : nodesInDisk) {
			if (!v.equals(this.x) & !v.equals(this.y) & !v.equals(this.z)) return false;
		}
		
		// Prüfen, ob eine neu erstellte Kante eine bereits vorhandene Kante
		// schneiden würde
		if (edgeSet == null){
			System.out.println("Es gibt keine weitere Kanten.");
			return true;}

		Line2D lineXY = new Line2D.Double(this.x.getPosition().xCoord,
				this.x.getPosition().yCoord, this.y.getPosition().xCoord,
				this.y.getPosition().yCoord);
		Line2D lineYZ = new Line2D.Double(this.y.getPosition().xCoord,
				this.y.getPosition().yCoord, this.z.getPosition().xCoord,
				this.z.getPosition().yCoord);
		Line2D lineZX = new Line2D.Double(this.z.getPosition().xCoord,
				this.z.getPosition().yCoord, this.x.getPosition().xCoord,
				this.x.getPosition().yCoord);

		for (Tuple<Node, Node> tuple : edgeSet) {

			Line2D line = new Line2D.Double(tuple.first.getPosition().xCoord,
					tuple.first.getPosition().yCoord,
					tuple.second.getPosition().xCoord,
					tuple.second.getPosition().yCoord);


			// auf Schnittpunkte bei gleichen Start-/Endknoten prüfen und ggf.
			// neuen Start-/Endpunkt setzen
			
			// wir verschieben Start-/Endknoten in Richtung des anderen Start-/Endknotens
			double shift = 0.0001;

			// Falls eine Strecke GENAU einer bereits bestehenden Strecke entspricht, dürfen wir ihre Schnittpunkte nicht prüfen,
			// sondern verwenden die bereits existente Strecke weiter.
			boolean lineXYcopy = false;
			boolean lineYZcopy = false;
			boolean lineZXcopy = false;
			
//			if (line.equals(lineXY)) lineXYcopy = true;
			if ( ( ( Math.round(line.getP1().getX()) == Math.round(lineXY.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineXY.getP1().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineXY.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineXY.getP2().getY()) ) ) || 
				 ( ( Math.round(line.getP1().getX()) == Math.round(lineXY.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineXY.getP2().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineXY.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineXY.getP1().getY()) ) )  ){
				lineXYcopy = true;
			}
			
//			if (line.equals(lineYZ)) lineYZcopy = true;
			if ( ( ( Math.round(line.getP1().getX()) == Math.round(lineYZ.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineYZ.getP1().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineYZ.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineYZ.getP2().getY()) ) ) || 
				 ( ( Math.round(line.getP1().getX()) == Math.round(lineYZ.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineYZ.getP2().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineYZ.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineYZ.getP1().getY()) ) )  ){
					lineYZcopy = true;
				}
			
//			if (line.equals(lineZX)) lineZXcopy = true;
			if ( ( ( Math.round(line.getP1().getX()) == Math.round(lineZX.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineZX.getP1().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineZX.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineZX.getP2().getY()) ) ) || 
				 ( ( Math.round(line.getP1().getX()) == Math.round(lineZX.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineZX.getP2().getY()) ) &
				   ( Math.round(line.getP2().getX()) == Math.round(lineZX.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineZX.getP1().getY()) ) )  ){
						lineZXcopy = true;
					}
			
//			if (line.getP1() == lineXY.getP1() || line.getP1() == lineXY.getP2()) {
			if ( !lineXYcopy && (
					( ( Math.round(line.getP1().getX()) == Math.round(lineXY.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineXY.getP1().getY()) ) )  ||
					( ( Math.round(line.getP1().getX()) == Math.round(lineXY.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineXY.getP2().getY()) ) ) ) ) {
				
				// line.P1 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP2().getX()-line.getP1().getX()), (line.getP2().getY()-line.getP1().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX() + shiftVector.getX(), line.getP1().getY() + shiftVector.getY(), line.getP2().getX(), line.getP2().getY());
				
			}

//			if (line.getP2() == lineXY.getP1() || line.getP2() == lineXY.getP2()) {
			if ( !lineXYcopy && (
					( ( Math.round(line.getP2().getX()) == Math.round(lineXY.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineXY.getP1().getY()) ) ) ||
					( ( Math.round(line.getP2().getX()) == Math.round(lineXY.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineXY.getP2().getY()) ) ) ) ) {
				
				// line.P2 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP1().getX()-line.getP2().getX()), (line.getP1().getY()-line.getP2().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX(), line.getP1().getY(), line.getP2().getX() + shiftVector.getX(), line.getP2().getY() + shiftVector.getY());
				
			}
			
//			if (line.getP1() == lineYZ.getP1() || line.getP1() == lineYZ.getP2()) {
			if ( !lineYZcopy && (
					( ( Math.round(line.getP1().getX()) == Math.round(lineYZ.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineYZ.getP1().getY()) ) ) ||
					( ( Math.round(line.getP1().getX()) == Math.round(lineYZ.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineYZ.getP2().getY()) ) ) ) ) {
				
				// line.P1 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP2().getX()-line.getP1().getX()), (line.getP2().getY()-line.getP1().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX() + shiftVector.getX(), line.getP1().getY() + shiftVector.getY(), line.getP2().getX(), line.getP2().getY());

			}
			
//			if (line.getP2() == lineYZ.getP1() || line.getP2() == lineYZ.getP2()) {
			if ( !lineYZcopy && (
					( ( Math.round(line.getP2().getX()) == Math.round(lineYZ.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineYZ.getP1().getY()) ) ) ||
					( ( Math.round(line.getP2().getX()) == Math.round(lineYZ.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineYZ.getP2().getY()) ) ) ) ) {	
			
				// line.P2 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP1().getX()-line.getP2().getX()), (line.getP1().getY()-line.getP2().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX(), line.getP1().getY(), line.getP2().getX() + shiftVector.getX(), line.getP2().getY() + shiftVector.getY());
				
			}
			
//			if (line.getP1() == lineZX.getP1() || line.getP1() == lineZX.getP2()) {
			if ( !lineZXcopy && (
					( ( Math.round(line.getP1().getX()) == Math.round(lineZX.getP1().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineZX.getP1().getY()) ) ) ||
					( ( Math.round(line.getP1().getX()) == Math.round(lineZX.getP2().getX()) ) & ( Math.round(line.getP1().getY()) == Math.round(lineZX.getP2().getY()) ) ) ) ) {
				
				// line.P1 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP2().getX()-line.getP1().getX()), (line.getP2().getY()-line.getP1().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX() + shiftVector.getX(), line.getP1().getY() + shiftVector.getY(), line.getP2().getX(), line.getP2().getY());
				
			}
			
//			if (line.getP2() == lineZX.getP1() || line.getP2() == lineZX.getP2()) {
			if ( !lineZXcopy && (
					( ( Math.round(line.getP2().getX()) == Math.round(lineZX.getP1().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineZX.getP1().getY()) ) ) ||
					( ( Math.round(line.getP2().getX()) == Math.round(lineZX.getP2().getX()) ) & ( Math.round(line.getP2().getY()) == Math.round(lineZX.getP2().getY()) ) ) ) ) {
				
				// line.P2 muss geshifted werden!
				Point2D v = new Point2D.Double((line.getP1().getX()-line.getP2().getX()), (line.getP1().getY()-line.getP2().getY()));
				Point2D shiftVector = new Point2D.Double(v.getX()*shift, v.getY()*shift);
				line.setLine(line.getP1().getX(), line.getP1().getY(), line.getP2().getX() + shiftVector.getX(), line.getP2().getY() + shiftVector.getY());
				
			}

			// auf Schnittpunkte prüfen
			if (!lineXYcopy && lineXY.intersectsLine(line)) return false;
			if (!lineYZcopy && lineYZ.intersectsLine(line)) return false;
			if (!lineZXcopy && lineZX.intersectsLine(line))	return false;
		}

		return true;
	}

	public String toString() {
		return Integer.toString(this.x.ID) + " " + Integer.toString(this.y.ID)
				+ " " + Integer.toString(this.z.ID);
	}

	@Override
	public int compareTo(NodeTriple arg0) {

		this.sort();
		arg0.sort();

		if (this.x.ID < arg0.x.ID)
			return -1;
		if (this.x.ID > arg0.x.ID)
			return 1;

		if (this.y.ID < arg0.y.ID)
			return -1;
		if (this.y.ID > arg0.y.ID)
			return 1;

		if (this.z.ID < arg0.z.ID)
			return -1;
		if (this.z.ID > arg0.z.ID)
			return 1;

		return 0;
	}

	public boolean equals(NodeTriple arg0) {
		if (this.compareTo(arg0) == 0)
			return true;
		else
			return false;
	}

	public Node getX() {
		return x;
	}

	public void setX(Node x) {
		this.x = x;
		this.sort();
	}

	public Node getY() {
		return y;
	}

	public void setY(Node y) {
		this.y = y;
		this.sort();
	}

	public Node getZ() {
		return z;
	}

	public void setZ(Node z) {
		this.z = z;
		this.sort();
	}

	public boolean isRedundant() {
		return redundant;
	}

	public void setRedundant(boolean redundant) {
		this.redundant = redundant;
	}
}
