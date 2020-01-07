package cn.zero.spider.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 首页小说栏目封装模块
 *
 * @author 蔡元豪
 * @date 2018/6/24 14:10
 */
@Data
public class NovelsList implements Serializable {

    private static final long serialVersionUID = -5393449053178403879L;

    /**
     * 栏目置顶文章
     */
    private Book top;

    private List<Book> books;

    private String type;

}
