package util;

import java.util.Arrays;
import java.util.Random;
import java.util.StringJoiner;

public class HandUtil {
    // cards length must be 7.
    public static int compareTo(int[] hand0, int[] hand1) {
        return getStrength(hand0).compareTo(getStrength(hand1));
    }

    // cards length must be 7.
    private static HandStrength getStrength(int[] cards) {
        int[] suits = suitFrequencies(cards);
        int[] numbers = numberFrequencies(cards);
        int flush = getFlushSuit(suits);
        int straight = getBestStraight(numbers);
        if (flush < 0) {
            if (straight > 0) {
                HandStrength numberHand = getBestNumberHand(numbers);
                if (numberHand.getQuality().ordinal() > HandQuality.STRAIGHT.ordinal()) {
                    return numberHand;
                } else {
                    return new HandStrength(HandQuality.STRAIGHT, new int[]{straight});
                }
            } else {
                return getBestNumberHand(numbers);
            }
        } else {

            if (straight > 0) {
                int sf = getBestStraightFlush(cards, flush);
                if (sf > 0) {
                    return new HandStrength(HandQuality.STRAIGHT_FLUSH, new int[]{sf});
                }
            }

            // Not possible to have a better number hand.
            return new HandStrength(HandQuality.FLUSH, getFlushHand(cards, flush)); // quads??
        }
    }

    private static int getBestStraight(int[] freqs) {
        int length = 0;
        int max = 0;
        for (int i = freqs.length - 1; i >= 0; i--) {
            int freq = freqs[i];
            if (freq == 0) {
                length = 0;
                continue;
            }

            if (length == 0) {
                max = i;
            }

            length++;
            if (length >= 5) {
                return max;
            }
        }

        // Check for low straights
        if (max == 3 && length == 4 && freqs[freqs.length - 1] > 0) {
            return 3;
        }

        // No straight
        return 0;
    }

    private static int[] collectForHand(int[] counts, int[] freqs) {
        int[] slots = new int[counts.length];
        int slot = 0;
        int sum = 0;
        for (int i = counts.length - 1; i >= 0; i--) {
            slots[i] = slot;
            slot += counts[i];
            sum += counts[i];
        }

        int[] hand = new int[sum];
        for (int i = freqs.length - 1; i >= 0; i--) {
            int freq = freqs[i];
            for (int j = freq; j >= 0; j--) {
                if (counts[j] > 0) {
                    counts[j]--;
                    hand[slots[j]++] = i;
                    break;
                }
            }
        }

        return hand;
    }

    private static HandStrength getBestNumberHand(int[] freqs) {
        int max = 0;
        for (int freq : freqs) {
            if (freq > max) {
                max = freq;
            }
        }

        if (max == 1) {
            return new HandStrength(HandQuality.HIGH, collectForHand(new int[]{0, 5, 0, 0, 0}, freqs));
        }

        int[] freqCounts = new int[5];
        for (int freq : freqs) {
            freqCounts[freq]++;
        }

        if (max == 2) {
            if (freqCounts[2] == 1) {
                return new HandStrength(HandQuality.PAIR, collectForHand(new int[]{0, 3, 1, 0, 0}, freqs));
            }

            return new HandStrength(HandQuality.TWO_PAIR, collectForHand(new int[]{0, 1, 2, 0, 0}, freqs));
        }

        if (max == 3) {
            if (freqCounts[2] > 0 || freqCounts[3] > 1) {
                return new HandStrength(HandQuality.FULL_HOUSE, collectForHand(new int[]{0, 0, 1, 1, 0}, freqs));
            }
            return new HandStrength(HandQuality.TRIPS, collectForHand(new int[]{0, 2, 0, 1, 0}, freqs));
        }

        return new HandStrength(HandQuality.QUADS, collectForHand(new int[]{0, 1, 0, 0, 1}, freqs));
    }

    private static int[] getFlushHand(int[] cards, int flush) {
        Arrays.sort(cards);
        int[] hand = new int[5];
        int count = 0;
        for (int i = cards.length - 1; i >= 0; i--) {
            if (Card.getSuit(cards[i]) == flush) {
                hand[count++] = Card.getNumber(cards[i]);
                if (count == hand.length) {
                    break;
                }
            }
        }

        return  hand;
    }

    private static int getBestStraightFlush(int[] cards, int flush) {
        return getBestStraight(getFlushNumberFreqs(cards, flush));
    }

    public static HandQuality getQuality(int[] cards) {
        try {
            if (cards.length < 5) {
                if (cards[0] == cards[1]) {
                    if (Card.getNumber(cards[0]) < 9) {
                        return HandQuality.PAIR_NUM;
                    } else {
                        return HandQuality.PAIR_AKQJ;
                    }
                } else {
                    return getBestHigh(cards);
                }
            }

            int[] suits = suitFrequencies(cards);
            int[] numbers = numberFrequencies(cards);
            int flush = getFlushSuit(suits);
            boolean straight = hasStraight(numbers);
            if (flush < 0) {
                if (straight) {
                    HandQuality numberHand = getBestNumberHandQuality(numbers, cards);

                    return HandQuality.values()[Math.max(numberHand.ordinal(), HandQuality.STRAIGHT.ordinal())];
                } else {
                    return getBestNumberHandQuality(numbers, cards);
                }
            } else {
                if (straight && hasStraightFlush(cards, flush)) {
                    return HandQuality.STRAIGHT_FLUSH;
                }

                return HandQuality.FLUSH;
            }
        } catch(Exception e) {
            Debug.println(Arrays.toString(cards));
            throw e;
        }
    }

