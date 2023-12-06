import groovy.transform.TupleConstructor;
import groovy.transform.ToString;


def input = new File("input/day06.txt")
    .readLines()

def times = input[0].split(/:\W+/)[1].split(/\W+/).collect { it.toLong() };
def distances = input[1].split(/:\W+/)[1].split(/\W+/).collect { it.toLong() };

@TupleConstructor
@ToString
class Race {
    long time;
    long distance;

    long play(long holdingButtonTime) {
        long speed = holdingButtonTime;
        long timeRemaining = time - holdingButtonTime;
        return timeRemaining * speed;
    }
}

{
    assert new Race(7, 9).play(0) == 0
    assert new Race(7, 9).play(1) == 6
    assert new Race(7, 9).play(2) == 10
    assert new Race(7, 9).play(3) == 12
    assert new Race(7, 9).play(4) == 12
    assert new Race(7, 9).play(5) == 10
    assert new Race(7, 9).play(6) == 6
    assert new Race(7, 9).play(7) == 0
}

def races = (0..<times.size()).collect { i -> 
    new Race(times[i], distances[i]);
}


println races.collect { race ->
    def results = (0..<(race.time)).collect { i ->
        return [i, race.play(i)]
    }.findAll { it[1] > race.distance }

    return results.size();
}.inject(1) { acc, it -> acc * it };


def finalRace = new Race(
    input[0].split(/:\W+/)[1].split(/\W+/).join("").toLong(),
    input[1].split(/:\W+/)[1].split(/\W+/).join("").toLong()
);

def count = 0;
def prev = null;
for (int i = 0; i < finalRace.time; i++) {
    def curr = finalRace.play(i);

    if (curr > finalRace.distance) {
        count += 1;
    }

    if (prev != null && curr < finalRace.distance && i > finalRace.time / 2) {
        break;
    }

    prev = curr
}

println(count);