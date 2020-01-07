package cn.zero.spider.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseData<T> {

    /**
     * 请求状态 true成功 false失败
     */
    private boolean status;

    /**
     * 返回描述
     */
    private String message;

    /**
     * 状态码
     */
    private int code;

    /**
     * 数据载体
     */
    private T data;

    public static<T> ResponseData<T> ok() {
        return new ResponseData<>(true, "success", 200, null);
    }

}
