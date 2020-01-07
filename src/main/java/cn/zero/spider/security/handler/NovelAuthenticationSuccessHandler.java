package cn.zero.spider.security.handler;

import cn.zero.spider.pojo.ResponseData;
import cn.zero.utils.JWTUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 姚铖杰
 * @date 2018/4/4 9:12
 * @describe 默认登录成功处理器实现
 **/
@Slf4j
@Component
@AllArgsConstructor
public class NovelAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("用户 [ {} ] 登录成功", authentication.getName());
        try {
            String token = JWTUtils.createToken(authentication.getName());
            ResponseData data = ResponseData.builder().code(200).message("登录成功").status(true).data(token).build();
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            objectMapper.writeValue(response.getOutputStream(), data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
