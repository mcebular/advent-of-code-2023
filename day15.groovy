import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


def HASH(String input) {
    def cval = 0;
    for (char c in input.toCharArray()) {
        int ci = c as int;
        cval += ci;
        cval *= 17;
        cval %= 256;
    }

    return cval;
}

{ // Test: HASH
    assert HASH("HASH") == 52;
}

def input = new File("input/day15.txt").readLines()
    .join(",")
    .split(",");

println(input.collect { HASH(it) }.sum());

def lenses = (0..255).collectEntries { [it, [:]] };
for (def it in input) {
    def matcher = it =~ /^([a-zA-Z]+)([-=]{1})([0-9]*)$/;
    def label = matcher[0][1];
    def operation = matcher[0][2];
    def value = matcher[0][3];
    def labelHash = HASH(label);
    if (operation == '=') {
        lenses[labelHash][label] = value.toInteger();
    } else if (operation == '-') {
        lenses[labelHash].remove(label);
    } else {
        assert false : "Invalid operatioin";
    }
}

def focusingPower(def lenses) {
    def result = 0;
    for (def entry in lenses) {
        def boxNumber = entry.key;
        def slot = 1;
        for (def lens in entry.value) {
            result += ((boxNumber + 1) * slot * lens.value);
            slot += 1;
        }
    }
    return result;
}

println(focusingPower(lenses));