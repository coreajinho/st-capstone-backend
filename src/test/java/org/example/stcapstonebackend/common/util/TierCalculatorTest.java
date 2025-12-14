package org.example.stcapstonebackend.common.util;

import org.example.stcapstonebackend.common.model.Division;
import org.example.stcapstonebackend.common.model.Tier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TierCalculatorTest {

    @Test
    @DisplayName("Iron 4 0LP should be 0 points")
    void testIron4ZeroLP() {
        int score = TierCalculator.calculateScore(Tier.IRON, Division.IV, 0);
        assertEquals(0, score);
    }

    @Test
    @DisplayName("Gold 2 26LP should be 1426 points")
    void testGold2_26LP() {
        int score = TierCalculator.calculateScore(Tier.GOLD, Division.II, 26);
        assertEquals(1426, score);
    }

    @Test
    @DisplayName("Diamond 1 99LP should be 2799 points")
    void testDiamond1_99LP() {
        int score = TierCalculator.calculateScore(Tier.DIAMOND, Division.I, 99);
        assertEquals(2799, score);
    }

    @Test
    @DisplayName("Master 500LP should be 3300 points")
    void testMaster500LP() {
        int score = TierCalculator.calculateScore(Tier.MASTER, Division.NONE, 500);
        assertEquals(3300, score);
    }

    @Test
    @DisplayName("Score 1426 should convert to Gold 2 26LP")
    void testScoreToTier() {
        TierCalculator.TierInfo tierInfo = TierCalculator.scoreToTier(1426);
        assertEquals(Tier.GOLD, tierInfo.tier());
        assertEquals(Division.II, tierInfo.division());
        assertEquals(26, tierInfo.lp());
    }

    @Test
    @DisplayName("Solo rank duo range for Platinum player should be within ±400 points")
    void testSoloRankDuoRange() {
        // Platinum 3 50LP = 1600 + 100 + 50 = 1750
        int platinumScore = TierCalculator.calculateScore(Tier.PLATINUM, Division.III, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(platinumScore);

        // Platinum: Gold 4 ~ Emerald 1 (1200~2399)
        assertEquals(1200, range[0]); // Gold 4 0LP
        assertEquals(2399, range[1]); // Emerald 1 99LP
    }

    @Test
    @DisplayName("Gold player: Silver 4 ~ Platinum 1")
    void testGoldDuoRange() {
        // Gold 2 50LP = 1200 + 200 + 50 = 1450
        int goldScore = TierCalculator.calculateScore(Tier.GOLD, Division.II, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(goldScore);

        // Gold: Silver 4 ~ Platinum 1 (800~1999)
        assertEquals(800, range[0]); // Silver 4 0LP
        assertEquals(1999, range[1]); // Platinum 1 99LP
    }

    @Test
    @DisplayName("Silver player: Iron 4 ~ Gold 1")
    void testSilverDuoRange() {
        // Silver 2 50LP = 800 + 200 + 50 = 1050
        int silverScore = TierCalculator.calculateScore(Tier.SILVER, Division.II, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(silverScore);

        // Silver: Iron 4 ~ Gold 1 (0~1599)
        assertEquals(0, range[0]); // Iron 4 0LP
        assertEquals(1599, range[1]); // Gold 1 99LP
    }

    @Test
    @DisplayName("Bronze player: Iron 4 ~ Silver 1")
    void testBronzeDuoRange() {
        // Bronze 2 50LP = 400 + 200 + 50 = 650
        int bronzeScore = TierCalculator.calculateScore(Tier.BRONZE, Division.II, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(bronzeScore);

        // Bronze: Iron 4 ~ Silver 1 (0~1199)
        assertEquals(0, range[0]); // Iron 4 0LP
        assertEquals(1199, range[1]); // Silver 1 99LP
    }

    @Test
    @DisplayName("Emerald 4,3: Platinum 4 ~ Emerald 1")
    void testEmeraldLowerDuoRange() {
        // Emerald 3 50LP = 2000 + 100 + 50 = 2150
        int emeraldScore = TierCalculator.calculateScore(Tier.EMERALD, Division.III, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(emeraldScore);

        // Emerald 4,3: Platinum 4 ~ Emerald 1 (1600~2399)
        assertEquals(1600, range[0]); // Platinum 4 0LP
        assertEquals(2399, range[1]); // Emerald 1 99LP
    }

    @Test
    @DisplayName("Emerald 2: Platinum 4 ~ Diamond 4")
    void testEmerald2DuoRange() {
        // Emerald 2 50LP = 2000 + 200 + 50 = 2250
        int emeraldScore = TierCalculator.calculateScore(Tier.EMERALD, Division.II, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(emeraldScore);

        // Emerald 2: Platinum 4 ~ Diamond 4 (1600~2499)
        assertEquals(1600, range[0]); // Platinum 4 0LP
        assertEquals(2499, range[1]); // Diamond 4 99LP
    }

    @Test
    @DisplayName("Emerald 1: Platinum 4 ~ Diamond 3")
    void testEmerald1DuoRange() {
        // Emerald 1 50LP = 2000 + 300 + 50 = 2350
        int emeraldScore = TierCalculator.calculateScore(Tier.EMERALD, Division.I, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(emeraldScore);

        // Emerald 1: Platinum 4 ~ Diamond 3 (1600~2599)
        assertEquals(1600, range[0]); // Platinum 4 0LP
        assertEquals(2599, range[1]); // Diamond 3 99LP
    }

    @Test
    @DisplayName("Diamond 4: Emerald 2 ~ Diamond 2")
    void testDiamond4DuoRange() {
        // Diamond 4 50LP = 2400 + 0 + 50 = 2450
        int diamondScore = TierCalculator.calculateScore(Tier.DIAMOND, Division.IV, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(diamondScore);

        // Diamond 4: Emerald 2 ~ Diamond 2 (2200~2699)
        assertEquals(2200, range[0]); // Emerald 2 0LP
        assertEquals(2699, range[1]); // Diamond 2 99LP
    }

    @Test
    @DisplayName("Diamond 3: Emerald 1 ~ Diamond 1")
    void testDiamond3DuoRange() {
        // Diamond 3 50LP = 2400 + 100 + 50 = 2550
        int diamondScore = TierCalculator.calculateScore(Tier.DIAMOND, Division.III, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(diamondScore);

        // Diamond 3: Emerald 1 ~ Diamond 1 (2300~2799)
        assertEquals(2300, range[0]); // Emerald 1 0LP
        assertEquals(2799, range[1]); // Diamond 1 99LP
    }

    @Test
    @DisplayName("Diamond 2: Diamond 4 ~ Diamond 1")
    void testDiamond2DuoRange() {
        // Diamond 2 50LP = 2400 + 200 + 50 = 2650
        int diamondScore = TierCalculator.calculateScore(Tier.DIAMOND, Division.II, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(diamondScore);

        // Diamond 2: Diamond 4 ~ Diamond 1 (2400~2799)
        assertEquals(2400, range[0]); // Diamond 4 0LP
        assertEquals(2799, range[1]); // Diamond 1 99LP
    }

    @Test
    @DisplayName("Diamond 1: Diamond 3 ~ Diamond 1")
    void testDiamond1DuoRange() {
        // Diamond 1 50LP = 2400 + 300 + 50 = 2750
        int diamondScore = TierCalculator.calculateScore(Tier.DIAMOND, Division.I, 50);
        int[] range = TierCalculator.calculateSoloRankDuoRange(diamondScore);

        // Diamond 1: Diamond 3 ~ Diamond 1 (2500~2799)
        assertEquals(2500, range[0]); // Diamond 3 0LP
        assertEquals(2799, range[1]); // Diamond 1 99LP
    }

    @Test
    @DisplayName("Master player should not be able to duo in solo rank")
    void testMasterPlayerCannotDuo() {
        int masterScore = TierCalculator.calculateScore(Tier.MASTER, Division.NONE, 100);
        int[] range = TierCalculator.calculateSoloRankDuoRange(masterScore);

        assertEquals(-1, range[0]);
        assertEquals(-1, range[1]);
    }

    @Test
    @DisplayName("String tier name should convert correctly")
    void testCalculateScoreFromString() {
        int score = TierCalculator.calculateScore("GOLD", "II", 26);
        assertEquals(1426, score);
    }
}

