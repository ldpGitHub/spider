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

    private boolean status;

    private String message;

    private int code;

    private T data;

    public static<T> ResponseData<T> ok() {
        return new ResponseData<>(true, "success", 200, null);
    }

}
