package com.example.demo.web;


import com.example.demo.util.Security;
import lombok.AllArgsConstructor;

@Security("base64")
public class SecurityResult extends Result {

    public SecurityResult(int resultCode, String resultMessage) {
        super(resultCode, resultMessage);
    }
}
