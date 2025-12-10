package org.example.stcapstonebackend.review.mapper;

import org.example.stcapstonebackend.review.dto.ReviewResponse;
import org.example.stcapstonebackend.review.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Review 엔티티와 DTO 간 변환을 담당하는 Mapper
 */
@Mapper(componentModel = "spring")
public interface ReviewMapper {

    /**
     * Review 엔티티를 ReviewResponse DTO로 변환
     */
    @Mapping(source = "reviewer.id", target = "reviewerId")
    @Mapping(source = "reviewer.username", target = "reviewerName")
    @Mapping(source = "reviewee.id", target = "revieweeId")
    @Mapping(source = "reviewee.username", target = "revieweeName")
    @Mapping(target = "overallRating", expression = "java(review.calculateOverallRating())")
    ReviewResponse toResponse(Review review);
}
