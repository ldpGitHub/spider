package cn.zero.spider.pojo;

import lombok.Data;

import java.util.List;

@Data
public class BookResult {

    /**
     * cover : https://www.liewen.la/files/article/image/37/37131/37131s.jpg
     * title : 重生在90年代
     * author : 橘子大叔
     * desc : 一不小心。 安子善重生到了1999年，正意气风发的想着“今生不留遗憾！” 一个冰冷的不带丝毫感情的声音突然响起，“剩余时间19年49天。” 纳尼？这什么意思？ 生命时钟已经开始了倒计时 （橘子搞了个书...
     * sources : [{"link":"https://www.liewen.la/b/37/37131/","source":{"id":1,"name":"猎文网","searchURL":"https://www.liewen.cc/search.php?keyword=%s","minKeywords":1}}]
     */

    private String cover;
    private String title;
    private String author;
    private String desc;
    private List<SourcesBean> sources;

    @Data
    public static class SourcesBean {
        /**
         * link : https://www.liewen.la/b/37/37131/
         * source : {"id":1,"name":"猎文网","searchURL":"https://www.liewen.cc/search.php?keyword=%s","minKeywords":1}
         */
        private String link;
        private SourceBean source;

        @Data
        public static class SourceBean {
            /**
             * id : 1
             * name : 猎文网
             * searchURL : https://www.liewen.cc/search.php?keyword=%s
             * minKeywords : 1
             */

            private int id;
            private String name;
            private String searchURL;
            private int minKeywords;

        }
    }
}
