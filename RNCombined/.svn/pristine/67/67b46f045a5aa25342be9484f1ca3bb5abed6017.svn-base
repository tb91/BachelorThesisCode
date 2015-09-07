package delaunay_triangulation;

import java.util.Comparator;

/**
 * Adapted from benmoshe@cs.bgu.ac.il.
 * 
 * @see http://www.cs.bgu.ac.il/~benmoshe/DT/Delaunay%20Triangulation%20in%20Java.htm
 */
class Compare implements Comparator {
	private int _flag;

	public Compare(int i) {
		this._flag = i;
	}

	public int compare(Object o1, Object o2) {
		int ans = 0;
		if ((o1 != null) && (o2 != null) && ((o1 instanceof Point_dt))
				&& ((o2 instanceof Point_dt))) {
			Point_dt d1 = (Point_dt) o1;
			Point_dt d2 = (Point_dt) o2;
			if (this._flag == 0) {
				if (d1.x > d2.x)
					return 1;
				if (d1.x < d2.x)
					return -1;

				if (d1.y > d2.y)
					return 1;
				if (d1.y < d2.y)
					return -1;
			} else if (this._flag == 1) {
				if (d1.x > d2.x)
					return -1;
				if (d1.x < d2.x)
					return 1;

				if (d1.y > d2.y)
					return -1;
				if (d1.y < d2.y)
					return 1;
			} else if (this._flag == 2) {
				if (d1.y > d2.y)
					return 1;
				if (d1.y < d2.y)
					return -1;

				if (d1.x > d2.x)
					return 1;
				if (d1.x < d2.x)
					return -1;

			} else if (this._flag == 3) {
				if (d1.y > d2.y)
					return -1;
				if (d1.y < d2.y)
					return 1;

				if (d1.x > d2.x)
					return -1;
				if (d1.x < d2.x)
					return 1;
			}
		} else {
			if ((o1 == null) && (o2 == null))
				return 0;
			if ((o1 == null) && (o2 != null))
				return 1;
			if ((o1 != null) && (o2 == null))
				return -1;
		}
		return ans;
	}

	public boolean equals(Object ob) {
		return false;
	}
}