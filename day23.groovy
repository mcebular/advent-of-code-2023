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
        assert pos.x >= 0 && pos.x < width;
        assert pos.y >= 0 && pos.y < height;
        int arrpos = pos.x + pos.y * width;
        return arr[arrpos];
    }

    def putAt(Pos pos, char value) {
        assert pos.x >= 0 && pos.x < width;
        assert pos.y >= 0 && pos.y < height;
        int arrpos = pos.x + pos.y * width;
        arr[arrpos] = value;
    }

    def posOf(char c) {
        return arr.findIndexValues { it == c }.collect {
            new Pos(it % width as int, Math.floor(it / width) as int)
        };
    }

    def isInBounds(Pos pos) {
        return pos.x >= 0 &&
            pos.x < width &&
            pos.y >= 0 &&
            pos.y < height;
    }

    def isIntersection(Pos pos) {
        def neighPaths = 0;
        for (def next in [
            new Pos(pos.x + 1, pos.y),
            new Pos(pos.x - 1, pos.y),
            new Pos(pos.x, pos.y + 1),
            new Pos(pos.x, pos.y - 1),
        ]) {
            if (isInBounds(next) && this[next] != '#') {
                neighPaths += 1;
            }
        }

        return neighPaths >= 3;
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

@EqualsAndHashCode
@ToString
@TupleConstructor
class MapState {
    final Pos pos;
    final char dir;
    
    final Pos from;
    final int steps;
    boolean unidir;

    def nexts(Map map) {
        def pos = this.pos;
        def dir = this.dir;
        def steps = this.steps;
        
        assert map[pos] != '#' as char;
        def result = [];

        for (def next in [
            new MapState(new Pos(pos.x + 1, pos.y), '>' as char, this.from, steps + 1, this.unidir),
            new MapState(new Pos(pos.x - 1, pos.y), '<' as char, this.from, steps + 1, this.unidir),
            new MapState(new Pos(pos.x, pos.y + 1), 'v' as char, this.from, steps + 1, this.unidir),
            new MapState(new Pos(pos.x, pos.y - 1), '^' as char, this.from, steps + 1, this.unidir),
        ]) {
            if (!map.isInBounds(next.pos)) {
                continue;
            }

            if (dir == '^' as char && next.dir == 'v' as char
            || dir == 'v' as char && next.dir == '^' as char
            || dir == '<' as char && next.dir == '>' as char
            || dir == '>' as char && next.dir == '<' as char
            ) {
                // No turning back.
                continue;
            }

            if (map[next.pos] == '#' as char) {
                // Can't walk here.
                continue;
            }

            if (map[next.pos] == '.' as char) {
                result << next;
                continue;
            }

            def tile = map[next.pos];
            def directionalTile = ['>' as char, '<' as char, '^' as char, 'v' as char].contains(tile);

            if (directionalTile) {
                if (tile == next.dir) {
                    next.unidir = true;
                    result << next;
                }
                continue;            
            }

            assert false;
        }

        return result;
    }
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class Connection {
    final int distance;
    final boolean slippery;
}

class Graph {
    private def connections = [:];

    void add(long from, long to, Connection connection) {
        if (connections[from] == null) {
            connections[from] = [:];
        }
        if (connections[from][to] != null) {
            assert "Connection from $from to $to already exists.";
        }

        connections[from][to] = connection;
    }

    Connection get(long from, long to) {
        if (connections[from] == null) {
            return null;
        }
        return connections[from][to];
    }

    def getConnections(long from) {
        return connections[from];
    }

    def getNodes() {
        def result = [] as Set;
        connections.each {
            result.add(it.key)
            it.value.each {
                result.add(it.key)
            }
        }
        return result;
    }

    int size() {
        return connections.values().collect { it.size() }.sum();
    }

    String toString() {
        def result = "";
        for (def from in connections.keySet()) {
            for (def to in connections[from].keySet()) {
                result += "${from} -> ${to}: ${connections[from][to]}\n";
            }
        }
        return result;
    }
}

def createGraph(def map) {
    def graph = new Graph();
    def nodeMasks = [:];
    def nodeMaskIndex = 0;

    def start = new Pos(1, 0);
    def finish = new Pos(map.width - 2, map.height - 1);
    assert map[start] == '.' as char;
    assert map[finish] == '.' as char;

    nodeMasks[start] = (0b1 as long) << nodeMaskIndex++;
    nodeMasks[finish] = (0b1 as long) << nodeMaskIndex++;
    assert nodeMasks[start] == 1;
    assert nodeMasks[finish] == 2;

    def frontier = [];
    frontier << new MapState(start, 'v' as char, start, 0, false);

    while (frontier.size() > 0) {
        def curr = frontier.pop();

        if (map.isIntersection(curr.pos) || curr.pos == finish) {
            def from = curr.from;
            def to = curr.pos;

            if (nodeMasks[from] == null) {
                nodeMasks[from] = (0b1 as long) << nodeMaskIndex++;
            }
            from = nodeMasks[from];

            if (nodeMasks[to] == null) {
                nodeMasks[to] = (0b1 as long) << nodeMaskIndex++;
            }
            to = nodeMasks[to];

            graph.add(from, to, new Connection(curr.steps - 1, false));
            graph.add(to, from, new Connection(curr.steps - 1, curr.unidir));

            curr = new MapState(curr.pos, curr.dir, curr.pos, 0, false);
        }

        for (def next in curr.nexts(map)) {
            frontier << next;
        }
    }

    return graph;
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class GraphState {
    long pos;
    long visited;
    int distance;
}

def longestPath(Graph graph, long from, long to, boolean slippery) {
    def frontier = [];
    frontier << new GraphState(from, from, 0);

    def finishStates = [];
    while (frontier.size() > 0) {
        GraphState curr = frontier.removeLast();

        if (curr.pos == to) {
            finishStates << curr;
            continue;
        }

        for (def next in graph.getConnections(curr.pos)) {
            long nextPos = next.key;
            def nextConn = next.value;

            if (slippery && nextConn.slippery) {
                continue;
            }

            if ((curr.visited & nextPos) > 0) {
                continue;
            }
            
            frontier << new GraphState(nextPos, curr.visited | nextPos, curr.distance + nextConn.distance);
        }
    }

    return finishStates.collect { it.distance + Long.bitCount(it.visited) - 1 }.max();
}

def printElapsedTime(Closure closure) {
    Date start = new Date();
    closure();
    Date stop = new Date();
    println(TimeCategory.minus(stop, start));
}

def input = new File("input/day23.txt").readLines();
def map = new Map(input);
// println(map);

def graph;
printElapsedTime {
    graph = createGraph(map);
}
// println(graph);
// println(graph.size());

def start = (0b1 as long) << 0;
def finish = (0b1 as long) << 1;
def oneBeforeFinish = graph.getConnections(finish).keySet()[0];
def distanceToFinish = graph.getConnections(finish).values()[0].distance + 1;

printElapsedTime {
    println(longestPath(graph, start, oneBeforeFinish, true) + distanceToFinish);
}
printElapsedTime {
    println(longestPath(graph, start, oneBeforeFinish, false) + distanceToFinish);
}