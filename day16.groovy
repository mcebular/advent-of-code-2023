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

@EqualsAndHashCode(callSuper = true)
class Beam extends Pos {
    final int dir;
    Beam(int x, int y, int dir) {
        super(x, y);
        this.dir = dir;
    }

    String toString() {
        return "Beam($x, $y, $dir)"
    }
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


def input = new File("input/day16.txt").readLines();
def platform = new Platform(input);
// println(platform);

Beam[] moveBeam(Platform platform, Beam beam) {
    switch (beam.dir) {
        case 0:
            def np = new Pos(beam.x, beam.y - 1);
            if (np.y < 0) {
                return [];
            }
            if (platform[np] == '.' as char || platform[np] == '|' as char) {
                return [new Beam(np.x, np.y, beam.dir)];
            } else if (platform[np] == '\\' as char) {
                return [new Beam(np.x, np.y, 3)];
            } else if (platform[np] == '/' as char) {
                return [new Beam(np.x, np.y, 1)];
            } else if (platform[np] == '-' as char) {
                return [new Beam(np.x, np.y, 1), new Beam(np.x, np.y, 3)];
            } else {
                assert false;
            }
        case 1:
            def np = new Pos(beam.x + 1, beam.y);
            if (np.x >= platform.width) {
                return [];
            }
            if (platform[np] == '.' as char || platform[np] == '-' as char) {
                return [new Beam(np.x, np.y, beam.dir)];
            } else if (platform[np] == '\\' as char) {
                return [new Beam(np.x, np.y, 2)];
            } else if (platform[np] == '/' as char) {
                return [new Beam(np.x, np.y, 0)];
            } else if (platform[np] == '|' as char) {
                return [new Beam(np.x, np.y, 0), new Beam(np.x, np.y, 2)];
            } else {
                assert false;
            }
        case 2:
            def np = new Pos(beam.x, beam.y + 1);
            if (np.y >= platform.height) {
                return [];
            }
            if (platform[np] == '.' as char || platform[np] == '|' as char) {
                return [new Beam(np.x, np.y, beam.dir)];
            } else if (platform[np] == '\\' as char) {
                return [new Beam(np.x, np.y, 1)];
            } else if (platform[np] == '/' as char) {
                return [new Beam(np.x, np.y, 3)];
            } else if (platform[np] == '-' as char) {
                return [new Beam(np.x, np.y, 1), new Beam(np.x, np.y, 3)];
            } else {
                assert false;
            }
        case 3:
            def np = new Pos(beam.x - 1, beam.y);
            if (np.x < 0) {
                return [];
            }
            if (platform[np] == '.' as char || platform[np] == '-' as char) {
                return [new Beam(np.x, np.y, beam.dir)];
            } else if (platform[np] == '\\' as char) {
                return [new Beam(np.x, np.y, 0)];
            } else if (platform[np] == '/' as char) {
                return [new Beam(np.x, np.y, 2)];
            } else if (platform[np] == '|' as char) {
                return [new Beam(np.x, np.y, 0), new Beam(np.x, np.y, 2)];
            } else {
                assert false;
            }
        default:
            assert false : "Invalid direction";
    }
}

int energize(Platform platform, Beam start) {
    def beams = [start];
    def energizedTiles = [:];
    while (beams.size() > 0) {
        def beam = beams.pop();
        if (energizedTiles[beam] != null) {
            continue;
        }
        energizedTiles[beam] = 1;

        beams.addAll(moveBeam(platform, beam));
    }

    return energizedTiles.keySet()
        .findAll { it != start }
        .collectEntries { [new Pos(it.x, it.y), 1] }
        .size();
}

def printElapsedTime(Closure closure) {
    Date start = new Date();
    closure();
    Date stop = new Date();
    println(TimeCategory.minus(stop, start));
}

printElapsedTime {
    println(energize(platform, new Beam(-1, 0, 1)));
}

printElapsedTime {
    def max = 0;
    for (int i = 0; i < platform.width; i++) {
        max = Math.max(max, energize(platform, new Beam(i, -1, 2)));
        max = Math.max(max, energize(platform, new Beam(i, platform.height, 0)));
    }

    for (int i = 0; i < platform.height; i++) {
        max = Math.max(max, energize(platform, new Beam(-1, i, 1)));
        max = Math.max(max, energize(platform, new Beam(platform.width, i, 3)));
    }

    println(max);
}