package util;

import java.util.Arrays;
import java.util.StringJoiner;
import trainer.*;

public class HandUtility {
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
                if (numberHand.getQuality().ordinal() > HandInfo.HandQuality.STRAIGHT.ordinal()) {
                    return numberHand;
                } else {
                    return new HandStrength(HandInfo.HandQuality.STRAIGHT, new int[]{straight});
                }
            } else {
                return getBestNumberHand(numbers);
            }
        } else {

            if (straight > 0) {
                int sf = getBestStraightFlush(cards, flush);
                if (sf > 0) {
                    return new HandStrength(HandInfo.HandQuality.STRAIGHT_FLUSH, new int[]{sf});
                }
            }

            // Not possible to have a better number hand.
            return new HandStrength(HandInfo.HandQuality.FLUSH, getFlushHand(cards, flush)); // quads??
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
            boolean akqj = false;
            for (int i = 12; i >= 9; --i) {
                if (freqs[i] > 0) {
                    akqj = true;
                }
            }
            if (akqj) {
                return new HandStrength(HandInfo.HandQuality.HIGH_AKQJ, collectForHand(new int[]{0, 5, 0, 0, 0}, freqs));
            } else {
                return new HandStrength(HandInfo.HandQuality.HIGH_NUM, collectForHand(new int[]{0, 5, 0, 0, 0}, freqs));
            }
        }

        int[] freqCounts = new int[5];
        for (int freq : freqs) {
            freqCounts[freq]++;
        }

        if (max == 2) {
            if (freqCounts[2] == 1) {
                boolean akqj = false;
                for (int i = 12; i >= 9; --i) {
                    if (freqs[i] > 1) {
                        akqj = true;
                    }
                }
                if (akqj) {
                    return new HandStrength(HandInfo.HandQuality.PAIR_AKQJ, collectForHand(new int[]{0, 3, 1, 0, 0}, freqs));
                } else {
                    return new HandStrength(HandInfo.HandQuality.PAIR_NUM, collectForHand(new int[]{0, 3, 1, 0, 0}, freqs));
                }
            }

            return new HandStrength(HandInfo.HandQuality.TWO_PAIR, collectForHand(new int[]{0, 1, 2, 0, 0}, freqs));
        }

        if (max == 3) {
            if (freqCounts[2] > 0 || freqCounts[3] > 1) {
                return new HandStrength(HandInfo.HandQuality.FULL_HOUSE, collectForHand(new int[]{0, 0, 1, 1, 0}, freqs));
            }
            return new HandStrength(HandInfo.HandQuality.TRIPS, collectForHand(new int[]{0, 2, 0, 1, 0}, freqs));
        }

        return new HandStrength(HandInfo.HandQuality.QUADS, collectForHand(new int[]{0, 1, 0, 0, 1}, freqs));
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

    public static HandInfo.HandQuality getQuality(int[] cards) {
        try {
            if (cards.length < 5) {
                if (cards[0] == cards[1]) {
                    if (Card.getNumber(cards[0]) < 9) {
                        return HandInfo.HandQuality.PAIR_NUM;
                    } else {
                        return HandInfo.HandQuality.PAIR_AKQJ;
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
                    HandInfo.HandQuality numberHand = getBestNumberHandQuality(numbers, cards);

                    return HandInfo.HandQuality.values()[Math.max(numberHand.ordinal(), HandInfo.HandQuality.STRAIGHT.ordinal())];
                } else {
                    return getBestNumberHandQuality(numbers, cards);
                }
            } else {
                if (straight && hasStraightFlush(cards, flush)) {
                    return HandInfo.HandQuality.STRAIGHT_FLUSH;
                }

                return HandInfo.HandQuality.FLUSH;
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

    private static HandInfo.HandQuality getBestHigh(int[] cards) {
        int num = -1;
        for (int c : cards) {
            if (num < Card.getNumber(c)) {
                num = Card.getNumber(c);
            }
        }
        if (num < 9) {
            return HandInfo.HandQuality.HIGH_NUM;
        } else {
            return HandInfo.HandQuality.HIGH_AKQJ;
        }
    }

    private static HandInfo.HandQuality getBestNumberHandQuality(int[] freqs, int[] cards) {
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
                int bestPair = -1;
                for (int i = freqs.length - 1; i >= 0; --i) {
                    if (freqs[i] > 1) {
                        bestPair = i;
                        break;
                    }
                }
                if (bestPair < 9) {
                    return HandInfo.HandQuality.PAIR_NUM;
                } else {
                    return HandInfo.HandQuality.PAIR_AKQJ;
                }
            }
            return HandInfo.HandQuality.TWO_PAIR;
        }

        if (max == 3) {
            if (freqCounts[2] > 0 || freqCounts[3] > 1) {
                return HandInfo.HandQuality.FULL_HOUSE;
            }
            return HandInfo.HandQuality.TRIPS;
        }

        return HandInfo.HandQuality.QUADS;
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

    public static String[][][] getHandsByQuality() {
        return new String[][][]{
                {
                        // High card
                        {"2d 3c 4c 7c 8c 9s Th"},
                        {"2d 3c 5c 7c 8c 9s Th"},
                        {"2d 6s 7s 8s 9s Jc Kh"},
                        {"2d 3c 4c 6c 7c 8s Ah"},
                        {"2h 3c 4c 6c 7c 8s Ad"},
                        {"2h 3c 4c 6c 7c 9s Ad"},
                },
                {
                        // Pair
                        {"2d 2c 4c 6c 7c 9s Ah"},
                        {"3d 3c 2d 4c 6c 7c 9h"},
                        {"8d 8c 2d 4c 5c 7c 9h"},
                        {"8d 8c 2d 4c 6c 7c 9h"},
                        {"Ac Ah 3d 4d 5c 6c 9c"},
                },
                {
                        // Two pair
                        {"2d 2c 3d 3c 7d 9d Kh"},
                        {"2d 2c 3d 3c 7d 9d Ah"},
                        {"3d 3c 4d 4c 7d 9d Ah"},
                        {"2d 2c Ad Ac 3d 4d 6h"},
                        {"3d 3c Ad Ac 2d 2c 4d"},
                        {"3d 3c Ad Ac 2d 2c 5d"},
                },

                {
                        // Trips
                        {"2d 2c 2h 3c 4c 5c 7d"},
                        {"2d 2c 2h 3c 4c 5c 8d"},
                        {"3d 3c 3h 2c 4c 5c 7d"},
                        {"6c 6d 6h 2c 3c 4c 7d"},
                        {"6c 6d 6s 2c 3c 4c 7d"},
                        {"Ac Ad Ah 3c 4c 5c 6d"},
                },
                {
                        // Straight
                        {"Ac 2d 3h 4c 5c 7c Kd"},
                        {"2c 3d 4h 5c 6c 8c Ad"},
                        {"4h 5c 6c 7c 8d 2c 3d"},
                        {"4h 5c 6c 7c 8d Ac 3d"},
                        {"Tc Jd Qh Kc Ac 2c 3d"},
                },
                {
                        // Flush
                        {"2d 3d 4d 7d 8d 9s Th"},
                        {"2d 3d 5d 7d 8d 9s Th"},
                        {"2c 4c 6c 7c 9c 2d Ah"},
                        {"2d 3c 4c 6c 7c 8s Ac"},
                        {"2h 3c 4c 6c 7c 9s Ac"},
                        {"2d 3h 4h 6h 7h 8s Ah"},
                        {"2d 3c 4c 6c 7c 8c Ac"},
                },
                {
                        // Full house
                        {"2d 2c 2h 3s 3c 5h 6d"},
                        {"2d 2c 2h 4s 4c 5h 6d"},
                        {"3d 3c 3h 2s 2c 5h 6d"},
                        {"3d 3c 3h 4s 4c 2h 2d"},
                        {"Ad Ac Ah 2s 2c 5h 6d"},
                        {"Ad Ac Ah Ks Kc 2h 2d"},
                },
                {
                        // Quads
                        {"2d 2c 2h 2s 7c 7h 7d"},
                        {"2d 2c 2h 2s 4c 5c 7d"},
                        {"3d 3c 3h 3s 4c 5c 7d"},
                        {"4h 4s 4d 4c 5d 8d 9d"},
                        {"4h 4s 4d 4c 2h 8d Td"},
                        {"5d 5c 5h 5s 2c 3c 3d"},
                        {"5d 5c 5h 5s 2c 3c 4d"},
                        {"Ad Ac Ah As 2c 3c Kd"},
                },
                {
                        // Straight flush
                        {"Ac 2c 3c 4c 5c 6d 7d"},
                        {"Ac 2c 3c 4c 5c 7d 8d"},
                        {"Ac 2c 3c 4c 5c 6c 7d"},
                        {"Ac 2c 3c 4c 5c 6c 7c"},
                        {"Tc Jc Qc Kc Ac 2c 3c"},
                        {"Td Jd Qd Kd Ad 2d 3d"},
                },
        };
    }

    public static int[] getHand(String input) {
        String[] split = input.split(" ");
        int[] hand = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            hand[i] = new Card(s).getId();
        }

        return hand;
    }

    public static void main(String[] args) {
        String[][][] handsByQuality = getHandsByQuality();
        for (String[][] strings : handsByQuality) {
            for (int j = 0; j < strings.length / 2; ++j) {
                int[] hand1 = getHand(strings[j][0]);
                int[] hand2 = getHand(strings[j + 1][0]);
                System.out.println(strings[j][0] + " " + getStrength(hand1));
                System.out.println(strings[j + 1][0] + " " + getStrength(hand2));
                System.out.println(compareTo(hand1, hand2));
            }
        }
    }
}
