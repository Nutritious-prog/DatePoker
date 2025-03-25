package com.datepoker.dp_backend.DTO;

public record VoteRequest(
        Long cardId,
        String value // "LIKE" or "DISLIKE"
) {}

