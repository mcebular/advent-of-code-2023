import groovy.transform.TupleConstructor;
import groovy.transform.ToString;


@TupleConstructor
@ToString
class Node {
    String name;
    String left;
    String right;
}

long lcm(long a, long b) {
    return (a * b) / gcf(a, b);
}

long gcf(long a, long b) {
    if (b == 0) {
        return a;
    } else {
        return (gcf(b, a % b));
    }
} 


def input = new File("input/day08.txt").readLines()

def instructions = input[0].split("");

def network = input[2..-1].collect { line -> 
    def matcher = line =~ /^([A-Z0-9]{3}) = \(([A-Z0-9]{3}), ([A-Z0-9]{3})\)$/
    def x = new Node(matcher[0][1], matcher[0][2], matcher[0][3]);
    return x;
}.collectEntries {
    [it.name, it]
}

def countSteps(instructions, network, startingNodes) {
    def currentNodes = startingNodes;
    def currentInstructionIndex = 0;
    def stepsCount = 0;
    def stepsToZ = [];
    while (currentNodes.size() > 0) {
        def instruction = instructions[currentInstructionIndex];
        switch(instruction) {
            case "L":
                currentNodes = currentNodes.collect { node -> network[network[node.name].left] };
            break
            case "R":
                currentNodes = currentNodes.collect { node -> network[network[node.name].right] };
            break
            default:
                assert false : "Invalid instruction";
        }

        currentInstructionIndex = (currentInstructionIndex + 1) % instructions.size();
        stepsCount++;

        currentNodes.findAll { it.name[-1] == "Z" }.each {
            stepsToZ << (stepsCount)
        };
        currentNodes = currentNodes.findAll { it.name[-1] != "Z" };
    }
    return stepsToZ.inject(1) { acc, it -> lcm(acc, it) };
}

println countSteps(instructions, network, [network["AAA"]]);
println countSteps(instructions, network, network.findAll { it.value.name[-1] == "A" }.values());
