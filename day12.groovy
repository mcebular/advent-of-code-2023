import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


long fitArrangementToCondition(String arrangement, int[] conditions) {
    return fitArrangementToConditionRecursive(arrangement, 0, conditions, false, [:]);
}

long fitArrangementToConditionRecursive(
    String arrangement, 
    int arrangementIndex, 
    int[] conditions, 
    boolean conditionActive, 
    Map<String, Long> memo
) {
    def remainingArrangement = arrangement.collect();
    def remainingConditions = conditions.collect();

    def memoKey = "${remainingArrangement[arrangementIndex..-1].join()}, $arrangementIndex, $remainingConditions, $conditionActive";
    if (memo[memoKey] != null) {
        return memo[memoKey];
    }

    if (remainingArrangement.size() - arrangementIndex <= remainingConditions.sum(0) + remainingConditions.size() - 2) {
        // Oops! We don't have enough space to satisfy all conditions.
        return 0L;
    }

    while (arrangementIndex < remainingArrangement.size()) {
        if (remainingArrangement[arrangementIndex] == '#' as char) {
            // Init or subtract from current condition.
            if (!conditionActive) {
                if (remainingConditions.size() == 0) {
                    // Oops! More # groups than there are conditions!
                    return 0L;
                }
                conditionActive = true;
                remainingConditions[0] -= 1;
            } else if (conditionActive && remainingConditions[0] <= 0) {
                // Oops! More #s than current condition allows!
                return 0L;
            } else {
                remainingConditions[0] -= 1;
            }
        } else if (remainingArrangement[arrangementIndex] == '.' as char) {
            // Deinit current condition (if condition fulfilled).
            if (conditionActive) {
                if (remainingConditions[0] == 0) {
                    remainingConditions.remove(0);
                    conditionActive = false;
                } else {
                    // Oops! Less #s than condition allows!
                    return 0L;
                }
            }
        } else if (remainingArrangement[arrangementIndex] == '?' as char) {
            // Option 1: the '?' is a '.'
            def remainingArrangement1 = remainingArrangement.collect();
            remainingArrangement1[arrangementIndex] = '.';

            // Option 2: the '?' is a '#'
            // Actually: there should be a series of '#' to cover the rest of the current condition.
            def remainingArrangement2 = null;
            if (remainingConditions[0] > 0) {
                remainingArrangement2 = remainingArrangement.collect();
                for (int i = 0; i < remainingConditions[0]; i++) {
                    if (arrangementIndex + i >= remainingArrangement2.size()) {
                        // Oops! We don't have enough remaining arrangement!
                        remainingArrangement2 = null;
                        break;
                    }

                    if (remainingArrangement2[arrangementIndex + i] == '.') {
                        // Oops! We don't have enough '#' or '?' to fulfill current condition.
                        remainingArrangement2 = null;
                        break;
                    } else {
                        remainingArrangement2[arrangementIndex + i] = "#";
                    }
                }
            }
            
            def result1 = fitArrangementToConditionRecursive(
                remainingArrangement1.join(), 
                arrangementIndex, 
                remainingConditions as int[], 
                conditionActive,
                memo
            );
            memo["${remainingArrangement1[arrangementIndex..-1].join()}, $arrangementIndex, $remainingConditions, $conditionActive"] = result1;

            def result2 = 0;
            if (remainingArrangement2 != null) {
                result2 = remainingArrangement2 == null ? 0L : fitArrangementToConditionRecursive(
                    remainingArrangement2.join(), 
                    arrangementIndex, 
                    remainingConditions as int[], 
                    conditionActive,
                    memo
                );
                memo["${remainingArrangement2[arrangementIndex..-1].join()}, $arrangementIndex, $remainingConditions, $conditionActive"] = result2;
            }

            return result1 + result2;
        } else {
            assert false : "Invalid character '${remainingArrangement[arrangementIndex]}'";
        }

        arrangementIndex += 1;
    }

    if (remainingConditions.size() > 1 || (remainingConditions.size() == 1 && remainingConditions[0] > 0)) {
        // Oops! Less #s than there are conditions.
        return 0L;
    }

    return 1L;
}

def unfold(String arrangement, int[] conditions) {
    def unfoldedArr = (0..<5).collect { arrangement }.join("?");
    def unfoldedCon = (0..<5).collect { conditions.collect().join(",") }.join(",").split(",").collect { it.toInteger() };
    return [unfoldedArr, unfoldedCon as int[]];
}

{ // Test: fitArrangementToCondition
    assert fitArrangementToCondition("???.###", [1, 1, 3] as int[]) == 1;
    assert fitArrangementToCondition("????.###", [1, 1, 3] as int[]) == 3;

    assert fitArrangementToCondition("?###????????", [3, 2, 1] as int[]) == 10;
    
    assert fitArrangementToCondition("?????", [1, 1] as int[]) == 6;
    assert fitArrangementToCondition("#????", [1, 2] as int[]) == 2;
    assert fitArrangementToCondition(".#????.", [1, 2] as int[]) == 2;
    assert fitArrangementToCondition("????#", [1, 2] as int[]) == 2;
    assert fitArrangementToCondition(".????#.", [1, 2] as int[]) == 2;
    
    assert fitArrangementToCondition("??????#?", [1, 2, 1] as int[]) == 3;
    assert fitArrangementToCondition("??????#????????#?", [1, 2, 1, 1, 2, 1] as int[]) == 22;

    assert fitArrangementToCondition("?????????#?", [1, 4, 1] as int[]) == 6;
    assert fitArrangementToCondition("?????????#???????????#?", [1, 4, 1, 1, 4, 1] as int[]) == 92;
    assert fitArrangementToCondition("?????????#???????????#???????????#?", [1, 4, 1, 1, 4, 1, 1, 4, 1] as int[]) == 1981;
}

def input = new File("input/day12.txt").readLines().collect { line ->
    def parts = line.split(" ");
    return [parts[0], parts[1].split(",").collect { it.toInteger() } as int[]];
};

def printElapsedTime(Closure closure) {
    Date start = new Date();
    closure();
    Date stop = new Date();
    println(TimeCategory.minus(stop, start));
}

printElapsedTime {
    println("\n" + input.collect {
        print('.');
        return fitArrangementToCondition(it[0], it[1]);
    }.sum());
}

printElapsedTime {
    println("\n" + input.collect{ unfold(it[0], it[1]) }.collect {
        print('.');
        return fitArrangementToCondition(it[0], it[1]);
    }.sum());
}
