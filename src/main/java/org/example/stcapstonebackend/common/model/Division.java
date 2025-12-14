package org.example.stcapstonebackend.common.model;

/**
 * 리그 오브 레전드 티어 내 단계(Division)를 나타내는 열거형입니다.
 * IV(4) > III(3) > II(2) > I(1) 순서입니다.
 */
public enum Division {
    /** IV 단계 (가장 낮은 단계) */
    IV(4),
    /** III 단계 */
    III(3),
    /** II 단계 */
    II(2),
    /** I 단계 (가장 높은 단계) */
    I(1),
    /** 단계 없음 (마스터 이상) */
    NONE(0);

    private final int numericValue;

    Division(int numericValue) {
        this.numericValue = numericValue;
    }

    /**
     * Division의 숫자 값을 반환합니다.
     * (4, 3, 2, 1, 0)
     *
     * @return 숫자 값
     */
    public int getNumericValue() {
        return numericValue;
    }

    /**
     * 문자열 Division명을 Division enum으로 변환합니다.
     * Riot API는 "I", "II", "III", "IV" 형식으로 반환합니다.
     *
     * @param divisionName Division명
     * @return Division enum
     */
    public static Division fromString(String divisionName) {
        if (divisionName == null || divisionName.trim().isEmpty()) {
            return NONE;
        }
        try {
            return Division.valueOf(divisionName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}

