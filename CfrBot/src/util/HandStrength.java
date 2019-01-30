package util;

import trainer.HandInfo;

import java.util.Arrays;
import java.util.Objects;

public class HandStrength implements Comparable<HandStrength>{
    private HandInfo.HandQuality quality;
    private int[] tieBreak;

    public HandInfo.HandQuality getQuality() {
        return quality;
    }

    public int[] getTieBreak() {
        return tieBreak;
    }

    public HandStrength(HandInfo.HandQuality quality, int[] tieBreak) {
        this.quality = quality;
        this.tieBreak = tieBreak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandStrength that = (HandStrength) o;
        return quality == that.quality &&
                Arrays.equals(tieBreak, that.tieBreak);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(quality);
        result = 31 * result + Arrays.hashCode(tieBreak);
        return result;
    }


    @Override
    public int compareTo(HandStrength o) {
        if (quality.ordinal() != o.quality.ordinal()) {
            return quality.ordinal() - o.quality.ordinal();
        }

        // Arrays should be same size.
        for (int i = 0; i < tieBreak.length; i++) {
            if (tieBreak[i] != o.tieBreak[i]) {
                return tieBreak[i] - o.tieBreak[i];
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "util.util.HandStrength{" +
                "quality=" + quality +
                ", tieBreak=" + Arrays.toString(tieBreak) +
                '}';
    }
}
