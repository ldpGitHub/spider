package cn.zero.spider.crawler.entity.source;

import lombok.ToString;

/**
 * 默认配置
 * 可能有部分源，比较复杂，需要多个xpath，那就继承重写
 * <p>
 * Created by quezhongsang on 2018/1/7.
 */
@ToString
public class SourceConfig {

    public int id;

    /**
     * 搜索
     */
    public Search search;

    /**
     * 小说目录内容
     */
    public Catalog catalog;

    /**
     * 小说内容
     */
    public Content content;

    public SourceConfig(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ToString
    public static class Search {

        public String charset;

        public String xpath;

        public String coverXpath;

        public String titleXpath;

        public String getLastChapterXpath() {
            return lastChapterXpath;
        }

        public void setLastChapterXpath(String lastChapterXpath) {
            this.lastChapterXpath = lastChapterXpath;
        }

        public String linkXpath;

        public String authorXpath;

        public String descXpath;

        public String lastChapterXpath;

    }

    public static class Catalog {
        public String xpath;

        public String titleXpath;

        public String linkXpath;
    }

    public static class Content {
        public String xpath;
    }
}
