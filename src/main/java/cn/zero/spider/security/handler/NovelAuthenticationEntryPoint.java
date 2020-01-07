package cn.zero.spider.security.handler;

import cn.zero.spider.pojo.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 姚铖杰
 * @date 2018/4/24 15:15
 * @describe 需要登录时的处理逻辑由此类实现
 **/
@Component
@AllArgsConstructor
public class NovelAuthenticationEntryPoint implements AuthenticationEntryPoint{

    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ResponseData data = ResponseData.builder().code(401).message("请登录").status(false).build();
        objectMapper.writeValue(response.getOutputStream(), data);
    }

}
