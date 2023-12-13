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

class Pattern {
    def arr;
    def width;
    def height;

    Pattern(List<String> input) {
        arr = input.join("").toCharArray();
        width = input[0].size();
        height = input.size();
    }

    def getAt(Pos pos) {
        int arrpos = pos.x + pos.y * width;
        return arr[arrpos];
    }

    def putAt(Pos pos, def value) {
        int arrpos = pos.x + pos.y * width;
        arr[arrpos] = value;
    }

    def posOf(char c) {
        return arr.findIndexValues { it == c }.collect {
            new Pos(it % width as int, Math.floor(it / width) as int)
        };
    }

    def isRowMirroredAt(int y, int r) {
        if (r == 0 || r == width) {
            return false;
        }

        for (int i = 0; i <= r; i++) {
            def left = this[new Pos(i, y)];
            def rightPos = r * 2 - 1 - i;
            if (rightPos < width) {
                def right = this[new Pos(rightPos, y)];
                if (left != right) {
                    return false;
                }
            }
        }
        return true;
    }

    def isColMirroredAt(int x, int r) {
        if (r == 0 || r == height) {
            return false;
        }

        for (int j = 0; j <= r; j++) {
            def top = this[new Pos(x, j)];
            def bottomPos = r * 2 - 1 - j;
            if (bottomPos < height) {
                def bottom = this[new Pos(x, bottomPos)];
                if (top != bottom) {
                    return false;
                }
            }
        }
        return true;
    }

    def findVerticalReflection(def ignore = null) {
        def possibleReflections = (1..width) as int[];
        for (int j = 0; j < height; j++) {
            possibleReflections = possibleReflections.findAll {
                isRowMirroredAt(j, it);
            }
        }

        possibleReflections = possibleReflections.findAll {
            ignore == null || ignore != it
        }
        assert possibleReflections.size() <= 1;
        if (possibleReflections.size() == 1) {
            return possibleReflections[0];
        } else {
            return null;
        }
    }

    def findHorizontalReflection(def ignore = null) {
        def possibleReflections = (1..height) as int[];
        for (int i = 0; i < width; i++) {
            
            possibleReflections = possibleReflections.findAll {
                isColMirroredAt(i, it);
            }
        }
        
        possibleReflections = possibleReflections.findAll {
            ignore == null || ignore != it
        }
        assert possibleReflections.size() <= 1 : "$possibleReflections\n$this";
        if (possibleReflections.size() == 1) {
            return possibleReflections[0];
        } else {
            return null;
        }
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

{ // Test: Pattern.isRowMirroredAt
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 0) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 1) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 2) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 3) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 4) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 5) == true;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 6) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 7) == true;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 8) == false;
    assert new Pattern(["#.##..##."]).isRowMirroredAt(0, 9) == false;
    assert new Pattern(["..#.##.#."]).isRowMirroredAt(0, 5) == true;
    assert new Pattern(["#.#.##."]).isRowMirroredAt(0, 6) == false;
}

def printResult(def reflections) {
    def result = reflections.collect {
        assert !(it[0] == null && it[1] == null) : "No reflection";
        [it[0] ?: 0, it[1] ?: 0]
    }.inject([0, 0]) { acc, it ->
        [acc[0] + it[0], acc[1] + it[1]]
    }
    println(result[0] + result[1] * 100);
}

def input = new File("input/day13.txt").readLines().join("\n").split("\n\n");

printResult(
    input.collect {
        new Pattern(it.split("\n") as List);
    }.collect { 
        [ it.findVerticalReflection(), it.findHorizontalReflection() ]
    }
);
printResult(
    input.collect { pattern -> 
        def orig = new Pattern(pattern.split("\n") as List);
        def result = [orig];
        for (int j = 0; j < orig.height; j++) {
            for (int i = 0; i < orig.width; i++) {
                def p = new Pattern(pattern.split("\n") as List);
                char c = p[new Pos(i, j)];
                p[new Pos(i, j)] = c == '.' as char ? '#' as char : '.' as char;
                result << p;
            }
        }
        return result;
    }.collect { patterns ->
        def orig = patterns[0];
        def ovr = orig.findVerticalReflection() ?: 0;
        def ohr = orig.findHorizontalReflection() ?: 0;
        for (def pattern in patterns[1..-1]) {
            def vr = pattern.findVerticalReflection(ovr);
            def hr = pattern.findHorizontalReflection(ohr);
            if (vr != null || hr != null) {
                return [vr, hr];
            }
        }
        assert false : "No reflection found";
    }
);