package cn.zero.spider.controller;

import cn.zero.spider.crawler.Crawler;
import cn.zero.spider.crawler.entity.MyView;
import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.content.Content;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.pojo.Article;
import cn.zero.spider.pojo.Book;
import cn.zero.spider.repository.ChapterRepository;
import cn.zero.spider.repository.ContentRepository;
import cn.zero.spider.repository.SearchResultRepository;
import cn.zero.spider.service.IArticleService;
import cn.zero.spider.service.IBookService;
import cn.zero.spider.webmagic.page.BiQuGePageProcessor;
import cn.zero.spider.webmagic.page.BiQuGeSearchPageProcessor;
import cn.zero.spider.webmagic.pipeline.BiQuGePipeline;
import cn.zero.spider.webmagic.task.AgainSpider;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 小说控制器
 *
 * @author 蔡元豪
 * @date 2018 /6/24 08:57
 */
@RestController
public class BookController extends BaseController {
    private static final String TAG  = BookController.class.getSimpleName();
    private Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private SearchResultRepository searchResultRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private IBookService bookService;

    @Autowired
    private IArticleService articleService;
    /**
     * 小说详情和章节保存组件
     */
    @Autowired
    private BiQuGePipeline biQuGePipeline;

    @Autowired
    private BiQuGePageProcessor biQuGePageProcessor;

    @Autowired
    private AgainSpider againSpider;

    @Autowired
    private RedisScheduler redisScheduler;

    @Value("${spider.url}")
    private String spiderUrl;


    private List<SearchBook> userResult = new ArrayList<>();
    private List<SearchBook> finalResult = new ArrayList<>();
    private List<Chapter> chapterList = new ArrayList<>();



    /**
     * 搜索小说
     *
     * @param request 请求
     * @return userResult 搜索结果
     */

    @RequestMapping( "/search")
    public  List<SearchBook> userSearch ( HttpServletRequest request) {
        userResult.clear();
        String bookName = request.getParameter("bookName");
        Crawler.userSearch(bookName, new SearchCallback() {
            @Override
            public synchronized void onResponse(String keyword, List<SearchBook> appendList) {
                logger.error("搜索结果:" + keyword + ": " + appendList);
                if (appendList.size() > 0) {
                    userResult.addAll(appendList);
                }
            }
            @Override
            public void onFinish() {
                logger.error("onFinish: 结果大小" + userResult.size());
            }
            @Override
            public void onError(String msg) {
                logger.error(msg);
            }
        });
        logger.error("请求到了: "+userResult);
        ModelAndView modelAndView = new ModelAndView(new MyView());
        Map<String, Object> data = new HashMap<>();
        data.put("success",true);
        data.put("message","成功");
        data.put("res",new Gson().toJson(userResult) );
        modelAndView.addAllObjects(data);
        searchResultRepository.saveAll(userResult);
        return userResult;
    }



    /**
     * 搜索小说
     *
     * @param request 请求
     * @return bookResult 小说详情
     */
    SearchBook bookResult = null ;
    @RequestMapping( "/getBookInfo")
    public SearchBook getBookInfo (HttpServletRequest request) {

        Long  bookId = 0L;
        try {
            bookId= Long.valueOf(request.getParameter("bookId"));
        }catch (Exception e){
            logger.error(e.toString());
        }
       Optional<SearchBook> book= searchResultRepository.findById(bookId);
        book.ifPresent(new Consumer<SearchBook>() {
            @Override
            public void accept(SearchBook searchBook) {
                bookResult =searchBook;
            }
        });
        return  bookResult;

    }

