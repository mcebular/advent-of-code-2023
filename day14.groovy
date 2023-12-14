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

class Platform {
    char[] arr;
    int width;
    int height;

    Platform(List<String> input) {
        arr = input.join("").toCharArray();
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

    def tiltNorth() {
        def roundRocks = posOf('O' as char);
        for (def rock in roundRocks) {
            // Move the rocks as far north as possible.
            for (int i = rock.y - 1; i >= 0; i--) {
                if (this[new Pos(rock.x, i)] == '.' as char) {
                    assert this[new Pos(rock.x, i + 1)] == 'O' as char;
                    this[new Pos(rock.x, i)] = 'O' as char;
                    this[new Pos(rock.x, i + 1)] = '.' as char;
                } else {
                    break;
                }
            }
        }
    }

    def tiltWest() {
        def roundRocks = posOf('O' as char);
        for (def rock in roundRocks) {
            // Move the rocks as far north as possible.
            for (int i = rock.x - 1; i >= 0; i--) {
                if (this[new Pos(i, rock.y)] == '.' as char) {
                    assert this[new Pos(i + 1, rock.y)] == 'O' as char;
                    this[new Pos(i, rock.y)] = 'O' as char;
                    this[new Pos(i + 1, rock.y)] = '.' as char;
                } else {
                    break;
                }
            }
        }
    }

    def tiltSouth() {
        def roundRocks = posOf('O' as char).reverse();
        for (def rock in roundRocks) {
            // Move the rocks as far north as possible.
            for (int i = rock.y + 1; i < height; i++) {
                if (this[new Pos(rock.x, i)] == '.' as char) {
                    assert this[new Pos(rock.x, i - 1)] == 'O' as char;
                    this[new Pos(rock.x, i)] = 'O' as char;
                    this[new Pos(rock.x, i - 1)] = '.' as char;
                } else {
                    break;
                }
            }
        }
    }

    def tiltEast() {
        def roundRocks = posOf('O' as char).reverse();
        for (def rock in roundRocks) {
            // Move the rocks as far north as possible.
            for (int i = rock.x + 1; i < width; i++) {
                if (this[new Pos(i, rock.y)] == '.' as char) {
                    assert this[new Pos(i - 1, rock.y)] == 'O' as char;
                    this[new Pos(i, rock.y)] = 'O' as char;
                    this[new Pos(i - 1, rock.y)] = '.' as char;
                } else {
                    break;
                }
            }
        }
    }

    def tiltCycle() {
        tiltNorth();
        tiltWest();
        tiltSouth();
        tiltEast();
    }

    def loadSumNorth() {
        def roundRocks = posOf('O' as char);
        return roundRocks.collect { height - it.y }.sum();
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

def input = new File("input/day14.txt").readLines();

{ // Part 1
    def platform = new Platform(input);
    platform.tiltNorth();
    println(platform.loadSumNorth());
}

{ // Part 2
    def platform = new Platform(input);
    def state = [:];
    def index = 0L;
    def firstRepeat = null;
    def totalCycles = 1000000000L;
    while (index < totalCycles) {
        if (state[platform.toString()] != null) {
            if (firstRepeat == null) {
                firstRepeat = [index, platform.toString()];
            } else if (firstRepeat[1] == platform.toString()) {
                def step = index - firstRepeat[0];
                def skipTo = Math.floor(totalCycles / step) as long;
                println("Repeat at $index, skipping to ${index + (skipTo - 3) * step}");
                index += (skipTo - 3) * step;
            }
        }

        state[platform.toString()] = platform.loadSumNorth();
        platform.tiltCycle();
        index++;
    }
    println(platform.loadSumNorth());
}