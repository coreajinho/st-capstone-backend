package org.example.stcapstonebackend.debate.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DebatePostNotFoundException extends RuntimeException {
    public DebatePostNotFoundException(String message) {
        super(message);
    }
}
