package trainer;

import util.HandUtility;
import java.util.Objects;

public class HandInfo {
    public enum HandQuality {
        HIGH_NUM, HIGH_AKQJ, PAIR_NUM, PAIR_AKQJ, TWO_PAIR, TRIPS, STRAIGHT, FLUSH, FULL_HOUSE, QUADS, STRAIGHT_FLUSH
        // No one cares about royals.
    }
    private HandQuality quality;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HandInfo handInfo = (HandInfo) o;
        return quality == handInfo.quality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quality);
    }

    @Override
    public String toString() {
        return "trainer.HandInfo{" +
                "quality=" + quality +
                '}';
    }

    public HandInfo(HandQuality quality) {
        this.quality = quality;
    }

    public HandInfo(String quality) {
        switch (quality) {
            case "HIGH_NUM":
                this.quality = HandQuality.HIGH_NUM;
                break;
            case "HIGH_AKQJ":
                this.quality = HandQuality.HIGH_AKQJ;
                break;
            case "PAIR_NUM":
                this.quality = HandQuality.PAIR_NUM;
                break;
            case "PAIR_AKQJ":
                this.quality = HandQuality.PAIR_AKQJ;
                break;
            case "TWO_PAIR":
                this.quality = HandQuality.TWO_PAIR;
                break;
            case "TRIPS":
                this.quality = HandQuality.TRIPS;
                break;
            case "STRAIGHT":
                this.quality = HandQuality.STRAIGHT;
                break;
            case "FLUSH":
                this.quality = HandQuality.FLUSH;
                break;
            case "FULL_HOUSE":
                this.quality = HandQuality.FULL_HOUSE;
                break;
            case "QUADS":
                this.quality = HandQuality.QUADS;
                break;
            case "STRAIGHT_FLUSH":
                this.quality = HandQuality.STRAIGHT_FLUSH;
                break;
        }
    }

    public static HandInfo getHandInfo(int[] cards) {
        return new HandInfo(HandUtility.getQuality(cards));
    }
}
