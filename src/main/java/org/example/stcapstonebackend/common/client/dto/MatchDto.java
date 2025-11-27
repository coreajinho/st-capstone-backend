package org.example.stcapstonebackend.common.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MatchDto(
        InfoDto info
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record InfoDto(
            long gameDuration,
            long gameEndTimestamp,
            String gameMode, //레거시 코드(추후 업데이트 가능성을 위해 남겨둠)
            int queueId,
            List<ParticipantDto> participants
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static record ParticipantDto(
                String riotIdGameName,
                String riotIdTagLine,
                String summonerName,
                String championName,
                int teamId,
                String teamPosition,
                int summoner1Id,
                int summoner2Id,
                int champLevel,
                int kills,
                int deaths,
                int assists,
                int item0,
                int item1,
                int item2,
                int item3,
                int item4,
                int item5,
                int item6,
                int participantId,
                int totalMinionsKilled,
                boolean win
        ) {
        }
    }
}
