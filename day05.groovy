import groovy.transform.TupleConstructor;
import groovy.transform.ToString;


@TupleConstructor
class MappingRange {
    long destination;
    long source;
    long length;

    String toString() {
        return "src: $source -> ${source+length-1}, dst: $destination -> ${destination+length-1}, len: $length";
    }

    Long map(long value) {
        if (value >= source && value < source + length) {
            long diff = value - source;
            return destination + diff;
        }
        return null;
    }
}

{ // Test: MappingRange.map()
    def range = new MappingRange(52, 50, 48);
    assert range.map(49) == null;
    assert range.map(50) == 52;
    assert range.map(97) == 99;
    assert range.map(98) == null;
}

@TupleConstructor
class Range {
    long start;
    long length;

    String toString() {
        return "Range($start -> ${start+length-1}, length=${length})";
    }

    Range[] split(MappingRange[] mappingRanges) {
        // Splits range into multiple ranges, where each range is either fully 
        // within or fully outside any of the mapping ranges.
        // Assumes mapping ranges do not overlap.

        def mappingPoints = mappingRanges.collect {
            [it.source, it.source + it.length]
        }.flatten().findAll { 
            it >= start && it <= start + length 
        };

        def splitPoints = [start, mappingPoints, start + length].flatten().sort().unique();
        
        def result = [];
        for (int i = 0; i < splitPoints.size() - 1; i++) {
            def rangeStart = splitPoints[i];
            def rangeEnd = splitPoints[i + 1];
            def rangeLength = rangeEnd - rangeStart;
            result << new Range(rangeStart, rangeLength);
        }

        return result;
    }

    Range map(MappingRange[] mappingRanges) {
        // Assumes range is fully-contained within one of the mapping ranges.
        for (def mappingRange in mappingRanges) {
            def mappedValue = mappingRange.map(start);
            if (mappedValue != null) {
                return new Range(mappedValue, length);
            }
        }
        return new Range(start, length);
    }

}

{ // Test: whole range is inside mapping.
    def testRangeSplit = new Range(15, 15).split([
        new MappingRange(110, 10, 50)
    ] as MappingRange[]);
    assert testRangeSplit.size() == 1;
    assert testRangeSplit[0].start == 15;
    assert testRangeSplit[0].length == 15;
}

{ // Test: start of the range is outside mapping.
    def testRangeSplit = new Range(0, 30).split([
        new MappingRange(110, 10, 50)
    ] as MappingRange[]);
    assert testRangeSplit.size() == 2;
    assert testRangeSplit[0].start == 0;
    assert testRangeSplit[0].length == 10;
    assert testRangeSplit[1].start == 10;
    assert testRangeSplit[1].length == 20;
}

{ // Test: end of the range is outside mapping.
    def testRangeSplit = new Range(10, 60).split([
        new MappingRange(110, 10, 50)
    ] as MappingRange[]);
    assert testRangeSplit.size() == 2;
    assert testRangeSplit[0].start == 10;
    assert testRangeSplit[0].length == 50;
    assert testRangeSplit[1].start == 60;
    assert testRangeSplit[1].length == 10;
}

{ // Test: start and end of the range are outside mapping.
    def testRangeSplit = new Range(0, 100).split([
        new MappingRange(110, 10, 50)
    ] as MappingRange[])
    assert testRangeSplit.size() == 3;
    assert testRangeSplit[0].start == 0;
    assert testRangeSplit[0].length == 10;
    assert testRangeSplit[1].start == 10;
    assert testRangeSplit[1].length == 50;
    assert testRangeSplit[2].start == 60;
    assert testRangeSplit[2].length == 40;
}

{ // Test: the range is split between two mapping ranges.
    def testRangeSplit = new Range(0, 100).split([
        new MappingRange(110, 10, 10),
        new MappingRange(110, 60, 10)
    ] as MappingRange[]);
    assert testRangeSplit.size() == 5;
    assert testRangeSplit[0].start == 0;
    assert testRangeSplit[0].length == 10;
    assert testRangeSplit[1].start == 10;
    assert testRangeSplit[1].length == 10;
    assert testRangeSplit[2].start == 20;
    assert testRangeSplit[2].length == 40;
    assert testRangeSplit[3].start == 60;
    assert testRangeSplit[3].length == 10;    
    assert testRangeSplit[4].start == 70;
    assert testRangeSplit[4].length == 30;
}

@TupleConstructor
@ToString
class Item {
    String type;
    Range[] ranges;
}

class Almanac {
    Map<String, Map<String, MappingRange[]>> ranges = [:];

    void addRange(String src, String dst, MappingRange range) {
        if (ranges[src] == null) {
            ranges[src] = [:];
        }
        if (ranges[src][dst] == null) {
            ranges[src][dst] = [];
        }
        ranges[src][dst] << range;
    }

    MappingRange[] getRanges(String src, String dst) {
        return ranges[src][dst];
    }

    Item mapItem(Item item) {
        def dst = ranges[item.type].keySet()[0];
        MappingRange[] targetRanges = ranges[item.type][dst];

        def itemRanges = item.ranges.collect {
            it.split(targetRanges);
        }.flatten().collect {
            it.map(targetRanges);
        };
        return new Item(dst, itemRanges as Range[]);
    }

    Item mapItemTo(Item item, String dst) {
        while (item.type != dst) {
            item = mapItem(item);
        }
        return item;
    }
}


def input = new File("input/day05.txt")
    .readLines()
    .inject("") { acc, line -> 
        acc + "_" + line 
    }
    .split("__")
    .collect { 
        it.split("_").join("\n")
    };

Almanac almanac = new Almanac();
input[1..-1].collect { section ->
    def parts = section.split("\n");
    
    def title = parts[0];
    def titleParts = title.split(" ")[0].split("-to-");
    def src = titleParts[0];
    def dst = titleParts[1];
    
    def ranges = parts[1..-1];
    ranges.collect { range -> 
        long[] t = range.split(/ +/).collect{ it.toLong() };
        new MappingRange(t[0], t[1], t[2]);
    }.each { almanac.addRange(src, dst, it) };
}

long[] seeds = input[0].split(": ")[1].split(/ +/).collect { it.toLong() };

println seeds
    .collect { 
        new Item("seed", [new Range(it.toLong(), 1)] as Range[]) 
    }.collect { item -> 
        almanac.mapItemTo(item, "location").ranges
            .collect { it.start }
            .min()
    }.min();

println seeds
    .inject([[]]) { acc, it ->
        // Make value pairs from the list.
        if (acc[-1].size() >= 2) {
            acc << [];
        }
        acc[-1] << it.toLong();
        return acc;
    }.collect {
        new Item("seed", [new Range(it[0], it[1])] as Range[]);
    }.collect { item ->
        return almanac.mapItemTo(item, "location").ranges
            .collect { it.start }
            .min();
    }.min();
