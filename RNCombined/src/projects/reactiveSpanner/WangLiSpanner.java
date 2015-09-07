package projects.reactiveSpanner;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import projects.reactiveSpanner.nodes.nodeImplementations.PhysicalGraphNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.tools.Tools;
import sinalgo.tools.Tuple;
import delaunay_triangulation.Delaunay_Triangulation;
import delaunay_triangulation.Point_dt;
import delaunay_triangulation.Triangle_dt;

public class WangLiSpanner {

	public static HashMap<Node, Set<Node>> buildWangLiSpanner(Set<Node> workingSet)
		{
			// Delaunay Graph bauen
			HashMap<Node, Set<Node>> adjazenzliste = buildDelaunayTriangulationNew(workingSet);
			
			// alle Kanten mit einer Laenge > 1 loeschen
			
			Set<Tuple<Node,Node>> longEdge = new HashSet<Tuple<Node,Node>>();
			
			for (Node n : workingSet) {
	
				Set<Node> delaunayNeighbors = adjazenzliste.get(n);
	
				for (Node dn : delaunayNeighbors) {
					try {
						if (n.getPosition().distanceTo(dn.getPosition()) > Configuration.getDoubleParameter("UDG/rMax")) {
							longEdge.add(new Tuple<Node,Node>(n,dn));
						}
					} catch (CorruptConfigurationEntryException e) {
						e.printStackTrace();
					}
				}
			}
			
			for (Tuple<Node,Node> tuple : longEdge) {
				adjazenzliste.get(tuple.first).remove(tuple.second);
				adjazenzliste.get(tuple.second).remove(tuple.first);
			}
	
			
			// Ordnung Pi erstellen
			
			//graph_i merkt sich alle Knoten, die zum Graphen i gehoeren.
			HashMap<Integer, Set<Node>> graph_i = new HashMap<Integer, Set<Node>>();
			graph_i.put(1, workingSet);
			
			// pi_u merkt sich die Ordnungsnummer Pi eines jeden Knoten u.
			// Key ist die jeweilige ID eines Knoten.
			HashMap<Integer, Integer> pi_u = new HashMap<Integer, Integer>();
			
			for (int i = 1; i < workingSet.size() + 1; i++) {
	
				// Graph i+1 entspricht Graph i
				Set<Node> oldGraph_i = new HashSet<Node>();
				oldGraph_i.addAll(graph_i.get(i));
				graph_i.put(i + 1, oldGraph_i);
	
				// Wir entfernen aus Graph i+1 den Knoten n mit dem kleinsten Grad
				Iterator<Node> tempIterator = graph_i.get(i + 1).iterator();
				// (beliebigen Knoten w�hlen)
				Node smallDegree = tempIterator.next();
	
				for (Node n : graph_i.get(i + 1)) {
	
					if (adjazenzliste.get(n).size() < adjazenzliste.get(smallDegree).size()) {
						smallDegree = n;
					}
	
					// Haben zwei Knoten den gleichen Knotengrad, sortieren wir nach ID
					if (adjazenzliste.get(n).size() == adjazenzliste.get(smallDegree).size()) {
						if (n.ID < smallDegree.ID)
							smallDegree = n;
					}
				}
	
				pi_u.put(smallDegree.ID, (workingSet.size() - i + 1));
	
				graph_i.get(i + 1).remove(smallDegree);
			}
			
			// initialisieren der WangLi-Kantenliste E'
			Set<Tuple<Node,Node>> edgeSet = new HashSet<Tuple<Node,Node>>();
			
			// initialisieren der Liste markierter Knoten
			Set<Node> markedNodes = new HashSet<Node>();
			
			// unmarkierten Knoten mit kleinstem Pi finden
			Node smallestPiNode = null;
			
			while (markedNodes.size() != workingSet.size() ) {
				
				int smallestNodePiVal = Integer.MAX_VALUE;
	
				for (Node n : workingSet) {
	
					if (markedNodes.contains(n))
						continue;
	
					int temp = pi_u.get(n.ID);
					if (temp < smallestNodePiVal) {
						smallestNodePiVal = temp;
						smallestPiNode = n;
					}
				}
			
			// Gebiet in Sektoren unterteilen
			
				// markierte Nachbarn des smallestPiNode finden
				// und "Geraden" (=rays) vom smallestPiNode zu jedem markiertem Nachbarn ziehen
				Set<Node> markedNeighbors = new HashSet<Node>();
				Set<Node> rays = new HashSet<Node>();
				
				for (Node n : markedNodes) {
					if (adjazenzliste.get(smallestPiNode).contains(n)) {
						markedNeighbors.add(n);
						rays.add(n);
					}
				}
				
				// Winkel benachbarter Geraden ausmessen
				// Geraden sind benachbart, wenn sie aufeinanderfolgende Ordnungsnummern in counterClock haben
			
				// Knoten in rays sortieren
				// Der Key ist die ID eines Knotens, die Value ist die
				// Ordnungsnummer des jeweiligen Knotens.
				HashMap<Integer, Integer> counterClock = sortNodesCCW(rays, smallestPiNode);
	
				// Winkel benachbarter Kanten ausmessen und ggf. "virtuelle Knoten" einf�gen
				
				double alpha = -Math.PI/1.5;
			
				// falls es keine Knoten in rays gibt: sechs virtuelle Knoten erzeugen und Geraden durch sie durchziehen
				if (rays.isEmpty() || rays.size() == 1) {
				
					Node virtualPosition1 = new PhysicalGraphNode();
	
					if (rays.isEmpty()) {
						virtualPosition1.setPosition(
								smallestPiNode.getPosition().xCoord,
								smallestPiNode.getPosition().yCoord + 3, 0);
	
						rays.add(virtualPosition1);
					} else {
						for (Node n : rays) {
							virtualPosition1 = n;//FIXME: vivas? what did you do here? (tim)
						}
					}
				
					Node virtualPosition2 = nextVirtualNode(smallestPiNode, virtualPosition1, alpha);
					Node virtualPosition3 = nextVirtualNode(smallestPiNode, virtualPosition2, alpha);
					Node virtualPosition4 = nextVirtualNode(smallestPiNode, virtualPosition3, alpha);
					Node virtualPosition5 = nextVirtualNode(smallestPiNode, virtualPosition4, alpha);
					Node virtualPosition6 = nextVirtualNode(smallestPiNode, virtualPosition5, alpha);
					
					rays.add(virtualPosition2);
					rays.add(virtualPosition3);
					rays.add(virtualPosition4);
					rays.add(virtualPosition5);
					rays.add(virtualPosition6);
				
					counterClock = sortNodesCCW(rays, smallestPiNode);
				
				} else {
				
					// Liste der Knoten in counterClock iterieren und schauen, ob zwei benachbarte Knoten (=Geraden) zu weit voneinander entfernt sind.
					// ggf. neuen Knoten (=Gerade) in rays einfuegen und counterClock mit der neuen rays-Liste updaten.
				
					boolean done = false;
					while (!done) {
	
						Node n1 = null;
						Node n2 = null;
	
						boolean lastPair = false;
					
						for (int g = 1; g < counterClock.size() + 1; g++) {
	
							// benachbarte Knoten (=Geraden) n1 und n2 besorgen
							for (Node ray : rays) {
	
								if (g == counterClock.size()) {
									if (counterClock.get(ray.ID) == g)
										n1 = ray;
									if (counterClock.get(ray.ID) == 1)
										n2 = ray;
	
									lastPair = true;
	
								} else {
									if (counterClock.get(ray.ID) == g)
										n1 = ray;
									if (counterClock.get(ray.ID) == g + 1)
										n2 = ray;
								}
	
							}
					
							// Winkel der benachbarten Knoten (=Geraden) messen
					
							double angleN1N2 = Algorithms.getAngleBetween2D(n1.getPosition(), smallestPiNode.getPosition(), n2.getPosition(), true);
							
							if (angleN1N2 > Math.PI/3) {
								// Winkel zu gross? Neuen Knoten (=Gerade) in rays einfuegen und counterClock mit der neuen rays-Liste updaten; wiederholen
						
								Node virtualPosition = nextVirtualNode(smallestPiNode, n1, alpha);
								rays.add(virtualPosition);
						
								break;
							}
					
							if (lastPair)
								done = true;
						} // end for
				
						counterClock = sortNodesCCW(rays, smallestPiNode);
				
				} // end while
			} // end else
			
				// Kanten zum edgeSet hinzufuegen
	
				// f�r alle Knoten der Nachbarschaft (die NICHT in rays sind)
				// pr�fen, zu welchem Sektor sie geh�ren
	
				// ein Sektor ist ein Tupel zweier benachbarter Knoten aus rays
				Set<Tuple<Node, Node>> sector = new HashSet<Tuple<Node, Node>>();
	
				for (int g = 1; g < counterClock.size() + 1; g++) {
	
					Node n1 = null;
					Node n2 = null;
	
					// benachbarte Knoten (=Geraden) n1 und n2 besorgen
					for (Node ray : rays) {
	
						if (g == counterClock.size()) {
							if (counterClock.get(ray.ID) == g)
								n1 = ray;
							if (counterClock.get(ray.ID) == 1)
								n2 = ray;
	
						} else {
	
							if (counterClock.get(ray.ID) == g)
								n1 = ray;
							if (counterClock.get(ray.ID) == g + 1)
								n2 = ray;
						}
					}
	
					sector.add(new Tuple<Node, Node>(n1, n2));
	
				}
				
				// Geraden eines Sektors in Line2D konvertieren, um Positionen
				// umliegender Knoten bestimmen zu koennen
				Set<Tuple<Line2D, Line2D>> sectorLines = new HashSet<Tuple<Line2D, Line2D>>();
				
				for (Tuple<Node, Node> t : sector) {
					
					Line2D firstLine = new Line2D.Double(smallestPiNode.getPosition().xCoord, smallestPiNode.getPosition().yCoord, t.first.getPosition().xCoord, t.first.getPosition().yCoord);
					Line2D secondLine = new Line2D.Double(smallestPiNode.getPosition().xCoord, smallestPiNode.getPosition().yCoord, t.second.getPosition().xCoord, t.second.getPosition().yCoord);
					Tuple<Line2D,Line2D> lineTuple = new Tuple<Line2D,Line2D>(firstLine,secondLine);
					sectorLines.add(lineTuple);
								
				}
			
				// Liste unmarkierter Knoten erstellen (wir betrachten nur unmarkierte Knoten
				// aus der UDel-Nachbarschaft des smallestPiNode!)
				Set<Node> unMarkedNodes = new HashSet<Node>();
				for (Node n : adjazenzliste.get(smallestPiNode)) {
					if (!markedNodes.contains(n))
						unMarkedNodes.add(n);
				}
			
				// f�r jeden unmarkierten Knoten pr�fen, in welchem Kegel (cone) er ist
				HashMap<Tuple<Line2D,Line2D>,HashSet<Node>> cones = new HashMap<Tuple<Line2D,Line2D>,HashSet<Node>>();
			
				for (Node n : unMarkedNodes) {
					
					for (Tuple<Line2D, Line2D> tuple : sectorLines) {
	
						if (tuple.first.relativeCCW(n.getPosition().xCoord, n.getPosition().yCoord) == -1
								& tuple.second.relativeCCW(n.getPosition().xCoord, n.getPosition().yCoord) == 1) {
							if (!cones.containsKey(tuple)) {
								HashSet<Node> temp = new HashSet<Node>();
								temp.add(n);
								cones.put(tuple, temp);
								
							} else {
								cones.get(tuple).add(n);
							}
	
						}
					}
				}
				
				// Kanten innerhalb eines Kegels erzeugen
				for (Tuple<Line2D, Line2D> tuple : sectorLines) {
					
	//				System.out.println("Sektor: " + Math.round(tuple.first.getX2())
	//						+ "|" + Math.round(tuple.first.getY2()) + "-"
	//						+ tuple.second.getX2() + "|"
	//						+ tuple.second.getY2()); // TODO delete
					
					HashSet<Node> coneNodeSet = cones.get(tuple);
	
					if (coneNodeSet == null) {continue;}
	
					// " For each cone, first add the shortest edge us_i in E to E' "
					Node shortestEdgeNode = null;
	
					double shortestEdge = java.lang.Double.MAX_VALUE;
	
					for (Node n : coneNodeSet) {
						double x = n.getPosition().xCoord - smallestPiNode.getPosition().xCoord;
						double y = n.getPosition().yCoord - smallestPiNode.getPosition().yCoord;
	
						double edgeLength = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	
						if (edgeLength < shortestEdge) {
							shortestEdge = edgeLength;
							shortestEdgeNode = n;
						}
					}
	
					// k�rzeste Kante von center zu einem Knoten erzeugen
					edgeSet.add(new Tuple<Node, Node>(smallestPiNode, shortestEdgeNode));
	//				System.out.println("	k�rzeste Kante: " + smallestPiNode.ID + "-" + shortestEdgeNode.ID); // TODO delete
					// restliche Kanten erzeugen
	
					// Knoten geometrisch ordnen
					HashMap<Integer, Integer> coneNodeCCW = sortNodesCCW(coneNodeSet, smallestPiNode);
	
					for (int g = 1; g < coneNodeCCW.size(); g++) {
	
						Node n1 = null;
						Node n2 = null;
	
						// benachbarte Knoten (=Geraden) n1 und n2 besorgen
						for (Node n : coneNodeSet) {
	
							if (coneNodeCCW.get(n.ID) == g)
								n1 = n;
							if (coneNodeCCW.get(n.ID) == g + 1)
								n2 = n;
	
						}
						
						edgeSet.add(new Tuple<Node,Node>(n1, n2));
	//					System.out.println("	Kante: " + n1.ID + "-" + n2.ID); // TODO delete
					}
	
				}
				
				// Knoten als bearbeitet markieren
				markedNodes.add(smallestPiNode);
				
			}
			
			
			// Kanten in Adjazenzliste eintragen
			for (Node n : workingSet) {
				adjazenzliste.get(n).clear();
			}
			
			for (Tuple<Node, Node> t : edgeSet) {
				adjazenzliste.get(t.first).add(t.second);
				adjazenzliste.get(t.second).add(t.first);
			}
			
			return adjazenzliste;
		}

