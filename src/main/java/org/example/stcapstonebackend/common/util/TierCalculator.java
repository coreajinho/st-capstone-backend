package org.example.stcapstonebackend.common.util;

import org.example.stcapstonebackend.common.model.Division;
import org.example.stcapstonebackend.common.model.Tier;

/**
 * 티어 정보를 점수로 환산하거나 점수를 티어 정보로 역변환하는 유틸리티 클래스입니다.
 *
 * 환산 공식: Score = BaseTierScore + (4 - Division) × 100 + LP
 *
 * 예시:
 * - Iron 4 0LP = 0점
 * - Gold 2 26LP = 1200 + (4-2) × 100 + 26 = 1426점
 * - Diamond 1 99LP = 2400 + (4-1) × 100 + 99 = 2799점
 * - Master 500LP = 2800 + 500 = 3300점
 */
public final class TierCalculator {

    private TierCalculator() {
        // Utility class
    }

    /**
     * 티어, Division, LP를 점수로 환산합니다.
     *
     * @param tier 티어
     * @param division Division (마스터 이상은 NONE)
     * @param lp LP
     * @return 환산된 점수
     */
    public static int calculateScore(Tier tier, Division division, int lp) {
        if (tier == Tier.UNRANKED) {
            return 0;
        }

        int baseScore = tier.getBaseScore();

        // 마스터 이상은 Division이 없고 LP만 적재됨
        if (tier.isMasterOrAbove()) {
            return baseScore + lp;
        }

        // 일반 티어: BaseTierScore + (4 - Division) × 100 + LP
        int divisionScore = (4 - division.getNumericValue()) * 100;
        return baseScore + divisionScore + lp;
    }

    /**
     * 티어 문자열 정보를 점수로 환산합니다.
     *
     * @param tierName 티어명 (예: "GOLD", "PLATINUM")
     * @param divisionName Division명 (예: "I", "II", "III", "IV")
     * @param lp LP
     * @return 환산된 점수
     */
    public static int calculateScore(String tierName, String divisionName, int lp) {
        Tier tier = Tier.fromString(tierName);
        Division division = Division.fromString(divisionName);
        return calculateScore(tier, division, lp);
    }

    /**
     * 점수를 티어 정보로 역변환합니다.
     *
     * @param score 점수
     * @return TierInfo 객체 (티어, Division, LP)
     */
    public static TierInfo scoreToTier(int score) {
        if (score < 0) {
            return new TierInfo(Tier.UNRANKED, Division.NONE, 0);
        }

        // 마스터 이상 (2800점 이상)
        if (score >= Tier.MASTER.getBaseScore()) {
            int lp = score - Tier.MASTER.getBaseScore();
            return new TierInfo(Tier.MASTER, Division.NONE, lp);
        }

        // 어떤 티어에 속하는지 찾기
        Tier tier = findTierByScore(score);
        if (tier == Tier.UNRANKED) {
            return new TierInfo(Tier.UNRANKED, Division.NONE, 0);
        }

        int baseScore = tier.getBaseScore();
        int remainder = score - baseScore;

        // Division과 LP 계산
        int divisionValue = 4 - (remainder / 100);
        int lp = remainder % 100;

        // divisionValue가 범위를 벗어나는 경우 보정
        if (divisionValue < 1) {
            divisionValue = 1;
        } else if (divisionValue > 4) {
            divisionValue = 4;
        }

        Division division = divisionFromNumeric(divisionValue);
        return new TierInfo(tier, division, lp);
    }

    /**
     * 점수에 해당하는 티어를 찾습니다.
     */
    private static Tier findTierByScore(int score) {
        if (score >= Tier.MASTER.getBaseScore()) return Tier.MASTER;
        if (score >= Tier.DIAMOND.getBaseScore()) return Tier.DIAMOND;
        if (score >= Tier.EMERALD.getBaseScore()) return Tier.EMERALD;
        if (score >= Tier.PLATINUM.getBaseScore()) return Tier.PLATINUM;
        if (score >= Tier.GOLD.getBaseScore()) return Tier.GOLD;
        if (score >= Tier.SILVER.getBaseScore()) return Tier.SILVER;
        if (score >= Tier.BRONZE.getBaseScore()) return Tier.BRONZE;
        if (score >= Tier.IRON.getBaseScore()) return Tier.IRON;
        return Tier.UNRANKED;
    }

    /**
     * 숫자 값을 Division enum으로 변환합니다.
     */
    private static Division divisionFromNumeric(int value) {
        return switch (value) {
            case 4 -> Division.IV;
            case 3 -> Division.III;
            case 2 -> Division.II;
            case 1 -> Division.I;
            default -> Division.NONE;
        };
    }

