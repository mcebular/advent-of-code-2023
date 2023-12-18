import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


@EqualsAndHashCode
@ToString
@TupleConstructor
class Pos {
    final long x;
    final long y;
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class Instruction {
    char dir;
    long len;
}

def area(def vertices) {
    def result = 0;

    // https://en.wikipedia.org/wiki/Shoelace_formula
    for (int i = 0; i < vertices.size(); i++) {
        def i1 = i + 1 == vertices.size() ? 0 : i + 1;
        def y0 = vertices[i].y
        def y1 = vertices[i1].y
        def x0 = vertices[i].x
        def x1 = vertices[i1].x

        result += ((y0 + y1) * (x0 - x1));
    }
    result = Math.abs(result);

    // Need to add border too...
    for (int i = 0; i < vertices.size(); i++) {
        def i1 = i + 1 == vertices.size() ? 0 : i + 1;
        def a = vertices[i];
        def b = vertices[i1];
        def diff = new Pos(Math.abs(a.x - b.x), Math.abs(a.y - b.y));
        assert diff.x == 0 && diff.y != 0 || diff.x != 0 && diff.y == 0;
        result += diff.x + diff.y;
    }

    return (result / 2) + 1;
}

def hex2instruction(def hex) {
    // Remove leading '#'
    hex = hex[1..-1];
    def dir = hex[-1];
    switch (dir) {
        case '0': dir = 'R' as char; break;
        case '1': dir = 'D' as char; break;
        case '2': dir = 'L' as char; break;
        case '3': dir = 'U' as char; break;
        default: assert false : "Invalid dir: '$dir'";
    }
    hex = hex[0..-2];

    return new Instruction(dir, Long.parseLong(hex, 16));
}

{ // Test: hex2instruction
    assert hex2instruction("#70c710") == new Instruction('R' as char, 461937);
    assert hex2instruction("#7a21e3") == new Instruction('U' as char, 500254);
}

def input = new File("input/day18.txt").readLines();
def instr1 = input.collect {
    def matcher = it =~ /^([A-Z]{1}) ([0-9]+) \((#[0-9a-f]{6})\)$/
    return new Instruction(matcher[0][1] as char, matcher[0][2].toInteger());
};
def instr2 = input.collect {
    def matcher = it =~ /^([A-Z]{1}) ([0-9]+) \((#[0-9a-f]{6})\)$/
    return hex2instruction(matcher[0][3]);
};

def instructionsToVertices(def instructions) {
    def vertices = [new Pos(0, 0)];
    for (Instruction instruction in instructions) {
        def curr = vertices[-1];
        switch(instruction.dir) {
            case 'U':
                vertices.add(new Pos(curr.x, curr.y - instruction.len))
            break
            case 'D':
                vertices.add(new Pos(curr.x, curr.y + instruction.len))
            break
            case 'L':
                vertices.add(new Pos(curr.x - instruction.len, curr.y))
            break
            case 'R':
                vertices.add(new Pos(curr.x + instruction.len, curr.y))
            break
            default:
                assert false : "Invalid instruction.dir";
            break
        }
    }

    assert vertices[0] == new Pos(0, 0);
    assert vertices[-1] == new Pos(0, 0);
    vertices.remove(vertices.size() - 1);
    assert vertices[0] == new Pos(0, 0);

    return vertices;
}

println(area(instructionsToVertices(instr1)));
println(area(instructionsToVertices(instr2)));
