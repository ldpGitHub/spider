package cn.zero.spider.security.handler;

import cn.zero.spider.pojo.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 姚铖杰
 * @date 2018/4/12 10:52
 * @describe 默认的的权限不足处理器实现
 **/
@Slf4j
@Component
@AllArgsConstructor
public class NovelAccessDeniedHandler implements AccessDeniedHandler {

    private ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {

        log.warn("权限不足：{}", e.getMessage());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ResponseData data = ResponseData.builder().code(403).message("权限不足：" + e.getMessage()).status(false).build();
        objectMapper.writeValue(response.getOutputStream(), data);
    }

}
