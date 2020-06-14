package cn.zero.spider.controller;

import cn.zero.spider.pojo.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常捕获
 * 可以在这里捕获想要的任何异常
 * ps：404异常需要特殊处理，不能被直接捕获
 */
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

    /**
     * 所有未被捕获的异常到会到这里
     * 此方法优先级别最低
     * @param e exception
     * @return err message
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public ResponseData<String> other(Exception e) {
        log.error("全局捕获异常", e);
        return new ResponseData<>(false, e.getMessage(), 500, "");
    }

}
