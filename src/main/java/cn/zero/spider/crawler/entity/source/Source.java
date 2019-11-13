package cn.zero.spider.crawler.entity.source;

import java.io.Serializable;

/**
 * 源
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
public class Source implements Serializable {


    public int id;

    public String name;

    public String searchURL;

    /**
     * 最少输入字数
     */
    public int minKeywords = 1;

    public Source(int  id, String name, String searchURL) {
        this.id = id;
        this.name = name;
        this.searchURL = searchURL;
    }

    @Override
    public String toString() {
        return "Source{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", searchURL='" + searchURL + '\'' +
                ", minKeywords=" + minKeywords +
                '}';
    }

    public Source(int  id, String name, String searchURL, int minKeywords) {
        this.id = id;
        this.name = name;
        this.searchURL = searchURL;
        this.minKeywords = minKeywords;
    }
}
