package delaunay_triangulation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Adapted from benmoshe@cs.bgu.ac.il.
 * 
 * @see http://www.cs.bgu.ac.il/~benmoshe/DT/Delaunay%20Triangulation%20in%20Java.htm
 */
public class Delaunay_Triangulation
{
  private Point_dt firstP;
  private Point_dt lastP;
  private boolean allCollinear;
  Triangle_dt firstT;
  Triangle_dt lastT;
  Triangle_dt currT;
  Triangle_dt startTriangle;
  public Triangle_dt startTriangleHull;
  private int nPoints = 0;
  private Set<Point_dt> _vertices;
  private Vector<Triangle_dt> _triangles;
  private int _modCount = 0; private int _modCount2 = 0;
  private Point_dt _bb_min;
  private Point_dt _bb_max;

  public Delaunay_Triangulation()
  {
    this(new Point_dt[0]);
  }

  public Delaunay_Triangulation(Point_dt[] ps)
  {
    this._modCount = 0; this._modCount2 = 0;
    this._bb_min = null; this._bb_max = null;
    this._vertices = new TreeSet(Point_dt.getComparator());
    this._triangles = new Vector();
    this.allCollinear = true;
    for (int i = 0; (ps != null) && (i < ps.length) && (ps[i] != null); i++)
      insertPoint(ps[i]);
  }

  public Delaunay_Triangulation(String file)
    throws Exception
  {
    this(read_file(file));
  }

  public int size()
  {
    if (this._vertices == null) return 0;
    return this._vertices.size();
  }

  public int trianglesSize()
  {
    initTriangles();
    return this._triangles.size();
  }

  public int getModeCounter()
  {
    return this._modCount;
  }

  public void insertPoint(Point_dt p)
  {
    if (this._vertices.contains(p)) return;
    this._modCount += 1;
    updateBB(p);
    this._vertices.add(p);
    Triangle_dt t = insertPointSimple(p);
    if (t == null)
      return;
    Triangle_dt tt = t;
    this.currT = t;
    do {
      flip(tt, this._modCount);
      tt = tt.canext;
    }while ((tt != t) && (!
      tt.halfplane));
  }

  public Iterator<Triangle_dt> getLastUpdatedTriangles()
  {
    Vector tmp = new Vector();
    if (trianglesSize() > 1) {
      Triangle_dt t = this.currT;
      allTriangles(t, tmp, this._modCount);
    }
    return tmp.iterator();
  }
  private void allTriangles(Triangle_dt curr, Vector<Triangle_dt> front, int mc) {
    if ((curr != null) && (curr._mc == mc) && (!front.contains(curr))) {
      front.add(curr);
      allTriangles(curr.abnext, front, mc);
      allTriangles(curr.bcnext, front, mc);
      allTriangles(curr.canext, front, mc);
    }
  }

  private Triangle_dt insertPointSimple(Point_dt p) { this.nPoints += 1;
    if (!this.allCollinear) {
      Triangle_dt t = find(this.startTriangle, p);
      if (t.halfplane)
        this.startTriangle = extendOutside(t, p);
      else
        this.startTriangle = extendInside(t, p);
      return this.startTriangle;
    }

    if (this.nPoints == 1) {
      this.firstP = p;
      return null;
    }

    if (this.nPoints == 2) {
      startTriangulation(this.firstP, p);
      return null;
    }

    switch (p.pointLineTest(this.firstP, this.lastP)) {
    case 1:
      this.startTriangle = extendOutside(this.firstT.abnext, p);
      this.allCollinear = false;
      break;
    case 2:
      this.startTriangle = extendOutside(this.firstT, p);
      this.allCollinear = false;
      break;
    case 0:
      insertCollinear(p, 0);
      break;
    case 3:
      insertCollinear(p, 3);
      break;
    case 4:
      insertCollinear(p, 4);
    }

    return null;
  }

