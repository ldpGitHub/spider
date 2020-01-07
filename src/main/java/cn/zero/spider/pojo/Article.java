package cn.zero.spider.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 蔡元豪
 * @date 2018/6/23 19:52
 */
@Data
public class Article implements Serializable {

    private static final long serialVersionUID = 6428186596698828991L;

    /**
     * 章节目录地址
     */
    private String url;

    /**
     * 小说详情地址
     */
    private String bookUrl;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

}
