import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


class Graph {
    final Map<String, Set<String>> nodes = [:];

    def add(String from, String to) {
        if (nodes[from] == null) {
            nodes[from] = [] as Set;
        }
        nodes[from].add(to);
    }

    def remove(String from, String to) {
        nodes[from].remove(to);
        nodes[to].remove(from);
    }

    String toString() {
        def result = "";
        for (String node in nodes.keySet()) {
            result += node + ": ";
            result += nodes[node].join(" ");
            result += "\n";
        }
        return result;
    }
}

def input = new File("input/day25.txt").readLines();
def graph = new Graph();
input.each { line -> 
    def t = line.split(": ");
    def p = t[0];
    def cs = t[1].split(" ");
    cs.each {
        graph.add(p, it);
        graph.add(it, p);
    }
}
println(graph);

def groupSize(def graph, def from) {
    def frontier = [];
    frontier << from;
    def reached = [:];
    reached[from] = null;

    while (frontier.size() > 0) {
        def curr = frontier.pop();
        for (def next in graph.nodes[curr]) {
            if (reached[next] == null) {
                frontier << next;
                reached[next] = curr;
            }
        }
    }

    return reached.size();
}

def findPath(def graph, def from, def to) {
    def frontier = [];
    frontier << from;
    def reached = [:];
    reached[from] = null;

    while (frontier.size() > 0) {
        def curr = frontier.pop();

        if (curr == to) {
            break;
        }

        for (def next in graph.nodes[curr]) {
            if (reached[next] == null) {
                frontier << next;
                reached[next] = curr;
            }
        }
    }

    def curr = to;
    def path = [];
    while (curr != from) {
        path << curr;
        curr = reached[curr];
    }
    path << from;
    path = path.reverse();
    return path;
}

def nodes = graph.nodes.keySet().collect();
println(groupSize(graph, nodes[0]));

def stats = nodes.collectEntries { [it, 0] };
def getTopVisited(def stats) {
    stats = stats.sort { -it.value };
    // println(stats);
    return stats.keySet().collect()[0..6].collectEntries { [it, stats[it]] };
}

for (int i = 0; i < nodes.size(); i++) {
    for (int j = i + 1; j < nodes.size(); j++) {
        def from = nodes[i];
        def to = nodes[j];
        def path = findPath(graph, from, to);
        path.each { stats[it] = stats[it] + 1 };
    }
    println(i + ": " + getTopVisited(stats));
    if (i == 100) {
        break;
    }
}
println('');

def top = getTopVisited(stats);
def topNodes = top.keySet().collect();
println(top);
for (int i = 0; i < topNodes.size(); i++) {
    for (int j = i + 1; j < topNodes.size(); j++) {
        def from = topNodes[i];
        def to = topNodes[j];
        graph.remove(from, to);
    }
}

def result = top.collect {
    groupSize(graph, it.key)
}.unique();
println(result);
assert result.size() == 2;
println(result[0] * result[1]);