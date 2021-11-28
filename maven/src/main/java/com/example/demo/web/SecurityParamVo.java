package com.example.demo.web;

import com.example.demo.util.Security;
import lombok.Data;

@Data
@Security("base64")
public class SecurityParamVo {

    public String test;

    private Long test2;
}
