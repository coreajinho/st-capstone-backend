package org.example.stcapstonebackend.debate.model;

public enum SearchType {
    TITLE,          // 제목으로 검색
    CONTENT,        // 내용으로 검색
    TITLE_CONTENT,  // 제목 + 내용으로 검색
    WRITER          // 작성자(writer 또는 coWriter)로 검색
}
