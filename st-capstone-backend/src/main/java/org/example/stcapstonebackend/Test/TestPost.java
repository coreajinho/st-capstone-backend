package org.example.stcapstonebackend.Test; // 패키지 경로는 본인 프로젝트에 맞게 수정하세요

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.stcapstonebackend.Test.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "test_post") // DB에 생성될 테이블 이름
public class TestPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255) // 제목은 비어있을 수 없고, 길이는 255자로 제한
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // 내용은 비어있을 수 없고, 긴 텍스트를 위해 TEXT 타입 사용
    private String content;

    @Column(nullable = false, length = 50)
    private String writer;

    @Builder
    public TestPost(String title, String content, String writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
    }
}