    private static int[] numberFrequencies(int[] cards) {
        int[] freq = new int[13];
        for (int card : cards) {
            freq[Card.getNumber(card)]++;
        }
        return freq;
    }

    private static HandQuality getBestHigh(int[] cards) {
        int num = Math.max(Card.getNumber(cards[0]), Card.getNumber(cards[1]));
        if (num < 9) {
            return HandQuality.HIGH_NUM;
        } else if (num < 11) {
            return HandQuality.HIGH_QJ;
        } else {
            return HandQuality.HIGH_AK;
        }
    }

    private static HandQuality getBestNumberHandQuality(int[] freqs, int[] cards) {
        int max = 0;
        for (int freq : freqs) {
            if (freq > max) {
                max = freq;
            }
        }

        if (max == 1) {
            return getBestHigh(cards);
        }

        int[] freqCounts = new int[5];
        for (int freq : freqs) {
            freqCounts[freq]++;
        }

        if (max == 2) {
            if (freqCounts[2] == 1) {
                int pair0 = freqs[Card.getNumber(cards[0])] > 1 ? Card.getNumber(cards[0]) : -1;
                int pair1 = freqs[Card.getNumber(cards[1])] > 1 ? Card.getNumber(cards[1]) : -1;
                int bestPair = Math.max(pair0, pair1);
                if (bestPair > 0) {
                    if (bestPair < 9) {
                        return HandQuality.PAIR_NUM;
                    } else {
                        return HandQuality.PAIR_AKQJ;
                    }
                } else {
                    return getBestHigh(cards);
                }
            }
            return HandQuality.TWO_PAIR;
        }

        if (max == 3) {
            if (freqCounts[2] > 0 || freqCounts[3] > 1) {
                return HandQuality.FULL_HOUSE;
            }
            return HandQuality.TRIPS;
        }

        return HandQuality.QUADS;
    }

    private static int[] suitFrequencies(int[] cards) {
        int[] freq = new int[4];
        for (int card : cards) {
            freq[Card.getSuit(card)]++;
        }

        return freq;
    }

    private static int getFlushSuit(int[] freqs) {
        for (int i = 0; i < freqs.length; i++) {
            if (freqs[i] >= 5) {
                return i;
            }
        }

        return -1;
    }

    private static boolean hasStraight(int[] freqs) {
        return getBestStraight(freqs) > 0;
    }

    private static boolean hasStraightFlush(int[] cards, int flush) {
        return hasStraight(getFlushNumberFreqs(cards, flush));
    }

    private static int[] getFlushNumberFreqs(int[] cards, int flush) {
        int[] freqs = new int[13];
        for (int card : cards) {
            if (Card.getSuit(card) == flush) {
                freqs[Card.getNumber(card)]++;
            }
        }

        return freqs;
    }

    public static String toString(int[] cards) {
        StringJoiner hand = new StringJoiner(" ");
        for (int card : cards) {
            hand.add(new Card(card).toString());
        }

        return hand.toString();
    }

    public static void main(String[] args) {
        // Run a benchmark.
        Random random = new Random(1234);
        int n = 10_000_000;
        int[][][] hands = new int[n][2][7];
        int[] deck = new int[52];
        for (int i = 0; i < deck.length; i++) {
            deck[i] = i;
        }

        // Generate hold'em hands.
        for (int i = 0; i < hands.length; i++) {
            // Swap 9 cards into first 9 positions.
            for (int j = 0; j < 9; j++) {
                int pick = random.nextInt(deck.length - j);
                int tmp = deck[j];
                deck[j] = deck[j + pick];
                deck[j + pick] = tmp;
            }

            // Set the common cards.
            for (int j = 0; j < 5; j++) {
                hands[i][0][j] = deck[j];
                hands[i][1][j] = deck[j];
            }

            // Set each person's cards.
            for (int j = 0; j < 2; j++) {
                hands[i][0][5 + j] = deck[5 + j];
                hands[i][1][5 + j] = deck[7 + j];
            }
        }
        System.out.println("Finished generating hands.");

        // Try comparisons
        long startTime = System.currentTimeMillis();
        int wins = 0;
        for (int[][] hand : hands) {
            int cmp = compareTo(hand[0], hand[1]);
            wins += cmp > 0 ? 1 : 0;
        }

        System.out.println("Wins: " + wins);
        long runTime = System.currentTimeMillis() - startTime;
        System.out.println(runTime + " ms");
    }
}
