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
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class PosX {
    final int x;
    final int y;
    final int z;
    final int dir;

    Pos toPos() {
        return new Pos(x, y);
    }
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class QueueItem {
    final PosX pos;
    final int priority;
}

class CityMap {
    int[] arr;
    int width;
    int height;

    CityMap(List<String> input) {
        arr = input.join("").toCharArray().collect { "$it".toInteger() };
        width = input[0].size();
        height = input.size();
    }

    def getAt(Pos pos) {
        int arrpos = pos.x + pos.y * width;
        return arr[arrpos];
    }

    def putAt(Pos pos, char value) {
        int arrpos = pos.x + pos.y * width;
        arr[arrpos] = value;
    }

    def posOf(char c) {
        return arr.findIndexValues { it == c }.collect {
            new Pos(it % width as int, Math.floor(it / width) as int)
        };
    }

    String toString() {
        def result = "";
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result += this[new Pos(x, y)];
            }
            result += "\n";
        }
        return result;
    }

    String toStringPath(def path) {
        def result = "";
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                def p = path.find { it.x == x && it.y == y };
                if (p != null) {
                    result += this[new Pos(x, y)];
                } else {
                    result += ".";
                }
            }
            result += "\n";
        }
        return result;
    }
}

def input = new File("input/day17.txt").readLines();
def map = new CityMap(input);
// println(map);

def nexts(CityMap map, PosX curr, int minBeforeTurn, int maxSameDir) {
    def result = [];

    if (curr.z >= maxSameDir) {
        return result;
    }

    if (curr.y - 1 >= 0 && curr.dir != 2) {
        result << new PosX(curr.x, curr.y - 1, curr.dir == 0 ? curr.z + 1 : 0, 0);
    }
    if (curr.x + 1 < map.width && curr.dir != 3) {
        result << new PosX(curr.x + 1, curr.y, curr.dir == 1 ? curr.z + 1 : 0, 1);
    }
    if (curr.y + 1 < map.height && curr.dir != 0) {
        result << new PosX(curr.x, curr.y + 1, curr.dir == 2 ? curr.z + 1 : 0, 2);
    }
    if (curr.x - 1 >= 0 && curr.dir != 1) {
        result << new PosX(curr.x - 1, curr.y, curr.dir == 3 ? curr.z + 1 : 0, 3);
    }

    if (curr.z < minBeforeTurn) {
        result = result.findAll { it.dir == curr.dir };
    } else {

    }

    return result;
}

def solve(CityMap map, int minBeforeTurn, int maxSameDir) {
    def start = new Pos(0, 0);
    def goal = new Pos(map.width - 1, map.height - 1);
    
    def frontier = new PriorityQueue({ a, b -> a.priority.compareTo(b.priority) });
    frontier.add(new QueueItem(new PosX(start.x, start.y, 0, 1), 0));
    frontier.add(new QueueItem(new PosX(start.x, start.y, 0, 2), 0));
    
    reached = [:];
    costSoFar = [:];
    
    reached[new PosX(start.x, start.y, 0, 1)] = null;
    reached[new PosX(start.x, start.y, 0, 2)] = null;
    costSoFar[new PosX(start.x, start.y, 0, 1)] = 0;
    costSoFar[new PosX(start.x, start.y, 0, 2)] = 0;

    def actualGoal = null;
    while (frontier.size() > 0) {
        def curr = frontier.poll().pos;

        if (curr.toPos() == goal && curr.z >= minBeforeTurn && curr.z < (maxSameDir - 1)) {
            actualGoal = curr;
            break;
        }

        for (def next in nexts(map, curr, minBeforeTurn, maxSameDir)) {
            def nextCost = costSoFar[curr] + map[next.toPos()];
            if (!(next in costSoFar) || nextCost < costSoFar[next]) {
                frontier.add(new QueueItem(next, nextCost));
                reached[next] = curr;
                costSoFar[next] = nextCost;
            }
        }
    }

    if (actualGoal == null) {
        assert false : "Goal not reached";
    }

    def curr = actualGoal;
    def path = [];
    while (curr.toPos() != start) {
        path.add(curr);
        curr = reached[curr];
    }
    path.add(start);
    path = path.reverse();

    // println(map.toStringPath(path));
    return costSoFar[actualGoal];
}


// Part 1:
println(solve(map, 0, 3));

// Part 2:
println(solve(map, 3, 10));
