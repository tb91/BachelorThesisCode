package delaunay_triangulation;

/**
 * Adapted from benmoshe@cs.bgu.ac.il.
 * 
 * @see http://www.cs.bgu.ac.il/~benmoshe/DT/Delaunay%20Triangulation%20in%20Java.htm
 */
public class Triangle_dt {
	Point_dt a;
	Point_dt b;
	Point_dt c;
	Triangle_dt abnext;
	Triangle_dt bcnext;
	Triangle_dt canext;
	Circle_dt circum;
	int _mc = 0;

	boolean halfplane = false;

	boolean _mark = false;

	public static int _counter = 0;
	public static int _c2 = 0;

	public Triangle_dt(Point_dt A, Point_dt B, Point_dt C) {
		this.a = A;
		int res = C.pointLineTest(A, B);
		if ((res <= 1) || (res == 3) || (res == 4)) {
			this.b = B;
			this.c = C;
		} else {
			System.out.println("Warning, ajTriangle(A,B,C) expects points in counterclockwise order.");
			System.out.println(a.toString() + ", " + b.toString() + ", "
					+ c.toString());
			this.b = C;
			this.c = B;
		}
		circumcircle();
	}

	public Triangle_dt(Point_dt A, Point_dt B) {
		this.a = A;
		this.b = B;
		this.halfplane = true;
	}

	public boolean isHalfplane() {
		return this.halfplane;
	}

	public Point_dt p1() {
		return this.a;
	}

	public Point_dt p2() {
		return this.b;
	}

	public Point_dt p3() {
		return this.c;
	}

	public Triangle_dt next_12() {
		return this.abnext;
	}

	public Triangle_dt next_23() {
		return this.bcnext;
	}

	public Triangle_dt next_31() {
		return this.canext;
	}

	void switchneighbors(Triangle_dt Old, Triangle_dt New) {
		if (this.abnext == Old)
			this.abnext = New;
		else if (this.bcnext == Old)
			this.bcnext = New;
		else if (this.canext == Old)
			this.canext = New;
		else
			System.out.println("Error, switchneighbors can't find Old.");
	}

	Triangle_dt neighbor(Point_dt p) {
		if (this.a == p)
			return this.canext;
		if (this.b == p)
			return this.abnext;
		if (this.c == p)
			return this.bcnext;
		System.out.println("Error, neighbors can't find p: " + p);
		return null;
	}

	Circle_dt circumcircle() {
		double u = ((this.a.x - this.b.x) * (this.a.x + this.b.x) + (this.a.y - this.b.y)
				* (this.a.y + this.b.y)) / 2.0D;
		double v = ((this.b.x - this.c.x) * (this.b.x + this.c.x) + (this.b.y - this.c.y)
				* (this.b.y + this.c.y)) / 2.0D;
		double den = (this.a.x - this.b.x) * (this.b.y - this.c.y)
				- (this.b.x - this.c.x) * (this.a.y - this.b.y);
		if (den == 0.0D) {
			this.circum = new Circle_dt(this.a, (1.0D / 0.0D));
		} else {
			Point_dt cen = new Point_dt((u * (this.b.y - this.c.y) - v
					* (this.a.y - this.b.y))
					/ den, (v * (this.a.x - this.b.x) - u
					* (this.b.x - this.c.x))
					/ den);
			this.circum = new Circle_dt(cen, cen.distance2(this.a));
		}
		return this.circum;
	}

	boolean circumcircle_contains(Point_dt p) {
		return this.circum.r > this.circum.c.distance2(p);
	}

	public String toString() {
		String res = "";
		res = res + this.a.toString() + this.b.toString();
		if (!this.halfplane) {
			res = res + this.c.toString();
		}
		return res;
	}

	public boolean contains(Point_dt p) {
		boolean ans = false;
		if ((this.halfplane | p == null))
			return false;

		if (((p.x == this.a.x ? 1 : 0) & (p.y == this.a.y ? 1 : 0)
				| (p.x == this.b.x ? 1 : 0) & (p.y == this.b.y ? 1 : 0) | (p.x == this.c.x ? 1
				: 0)
				& (p.y == this.c.y ? 1 : 0)) != 0)
			return true;
		int a12 = p.pointLineTest(this.a, this.b);
		int a23 = p.pointLineTest(this.b, this.c);
		int a31 = p.pointLineTest(this.c, this.a);

		if (((a12 == 1) && (a23 == 1) && (a31 == 1))
				|| ((a12 == 2) && (a23 == 2) && (a31 == 2)) || (a12 == 0)
				|| (a23 == 0) || (a31 == 0)) {
			ans = true;
		}
		return ans;
	}

	public double z_value(Point_dt q) {
		if ((q == null) || (this.halfplane))
			throw new RuntimeException(
					"*** ERR wrong parameters, can't approximate the z value ..***: "
							+ q);

		if (((q.x == this.a.x ? 1 : 0) & (q.y == this.a.y ? 1 : 0)) != 0)
			return this.a.z;
		if (((q.x == this.b.x ? 1 : 0) & (q.y == this.b.y ? 1 : 0)) != 0)
			return this.b.z;
		if (((q.x == this.c.x ? 1 : 0) & (q.y == this.c.y ? 1 : 0)) != 0)
			return this.c.z;

		double X = 0.0D;
		double x0 = q.x;
		double x1 = this.a.x;
		double x2 = this.b.x;
		double x3 = this.c.x;
		double Y = 0.0D;
		double y0 = q.y;
		double y1 = this.a.y;
		double y2 = this.b.y;
		double y3 = this.c.y;
		double Z = 0.0D;
		double m01 = 0.0D;
		double k01 = 0.0D;
		double m23 = 0.0D;
		double k23 = 0.0D;

		int flag01 = 0;
		if (x0 != x1) {
			m01 = (y0 - y1) / (x0 - x1);
			k01 = y0 - m01 * x0;
			if (m01 == 0.0D)
				flag01 = 1;
		} else {
			flag01 = 2;
		}
		int flag23 = 0;
		if (x2 != x3) {
			m23 = (y2 - y3) / (x2 - x3);
			k23 = y2 - m23 * x2;
			if (m23 == 0.0D)
				flag23 = 1;
		} else {
			flag23 = 2;
		}

		if (flag01 == 2) {
			X = x0;
			Y = m23 * X + k23;
		} else if (flag23 == 2) {
			X = x2;
			Y = m01 * X + k01;
		} else {
			X = (k23 - k01) / (m01 - m23);
			Y = m01 * X + k01;
		}

		double r = 0.0D;
		if (flag23 == 2)
			r = (y2 - Y) / (y2 - y3);
		else
			r = (x2 - X) / (x2 - x3);
		Z = this.b.z + (this.c.z - this.b.z) * r;
		if (flag01 == 2)
			r = (y1 - y0) / (y1 - Y);
		else
			r = (x1 - x0) / (x1 - X);
		double qZ = this.a.z + (Z - this.a.z) * r;
		return qZ;
	}

	public double z(double x, double y) {
		return z_value(new Point_dt(x, y));
	}

	public Point_dt z(Point_dt q) {
		double z = z_value(q);
		return new Point_dt(q.x, q.y, z);
	}
}