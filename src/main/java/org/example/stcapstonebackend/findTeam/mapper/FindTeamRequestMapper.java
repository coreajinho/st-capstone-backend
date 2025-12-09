package org.example.stcapstonebackend.findTeam.mapper;

import org.example.stcapstonebackend.findTeam.dto.FindTeamRequestResponse;
import org.example.stcapstonebackend.findTeam.model.FindTeamRequest;
import org.springframework.stereotype.Component;

/**
 * 팀 찾기 신청 요청 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스입니다.
 */
@Component
public class FindTeamRequestMapper {

    /**
     * 엔티티를 응답 DTO로 변환합니다.
     *
     * @param request 신청 요청 엔티티
     * @return 변환된 신청 요청 응답 DTO
     */
    public FindTeamRequestResponse toDto(FindTeamRequest request) {
        return new FindTeamRequestResponse(
                request.getId(),
                request.getContent(),
                request.getWriter(),
                request.getDesiredTag(),
                request.getIsAccepted(),
                request.getCreatedAt(),
                request.getModifiedAt()
        );
    }
}