	/**
	 * Sortiert Knoten eines Sets workingSet gegen den Uhrzeigersinn (im Sinalgo-Framework!)
	 * um einen Knoten center. Der Key ist die ID eines Knotens, die Value ist die
	 * Ordnungsnummer des jeweiligen Knotens.
	 **/
	public static HashMap<Integer, Integer> sortNodesCCW(Set<Node> workingSet, Node center){
		
		HashMap<Integer,Integer> counterClock = new HashMap<Integer,Integer>();
		
		Position xAxis = new Position(center.getPosition().xCoord + 1, center.getPosition().yCoord, 0);
		
		int counter = 1;
		Node smallestAngleNode = null;
		
		while (counterClock.size() != workingSet.size()) {
			
			double smallestAngle = java.lang.Double.MAX_VALUE;
			
			for (Node n : workingSet) {
				
				if (counterClock.containsKey(n.ID))
					continue;
				
				double angle = Algorithms.getAngleBetween2D(xAxis, center.getPosition(), n.getPosition(), true);
				
				if (angle < smallestAngle) {
					smallestAngleNode = n;
					smallestAngle = angle;
				}
			}
			
			counterClock.put(smallestAngleNode.ID, counter++);
			
		}
		
		return counterClock;
	}

	/**
	 * Sortiert Positions eines Sets workingSet gegen den Uhrzeigersinn (im Sinalgo-Framework!)
	 * um eine Position center. Die Value ist die Position, der Key ist die Ordnungsnummer.
	 **/
	public static HashMap<Integer, Position> sortPositionsCCW(Set<Position> workingSet, Position center){
		
		HashMap<Integer, Position> counterClock = new HashMap<Integer, Position>();
		
		Position xAxis = new Position(center.xCoord + 1, center.yCoord, 0);
		
		int counter = 1;
		Position smallestAnglePos = new Position();
		
		while (counterClock.size() != workingSet.size()) {
			
			double smallestAngle = java.lang.Double.MAX_VALUE;
			
			for (Position p : workingSet) {
				
				if (counterClock.containsValue(p))
					continue;
				
				double angle = Algorithms.getAngleBetween2D(xAxis, center, p, true);
				
				if (angle < smallestAngle) {
					smallestAnglePos = p;
					smallestAngle = angle;
				}
			}
			
			counterClock.put(counter++, smallestAnglePos);
			
		}
		
		return counterClock;
	}

