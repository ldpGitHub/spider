package cn.zero.spider.controller;

import cn.zero.spider.pojo.ResponseData;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = Exception.class)
    public ResponseData cnm(Exception e, HttpServletResponse response) {
        response.setStatus(500);
        return new ResponseData<>(false, e.getMessage(), 500, null);
    }

}
