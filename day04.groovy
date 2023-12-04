def input = new File("input/day04.txt").readLines();

class Scratchcard {
    int id;
    int[] myNumbers;
    int[] winningNumbers;

    Scratchcard(String input) {
        def matcher = input =~ /^Card {1,3}([0-9]+): (.*)$/;
        id = matcher[0][1].toInteger();
        def numbers = matcher[0][2].split(/ \| /);
        winningNumbers = numbers[0].trim().split(/ +/).collect { it.toInteger() };
        myNumbers = numbers[1].trim().split(/ +/).collect { it.toInteger() };
    }

    int[] getMatchingNumbers() {
        return myNumbers.findAll {
            winningNumbers.contains(it);
        };
    }

    String toString() {
        return "Card $id: $winningNumbers | $myNumbers";
    }
}

def scratchcards = input.collect { new Scratchcard(it) };

int totalCardsWorth = scratchcards.inject(0) { acc, card -> 
    acc + Math.floor(Math.pow(2, card.getMatchingNumbers().size() - 1));
};
println(totalCardsWorth);

def cardCounts = scratchcards.collectEntries { [it.id, 1] };
scratchcards.each { card ->
    for (i in 0<..card.getMatchingNumbers().size()) {
        cardCounts[card.id + i] = cardCounts[card.id + i] + cardCounts[card.id];
    }
}
println(cardCounts.inject(0) { acc, it -> acc + it.value });
