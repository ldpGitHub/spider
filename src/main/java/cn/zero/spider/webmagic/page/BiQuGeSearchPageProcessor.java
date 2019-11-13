package cn.zero.spider.webmagic.page;

import cn.zero.spider.crawler.Crawler;
import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.pojo.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索
 *
 * @author 蔡元豪
 * @date 2018/6/26 17:54
 */
public class BiQuGeSearchPageProcessor implements PageProcessor {
    private Logger logger = LoggerFactory.getLogger(BiQuGeSearchPageProcessor.class);

    private Site site = Site.me()
            //下载失败的url重新放入队列尾部重试
            .setCycleRetryTimes(3)
            //重试次数
            .setRetryTimes(3)
            //2页处理间隔 单位毫秒
            .setSleepTime(500)
            //超时时间
            .setTimeOut(30000)
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");


   private List<SearchBook> finalResult = new ArrayList<>();
    /**
     *
     * @param page page
     */
    @Override
    public void process(Page page) {

//        Crawler.search("仙都", new SearchCallback() {
//            @Override
//            public synchronized void onResponse(String keyword, List<SearchBook> appendList) {
//                logger.error("结果:"+keyword+": " +appendList);
//                if (appendList.size()>0){
//                    finalResult.addAll(appendList);
//                }
//
//
//            }
//
//            @Override
//            public void onFinish() {
//                logger.error("onFinish: 结果大小"+finalResult.size());
//                Crawler.catalog(finalResult.get(0).sources.get(0), new ChapterCallback() {
//                    @Override
//                    public void onResponse(List<Chapter> chapters) {
//                        logger.error("chapters: " + chapters);
//                    }
//
//                    @Override
//                    public void onError(String msg) {
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onError(String msg) {
//                logger.error(msg);
//
//            }
//        });








        boolean a = true;
        if (a)
        {return;}


        //结果
        List<Book> books = new ArrayList<>();
        page.getHtml().xpath("//*[@id=\"nr\"]").nodes().forEach(
                selectable -> {
                    Book book = new Book();
                    //标题
                    book.setTitle(selectable.xpath("//*td[@class=\"odd\"]/a/text()").toString());
                    //作者
                    book.setAuthor(selectable.xpath("//*[@id=\"nr\"]/td[3]/text()").toString());
                    //单词数量
                    book.setWordCount(selectable.xpath("//*[@id=\"nr\"]/td[4]/text()").toString());
                    //更新时间
                    book.setUpdateTime(selectable.xpath("//*[@id=\"nr\"]/td[5]/text()").toString());
                    //状态
                    book.setStatus(selectable.xpath("//*[@id=\"nr\"]/td[6]/text()").toString());
                    //小说url
//                    book.setBookUrl(selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").regex(UrlUtils.getHost(page.getUrl().toString()) + "/(\\w+)").toString());
                    logger.info("BookUrl处理前"+selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").toString());
                    book.setBookUrl(selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").regex("https://www.biquyun.com" + "/(\\w+)").toString());
                    logger.info("BookUrl处理后"+selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").regex("https://www.biquyun.com" + "/(\\w+)").toString());


                    logger.info(selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").toString());

                    logger.info(""+page.getUrl().toString());
//                    book.setBookUrl(selectable.xpath("//*[@id=\"nr\"]/td[1]/a/@href").toString());

                    book.setLatestChapterTitle(selectable.xpath("//*[@id=\"nr\"]/td[2]/a/text()").toString());
                    logger.info("ChapterUrl处理后"+selectable.xpath("//*[@id=\"nr\"]/td[2]/a/@href").toString());

                    book.setLatestChapterUrl(selectable.xpath("//*[@id=\"nr\"]/td[2]/a/@href").regex("/\\w+/(\\w+)\\.html").toString());
                    logger.info("ChapterUrl处理后"+selectable.xpath("//*[@id=\"nr\"]/td[2]/a/@href").regex("/\\w+/(\\w+)\\.html").toString());

//                    book.setLatestChapterUrl(selectable.xpath("//*[@id=\"nr\"]/td[2]/a/@href").toString());

                    books.add(book);
                    logger.info(book+"");
                }
        );

        page.putField("books", books);
        //结果页数
        page.putField("num", page.getHtml().xpath("//*[@id=\"pagelink\"]/a[@class=\"last\"]/text()").toString());

    }


    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }

}