	public static Position checkLexicographicOrder(Set<Position> workingSet, Position p, boolean add, boolean clockwise) {
		
		System.out.println("Untersuchte Position: " + p.toString());
		
		Position result = p;
		
		ArrayList<Position> lexicographicOrder = new ArrayList<Position>();
		
		// alle Knoten lexikographisch sortieren
		while(lexicographicOrder.size() < workingSet.size()) {
			
			Position temp = new Position(0, Double.MIN_VALUE, 0);
		
			for (Position pos : workingSet) {
				
				if (lexicographicOrder.contains(pos)) {
					continue;
				}
			
				if (pos.yCoord > temp.yCoord) {
					temp = pos;
				} else {
			
					if (pos.yCoord == temp.yCoord) {
						if (pos.xCoord > temp.xCoord) {
							temp = pos;
						}
					}
				
				}
			}
			
			lexicographicOrder.add(temp);
		}
		
		boolean notEquals = true;
		
		while (notEquals) {
			
			System.out.println("");
			System.out.println("Aktuelle Position: " + result.toString());
			
			// Knoten um den Knoten p sortieren
			ArrayList<Position> clockwiseOrder = new ArrayList<Position>();
	
			HashMap<Integer, Position> cClockwise = sortPositionsCCW(workingSet, result);
			
			for (int i = 1; i <= cClockwise.size(); i++) {
				clockwiseOrder.add(cClockwise.get(i));
			}
			
			if (clockwise) {
				Collections.reverse(clockwiseOrder);
			}
			
			// clockwiseOrder so sortieren, dass die Reihenfolge richtig bleibt,
			// aber das erste Element in clockwiseOrder dem ersten Element in lexicographicOrder entspricht
			
			boolean sortingDone = false;
			
			while (!sortingDone) {
				
				sortingDone = true;
				
				if (!PositionTriple.comparePosition(lexicographicOrder.get(0),
						clockwiseOrder.get(0))) {
					Position temp = clockwiseOrder.get(0);
					clockwiseOrder.remove(0);
					clockwiseOrder.add(temp);
					
					sortingDone = false;
				}
			}
			
			// kontrollieren, ob beide Listen identisch sind
			
			Iterator<Position> lexicoIterator = lexicographicOrder.iterator();
			Iterator<Position> clockIterator = clockwiseOrder.iterator();
			
			System.out.println("clockwiseOrder: " + clockwiseOrder.toString());
			System.out.println("lexicographicOrder: " + lexicographicOrder.toString());
			
			notEquals = false;
			
			while (lexicoIterator.hasNext()) {
			
				if (!clockIterator.hasNext()) {
					
					if (add) {
						result.xCoord += 0.1;
					} else {
						result.xCoord -= 0.1;
					}
					
					notEquals = true;
					
					break;
				}
				
				if (!PositionTriple.comparePosition(lexicoIterator.next(), clockIterator.next())) {
					
					if (add) {
						result.xCoord += 0.1;
					} else {
						result.xCoord -= 0.1;
					}
					
					notEquals = true;
					
					break;
				}
			}
		}
		
		return result;
	}