    /**
     * 获取目录
     *
     * @param request 请求
     * @return
     */
   private  Optional<SearchBook> book = null;
    @RequestMapping( "/getBookFolder")
    public List<Chapter>  getBookFolder (HttpServletRequest request) {
         List<Chapter> chaptersResult = new ArrayList<>();
        Long  bookId = 0L;
        try {
            bookId= Long.valueOf(request.getParameter("bookId"));
        }catch (Exception e){
            logger.error(e.toString());
        }
       book= searchResultRepository.findById(bookId);
        book.ifPresent(new Consumer<SearchBook>() {
            @Override
            public void accept(SearchBook searchBook) {
                if(searchBook.sources.isEmpty()){
                    return;
                }
               Crawler.catalog(searchBook.sources.get(0), new ChapterCallback() {
                   @Override
                   public void onResponse(List<Chapter> chapters) {
                       for (Chapter chapter:chapters) {
                           chapter.setBookId(searchBook.getBookId());
                       }
                       chaptersResult.addAll(chapters);
                       ExecutorService folderService = Executors.newSingleThreadExecutor();
                       folderService.submit(new Runnable() {
                           @Override
                           public void run() {
                               chapterRepository.saveAll(chapters);

                           }
                       });
                   }

                   @Override
                   public void onError(String msg) {

                   }
               });
            }
        });
        return  chaptersResult;
    }


    /**
     * 获取内容
     *
     * @param request 请求
     * @return
     */

    private  Optional<Chapter> chapter = null;
    Content contentResponse = null;

    @RequestMapping("/getBookContent")
    public Content getBookContent(HttpServletRequest request) {
        Long bookId = 0L;
        Long chapterId = 0L;

        try {
            bookId = Long.valueOf(request.getParameter("bookId"));
            chapterId = Long.valueOf(request.getParameter("chapterId"));

        } catch (Exception e) {
            logger.error(e.toString());
        }
        book = searchResultRepository.findById(bookId);
        chapter = chapterRepository.findById(chapterId);
        if (book.isPresent() && chapter.isPresent()) {
            Crawler.content(book.get().sources.get(0), chapter.get().link, new ContentCallback() {
                @Override
                public void onResponse(String content) {
                    Content chapterContent = new Content();
                    chapterContent.setContent(content);
                    chapterContent.setLink( chapter.get().link);
                    chapterContent.setTitle( chapter.get().title);
                    contentRepository.save(chapterContent);
                    contentResponse = chapterContent;
                }

                @Override
                public void onError(String msg) {

                }
            });
        }


        return contentResponse;
    }


//    /**
//     * 搜索小说
//     *
//     * @param bookName 小说名称
//     * @return book book
//     */
//
//    @RequestMapping(value = "/{bookName}")
//    public  List<SearchBook> book (@PathVariable("bookName") String bookName, HttpServletRequest request) {
//
//        userResult.clear();
//        finalResult.clear();
//
//
//        Crawler.userSearch(bookName, new SearchCallback() {
//            @Override
//            public synchronized void onResponse(String keyword, List<SearchBook> appendList) {
//                logger.error("搜索结果:" + keyword + ": " + appendList);
//                if (appendList.size() > 0) {
//                    userResult.addAll(appendList);
//                }
//
//            }
//
//            @Override
//            public void onFinish() {
//                logger.error("onFinish: 结果大小" + userResult.size());
//            }
//
//            @Override
//            public void onError(String msg) {
//                logger.error(msg);
//            }
//        });
//        logger.error("请求到了: "+userResult);
//        ModelAndView modelAndView = new ModelAndView(new MyView());
//        Map<String, Object> data = new HashMap<>();
//        data.put("success",true);
//        data.put("message","成功");
//        data.put("res",new Gson().toJson(userResult) );
//        modelAndView.addAllObjects(data);
//        searchResultRepository.saveAll(userResult);
//        ExecutorService bookSerVice= Executors.newSingleThreadExecutor();
//        bookSerVice.submit(new Runnable() {
//            @Override
//            public void run() {
//                Crawler.search(bookName, new SearchCallback() {
//                    @Override
//                    public void onResponse(String keyword, List<SearchBook> appendList) {
//                        if (appendList.size() > 0) {
//                            finalResult.addAll(appendList);
//                        }
//
//                    }
//
//                    @Override
//                    public void onFinish() {
//                        searchResultRepository.saveAll(finalResult);
//                        for (SearchBook searchBookItem:finalResult) {
//                            for (SearchBook.SL slItem:searchBookItem.getSources())
//                            Crawler.catalog(slItem, new ChapterCallback() {
//                                @Override
//                                public void onResponse(List<Chapter> chapters) {
//                                    chapterList.addAll(chapters);
//                                    chapterRepository.saveAll(chapterList);
//                                    for (Chapter chapterItem:chapterList) {
//                                        logger.info(TAG,chapterItem);
//
//                                        Crawler.content(slItem, chapterItem.link, new ContentCallback() {
//                                            @Override
//                                            public void onResponse(String content) {
//                                                Content chapterContent = new Content();
//                                                chapterContent.setContent(content);
//                                                chapterContent.setLink(chapterItem.link);
//                                                chapterContent.setTitle(searchBookItem.title);
//                                                contentRepository.save(chapterContent);
//                                            }
//
//                                            @Override
//                                            public void onError(String msg) {
//
//                                            }
//                                        });
//
//                                    }
//                                }
//
//                                @Override
//                                public void onError(String msg) {
//
//                                }
//                            });
//                        }
//                    }
//
//                    @Override
//                    public void onError(String msg) {
//
//                    }
//                });
//            }
//        });
//        return userResult;
//
//    }
//



