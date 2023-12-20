import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


@EqualsAndHashCode
@ToString
@TupleConstructor
class Rule {
    char category;
    char comparator;
    int value;
    String target;

    boolean matches(Part part) {
        if (category == '_' as char) {
            return true;
        }

        int partValue;
        switch(category) {
            case 'x' as char: partValue = part.x; break;
            case 'm' as char: partValue = part.m; break;
            case 'a' as char: partValue = part.a; break;
            case 's' as char: partValue = part.s; break;
            default: assert false : "Invalid part category";
        }

        if (comparator == '>' as char) {
            return partValue > value;
        } else if (comparator == '<' as char) {
            return partValue < value;
        } else {
            assert false : "Invalid rule comparator";
        }
    }
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class Workflow {
    String name;
    List<Rule> rules;

    String process(Part part) {
        for (Rule rule in rules) {
            if (rule.matches(part)) {
                return rule.target;
            }
        }
    }
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class Part {
    int x;
    int m;
    int a;
    int s;

    def value() {
        return x + m + a + s;
    }
}

def input = new File("input/day19.txt").readLines().join("\n").split("\n\n");
def workflows = input[0].split("\n").collect { workflow ->
    def parts = workflow.split("[{]");
    def name = parts[0];
    def rules = parts[1][0..-2].split(",").collect { rule -> 
        if (rule.contains(":")) {
            def matcher = rule =~ /^([xmas]{1})([><]{1})([0-9]+):(.*)$/
            return new Rule(matcher[0][1] as char, matcher[0][2] as char, matcher[0][3].toInteger(), matcher[0][4]);
        } else {
            return new Rule('_' as char, '_' as char, -1, rule);
        }
    };
    return new Workflow(name, rules);
}.collectEntries { wf -> [wf.name, wf] };
def parts = input[1].split("\n").collect { part ->
    part = part[1..-2].split(",").collect { rating ->
        def parts = rating.split("=");
        return parts[1].toInteger();
    }
    return new Part(part[0], part[1], part[2], part[3]);
};

// println(workflows);
// println(parts);

def isPartAccepted(def workflows, Part part) {
    String curr = 'in';
    while (curr != 'A' && curr != 'R') {
        def workflow = workflows[curr];
        curr = workflow.process(part);
    }

    if (curr == 'A' || curr == 'R') {
        return curr;
    }

    assert false : "Undefined part acceptance";
}

{ // Part 1
    def acceptedParts = [];
    for (Part part in parts) {
        if (isPartAccepted(workflows, part) == 'A') {
            acceptedParts << part;
        }
    }
    println(acceptedParts.collect { it.value() }.sum());
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class PartRange {
    Integer xFrom;
    Integer xTo;
    Integer mFrom;
    Integer mTo;
    Integer aFrom;
    Integer aTo;
    Integer sFrom;
    Integer sTo;

    def split(Rule rule) {
        // I'm sure this can be expressed better with map or something.
        if (rule.category == 'x') {
            def xStart = xFrom;
            def xSplit = rule.value - (rule.comparator == '<' ? 1 : 0);
            def xEnd = xTo;

            return [
                new PartRange(xStart, xSplit, mFrom, mTo, aFrom, aTo, sFrom, sTo),
                new PartRange(xSplit + 1, xEnd, mFrom, mTo, aFrom, aTo, sFrom, sTo),
            ];
        } else if (rule.category == 'm') {
            def mStart = mFrom;
            def mSplit = rule.value - (rule.comparator == '<' ? 1 : 0);
            def mEnd = mTo;

            return [
                new PartRange(xFrom, xTo, mStart, mSplit, aFrom, aTo, sFrom, sTo),
                new PartRange(xFrom, xTo, mSplit + 1, mEnd, aFrom, aTo, sFrom, sTo),
            ];
        } else if (rule.category == 'a') {
            def aStart = aFrom;
            def aSplit = rule.value - (rule.comparator == '<' ? 1 : 0);
            def aEnd = aTo;

            return [
                new PartRange(xFrom, xTo, mFrom, mTo, aStart, aSplit, sFrom, sTo),
                new PartRange(xFrom, xTo, mFrom, mTo, aSplit + 1, aEnd, sFrom, sTo),
            ];
        } else if (rule.category == 's') {
            def sStart = sFrom;
            def sSplit = rule.value - (rule.comparator == '<' ? 1 : 0);
            def sEnd = sTo;

            return [
                new PartRange(xFrom, xTo, mFrom, mTo, aFrom, aTo, sStart, sSplit),
                new PartRange(xFrom, xTo, mFrom, mTo, aFrom, aTo, sSplit + 1, sEnd),
            ];
        } else if (rule.category == '_') {
            return [new PartRange(xFrom, xTo, mFrom, mTo, aFrom, aTo, sFrom, sTo)];
        } else {
            assert false : "Invalid rule category";
        }
    }
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class State {
    PartRange range;
    String workflowName;
}

def start = new State(new PartRange(1, 4000, 1, 4000, 1, 4000, 1, 4000), 'in');
def states = [start];
def acceptedRanges = [];
def rejectedRanges = [];
while (states.size() > 0) {
    def curr = states.pop();
    def range = curr.range;
    def workflow = workflows[curr.workflowName];
    
    if (curr.workflowName == 'A') {
        acceptedRanges << range;
        continue;
    }

    if (curr.workflowName == 'R') {
        rejectedRanges << range;
        continue;
    }

    // Split workflow range for each workflow rule.
    def remainingRange = range;
    for (def rule in workflow.rules) {
        def subranges = remainingRange.split(rule);
        if (subranges.size() == 1) {
            states << new State(subranges[0], rule.target);
            continue;
        }

        if (rule.comparator == '>') {
            states << new State(subranges[1], rule.target);
            remainingRange = subranges[0];
            continue;
        } else if (rule.comparator == '<') {
            states << new State(subranges[0], rule.target);
            remainingRange = subranges[1];
            continue;
        } else {
            assert false : "Invalid rule comparator";
        }
    }
}

def rangesToCount(List<PartRange> ranges) {
    ranges.collect {
        return 1 as BigInteger * 
            (1 + it.xTo - it.xFrom) *
            (1 + it.mTo - it.mFrom) *
            (1 + it.aTo - it.aFrom) *
            (1 + it.sTo - it.sFrom);
    }.inject(0 as BigInteger) { acc, it -> acc + it };
}

def totalParts = 4000 as BigInteger * 4000 * 4000 * 4000;
def acceptedParts = rangesToCount(acceptedRanges);
def rejectedParts = rangesToCount(rejectedRanges);

for (def range in acceptedRanges) {
    def rsx = new Part(range.xFrom, range.mFrom, range.aFrom, range.sFrom);
    def rex = new Part(range.xTo, range.mTo, range.aTo, range.sTo);
    assert isPartAccepted(workflows, rsx) == 'A';
    assert isPartAccepted(workflows, rex) == 'A';
}
for (def range in rejectedRanges) {
    def rsx = new Part(range.xFrom, range.mFrom, range.aFrom, range.sFrom);
    def rex = new Part(range.xTo, range.mTo, range.aTo, range.sTo);
    assert isPartAccepted(workflows, rsx) == 'R';
    assert isPartAccepted(workflows, rex) == 'R';
}


println(acceptedParts);