  private void insertCollinear(Point_dt p, int res)
  {
	  
	  Triangle_dt t, tp;
	  
    switch (res) {
    case 3:
      t = new Triangle_dt(this.firstP, p);
      tp = new Triangle_dt(p, this.firstP);
      t.abnext = tp;
      tp.abnext = t;
      t.bcnext = tp;
      tp.canext = t;
      t.canext = this.firstT;
      this.firstT.bcnext = t;
      tp.bcnext = this.firstT.abnext;
      this.firstT.abnext.canext = tp;
      this.firstT = t;
      this.firstP = p;
      break;
    case 4:
      t = new Triangle_dt(p, this.lastP);
      tp = new Triangle_dt(this.lastP, p);
      t.abnext = tp;
      tp.abnext = t;
      t.bcnext = this.lastT;
      this.lastT.canext = t;
      t.canext = tp;
      tp.bcnext = t;
      tp.canext = this.lastT.abnext;
      this.lastT.abnext.bcnext = tp;
      this.lastT = t;
      this.lastP = p;
      break;
    case 0:
      Triangle_dt u = this.firstT;
      while (p.isGreater(u.a))
        u = u.canext;
      t = new Triangle_dt(p, u.b);
      tp = new Triangle_dt(u.b, p);
      u.b = p;
      u.abnext.a = p;
      t.abnext = tp;
      tp.abnext = t;
      t.bcnext = u.bcnext;
      u.bcnext.canext = t;
      t.canext = u;
      u.bcnext = t;
      tp.canext = u.abnext.canext;
      u.abnext.canext.bcnext = tp;
      tp.bcnext = u.abnext;
      u.abnext.canext = tp;
      if (this.firstT == u)
        this.firstT = t;
      break;
    case 1:
    case 2:
    }
  }

  private void startTriangulation(Point_dt p1, Point_dt p2)
  {
    Point_dt pb;
    Point_dt ps;
    if (p1.isLess(p2)) {
      ps = p1;
      pb = p2;
    } else {
      ps = p2;
      pb = p1;
    }
    this.firstT = new Triangle_dt(pb, ps);
    this.lastT = this.firstT;
    Triangle_dt t = new Triangle_dt(ps, pb);
    this.firstT.abnext = t;
    t.abnext = this.firstT;
    this.firstT.bcnext = t;
    t.canext = this.firstT;
    this.firstT.canext = t;
    t.bcnext = this.firstT;
    this.firstP = this.firstT.b;
    this.lastP = this.lastT.a;
    this.startTriangleHull = this.firstT;
  }

  private Triangle_dt extendInside(Triangle_dt t, Point_dt p)
  {
    Triangle_dt h1 = treatDegeneracyInside(t, p);
    if (h1 != null) return h1;

    h1 = new Triangle_dt(t.c, t.a, p);
    Triangle_dt h2 = new Triangle_dt(t.b, t.c, p);
    t.c = p;
    t.circumcircle();
    h1.abnext = t.canext;
    h1.bcnext = t;
    h1.canext = h2;
    h2.abnext = t.bcnext;
    h2.bcnext = h1;
    h2.canext = t;
    h1.abnext.switchneighbors(t, h1);
    h2.abnext.switchneighbors(t, h2);
    t.bcnext = h2;
    t.canext = h1;
    return t;
  }

  private Triangle_dt treatDegeneracyInside(Triangle_dt t, Point_dt p)
  {
    if ((t.abnext.halfplane) && (p.pointLineTest(t.b, t.a) == 0))
      return extendOutside(t.abnext, p);
    if ((t.bcnext.halfplane) && (p.pointLineTest(t.c, t.b) == 0))
      return extendOutside(t.bcnext, p);
    if ((t.canext.halfplane) && (p.pointLineTest(t.a, t.c) == 0))
      return extendOutside(t.canext, p);
    return null;
  }

  private Triangle_dt extendOutside(Triangle_dt t, Point_dt p)
  {
    if (p.pointLineTest(t.a, t.b) == 0) {
      Triangle_dt dg = new Triangle_dt(t.a, t.b, p);
      Triangle_dt hp = new Triangle_dt(p, t.b);
      t.b = p;
      dg.abnext = t.abnext;
      dg.abnext.switchneighbors(t, dg);
      dg.bcnext = hp;
      hp.abnext = dg;
      dg.canext = t;
      t.abnext = dg;
      hp.bcnext = t.bcnext;
      hp.bcnext.canext = hp;
      hp.canext = t;
      t.bcnext = hp;
      return dg;
    }
    Triangle_dt ccT = extendcounterclock(t, p);
    Triangle_dt cT = extendclock(t, p);
    ccT.bcnext = cT;
    cT.canext = ccT;
    this.startTriangleHull = cT;
    return cT.abnext;
  }

