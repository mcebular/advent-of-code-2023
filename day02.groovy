def input = new File("input/day02.txt").readLines();

def parseCubeSet(input) {
    return input.trim().split(", ").collectEntries { cubes ->
        def cube = cubes.split(" ");
        return [cube[1], cube[0].toInteger()];
    }
}

def cubeBag = [
    "red": 12,
    "green": 13,
    "blue": 14,
];

def cubeSetFits(input, bag) {
    for (entry in input) {
        if (entry.value > bag[entry.key]) {
            return false;
        }
    }

    return true;
}

def minRequiredCubes(game) {
    def cubeBag = [
        "red": 0,
        "green": 0,
        "blue": 0,
    ];

    game.each{ cubeSet ->
        for (entry in cubeSet) {
            if (cubeBag[entry.key] < entry.value) {
                cubeBag[entry.key] = entry.value;
            }
        }
    }

    return cubeBag;
}

def games = input.collectEntries { line ->
    def matcher = line =~ /^Game ([0-9]+): (.*)$/
    def gameId = matcher[0][1];
    def cubeSets = matcher[0][2].split(";").collect { parseCubeSet(it) };
    
    return [gameId, cubeSets];
};

def day1 = 0;
games.each { key, value -> 
    def validGame = value.every { cubeSetFits(it, cubeBag) };
    if (validGame) {
        day1 += key.toInteger();
    }
}
println(day1);

def day2 = 0;
games.each { key, value ->
    def gameBag = minRequiredCubes(value);
    def cubePower = gameBag.inject(1) { acc, it -> acc * it.value };
    day2 += cubePower;
}
println(day2);
