import groovy.transform.TupleConstructor;


def input = new File("input/day03.txt").readLines();

@TupleConstructor
class PartNumber {
    Schematic schematic;
    int x;
    int y;
    int len;
    int value;

    boolean isValid() {
        int x0 = Math.max(0, x - 1);
        int x1 = Math.min(schematic.width - 1, x + len + 1);
        int y0 = Math.max(0, y - 1);
        int y1 = Math.min(schematic.height - 1, y + 1);
        for (int j in y0..y1) {
            for (int i in x0..x1) {
                char c = schematic.get(i, j);
                if (!c.isDigit() && c != '.') {
                    return true;
                }
            }
        }

        return false;
    }

    boolean includes(int px, int py) {
        int x0 = Math.max(0, x - 1);
        int x1 = Math.min(schematic.width - 1, x + len + 1);
        int y0 = Math.max(0, y - 1);
        int y1 = Math.min(schematic.height - 1, y + 1);
        
        return x0 <= px && px <= x1 && y0 <= py && py <= y1;
    }

    String toString() {
        int x0 = Math.max(0, x - 1);
        int x1 = Math.min(schematic.width - 1, x + len + 1);
        int y0 = Math.max(0, y - 1);
        int y1 = Math.min(schematic.height - 1, y + 1);
        
        def result = "";
        for (int j in y0..y1) {
            for (int i in x0..x1) {
                result += schematic.get(i, j);
            }
            result += "\n";
        }
        return result;
    }
}

class Schematic {
    private final char[] arr;
    final int width;
    final int height;

    private def partNumbersCache;

    Schematic(List<String> input) {
        arr = input.join("").toCharArray();
        width = input[0].size();
        height = input.size();
    }

    def get(int x, int y) {
        int pos = x + y * width;
        return arr[pos];
    }

    def getPartNumbers() {
        if (partNumbersCache != null) {
            return partNumbersCache;
        }

        def result = [];
        
        def numberIndices = arr.findIndexValues { it.isDigit() };

        int pnStart = numberIndices[0];
        int curr, next;
        for (int i = 0; i < numberIndices.size(); i++) {
            curr = numberIndices[i];
            next = numberIndices[i + 1] ?: -1;
            if (next - curr == 1) {
                continue;
            }

            int pnX = pnStart % width;
            int pnY = Math.floor(pnStart / width);
            def pnEnd = curr;
            def pnLength = pnEnd - pnStart;
            result << new PartNumber(this, pnX, pnY, pnLength, arr[pnStart..pnEnd].join("").toInteger());
            
            pnStart = next;
        }

        partNumbersCache = result;
        return result;
    }

    def getGears() {
        def gearIndices = arr.findIndexValues { it == '*' };
        return gearIndices.collect { 
            int gX = it % width;
            int gY = Math.floor(it / width);
            return getPartNumbers().findAll { pn -> 
                return pn.includes(gX, gY);
            };
        }.findAll { pnc -> pnc.size() == 2 };
    }

    String toString() {
        def result = "";
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result += this.get(x, y);
            }
            result += "\n";
        }
        return result;
    }
}

def schematic = new Schematic(input);

def partNumberSum = schematic.getPartNumbers().inject(0) { acc, it ->
    return it.isValid() ? acc + it.value : acc;
};
println(partNumberSum);

def gearRatioSum = schematic.getGears().inject(0) { acc, it ->
    return acc + (it[0].value * it[1].value);
};
println(gearRatioSum);