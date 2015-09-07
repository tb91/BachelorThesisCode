package delaunay_triangulation;

/**
 * Adapted from benmoshe@cs.bgu.ac.il.
 * 
 * @see http://www.cs.bgu.ac.il/~benmoshe/DT/Delaunay%20Triangulation%20in%20Java.htm
 */
class Circle_dt {
	Point_dt c;
	double r;

	public Circle_dt() {
	}

	public Circle_dt(Point_dt c, double r) {
		this.c = c;
		this.r = r;
	}

	public Circle_dt(Circle_dt circ) {
		this.c = circ.c;
		this.r = circ.r;
	}

	public String toString() {
		return new String(" Circle[" + this.c.toString() + "|" + this.r + "|"
				+ (int) Math.round(Math.sqrt(this.r)) + "]");
	}
}