package org.example.stcapstonebackend.findTeam.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 티어 범위를 나타내는 DTO입니다.
 * 프론트엔드와 백엔드 간 티어 정보를 주고받을 때 사용됩니다.
 * 백엔드 내부에서는 점수(score)로 변환하여 처리합니다.
 *
 * @param tier 티어 (IRON, BRONZE, SILVER, GOLD, PLATINUM, EMERALD, DIAMOND, MASTER, GRANDMASTER, CHALLENGER)
 * @param division Division (I, II, III, IV, 빈 문자열은 마스터 이상)
 * @param lp LP (기본값 0, 마스터 이상은 실제 LP 값)
 */
@Builder
public record TierRange(
        @NotBlank String tier,
        String division,
        @Min(0) @Max(9999) Integer lp
) {
    /**
     * LP 기본값 생성자
     */
    public TierRange {
        if (lp == null) {
            lp = 0;
        }
        if (division == null) {
            division = "";
        }
    }

    /**
     * 티어와 Division만으로 생성하는 정적 팩토리 메서드 (LP는 0)
     */
    public static TierRange of(String tier, String division) {
        return new TierRange(tier, division, 0);
    }

    /**
     * 티어, Division, LP로 생성하는 정적 팩토리 메서드
     */
    public static TierRange of(String tier, String division, Integer lp) {
        return new TierRange(tier, division, lp);
    }

    @Override
    @NotBlank
    public String toString() {
        if (tier.equals("MASTER") || tier.equals("GRANDMASTER") || tier.equals("CHALLENGER")) {
            return tier + " " + lp + "LP";
        }
        return tier + " " + division + " " + lp + "LP";
    }
}

