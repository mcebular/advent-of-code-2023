import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


@EqualsAndHashCode
@ToString
@TupleConstructor
class Pos {
    final int x;
    final int y;

    Pos dir(char d) {
        switch(d) {
            case "N": return new Pos(x    , y - 1);
            case "E": return new Pos(x + 1, y    );
            case "S": return new Pos(x    , y + 1);
            case "W": return new Pos(x - 1, y    );
            default:
                assert false: "Invalid direction: $d";
        }
    }

    Pos[] diadir(int d) {
        // 0N1
        // W E
        // 3S2
        switch(d) {
            case 0: return [new Pos(x - 1, y - 1), new Pos(x    , y - 1)];
            case 1: return [new Pos(x + 1, y - 1), new Pos(x + 1, y    )];
            case 2: return [new Pos(x + 1, y + 1), new Pos(x    , y + 1)];
            case 3: return [new Pos(x - 1, y + 1), new Pos(x - 1, y    )];
            default:
                assert false: "Invalid direction: $d";
        }
    }
}

class Tiles {
    def arr;
    def width;
    def height;

    Tiles(List<String> input) {
        arr = input.join("").toCharArray();
        width = input[0].size();
        height = input.size();
    }

    def getAt(Pos pos) {
        int arrpos = pos.x + pos.y * width;
        return arr[arrpos];
    }

    def putAt(Pos pos, def val) {
        int arrpos = pos.x + pos.y * width;
        arr[arrpos] = val;
    } 

    def positionOf(char c) {
        def startPos = arr.findIndexOf { it == c };
        return new Pos(startPos % width as int, Math.floor(startPos / width) as int);
    }

    def nextPositions(Pos pos) {
        def x = pos.x;
        def y = pos.y;

        switch(this[pos]) {
            case "|":
                return [new Pos(x, y + 1), new Pos(x, y - 1)];
            case "-":
                return [new Pos(x + 1, y), new Pos(x - 1, y)];
            case "F":
                return [new Pos(x + 1, y), new Pos(x, y + 1)];
            case "L":
                return [new Pos(x + 1, y), new Pos(x, y - 1)];
            case "J":
                return [new Pos(x - 1, y), new Pos(x, y - 1)];
            case "7":
                return [new Pos(x - 1, y), new Pos(x, y + 1)];
            default:
                assert false;
        }
    }

