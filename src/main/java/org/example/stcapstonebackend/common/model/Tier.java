package org.example.stcapstonebackend.common.model;

public enum Tier {
    UNRANKED(-1),
    IRON(0),
    BRONZE(400),
    SILVER(800),
    GOLD(1200),
    PLATINUM(1600),
    EMERALD(2000),
    DIAMOND(2400),
    MASTER(2800),
    GRANDMASTER(2800),
    CHALLENGER(2800);

    private final int baseScore;

    Tier(int baseScore) {
        this.baseScore = baseScore;
    }

    public int getBaseScore() {
        return baseScore;
    }

    public static Tier fromString(String tierName) {
        if (tierName == null || tierName.trim().isEmpty() || tierName.equalsIgnoreCase("UNRANKED")) {
            return UNRANKED;
        }
        try {
            return Tier.valueOf(tierName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNRANKED;
        }
    }

    public boolean isMasterOrAbove() {
        return this == MASTER || this == GRANDMASTER || this == CHALLENGER;
    }
}

