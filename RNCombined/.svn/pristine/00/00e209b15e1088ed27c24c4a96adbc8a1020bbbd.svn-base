package delaunay_triangulation;

import java.util.Comparator;

/**
 * Adapted from benmoshe@cs.bgu.ac.il.
 * 
 * @see http://www.cs.bgu.ac.il/~benmoshe/DT/Delaunay%20Triangulation%20in%20Java.htm
 */
public class Point_dt {
	double x;
	double y;
	double z;
	public static final int ONSEGMENT = 0;
	public static final int LEFT = 1;
	public static final int RIGHT = 2;
	public static final int INFRONTOFA = 3;
	public static final int BEHINDB = 4;
	public static final int ERROR = 5;

	public Point_dt() {
	}

	public Point_dt(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point_dt(double x, double y) {
		this(x, y, 0.0D);
	}

	public Point_dt(Point_dt p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	public double x() {
		return this.x;
	}

	public double y() {
		return this.y;
	}

	public double z() {
		return this.z;
	}

	double distance2(Point_dt p) {
		return (p.x - this.x) * (p.x - this.x) + (p.y - this.y)
				* (p.y - this.y);
	}

	double distance2(double px, double py) {
		return (px - this.x) * (px - this.x) + (py - this.y) * (py - this.y);
	}

	boolean isLess(Point_dt p) {
		return (this.x < p.x) || ((this.x == p.x) && (this.y < p.y));
	}

	boolean isGreater(Point_dt p) {
		return (this.x > p.x) || ((this.x == p.x) && (this.y > p.y));
	}

	public boolean equals(Point_dt p) {
		return (this.x == p.x) && (this.y == p.y);
	}

	public String toString() {
		return new String(" Pt[" + this.x + "," + this.y + "," + this.z + "]");
	}

	public double distance(Point_dt p) {
		double temp = Math.pow(p.x() - this.x, 2.0D)
				+ Math.pow(p.y() - this.y, 2.0D);
		return Math.sqrt(temp);
	}

	public double distance3D(Point_dt p) {
		double temp = Math.pow(p.x() - this.x, 2.0D)
				+ Math.pow(p.y() - this.y, 2.0D)
				+ Math.pow(p.z() - this.z, 2.0D);
		return Math.sqrt(temp);
	}

	public String toFile() {
		return this.x + " " + this.y + " " + this.z;
	}

	String toFileXY() {
		return this.x + " " + this.y;
	}

	public int pointLineTest(Point_dt a, Point_dt b) {
		double dx = b.x - a.x;
		double dy = b.y - a.y;
		double res = dy * (this.x - a.x) - dx * (this.y - a.y);

		if (res < 0.0D)
			return 1;
		if (res > 0.0D)
			return 2;

		if (dx > 0.0D) {
			if (this.x < a.x)
				return 3;
			if (b.x < this.x)
				return 4;
			return 0;
		}
		if (dx < 0.0D) {
			if (this.x > a.x)
				return 3;
			if (b.x > this.x)
				return 4;
			return 0;
		}
		if (dy > 0.0D) {
			if (this.y < a.y)
				return 3;
			if (b.y < this.y)
				return 4;
			return 0;
		}
		if (dy < 0.0D) {
			if (this.y > a.y)
				return 3;
			if (b.y > this.y)
				return 4;
			return 0;
		}
		System.out.println("Error, pointLineTest with a=b");
		return 5;
	}

	boolean areCollinear(Point_dt a, Point_dt b) {
		double dx = b.x - a.x;
		double dy = b.y - a.y;
		double res = dy * (this.x - a.x) - dx * (this.y - a.y);
		return res == 0.0D;
	}

	Point_dt circumcenter(Point_dt a, Point_dt b) {
		double u = ((a.x - b.x) * (a.x + b.x) + (a.y - b.y) * (a.y + b.y)) / 2.0D;
		double v = ((b.x - this.x) * (b.x + this.x) + (b.y - this.y)
				* (b.y + this.y)) / 2.0D;
		double den = (a.x - b.x) * (b.y - this.y) - (b.x - this.x)
				* (a.y - b.y);
		if (den == 0.0D)
			System.out.println("circumcenter, degenerate case");
		return new Point_dt((u * (b.y - this.y) - v * (a.y - b.y)) / den, (v
				* (a.x - b.x) - u * (b.x - this.x))
				/ den);
	}

	public static Comparator<Point_dt> getComparator(int flag) {
		return new Compare(flag);
	}

	public static Comparator<Point_dt> getComparator() {
		return new Compare(0);
	}

}