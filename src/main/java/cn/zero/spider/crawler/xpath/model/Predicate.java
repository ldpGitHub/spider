package cn.zero.spider.crawler.xpath.model;


import cn.zero.spider.crawler.xpath.util.OpEm;

/**
 * xpath语法节点的谓语部分，即要满足的限定条件
 */
public class Predicate {

    private OpEm opEm;
    private String left;
    private String right;
    private String value;

    public OpEm getOpEm() {
        return opEm;
    }

    public void setOpEm(OpEm opEm) {
        this.opEm = opEm;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
