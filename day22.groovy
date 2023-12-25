import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


@EqualsAndHashCode
@ToString
@TupleConstructor
class Pos {
    final int x;
    final int y;
    final int z;

    Pos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Pos(String coords) {
        def parts = coords.split(",");
        this.x = parts[0].toInteger();
        this.y = parts[1].toInteger();
        this.z = parts[2].toInteger();
    }
}

@EqualsAndHashCode
@TupleConstructor
class Brick {
    final String id;
    final Pos from;
    final Pos to;

    Brick(String id, String brick) {
        this.id = id;

        def parts = brick.split('~');
        this.from = new Pos(parts[0]);
        this.to = new Pos(parts[1]);
    }

    Brick(String id, Pos from, Pos to) {
        this.id = id;
        this.from = from;
        this.to = to;
    }

    String toString() {
        return "$from.x,$from.y,$from.z~$to.x,$to.y,$to.z";
    }

    Pos[] positions() {
        def result = [];
        for (int a = from.x; a <= to.x; a++) {
            for (int b = from.y; b <= to.y; b++) {
                for (int c = from.z; c <= to.z; c++) {
                    result << new Pos(a, b, c);
                }
            }
        }
        return result;
    }

    int volume() {
        return positions().size();
    }

    Brick move(Pos delta) {
        return new Brick(
            this.id,
            new Pos(from.x + delta.x, from.y + delta.y, from.z + delta.z),
            new Pos(to.x + delta.x, to.y + delta.y, to.z + delta.z),
        );
    }
}

assert new Brick("A", "1,1,1~1,1,1").positions() == [new Pos(1, 1, 1)];
assert new Brick("A", "1,1,1~1,1,3").positions() == [new Pos(1, 1, 1), new Pos(1, 1, 2), new Pos(1, 1, 3)];
assert new Brick("A", "3,4,5~5,4,5").positions() == [new Pos(3, 4, 5), new Pos(4, 4, 5), new Pos(5, 4, 5)];

boolean overlap(def field, def brick) {
    return brick.positions().collect { field[it] }.findAll { it != null }.size() > 0;
}

assert overlap([new Pos(1, 0, 0): "A"], new Brick("X", "0,0,0~2,0,0")) == true;

def settle(def field, def settledBricks, def brick) {
    boolean brickMoved = false;
    // Just making sure we don't overlap before we even start...
    assert !overlap(field, brick);

    // Lower the brick as far as it goes...
    assert brick.from.z <= brick.to.z;
    while (brick.from.z > 0) {
        def next = brick.move(new Pos(0, 0, -1));
        if (overlap(field, next)) {
            break;
        }
        brickMoved = true;
        brick = next;
    }

    // Just making sure we aren't overlapping now...
    assert !overlap(field, brick);
    settledBricks << brick;
    brick.positions().each { field[it] = brick.id };
    return brickMoved;
}

def input = new File("input/day22.txt").readLines();
def bricks = input.withIndex().collect { it, idx -> new Brick(Integer.toHexString(idx), it) }.sort { it.from.z }
// bricks.each { println(it) }

// key:value of position:brick.id
def field = [:];
def settledBricks = [];
bricks.each { settle(field, settledBricks, it) }
assert bricks.size() == settledBricks.size();
// println(field);

// Build a graph of supporting and supported bricks.
// That is, find all pairs of bricks that share (x, y) and diff on z by 1.
def supporting = settledBricks.collectEntries { [it.id, [] as Set] };
def supported = settledBricks.collectEntries { [it.id, [] as Set] };
for (Pos a in field.keySet()) {
    for (Pos b in field.keySet()) {
        if (a.x == b.x && a.y == b.y && field[a] != field[b]) {
            if (a.z == b.z + 1) {
                supporting[field[b]] << field[a];
                supported[field[a]] << field[b];
            } else if (a.z + 1 == b.z) {
                supporting[field[a]] << field[b];
                supported[field[b]] << field[a];
            }
        }
    }
}
// println(supporting);
// println(supported);

def getTotalSupported(def supporting, def supported, def brickId) {
    def result = [] as Set;
    result << brickId;
    def frontier = [];
    frontier << brickId;
    while (frontier.size() > 0) {
        def curr = frontier.pop();
        for (def next in supporting[curr]) {
            // If all supported bricks have moved, this one moves, too.
            def allSupportsMoved = supported[next].collect { result.contains(it) }.findAll { it == false }.size() == 0;
            if (allSupportsMoved && !result.contains(next)) {
                result << next;
                frontier << next;
            }
        }
    }

    return result.size() - 1;
}

// Part 1
println(settledBricks
    .collect { it.id }
    .findAll { supporting[it].findAll { supported[it].size() == 1 }.size() == 0 }
    .size()
);

// Part 2
println(settledBricks
    .collect { it.id }
    .collect { getTotalSupported(supporting, supported, it) }
    .sum()
); 