    def removeJunkPipes(def mainPipe) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!mainPipe.contains(new Pos(x, y))) {
                    this[new Pos(x, y)] = "." as char;
                }
            }
        }
    }

    def floodFill(Pos[] start, Closure condition, char value) {
        def frontier = start.collect();
        while (frontier.size() > 0) {
            def curr = frontier.pop();
            if (condition(this[curr])) {
                this[curr] = value;
                if (curr.x < this.width - 1)  frontier.push(new Pos(curr.x + 1, curr.y));
                if (curr.x > 0)               frontier.push(new Pos(curr.x - 1, curr.y));
                if (curr.y < this.height - 1) frontier.push(new Pos(curr.x, curr.y + 1));
                if (curr.y > 0)               frontier.push(new Pos(curr.x, curr.y - 1));
            }
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

def input = new File("input/day10.txt").readLines();
def tiles = new Tiles(input);

// Find starting position.
Pos startPos = tiles.positionOf("S" as char);

{ // Replace starting position with an appropriate pipe.
    def northPipe = "|7F".toCharArray().findIndexOf { it == tiles[new Pos(startPos.x, startPos.y - 1)] } >= 0;
    def southPipe = "|LJ".toCharArray().findIndexOf { it == tiles[new Pos(startPos.x, startPos.y + 1)] } >= 0;
    def westPipe = "-LF".toCharArray().findIndexOf { it == tiles[new Pos(startPos.x - 1, startPos.y)] } >= 0; 
    def eastPipe = "-7J".toCharArray().findIndexOf { it == tiles[new Pos(startPos.x + 1, startPos.y)] } >= 0;
    if (northPipe && southPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "|" as char;
    } else if (westPipe && eastPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "-" as char;
    } else if (northPipe && westPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "J" as char;
    } else if (northPipe && eastPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "L" as char;
    } else if (southPipe && westPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "7" as char;
    } else if (southPipe && eastPipe) {
        tiles[new Pos(startPos.x, startPos.y)] = "F" as char;
    } else {
        assert false;
    }
}

def pipeLoop = { // Part 1
    def curr = startPos;
    def direction = 
        tiles[curr] == "|" as char ? "N" :
        tiles[curr] == "-" as char ? "E" :
        tiles[curr] == "7" as char ? "W" :
        tiles[curr] == "F" as char ? "E" :
        tiles[curr] == "J" as char ? "N" :
        tiles[curr] == "L" as char ? "E" :
        null;
    def loop = [];
    while (loop[0] != curr) {
        loop << curr;
        curr = curr.dir(direction as char);
        switch (direction) {
            case "N":
                if (tiles[curr] == "|" as char) {
                    direction = "N";
                } else if (tiles[curr] == "F" as char) {
                    direction = "E";
                } else if (tiles[curr] == "7" as char) {
                    direction = "W";
                } else assert false;
                break;
            case "S":
                if (tiles[curr] == "|" as char) {
                    direction = "S";
                } else if (tiles[curr] == "L" as char) {
                    direction = "E";
                } else if (tiles[curr] == "J" as char) {
                    direction = "W";
                } else assert false;
                break;
            case "E":
                if (tiles[curr] == "-" as char) { 
                    direction = "E";
                } else if (tiles[curr] == "7" as char) {
                    direction = "S";
                } else if (tiles[curr] == "J" as char) {
                    direction = "N";
                } else assert false;
                break;
            case "W":
                if (tiles[curr] == "-" as char) {
                    direction = "W";
                } else if (tiles[curr] == "L" as char) {
                    direction = "N";
                } else if (tiles[curr] == "F" as char) {
                    direction = "S";
                } else assert false;
                break;
        }
    }

    println(loop.size() / 2);
    return loop;
}();

{ // Part 2
    tiles.removeJunkPipes(pipeLoop);
    {
        def curr = tiles.positionOf("F" as char);
        def direction = "E";

        def innerDirection = 2;
        def inner = [];
        for (int i in 0..(pipeLoop.size())) {
            curr.diadir(innerDirection).each { inner << it };
            curr = curr.dir(direction as char);
            switch (direction) {
                case "N":
                    if (tiles[curr] == "|" as char) {
                        direction = "N";
                    } else if (tiles[curr] == "F" as char) {
                        direction = "E";
                        innerDirection += 1;
                    } else if (tiles[curr] == "7" as char) {
                        direction = "W";
                        innerDirection -= 1;
                    } else assert false;
                    break;
                case "S":
                    if (tiles[curr] == "|" as char) {
                        direction = "S";
                    } else if (tiles[curr] == "L" as char) {
                        direction = "E";
                        innerDirection -= 1;
                    } else if (tiles[curr] == "J" as char) {
                        direction = "W";
                        innerDirection += 1;
                    } else assert false;
                    break;
                case "E":
                    if (tiles[curr] == "-" as char) { 
                        direction = "E";
                    } else if (tiles[curr] == "7" as char) {
                        direction = "S";
                        innerDirection += 1;
                    } else if (tiles[curr] == "J" as char) {
                        direction = "N";
                        innerDirection -= 1;
                    } else assert false;
                    break;
                case "W":
                    if (tiles[curr] == "-" as char) {
                        direction = "W";
                    } else if (tiles[curr] == "L" as char) {
                        direction = "N";
                        innerDirection += 1;
                    } else if (tiles[curr] == "F" as char) {
                        direction = "S";
                        innerDirection -= 1;
                    } else assert false;
                    break;
            }

            innerDirection = (innerDirection + 4) % 4;
        }

        tiles.floodFill(
            inner.findAll { tiles[it] == "." as char } as Pos[],
            { it == "." as char },
            "X" as char
        );
    }
    
    println(tiles.arr.findAll { it == "X" as char }.size());
}