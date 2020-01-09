package cn.zero.spider.crawler.entity.source;

import lombok.Data;

/**
 * 默认配置
 * 可能有部分源，比较复杂，需要多个xpath，那就继承重写
 * <p>
 * Created by quezhongsang on 2018/1/7.
 */
@Data
public class SourceConfig {

    private int id;

    private String name;

    private String searchURL;

    private int minKeywords;

    private boolean enable;

    /**
     * 搜索
     */
    private Search search;

    /**
     * 小说目录内容
     */
    private Catalog catalog;

    /**
     * 小说内容
     */
    private Content content;

    @Data
    public static class Search {

        private String charset;

        private String xpath;

        private String coverXpath;

        private String titleXpath;

        private String linkXpath;

        private String authorXpath;

        private String descXpath;

        private String lastChapterXpath;

    }

    @Data
    public static class Catalog {
        private String xpath;

        private String titleXpath;

        private String linkXpath;
    }

    @Data
    public static class Content {
        private String xpath;
    }

}
