import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;

@EqualsAndHashCode
@ToString
@TupleConstructor
class Vec {
    final BigDecimal x;
    final BigDecimal y;
    final BigDecimal z;

    Vec plus(Vec other) {
        return new Vec(
            this.x + other.x,
            this.y + other.y,
            this.z + other.z,
        );
    }

    Vec minus(Vec other) {
        return new Vec(
            this.x - other.x,
            this.y - other.y,
            this.z - other.z,
        );
    }

    Vec multiply(BigDecimal scalar) {
        return new Vec(
            this.x * scalar,
            this.y * scalar,
            this.z * scalar,
        );
    }

    Vec cross(Vec other) {
        return new Vec(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    def length() {
        Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
}

{ // Test: Vec.minus
    assert new Vec(1, 2, 3) - new Vec(0, 2, 1) == new Vec(1, 0, 2);
}

{ // Test: Vec.cross
    assert new Vec(1, 2, 3).cross(new Vec(4, 5, 6)) == new Vec(-3, 6, -3);
}

{ // Test: Vec.length
    assert new Vec(3, 4, 12).length() == 13.0;
}

BigDecimal determinant(Vec a, Vec b, Vec c) {
    // ax bx cx
    // ay by cy
    // az bz cz
    BigDecimal p1 = a.x * ((b.y * c.z) - (c.y * b.z));
    BigDecimal p2 = b.x * ((a.y * c.z) - (c.y * a.z));
    BigDecimal p3 = c.x * ((a.y * b.z) - (b.y * a.z));
    return p1 - p2 + p3;
}

{ // Test: determinant
    assert determinant(new Vec(1, 4, 5), new Vec(3, 6, 7), new Vec(3, 6, 1)) == 36;
}

Vec[] intersects(Ray a, Ray b) {
    Vec p1 = a.position;
    Vec v1 = a.velocity;
    Vec p2 = b.position;
    Vec v2 = b.velocity;

    Vec vc = v1.cross(v2);

    if (vc.length() == 0) {
        return null;
    }

    def t = determinant((p2 - p1), v2, vc) / Math.pow(vc.length(), 2);
    def s = determinant((p2 - p1), v1, vc) / Math.pow(vc.length(), 2);

    def l1 = p1 + v1 * t;
    def l2 = p2 + v2 * s;
    def diff = l1 - l2;

    if (t < 0 || s < 0) {
        return null;
    }

    if (diff.length() > 1) {
        return null;
    }

    return [l1, l2];
}

{ // Test: intersects
    // assert intersects(new Ray("1, 1, 0 @ 1, 1, 1"), new Ray("3, 3, 0 @ -1, -1, 1"));
    // assert !intersects(new Ray("1, 1, 0 @ 1, 1, 1"), new Ray("3, 3, 0 @ 1, 2, 1"));
    assert intersects(new Ray("19, 13, 0 @ -2, 1, 0"), new Ray("18, 19, 0 @ -1, -1, 0"));
    assert intersects(new Ray("19, 13, 0 @ -2, 1, 0"), new Ray("20, 25, 0 @ -2, -2, 0"));
    assert intersects(new Ray("19, 13, 0 @ -2, 1, 0"), new Ray("12, 31, 0 @ -1, -2, 0"));
    assert ! intersects(new Ray("19, 13, 0 @ -2, 1, 0"), new Ray("20, 19, 0 @ 1, -5, 0"));

    def ray = new Ray(new Vec(24, 13, 10), new Vec(-3, 1, 2));
    assert intersects(ray, new Ray("19, 13, 30 @ -2, 1, -2"));
    assert intersects(ray, new Ray("18, 19, 22 @ -1, -1, -2"));
    assert intersects(ray, new Ray("20, 25, 34 @ -2, -2, -4"));
    assert intersects(ray, new Ray("12, 31, 28 @ -1, -2, -1"));
    assert intersects(ray, new Ray("20, 19, 15 @ 1, -5, -3"));
}

boolean withinBounds(Vec pos, Vec min, Vec max) {
    return pos.x >= min.x && pos.x <= max.x
        && pos.y >= min.y && pos.y <= max.y
        && pos.z >= min.z && pos.z <= max.z;
}

@EqualsAndHashCode
class Ray {
    final Vec position;
    final Vec velocity;

    Ray(String input) {
        def parts = input.split(" @ ");
        def ppos = parts[0].split(", ").collect { it.toDouble() as BigDecimal };
        def pvel = parts[1].split(", ").collect { it.toDouble() as BigDecimal };
        this.position = new Vec(ppos[0], ppos[1], ppos[2]);
        this.velocity = new Vec(pvel[0], pvel[1], pvel[2]);
    }

    Ray(Vec pos, Vec vel) {
        this.position = pos;
        this.velocity = vel;
    }

    String toString() {
        return "$position.x, $position.y, $position.z @ $velocity.x, $velocity.y, $velocity.z";
    }
}

def countIntersections(def rays, def min, def max) {
    def result = 0;
    for (int i = 0; i < rays.size(); i++) {
        for (int j = i + 1; j < rays.size(); j++) {
            def a = rays[i];
            def b = rays[j];
            def abx = intersects(a, b);
            if (abx && withinBounds(abx[0], min, max) && withinBounds(abx[1], min, max)) {
                result += 1;
            }
        }
    }
    return result;
}

def gaussianElimination(def matrix, def fc) {
    int ccount = matrix.length;
    for (int i = 0; i < ccount; i++) {
        double pivot = matrix[i][i];
        for (int j = 0; j < ccount; j++) {
            matrix[i][j] = matrix[i][j] / pivot;
        }
        fc[i] = fc[i] / pivot;
        for (int k = 0; k < ccount; k++) {
            if (k != i) {
                double factor = matrix[k][i];
                for (int j = 0; j < ccount; j++) {
                    matrix[k][j] = matrix[k][j] - factor * matrix[i][j];
                }
                fc[k] = fc[k] - factor * fc[i];
            }
        }
    }
}

def findIntersectingAll(def rays) {
    // Time for some more math magic?
    // This is essentially linear equation solving. 

    def matrix = new double[4][4];
    def sol = new double[4];

    for (int i = 0; i < 4; i++) {
        Ray l1 = rays.get(i);
        Ray l2 = rays.get(i + 1);
        matrix[i][0] = l2.velocity.y - l1.velocity.y;
        matrix[i][1] = l1.velocity.x - l2.velocity.x;
        matrix[i][2] = l1.position.y - l2.position.y;
        matrix[i][3] = l2.position.x - l1.position.x;
        sol[i] = -l1.position.x * l1.velocity.y + l1.position.y * l1.velocity.x + l2.position.x * l2.velocity.y - l2.position.y * l2.velocity.x;
    }

    gaussianElimination(matrix, sol);

    long x = Math.round(sol[0]);
    long y = Math.round(sol[1]);
    long vx = Math.round(sol[2]);
    long vy = Math.round(sol[3]);

    matrix = new double[2][2];
    sol = new double[2];
    for (int i = 0; i < 2; i++) {
        Ray l1 = rays.get(i);
        Ray l2 = rays.get(i + 1);
        matrix[i][0] = l1.velocity.x - l2.velocity.x;
        matrix[i][1] = l2.position.x - l1.position.x;
        sol[i] = -l1.position.x * l1.velocity.z + l1.position.z * l1.velocity.x + l2.position.x * l2.velocity.z - 
                 l2.position.z * l2.velocity.x - ((l2.velocity.z - l1.velocity.z) * x) - ((l1.position.z - l2.position.z) * vx);
    }

    gaussianElimination(matrix, sol);

    long z = Math.round(sol[0]);
    long vz = Math.round(sol[1]);

    Ray rock = new Ray(new Vec(x, y, z), new Vec(vx, vy, vz));
    return rock.position.x + rock.position.y + rock.position.z;
}

def input = new File("input/day24.txt").readLines();
def rays = input.collect { new Ray(it) };
// rays.each { println(it) };

def rays2d = rays.collect {
    new Ray(new Vec(it.position.x, it.position.y, 0), new Vec(it.velocity.x, it.velocity.y, 0))
}

// Part 1
// println(countIntersections(rays2d, new Vec(7, 7, 0), new Vec(27, 27, 0)));
println(countIntersections(rays2d, new Vec(200000000000000, 200000000000000, 0), new Vec(400000000000000, 400000000000000, 0)));

// Part 2
println(findIntersectingAll(rays));
