package cn.zero.spider.ldp;

import java.util.concurrent.ExecutionException;

/**
 * @author ycj
 * @datetime 2020-6-14 15:13
 * @describe
 */
@FunctionalInterface
public interface LjLdp<K> {

    boolean response(K data, boolean done) throws ExecutionException, InterruptedException;

}
