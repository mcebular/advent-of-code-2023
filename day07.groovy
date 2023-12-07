import groovy.transform.TupleConstructor;
import groovy.transform.ToString;


enum HandType {
    FiveOfAKind,
    FourOfAKind,
    FullHouse,
    ThreeOfAKind,
    TwoPair,
    OnePair,
    HighCard;
}


@ToString
class Hand {
    int[] cards;
    long value;
    HandType type;

    Hand(String hand, long value, boolean jokerMode = false) {
        this.cards = hand.getChars().collect {
            switch(it) {
                case '0'..'9': return "$it".toInteger();
                case 'T': return 10; 
                case 'J': return jokerMode ? 0 : 11; 
                case 'Q': return 12; 
                case 'K': return 13; 
                case 'A': return 14; 
                default:
                    assert false : "Hand parsing error"
            }
        }
        this.value = value
        this.type = getType();
    }

    private HandType getType() {
        def cardCounts = cards.inject([:]) { acc, it ->
            if (acc[it] != null) {
                acc[it] += 1;
            } else {
                acc[it] = 1;
            }
            return acc;
        };

        def jokerCount = cardCounts[0];
        if (jokerCount == 5) {
            return HandType.FiveOfAKind;
        }
        cardCounts.remove(0);
        cardCounts = cardCounts.values().sort();
        cardCounts[-1] = cardCounts[-1] + (jokerCount ?: 0);

        if (cardCounts[0] == 5) {
            return HandType.FiveOfAKind;
        } else if (cardCounts[0] == 1 && cardCounts[1] == 4) {
            return HandType.FourOfAKind;
        } else if (cardCounts[0] == 2 && cardCounts[1] == 3) {
            return HandType.FullHouse;
        } else if (cardCounts[0] == 1 && cardCounts[1] == 1 && cardCounts[2] == 3) {
            return HandType.ThreeOfAKind;
        } else if (cardCounts[0] == 1 && cardCounts[1] == 2 && cardCounts[2] == 2) {
            return HandType.TwoPair;
        } else if (cardCounts[-1] == 2) {
            return HandType.OnePair;
        } else if (cardCounts.findAll { it == 1 }.size() == 5) {
            return HandType.HighCard;
        } else {
            assert false : "Unknown hand type";
        }
    }

    int compareTo(Hand other) {
        int handTypeCompare = this.type <=> other.type;
        if (handTypeCompare != 0) {
            return -handTypeCompare;
        }

        for (int i = 0; i < 5; i++) {
            int cardCompare = this.cards[i] <=> other.cards[i]
            if (cardCompare != 0) {
                return cardCompare;
            } 
        }
    }
}

{ // Test: Hand.type
    assert new Hand("AAAAA", 0L).type == HandType.FiveOfAKind;
    assert new Hand("AA3AA", 0L).type == HandType.FourOfAKind;
    assert new Hand("AA33A", 0L).type == HandType.FullHouse;
    assert new Hand("AA3TA", 0L).type == HandType.ThreeOfAKind;
    assert new Hand("AATT2", 0L).type == HandType.TwoPair;
    assert new Hand("AAT32", 0L).type == HandType.OnePair;
    assert new Hand("34567", 0L).type == HandType.HighCard;
}

{ // Test: Hand.compareTo
    assert new Hand("AAAAA", 0L).compareTo(new Hand("AAAA2", 0L)) == 1;
    assert new Hand("AAA22", 0L).compareTo(new Hand("AAAA2", 0L)) == -1;
    assert new Hand("44444", 0L).compareTo(new Hand("33333", 0L)) == 1;
}

def input = new File("input/day07.txt").readLines()

def totalWinnings = input.collect { line -> 
    def parts = line.split(" ");
    return new Hand(parts[0], parts[1].toLong());
}.sort { h1, h2 -> 
    h1.compareTo(h2) 
}.withIndex().collect { hand, index ->
    [index + 1, hand]
}.inject(0) { acc, it ->
    acc += it[0] * it[1].value
}
println totalWinnings;

def totalWinnings2 = input.collect { line -> 
    def parts = line.split(" ");
    return new Hand(parts[0], parts[1].toLong(), true);
}.sort { h1, h2 -> 
    h1.compareTo(h2) 
}.withIndex().collect { hand, index ->
    [index + 1, hand]
}.inject(0) { acc, it ->
    acc += it[0] * it[1].value
}
println totalWinnings2;
