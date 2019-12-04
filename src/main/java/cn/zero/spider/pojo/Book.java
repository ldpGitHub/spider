package cn.zero.spider.pojo;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author 蔡元豪
 * @date 2018/6/23 19:30
 */
@Data
@Entity
public class Book implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; // 主键ID

    /**
     * 小说链接
     */
    private String bookUrl;

    /**
     * 作者
     */
    private String author;

    /**
     * 书名
     */
    private String title;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 简介
     */
    private String intro;

    /**
     * 最新文章
     */
    private String latestChapterTitle;

    /**
     * 最新文章链接
     */
    private String latestChapterUrl;

    /**
     * 封面图片链接
     */
    private String titlePageUrl;

    /**
     * 来源地址
     */
    private String sourceUrl;

    /**
     * 章节页面
     */
    private String chapterPage;

    /**
     * 小说状态
     */
    private String status;

    /**
     * 字数
     */
    private String wordCount;

}
