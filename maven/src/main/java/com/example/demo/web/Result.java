package com.example.demo.web;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {

    @JsonProperty("RESULT_CODE")
    private final int resultCode;

    @JsonProperty("RESULT_MESSAGE")
    private final String resultMessage;
}
