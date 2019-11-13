package cn.zero.spider.crawler.entity.source;

/**
 * 默认配置
 * 可能有部分源，比较复杂，需要多个xpath，那就继承重写
 * <p>
 * Created by quezhongsang on 2018/1/7.
 */
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

    public SourceConfig(int  id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int  id) {
        this.id = id;
    }

    public static class Search {
        @Override
        public String toString() {
            return "Search{" +
                    "charset='" + charset + '\'' +
                    ", xpath='" + xpath + '\'' +
                    ", coverXpath='" + coverXpath + '\'' +
                    ", titleXpath='" + titleXpath + '\'' +
                    ", linkXpath='" + linkXpath + '\'' +
                    ", authorXpath='" + authorXpath + '\'' +
                    ", descXpath='" + descXpath + '\'' +
                    '}';
        }

        public String charset;

        public String xpath;

        public String coverXpath;

        public String titleXpath;

        public String linkXpath;

        public String authorXpath;

        public String descXpath;
    }

    public static class Catalog {
        public String xpath;

        public String titleXpath;

        public String linkXpath;
    }

    @Override
    public String toString() {
        return "SourceConfig{" +
                "id=" + id +
                ", search=" + search +
                ", catalog=" + catalog +
                ", content=" + content +
                '}';
    }

    public static class Content {
        public String xpath;
    }
}
