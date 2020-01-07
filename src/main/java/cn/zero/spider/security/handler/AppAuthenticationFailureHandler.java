package cn.zero.spider.security.handler;

import cn.zero.spider.pojo.ResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 姚铖杰
 * @date 2018/4/4 8:44
 * @describe 默认登录失败处理器实现
 **/

public class AppAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    protected ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        logger.info("登录失败");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
       // String message = e instanceof SessionAuthenticationException ? "账号已达到同时登录上限" : e.getMessage();
        ResponseData data = ResponseData.builder().code(401).message("用户名或密码错误").status(false).build();
        response.getWriter().write(objectMapper.writeValueAsString(data));

    }

}
