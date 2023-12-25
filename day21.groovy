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
class Map {
    char[] arr;
    int width;
    int height;

    Map(List<String> input) {
        this.arr = input.join("").toCharArray();
        this.width = input[0].size();
        this.height = input.size();
    }

    def getAt(Pos pos) {
        def x = Math.floorMod(pos.x, width);
        def y = Math.floorMod(pos.y, height);
        int arrpos = x + y * width;
        return arr[arrpos];
    }

    def putAt(Pos pos, char value) {
        def x = Math.floorMod(pos.x, width);
        def y = Math.floorMod(pos.y, height);
        int arrpos = x + y * width;
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

}

assert new Map(['...', '...', '...']) == new Map(['...', '...', '...']);
assert new Map(['...', '...', '...']) != new Map(['...', '.S.', '...']);

@EqualsAndHashCode
@ToString
@TupleConstructor
class State {
    final Pos pos;
    final int step;

    State[] nexts() {
        return [
            new State(new Pos(pos.x + 1, pos.y), step + 1),
            new State(new Pos(pos.x - 1, pos.y), step + 1),
            new State(new Pos(pos.x, pos.y + 1), step + 1),
            new State(new Pos(pos.x, pos.y - 1), step + 1),
        ];
    }
}

def input = new File("input/day21.txt").readLines();
def map = new Map(input);
def center = map.posOf('S' as char)[0];
// map[center] = '.' as char;
// println(map);


def frontier = [];
frontier << new State(center, 0);
def reached = [:];
//reached[center] = 0;

def maxSteps = 400;
while (frontier.size() > 0) {
    def curr = frontier.pop();
    for (def next in curr.nexts()) {
        if (
            map[next.pos] != '#' as char && 
            next.step <= maxSteps && 
            reached[next.pos] == null
        ) {
            frontier << next;
            reached[next.pos] = next.step;
        }
    }
}

def countPlots(def reached, int step) {
    return reached.findAll { it.value % 2 == step % 2 && it.value <= step }.size();
}

// Part 1
println(countPlots(reached, 64));

// Part 2
// Some mathematical magic (see: Lagrange interpolation polynomial)
def halfWidth = (map.width - 1) / 2 as int;
def p = countPlots(reached, halfWidth);
def q = countPlots(reached, halfWidth + map.width);
def r = countPlots(reached, halfWidth + map.width * 2);
// println("$p, $q, $r")

def a = (p - 2 * q + r) / 2;
def b = (-3 * p + 4 * q - r) / 2;
def c = p;
int x = (26501365 / map.width);
println(a * x * x + b * x + c);