    /**
     * 小说详情页面
     *
     * @param bookUrl 小说url
     * @return book book
     */
//    @RequestMapping(value = "/{bookUrl}")
//    public ModelAndView book(@PathVariable("bookUrl") String bookUrl, HttpServletRequest request) {
//        logger.info("小说详情页面：" + spiderUrl+"/" +bookUrl);
//
//        ModelAndView modelAndView = new ModelAndView();
//        for (Cookie cookie : request.getCookies()) {
//            if (cookie.getName().equals(bookUrl)) {
//                Article article = articleService.getByUrl(bookUrl, cookie.getValue());
//                article.setContent(null);
//                modelAndView.addObject("record", article);
//            }
//        }
//        Book book = bookService.getById(bookUrl);
//        if (book != null) {
//            modelAndView.addObject("book", book);
//            modelAndView.setViewName("book/index");
//            //如果小说不存在 开始爬取
//        } else {
//            logger.info("开始新抓小说：" + spiderUrl+"/" +bookUrl);
//            Spider.create(biQuGePageProcessor)
//                    .addUrl(spiderUrl+"/" + bookUrl).addPipeline(biQuGePipeline)
//                    //url管理
//                    .setScheduler(redisScheduler)
//                    .thread(30).runAsync();
//            modelAndView.setViewName("book/info");
//        }
//
//        return modelAndView;
//    }

//    /**
//     * 查询小说
//     *
//     * @param key  查询关键字
//     * @param page 分页
//     * @return m
//     */
//    @RequestMapping(value = "/search")
//    public ModelAndView search(@RequestParam(value = "key") String key,
//                               @RequestParam(value = "page", required = false) Integer page) {
//        ModelAndView modelAndView = new ModelAndView();
//        ResultItems resultItems = null;
//        try {
//            String encodeKey = URLEncoder.encode(key, "gb2312");
//            resultItems = Spider.create(new BiQuGeSearchPageProcessor())
//                    .get(spiderUrl+"/modules/article/soshu.php?searchkey=+"
//                            + encodeKey + (page == null ? "" : "&page=" + page));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        if (resultItems != null) {
//            resultItems.getAll().forEach(modelAndView::addObject);
//        }
//        //搜索关键字
//        modelAndView.addObject("key", key);
//        //当前页面
//        modelAndView.addObject("page", page != null ? page : 1);
//        modelAndView.setViewName("book/result");
//        return modelAndView;
//    }


    /**
     * 手动更新小说
     */
//    @RequestMapping("booksUpdate")
//    public void update() {
//        againSpider.books();
//    }

}
