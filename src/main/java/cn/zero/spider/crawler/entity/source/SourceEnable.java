package cn.zero.spider.crawler.entity.source;

/**
 * Created by yuyuhang on 2018/1/12.
 */
public class SourceEnable {


    public SourceID id;

    public boolean enable;

    public SourceEnable(SourceID id, boolean enable) {
        this.id = id;
        this.enable = enable;
    }
}
