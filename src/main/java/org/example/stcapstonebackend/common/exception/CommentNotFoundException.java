package org.example.stcapstonebackend.common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}