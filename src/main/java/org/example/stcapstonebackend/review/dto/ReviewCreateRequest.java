package org.example.stcapstonebackend.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 리뷰 작성 요청 DTO
 *
 * @param revieweeId 피평가자 ID
 * @param comment 리뷰 코멘트
 * @param skill 실력 평가 (0-5점)
 * @param cooperation 협동 평가 (0-5점)
 * @param mental 멘탈 평가 (0-5점)
 * @param manner 매너 평가 (0-5점)
 */
public record ReviewCreateRequest(
    @NotNull(message = "평가 대상자 ID는 필수입니다.")
    Long revieweeId,

    @NotBlank(message = "코멘트는 필수입니다.")
    String comment,

    @NotNull(message = "실력 평가는 필수입니다.")
    @Min(value = 0, message = "실력 평가는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "실력 평가는 5점 이하여야 합니다.")
    Integer skill,

    @NotNull(message = "협동 평가는 필수입니다.")
    @Min(value = 0, message = "협동 평가는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "협동 평가는 5점 이하여야 합니다.")
    Integer cooperation,

    @NotNull(message = "멘탈 평가는 필수입니다.")
    @Min(value = 0, message = "멘탈 평가는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "멘탈 평가는 5점 이하여야 합니다.")
    Integer mental,

    @NotNull(message = "매너 평가는 필수입니다.")
    @Min(value = 0, message = "매너 평가는 0점 이상이어야 합니다.")
    @Max(value = 5, message = "매너 평가는 5점 이하여야 합니다.")
    Integer manner
) {}

