package cn.zero.spider.crawler.entity;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * 重写视图
 * @author 陈嘉豪
 */
public class MyView extends AbstractView {
    @Override
    protected void renderMergedOutputModel(Map<String, Object> map, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        //转json
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(map);

        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        bas.write(jsonObject.toJSONString().getBytes("utf-8"));
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setContentLength(bas.size());
        OutputStream os = httpServletResponse.getOutputStream();
        bas.writeTo(os);
        os.flush();
    }
}