    /**
     * 솔로랭크 듀오 가능 범위를 계산합니다.
     *
     * 규칙 (티어/Division 단위):
     * - Iron (0~399): Iron 4 ~ Silver 1 (0~1199)
     * - Bronze (400~799): Iron 4 ~ Silver 1 (0~1199)
     * - Silver (800~1199): Iron 4 ~ Gold 1 (0~1599)
     * - Gold (1200~1599): Silver 4 ~ Platinum 1 (800~1999)
     * - Platinum (1600~1999): Gold 4 ~ Emerald 1 (1200~2399)
     * - Emerald (2000~2399): Platinum 4 ~ Diamond 3/2/1 (±2 Division)
     *   - Emerald 4,3: Platinum 4 ~ Emerald 1 (1600~2399)
     *   - Emerald 2: Platinum 4 ~ Diamond 1 (1600~2699)
     *   - Emerald 1: Platinum 4 ~ Diamond 3 (1600~2799)
     * - Diamond (2400~2799): ±2 Division (Master 불가)
     *   - Diamond 4: Emerald 2 ~ Diamond 2 (2200~2599)
     *   - Diamond 3: Emerald 1 ~ Diamond 1 (2300~2699)
     *   - Diamond 2,1: Diamond 4 ~ Diamond 1 (2400~2799)
     * - Master+ (2800~): 듀오 불가
     *
     * @param writerScore 작성자의 티어 점수
     * @return 듀오 가능한 [최소점수, 최대점수] 배열, 듀오 불가 시 [-1, -1]
     */
    public static int[] calculateSoloRankDuoRange(int writerScore) {
        // 마스터 이상은 듀오 불가
        if (writerScore >= Tier.MASTER.getBaseScore()) {
            return new int[]{-1, -1};
        }

        // Diamond 구간 (2400 ~ 2799): ±2 Division
        if (writerScore >= Tier.DIAMOND.getBaseScore()) {
            int minScore;
            int maxScore;

            // Diamond 4 (2400~2499): Emerald 2 ~ Diamond 2
            if (writerScore < 2500) {
                minScore = 2200; // Emerald 2 0LP
                maxScore = 2699; // Diamond 2 99LP
            }
            // Diamond 3 (2500~2599): Emerald 1 ~ Diamond 1
            else if (writerScore < 2600) {
                minScore = 2300; // Emerald 1 0LP
                maxScore = 2799; // Diamond 1 99LP
            }
            // Diamond 2 (2600~2699): Diamond 4 ~ Diamond 1
            else if(writerScore < 2700){
                minScore = 2400; // Diamond 4 0LP
                maxScore = 2799; // Diamond 1 99LP
            }
            // Diamond 1 (2700~2799)
            else{
                minScore = 2500; // Diamond 3 0LP
                maxScore = 2799; // Diamond 1 99LP
            }

            return new int[]{minScore, maxScore};
        }

        // Emerald 구간 (2000 ~ 2399): Platinum 4 ~ (±2 Division)
        if (writerScore >= Tier.EMERALD.getBaseScore()) {
            int minScore = 1600; // Platinum 4 0LP
            int maxScore;

            // Emerald 4,3 (2000~2199): Platinum 4 ~ Emerald 1
            if (writerScore < 2200) {
                maxScore = 2399; // Emerald 1 99LP
            }
            // Emerald 2 (2200~2299): Platinum 4 ~ Diamond 4
            else if (writerScore < 2300) {
                maxScore = 2499; // Diamond 4 99LP
            }
            // Emerald 1 (2300~2399): Platinum 4 ~ Diamond 3
            else {
                maxScore = 2599; // Diamond 3 99LP
            }

            return new int[]{minScore, maxScore};
        }

        // Platinum 구간 (1600 ~ 1999): Gold 4 ~ Emerald 1
        if (writerScore >= Tier.PLATINUM.getBaseScore()) {
            return new int[]{1200, 2399}; // Gold 4 0LP ~ Emerald 1 99LP
        }

        // Gold 구간 (1200 ~ 1599): Silver 4 ~ Platinum 1
        if (writerScore >= Tier.GOLD.getBaseScore()) {
            return new int[]{800, 1999}; // Silver 4 0LP ~ Platinum 1 99LP
        }

        // Silver 구간 (800 ~ 1199): Iron 4 ~ Gold 1
        if (writerScore >= Tier.SILVER.getBaseScore()) {
            return new int[]{0, 1599}; // Iron 4 0LP ~ Gold 1 99LP
        }

        // Iron, Bronze 구간 (0 ~ 799): Iron 4 ~ Silver 1
        return new int[]{0, 1199}; // Iron 4 0LP ~ Silver 1 99LP
    }

    /**
     * 티어 정보를 담는 레코드 클래스
     */
    public record TierInfo(Tier tier, Division division, int lp) {
        @Override
        public String toString() {
            if (tier == Tier.UNRANKED) {
                return "UNRANKED";
            }
            if (tier.isMasterOrAbove()) {
                return tier.name() + " " + lp + "LP";
            }
            return tier.name() + " " + division.name() + " " + lp + "LP";
        }
    }
}