  private Triangle_dt extendcounterclock(Triangle_dt t, Point_dt p)
  {
    t.halfplane = false;
    t.c = p;
    t.circumcircle();

    Triangle_dt tca = t.canext;

    if (p.pointLineTest(tca.a, tca.b) >= 2) {
      Triangle_dt nT = new Triangle_dt(t.a, p);
      nT.abnext = t;
      t.canext = nT;
      nT.canext = tca;
      tca.bcnext = nT;
      return nT;
    }
    return extendcounterclock(tca, p);
  }

  private Triangle_dt extendclock(Triangle_dt t, Point_dt p)
  {
    t.halfplane = false;
    t.c = p;
    t.circumcircle();

    Triangle_dt tbc = t.bcnext;

    if (p.pointLineTest(tbc.a, tbc.b) >= 2) {
      Triangle_dt nT = new Triangle_dt(p, t.b);
      nT.abnext = t;
      t.bcnext = nT;
      nT.bcnext = tbc;
      tbc.canext = nT;
      return nT;
    }
    return extendclock(tbc, p);
  }

  private void flip(Triangle_dt t, int mc)
  {
	  Triangle_dt v;
    Triangle_dt u = t.abnext;
    t._mc = mc;
    if ((u.halfplane) || (!u.circumcircle_contains(t.c)))
      return;
    
    if (t.a == u.a) {
      v = new Triangle_dt(u.b, t.b, t.c);
      v.abnext = u.bcnext;
      t.abnext = u.abnext;
    }
    else if (t.a == u.b) {
      v = new Triangle_dt(u.c, t.b, t.c);
      v.abnext = u.canext;
      t.abnext = u.bcnext;
    }
    else if (t.a == u.c) {
      v = new Triangle_dt(u.a, t.b, t.c);
      v.abnext = u.abnext;
      t.abnext = u.canext;
    }
    else {
      System.out.println("Error in flip.");
      return;
    }

    v._mc = mc;
    v.bcnext = t.bcnext;
    v.abnext.switchneighbors(u, v);
    v.bcnext.switchneighbors(t, v);
    t.bcnext = v;
    v.canext = t;
    t.b = v.a;
    t.abnext.switchneighbors(u, t);
    t.circumcircle();

    this.currT = v;
    flip(t, mc);
    flip(v, mc);
  }

  public void write_tsin(String tsinFile)
    throws Exception
  {
    FileWriter fw = new FileWriter(tsinFile);
    PrintWriter os = new PrintWriter(fw);

    int len = this._vertices.size();
    os.println(len);
    Iterator it = this._vertices.iterator();
    while (it.hasNext()) {
      os.println(((Point_dt)it.next()).toFile());
    }
    os.close();
    fw.close();
  }

  public void write_smf(String smfFile)
    throws Exception
  {
    int len = this._vertices.size();
    Point_dt[] ans = new Point_dt[len];
    Iterator it = this._vertices.iterator();
    Comparator comp = Point_dt.getComparator();
    for (int i = 0; i < len; i++) ans[i] = ((Point_dt)it.next());
    Arrays.sort(ans, comp);

    FileWriter fw = new FileWriter(smfFile);
    PrintWriter os = new PrintWriter(fw);

    os.println("begin");

    for (int i = 0; i < len; i++) {
      os.println("v " + ans[i].toFile());
    }
    int t = 0; int i1 = -1; int i2 = -1; int i3 = -1;
    for (Iterator dt = trianglesIterator(); dt.hasNext(); ) {
      Triangle_dt curr = (Triangle_dt)dt.next();
      t++;
      if (!curr.halfplane) {
        i1 = Arrays.binarySearch(ans, curr.a, comp);
        i2 = Arrays.binarySearch(ans, curr.b, comp);
        i3 = Arrays.binarySearch(ans, curr.c, comp);
        if (((i1 < 0 ? 1 : 0) | (i2 < 0 ? 1 : 0) | (i3 < 0 ? 1 : 0)) != 0) throw new RuntimeException("** ERR: wrong triangulation inner bug - cant write as an SMF file! **");
        os.println("f " + (i1 + 1) + " " + (i2 + 1) + " " + (i3 + 1));
      }
    }
    os.println("end");
    os.close();
    fw.close();
  }

