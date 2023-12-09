import groovy.transform.TupleConstructor;
import groovy.transform.ToString;


List sequenceDiff(List seq) {
    def result = [];
    for (int i = 0; i < seq.size() - 1; i++) {
        result << (seq[i + 1] - seq[i]);
    }
    return result;
}

{ // Test: sequenceDiff
    def d1 = sequenceDiff([0, 3, 6, 9, 12, 15]);
    assert d1.size() == 5;
    assert d1[0] == 3;
    assert d1[-1] == 3;

    def d2 = sequenceDiff([1, 3, 6, 10, 15, 21]);
    assert d2.size() == 5;
    assert d2[0] == 2;
    assert d2[-1] == 6;
}

boolean sequenceIsAllZeros(List seq) {
    for (int i = 0; i < seq.size(); i++) {
        if (seq[i] != 0) {
            return false;
        }
    }
    return true;
}

{ // Test: sequence is all zeros
    assert sequenceIsAllZeros([1, 0, 0]) == false;
    assert sequenceIsAllZeros([0, 1, 0]) == false;
    assert sequenceIsAllZeros([0, 0, 1]) == false;
    assert sequenceIsAllZeros([0, 0, 0]) == true;
}

def extendSequence(List seqs) {
    // forwards
    for (int i = seqs.size() - 1; i >= 0; i--) {
        seqs[i - 1] << (seqs[i - 1][-1] + seqs[i][-1]);
    }
    // backwards
    for (int i = seqs.size() - 1; i >= 0; i--) {
        seqs[i - 1].add(0, seqs[i - 1][0] - seqs[i][0]);
    }
}

def inputs = new File("input/day09.txt").readLines().collect { it.split(/\s+/).collect { it.toLong() } };

inputs.collect { input -> 
    def seqs = [input];
    while(!sequenceIsAllZeros(seqs[-1])) {
        seqs << sequenceDiff(seqs[-1]);
    }
    return seqs;
}.each { 
    extendSequence(it);
}.collect {
    [it[0][0], it[0][-1]]
}.inject([0, 0]) { acc, it -> 
    [acc[0] + it[0], acc[1] + it[1]] 
}.each { println(it) };
