import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


@EqualsAndHashCode
@ToString
@TupleConstructor
class Pos {
    final int x;
    final int y;
}

class Image {
    def arr;
    def width;
    def height;

    Image(List<String> input) {
        arr = input.join("").toCharArray();
        width = input[0].size();
        height = input.size();
    }

    def getAt(Pos pos) {
        int arrpos = pos.x + pos.y * width;
        return arr[arrpos];
    }

    def posOf(char c) {
        return arr.findIndexValues { it == c }.collect {
            new Pos(it % width as int, Math.floor(it / width) as int)
        };
    }

    def emptyRows() {
        def result = [];
        for (int j = 0; j < height; j++) {
            def isEmpty = true;
            for (int i = 0; i < width; i++) {
                if (this[new Pos(i, j)] != '.' as char) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                result << j;
            }
        }
        return result;
    }

    def emptyCols() {
        def result = [];
        for (int i = 0; i < width; i++) {
            def isEmpty = true;
            for (int j = 0; j < height; j++) {
                if (this[new Pos(i, j)] != '.' as char) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                result << i;
            }
        }
        return result;
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


def pathBetween(Pos a, Pos b) {
    def x0 = Math.min(a.x, b.x);
    def x1 = Math.max(a.x, b.x);
    def y0 = Math.min(a.y, b.y);
    def y1 = Math.max(a.y, b.y);

    def result = [];
    result.addAll((x0..x1).collect { new Pos(it, y0) });
    result.addAll((y0<..y1).collect { new Pos(x1, it) });
    return result;
}

def galaxyDistancesSum(def image, int distance) {
    def galaxies = image.posOf('#' as char);
    def emptyRows = image.emptyRows();
    def emptyCols = image.emptyCols();

    def galaxyPairs = [];
    def galaxyPairsDistances = [];
    for (int a = 0; a < galaxies.size(); a++) {
        for (int b = a + 1; b < galaxies.size(); b++) {
            def ga = galaxies[a];
            def gb = galaxies[b];
            galaxyPairs = [ga, gb];

            def path = pathBetween(ga, gb)[1..-1];
            def pathAfterExpansion = path.collect {
                if (emptyRows.contains(it.y) || emptyCols.contains(it.x)) {
                    return distance;
                } else {
                    return 1;
                }
            }.inject(0 as BigInteger) { acc, it -> acc + it };
            galaxyPairsDistances << pathAfterExpansion;
        }
    }
    return galaxyPairsDistances.sum();
}


def input = new File("input/day11.txt").readLines();
def image = new Image(input);
// println(image);

println(galaxyDistancesSum(image, 2));
println(galaxyDistancesSum(image, 1000000));