	/**
	 * Hilfsfunktion f�r buildWangLiSpanner. Erzeugt einen Knoten,
	 * dessen Position der Position des lastNode entspricht, die um alpha Grad
	 * um den centerNode rotiert wurde.
	 * 
	 * @param centerNode
	 * @param lastNode
	 * @param alpha
	 * @return
	 */
	public static Node nextVirtualNode(Node centerNode, Node lastNode, double alpha){
		
		Node virtualNode = new PhysicalGraphNode();
		virtualNode.setPosition(nextVirtualPosition(centerNode, lastNode, alpha));
		
		return virtualNode;
		
	}

	/**
	 * Hilfsfunktion f�r buildWangLiSpanner. Erzeugt eine Position,
	 * welche der Position des lastNode entspricht, die um alpha Grad
	 * um den centerNode rotiert wurde.
	 * 
	 * @param centerNode
	 * @param lastNode
	 * @param alpha
	 * @return
	 */
	public static Position nextVirtualPosition(Node centerNode, Node lastNode, double alpha){
		
		Position virtualPosition = new Position();
		double x = ((centerNode.getPosition().xCoord - lastNode.getPosition().xCoord) * Math.cos(alpha)) - ((centerNode.getPosition().yCoord - lastNode.getPosition().yCoord) * Math.sin(alpha)) + centerNode.getPosition().xCoord;
		double y = ((centerNode.getPosition().yCoord - lastNode.getPosition().yCoord) * Math.cos(alpha)) + ((centerNode.getPosition().xCoord - lastNode.getPosition().xCoord) * Math.sin(alpha)) + centerNode.getPosition().yCoord;
		
		while (x < 0 || y < 0 || x > Configuration.dimX || y > Configuration.dimY) {
			double temp_x = x;
			double temp_y = y;
			
			if (x < 0) {
				temp_x = 0;
				temp_y = ((y - centerNode.getPosition().yCoord)/(x - centerNode.getPosition().xCoord)) * (temp_x - centerNode.getPosition().xCoord) + centerNode.getPosition().yCoord;
				x = temp_x;
				y = temp_y;
				continue;
			}
			
			if (y < 0) {
				temp_y = 0;
				temp_x = ((temp_y - centerNode.getPosition().yCoord)/(y - centerNode.getPosition().yCoord)) * (x - centerNode.getPosition().xCoord) + centerNode.getPosition().xCoord;
				x = temp_x;
				y = temp_y;
				continue;
			}
			
			if (x > Configuration.dimX) {
				temp_x = Configuration.dimX;
				temp_y = ((y - centerNode.getPosition().yCoord)/(x - centerNode.getPosition().xCoord)) * (temp_x - centerNode.getPosition().xCoord) + centerNode.getPosition().yCoord;
				x = temp_x;
				y = temp_y;
				continue;
			}
			
			if (y > Configuration.dimY) {
				temp_y = Configuration.dimY;
				temp_x = ((temp_y - centerNode.getPosition().yCoord)/(y - centerNode.getPosition().yCoord)) * (x - centerNode.getPosition().xCoord) + centerNode.getPosition().xCoord;
				x = temp_x;
				y = temp_y;
				continue;
			}
		}
		
		virtualPosition.assign(x, y, 0);
		return virtualPosition;
		
	}

