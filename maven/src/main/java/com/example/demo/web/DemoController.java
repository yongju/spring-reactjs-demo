package com.example.demo.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Controller
@RestController
@Slf4j
public class DemoController {

    @PostMapping("/test")
    public Result test(
            @RequestBody ParamVo params ) {

        return new Result(0, params.toString());
    }

    @PostMapping("/security-test")
    public SecurityResult securityMessage(
            @RequestBody SecurityParamVo params ) {

        return new SecurityResult(0, params.toString());

    }

}
