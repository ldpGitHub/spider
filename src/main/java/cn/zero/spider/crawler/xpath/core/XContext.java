package cn.zero.spider.crawler.xpath.core;



import cn.zero.spider.crawler.xpath.model.Node;

import java.util.LinkedList;

public class XContext {
    public LinkedList<Node> xpathTr;

    public XContext() {
        if (xpathTr == null) {
            xpathTr = new LinkedList<Node>();
        }
    }
}
