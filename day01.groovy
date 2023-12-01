def input = new File("input/day01.txt").readLines();

def numberMapping = [
    "one": "1", 
    "two": "2",
    "three": "3",
    "four": "4",
    "five": "5",
    "six": "6",
    "seven": "7",
    "eight": "8",
    "nine": "9",
];

def numbers = input.collect { value ->
    def digits1 = [];
    def digits2 = [];
    for (def i = 0; i < value.length(); i++) {
        def rem = value[i..-1];
        if (rem[0] =~ /[0-9]/) {
            digits1 << rem[0];
        }
        rem = rem.replace(numberMapping);
        if (rem[0] =~ /[0-9]/) {
            digits2 << rem[0];
        }
    }
    return [
        (digits1[0] + "" + digits1[-1]).toInteger(),
        (digits2[0] + "" + digits2[-1]).toInteger(),
    ];
};

numbers.inject([0, 0]) { result, i -> [result[0] + i[0], result[1] + i[1]] }
    .each { n -> println n }; 