	public static HashMap<Node, Set<Node>> buildDelaunayTriangulation(Set<Node> workingSet)
		{
			
			HashMap<Node, Set<Node>> adjazenzliste = new HashMap<Node, Set<Node>>();
			
			for (Node n : workingSet) {
				adjazenzliste.put(n, new HashSet<Node>());
			}
			
			// Falls es nur zwei Knoten gibt, trotzdem Kante erzeugen
			if (workingSet.size() == 2) {
				Iterator<Node> iterator = workingSet.iterator();
				Node n1 = iterator.next();
				Node n2 = iterator.next();
				
				Set<Node> list1 = new HashSet<Node>();
				Set<Node> list2 = new HashSet<Node>();
				list1.add(n2);
				list2.add(n1);
				
				adjazenzliste.put(n1, list1);
				adjazenzliste.put(n2, list2);
				
				return adjazenzliste;
			}
			
			ArrayList<NodeTriple> triples = NodeTriple.potenzmenge(workingSet);
			Set<Tuple<Node,Node>> edgeSet = new HashSet<Tuple<Node,Node>>();
			
			for(NodeTriple triple : triples){
				if(triple.delaunayCondition(workingSet, edgeSet)) {
					
					Node x = triple.getX();
					Node y = triple.getY();
					Node z = triple.getZ();
					
					adjazenzliste.get(x).add(y);
					adjazenzliste.get(x).add(z);
					
					adjazenzliste.get(y).add(x);
					adjazenzliste.get(y).add(z);
					
					adjazenzliste.get(z).add(x);
					adjazenzliste.get(z).add(y);
					
					edgeSet.add(new Tuple<Node, Node>(triple.getX(),triple.getY()));
					edgeSet.add(new Tuple<Node, Node>(triple.getY(),triple.getZ()));
					edgeSet.add(new Tuple<Node, Node>(triple.getZ(),triple.getX()));
				}
			}
			
			Tools.repaintGUI();
			
	//		System.out.println(adjazenzliste.toString());
			
			return adjazenzliste;
		}

