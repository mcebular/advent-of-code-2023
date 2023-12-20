import groovy.time.TimeCategory; 
import groovy.time.TimeDuration;
import groovy.transform.EqualsAndHashCode;
import groovy.transform.ToString;
import groovy.transform.TupleConstructor;


enum Signal { low, high }

@EqualsAndHashCode
@ToString
@TupleConstructor
class Module {
    String name;
    String type;
    List<String> outputs;

    boolean ffState = false;
    Map<String, Signal> cjState = [:];
}

@EqualsAndHashCode
@ToString
@TupleConstructor
class Pulse {
    String from;
    Signal signal;
    String to;

    String toString() {
        return "$from -$signal-> $to";
    }
}

def input = new File("input/day20.txt").readLines();
def modules = input.collect {
    def matcher = it =~ /^([%&]?)([a-z]+) -> ([a-z, ]+)$/
    def type = matcher[0][1];
    def name = matcher[0][2];
    def outputs = matcher[0][3].split(", ").collect();
    
    return new Module(name, type, outputs);
}.collectEntries { [it.name, it] }

// Init cjState
for (def module in modules.values()) {
    if (module.type != '&') {
        continue;
    }

    for (def module2 in modules.values()) {
        if (module2.outputs.contains(module.name)) {
            module.cjState[module2.name] = Signal.low;
        }
    }
}

modules['button'] = new Module('button', '', ['broadcaster']);
// println(modules);

def pressButton(def modules) {
    def pulseCount = [0, 0];
    def pulses = [];
    pulses.add(new Pulse("button", Signal.low, "broadcaster"));

    while (pulses.size() > 0) {
        def curr = pulses.pop();
        def from = modules[curr.from];
        def to = modules[curr.to];
        def signal = curr.signal;
        
        signal == Signal.low ? pulseCount[0]++ : pulseCount[1]++;
        // println(curr);

        // if (signal == Signal.high && curr.to == 'mg') {
        //     ...
        // }

        if (to == null) {
            continue;
        }

        if (to.type == '') {
            for (def output in to.outputs) {
                pulses << new Pulse(to.name, signal, output);
            }
        } else if (to.type == '%') {
            if (signal == Signal.high) {
                // Do nothing.
            } else if (signal == Signal.low) {
                // Flip flip-flop state
                to.ffState = !to.ffState;
                for (def output in to.outputs) {
                    pulses << new Pulse(to.name, to.ffState ? Signal.high : Signal.low, output);
                }
            } else {
                assert false : "Invalid signal";
            }
        } else if (to.type == '&') {
            // Store received state.
            if (!(from.name in to.cjState)) {
                assert false : "Invalid initial state";
            }
            to.cjState[from.name] = signal;
            // If all inputs are high, send low, otherwise high.
            def lowSignalCount = to.cjState.values().findAll { it == Signal.low };
            def signalToSend = lowSignalCount.size() > 0 ? Signal.high : Signal.low;
            for (def output in to.outputs) {
                pulses << new Pulse(to.name, signalToSend, output);
            }
        } else {
            assert false : "Invalid type: '$to.type'";
        }
    }

    return pulseCount;
}

def count = [0, 0];
for (int i = 0; i < 1000; i++) {
    def t = pressButton(modules);
    count[0] += t[0];
    count[1] += t[1];
}
println(count[0] * count[1]);

/*
Part 2 done by hand :)

First, I drew the graph with https://play.d2lang.com.

Looking at the graph, I can see immediately see four distinct sections:
(1) starts with gb, ends with hf
(2) starts with ht, ends with rh
(3) starts with zz, ends with jg
(4) starts with vk, ends with jm

Endings of the four sections then connect to mg, which then connects to rx.
mg is a conjunction node, so we need all inputs to mg high to emit a low to rx.

Running the cycles on each of the four sub-graphs and checking when they output
high to mg nets the following results:

Section (1) outputs high every 3947 cycles (3947, 7894, 11841, 15788, ...)
Section (2) outputs high every 4019 cycles (4019, 8038, 12057, 16076, ...)
Section (3) outputs high every 3793 cycles (3793, 7586, 11379, 15172, ...)
Section (4) outputs high every 4003 cycles (4003, 8006, 12009, 16012, ...)

Cycle at which mr will receive all high inputs is the least common multiplier 
(LCM) of the above four repeats:

LCM(3947, 4019, 3793, 4003) = 240853834793347
*/
println("240853834793347");
