package trainer;

import util.HandQuality;
import util.HandUtil;

import java.util.HashSet;
import java.util.Objects;

public class HandInfo {
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
            case "HIGH":
                this.quality = HandQuality.HIGH;
                break;
            case "PAIR":
                this.quality = HandQuality.PAIR;
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
        return new HandInfo(HandUtil.getQuality(cards));
    }

    public HandInfo lowerQuality() {
        if (quality.ordinal() >= 1) {
            return new HandInfo(HandQuality.values()[quality.ordinal() - 1]);
        }
        return null;
    }
}