	public static HashMap<Node, Set<Node>> buildDelaunayTriangulationNew (Set<Node> workingSet) {
		
		if (workingSet.size() < 4) return simpleDelaunayTriangulation(workingSet);
		
		HashMap<Node, Set<Node>> adjazenzliste = new HashMap<Node, Set<Node>>();
		
		for (Node n : workingSet){
			adjazenzliste.put(n,  new HashSet<Node>());
		}
		
		// Liste aller Knotenpositionen
		Set<Position> nodePositionSet = new HashSet<Position>();
		
		// Liste und HashMap f�llen
		for (Node n : workingSet) {
			nodePositionSet.add(n.getPosition());
		}
		
		// Array von Points zur Berechnung der DelTriangulation vorbereiten
		Point_dt[] pointArray = new Point_dt[workingSet.size()];
		
		int count = 0;
		for (Position p : nodePositionSet) {
			Point_dt tempPoint = new Point_dt(p.xCoord, p.yCoord, p.zCoord);
			pointArray[count] = tempPoint;
			count++;
		}
		
		// DelTriangulation berechnen
		Delaunay_Triangulation delTri = new Delaunay_Triangulation(pointArray);
		
		Iterator<Triangle_dt> trianglesIterator = delTri.trianglesIterator();
		
		while (trianglesIterator.hasNext()) {
			Triangle_dt triangle = trianglesIterator.next();
			
			Position tempPos1 = new Position(triangle.p1().x(), triangle.p1().y(), triangle.p1().z());
			Position tempPos2 = new Position(triangle.p2().x(), triangle.p2().y(), triangle.p2().z());
			Position tempPos3 = new Position();
			boolean tempPos3exists = false;
			if (triangle.p3() != null) {
				tempPos3 = new Position(triangle.p3().x(), triangle.p3()
						.y(), triangle.p3().z());
				tempPos3exists = true;
			}
			
			// Kante 1-2 und Kante 1-3 hinzuf�gen
			adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos1, workingSet)).add(Algorithms.getNodeAtPosition(tempPos2, workingSet));
			if (tempPos3exists) {
				adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos1, workingSet)).add(
						Algorithms.getNodeAtPosition(tempPos3, workingSet));
			}
	
			// Kante 2-1 und Kante 2-3 hinzuf�gen
			adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos2, workingSet)).add(Algorithms.getNodeAtPosition(tempPos1, workingSet));
			if (tempPos3exists) {
				adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos2, workingSet)).add(
						Algorithms.getNodeAtPosition(tempPos3, workingSet));
			}
			
			if (tempPos3exists) {
				// Kante 3-1 und Kante 3-2 hinzuf�gen
				adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos3, workingSet)).add(
						Algorithms.getNodeAtPosition(tempPos1, workingSet));
				adjazenzliste.get(Algorithms.getNodeAtPosition(tempPos3, workingSet)).add(
						Algorithms.getNodeAtPosition(tempPos2, workingSet));
			}
		}
		
		return adjazenzliste;
		
	}

	/**
	 * DelaunayTriangualtion f�r ein bis drei Knoten
	 * 
	 * @param workingSet
	 * @return
	 */
	public static HashMap<Node, Set<Node>> simpleDelaunayTriangulation (Set<Node> workingSet) {
		
		HashMap<Node, Set<Node>> adjazenzliste = new HashMap<Node, Set<Node>>();
		for (Node n : workingSet) {
			adjazenzliste.put(n, new HashSet<Node>());
		}
	
		// Falls es nur einen Knoten gibt
		if (workingSet.size() == 1) return adjazenzliste;
		
		// Falls es zwei Knoten gibt
		if (workingSet.size() == 2) {
			Iterator<Node> iterator = workingSet.iterator();
			Node n1 = iterator.next();
			Node n2 = iterator.next();
	
			Set<Node> list1 = new HashSet<Node>();
			Set<Node> list2 = new HashSet<Node>();
			list1.add(n2);
			list2.add(n1);
	
			adjazenzliste.put(n1, list1);
			adjazenzliste.put(n2, list2);
	
			return adjazenzliste;
		} else
	
		// Falls es drei Knoten gibt
		{
			Iterator<Node> iterator = workingSet.iterator();
			Node n1 = iterator.next();
			Node n2 = iterator.next();
			Node n3 = iterator.next();
	
			Set<Node> list1 = new HashSet<Node>();
			Set<Node> list2 = new HashSet<Node>();
			Set<Node> list3 = new HashSet<Node>();
			list1.add(n2);
			list1.add(n3);
			list2.add(n1);
			list2.add(n3);
			list3.add(n1);
			list3.add(n2);
	
			adjazenzliste.put(n1, list1);
			adjazenzliste.put(n2, list2);
			adjazenzliste.put(n3, list3);
	
			return adjazenzliste;
		}
				
	}

}
