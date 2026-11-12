package com.ddaii.ximilar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record XimilarSearchResponse(
        @JsonProperty("answer_records") List<AnswerRecord> answerRecords
) {
    public record AnswerRecord(@JsonProperty("_id") String id) {}
}