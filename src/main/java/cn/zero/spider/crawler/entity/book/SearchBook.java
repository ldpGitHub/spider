package cn.zero.spider.crawler.entity.book;


import cn.zero.spider.crawler.entity.source.Source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索结果（搜索时，书名和作者名一样时，认为是同一本书，合并起来，同时增加一个源）
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
public class SearchBook implements Serializable {

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<SL> getSources() {
        return sources;
    }

    public void setSources(List<SL> sources) {
        this.sources = sources;
    }

    /**
     * 封面图
     */
    public String cover;

    /**
     * 书名
     */
    public String title;

    /**
     * 作者名
     */
    public String author;

    @Override
    public String toString() {
        return "SearchBook{" +
                "cover='" + cover + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", desc='" + desc + '\'' +
                ", sources=" + sources +
                '}';
    }

    /**
     * 描述
     */
    public String desc;

    /**
     * 源&目录列表链接
     */
    public List<SL> sources = new ArrayList<>();

    /**
     * 源和对应链接
     */
    public static class SL implements Serializable{
        public String link;

        @Override
        public String toString() {
            return "SL{" +
                    "link='" + link + '\'' +
                    ", source=" + source +
                    '}';
        }

        public Source source;

        public SL(String link, Source source) {
            this.link = link;
            this.source = source;
        }
    }
}