  public int CH_size()
  {
    int ans = 0;
    Iterator it = CH_vertices_Iterator();
    while (it.hasNext()) {
      ans++;
      it.next();
    }
    return ans;
  }
  public void write_CH(String tsinFile) throws Exception {
    FileWriter fw = new FileWriter(tsinFile);
    PrintWriter os = new PrintWriter(fw);

    os.println(CH_size());
    Iterator it = CH_vertices_Iterator();
    while (it.hasNext()) {
      os.println(((Point_dt)it.next()).toFileXY());
    }
    os.close();
    fw.close();
  }
  private static Point_dt[] read_file(String file) throws Exception {
    if ((file.substring(file.length() - 4).equals(".smf") | file.substring(file.length() - 4).equals(".SMF")))
      return read_smf(file);
    return read_tsin(file);
  }
  private static Point_dt[] read_tsin(String tsinFile) throws Exception {
    FileReader fr = new FileReader(tsinFile);
    BufferedReader is = new BufferedReader(fr);
    String s = is.readLine();

    while (s.charAt(0) == '/') s = is.readLine();
    StringTokenizer st = new StringTokenizer(s);
    int numOfVer = new Integer(s).intValue();

    Point_dt[] ans = new Point_dt[numOfVer];

    for (int i = 0; i < numOfVer; i++) {
      st = new StringTokenizer(is.readLine());
      double d1 = new Double(st.nextToken()).doubleValue();
      double d2 = new Double(st.nextToken()).doubleValue();
      double d3 = new Double(st.nextToken()).doubleValue();
      ans[i] = new Point_dt((int)d1, (int)d2, d3);
    }
    return ans;
  }

  private static Point_dt[] read_smf(String smfFile)
    throws Exception
  {
    return read_smf(smfFile, 1.0D, 1.0D, 1.0D, 0.0D, 0.0D, 0.0D);
  }
  private static Point_dt[] read_smf(String smfFile, double dx, double dy, double dz, double minX, double minY, double minZ) throws Exception {
    FileReader fr = new FileReader(smfFile);
    BufferedReader is = new BufferedReader(fr);
    String s = is.readLine();

    while (s.charAt(0) != 'v') s = is.readLine();

    Vector vec = new Vector();
    Point_dt[] ans = (Point_dt[])null;
    while ((s != null) && (s.charAt(0) == 'v')) {
      StringTokenizer st = new StringTokenizer(s);
      st.nextToken();
      double d1 = new Double(st.nextToken()).doubleValue() * dx + minX;
      double d2 = new Double(st.nextToken()).doubleValue() * dy + minY;
      double d3 = new Double(st.nextToken()).doubleValue() * dz + minZ;
      vec.add(new Point_dt((int)d1, (int)d2, d3));

      s = is.readLine();
    }

    ans = new Point_dt[vec.size()];
    for (int i = 0; i < vec.size(); i++)
      ans[i] = ((Point_dt)vec.elementAt(i));
    return ans;
  }

  public Triangle_dt find(Point_dt p)
  {
    Triangle_dt T = find(this.startTriangle, p);
    return T;
  }

  public Triangle_dt find(Point_dt p, Triangle_dt start)
  {
    if (start == null) start = this.startTriangle;
    Triangle_dt T = find(start, p);
    return T;
  }

  private static Triangle_dt find(Triangle_dt curr, Point_dt p) {
    if (p == null) return null;

    if (curr.halfplane) {
      Triangle_dt next_t = findnext2(p, curr);
      if ((next_t == null) || (next_t.halfplane)) return curr;
      curr = next_t;
    }
    while (true) {
      Triangle_dt next_t = findnext1(p, curr);
      if (next_t == null) return curr;
      if (next_t.halfplane) return next_t;
      curr = next_t;
    }
  }

  private static Triangle_dt findnext1(Point_dt p, Triangle_dt v)
  {
    if ((p.pointLineTest(v.a, v.b) == 2) && (!v.abnext.halfplane)) return v.abnext;
    if ((p.pointLineTest(v.b, v.c) == 2) && (!v.bcnext.halfplane)) return v.bcnext;
    if ((p.pointLineTest(v.c, v.a) == 2) && (!v.canext.halfplane)) return v.canext;
    if (p.pointLineTest(v.a, v.b) == 2) return v.abnext;
    if (p.pointLineTest(v.b, v.c) == 2) return v.bcnext;
    if (p.pointLineTest(v.c, v.a) == 2) return v.canext;
    return null;
  }

  private static Triangle_dt findnext2(Point_dt p, Triangle_dt v) {
    if ((v.abnext != null) && (!v.abnext.halfplane)) return v.abnext;
    if ((v.bcnext != null) && (!v.bcnext.halfplane)) return v.bcnext;
    if ((v.canext != null) && (!v.canext.halfplane)) return v.canext;
    return null;
  }

  public boolean contains(Point_dt p)
  {
    Triangle_dt tt = find(p);
    return !tt.halfplane;
  }

