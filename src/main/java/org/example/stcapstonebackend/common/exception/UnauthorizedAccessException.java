package org.example.stcapstonebackend.common.exception;

import lombok.NoArgsConstructor;

/**
 * 작성자가 아닌 사용자가 게시글이나 댓글을 수정/삭제하려 할 때 발생하는 예외입니다.
 */
@NoArgsConstructor
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