  public boolean contains(double x, double y)
  {
    return contains(new Point_dt(x, y));
  }

  public Point_dt z(Point_dt q)
  {
    Triangle_dt t = find(q);
    return t.z(q);
  }
  Triangle_dt safeFind(Point_dt q) {
    Triangle_dt curr = null;
    Iterator it = trianglesIterator();
    while (it.hasNext()) {
      curr = (Triangle_dt)it.next();
      if (curr.contains(q)) return curr;
    }
    System.out.println("@@@@@ERR: point " + q + " was NOT found! :");
    return null;
  }

  public double z(double x, double y)
  {
    Point_dt q = new Point_dt(x, y);
    Triangle_dt t = find(q);
    return t.z_value(q);
  }

  private void updateBB(Point_dt p) {
    double x = p.x; double y = p.y; double z = p.z;
    if (this._bb_min == null) {
      this._bb_min = new Point_dt(p);
      this._bb_max = new Point_dt(p);
    }
    else {
      if (x < this._bb_min.x) this._bb_min.x = x; else if (x > this._bb_max.x) this._bb_max.x = x;
      if (y < this._bb_min.y) this._bb_min.y = y; else if (y > this._bb_max.y) this._bb_max.y = y;
      if (z < this._bb_min.z) this._bb_min.z = z; else if (z > this._bb_max.z) this._bb_max.z = z; 
    }
  }

  public Point_dt bb_min() { return this._bb_min; } 
  public Point_dt bb_max() {
    return this._bb_max;
  }

  String info()
  {
    String ans = getClass().getCanonicalName() + "  # vertices:" + size() + "  # triangles:" + trianglesSize() + "  modCountr:" + this._modCount + "\n";
    ans = ans + "min BB:" + bb_min() + "  max BB:" + bb_max();
    return ans;
  }

  public Iterator<Triangle_dt> trianglesIterator()
  {
    if (size() <= 2) this._triangles = new Vector();
    initTriangles();
    return this._triangles.iterator();
  }

  public Iterator<Point_dt> CH_vertices_Iterator()
  {
    Vector ans = new Vector();
    Triangle_dt curr = this.startTriangleHull;
    boolean cont = true;
    double x0 = this._bb_min.x(); double x1 = this._bb_max.x();
    double y0 = this._bb_min.y(); double y1 = this._bb_max.y();

    while (cont) {
      boolean sx = (curr.p1().x() == x0) || (curr.p1().x() == x1);
      boolean sy = (curr.p1().y() == y0) || (curr.p1().y() == y1);
      
      
      // TODO: check if this is correct translated by me (MAX)
      
      /*
		FIXME: MAX
		TODO: check if the following statement is translated correctly.
		Original decompiler output was:
		
      if ((sx & sy | (sx ? 0 : 1) & (sy ? 0 : 1))) {
        ans.add(curr.p1());
        System.out.println(curr.p1());
      }
      
      	But Java allows only boolean terms as if conditions.
		
		FIXME: possible error!
		
       */
      
      if ( (((sx ? 1 : 0) & (sy ? 1 : 0)) | (sx ? 0 : 1) & (sy ? 0 : 1))  != 0 ) {
        ans.add(curr.p1());
        System.out.println(curr.p1());
      }
      
      if ((curr.bcnext != null) && (curr.bcnext.halfplane)) curr = curr.bcnext;
      if (curr == this.startTriangleHull) cont = false;
    }
    return ans.iterator();
  }

  public Iterator<Point_dt> verticesIterator()
  {
    return this._vertices.iterator();
  }
  private void initTriangles() {
    if (this._modCount == this._modCount2) return;
    if (size() > 2) {
      this._modCount2 = this._modCount;
      Vector front = new Vector();
      this._triangles = new Vector();
      front.add(this.startTriangle);
      while (front.size() > 0) {
        Triangle_dt t = (Triangle_dt)front.remove(0);
        if (!t._mark) {
          t._mark = true;
          this._triangles.add(t);
          if ((t.abnext != null) && (!t.abnext._mark)) front.add(t.abnext);
          if ((t.bcnext != null) && (!t.bcnext._mark)) front.add(t.bcnext);
          if ((t.canext != null) && (!t.canext._mark)) front.add(t.canext);
        }
      }

      for (int i = 0; i < this._triangles.size(); i++)
        ((Triangle_dt)this._triangles.elementAt(i))._mark = false;
    }
  }
}