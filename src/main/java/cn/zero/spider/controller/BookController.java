package cn.zero.spider.controller;

import cn.zero.spider.crawler.Crawler;
import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.chapter.ChapterList;
import cn.zero.spider.crawler.entity.content.Content;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.repository.ChapterListRepository;
import cn.zero.spider.repository.ChapterRepository;
import cn.zero.spider.repository.ContentRepository;
import cn.zero.spider.repository.SearchResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    private ChapterListRepository chapterListRepository;

    /**
     * 搜索小说
     *
     * @param request 请求
     * @return userResult 搜索结果
     */

    @RequestMapping( "/search")
    public  List<SearchBook> userSearch ( HttpServletRequest request) {
        List<SearchBook> userResult =new ArrayList<>(),finalResult = new ArrayList<>();
        String bookName = request.getParameter("bookName");
        Crawler.search(bookName,true, new SearchCallback() {
            @Override
            public synchronized void onResponse(String keyword, List<SearchBook> appendList) {
                logger.info("搜索结果:" + keyword + ": " + appendList);
                if (appendList.size() > 0) {
                    userResult.addAll(appendList);
                }
            }
            @Override
            public void onFinish() {
                logger.info("onFinish: 结果大小" + userResult.size());
            }
            @Override
            public void onError(String msg) {
                logger.error(msg);
            }
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> Crawler.search(bookName,false, new SearchCallback() {
            @Override
            public void onResponse(String keyword, List<SearchBook> appendList) {
                for (SearchBook searchBook : appendList) {
                    if (finalResult.contains(searchBook)) {
                        finalResult.get(finalResult.indexOf(searchBook)).getSources().addAll(searchBook.getSources());
                        logger.info("增加书源头: " + finalResult.get(finalResult.indexOf(searchBook)).getSources());
                    } else {
                        finalResult.add(searchBook);
                    }
                }
            }
            @Override
            public void onFinish() {
                logger.info("爬取完成: " + finalResult);
                for (SearchBook searchBook: finalResult) {
                    searchResultRepository.saveAndFlush(searchBook);
                }
            }
            @Override
            public void onError(String msg) {

            }
        }));

        logger.info("userSearch请求结果: "+userResult);
//        ModelAndView modelAndView = new ModelAndView(new MyView());
//        Map<String, Object> data = new HashMap<>();
//        data.put("success",true);
//        data.put("message","成功");
//        data.put("res",new Gson().toJson(userResult) );
//        modelAndView.addAllObjects(data);

        searchResultRepository.saveAll(userResult);
        return userResult;
    }



    /**
     * 搜索小说
     *
     * @param request 请求
     * @return bookResult 小说详情
     */

    @RequestMapping( "/getBookInfo")
    public SearchBook getBookInfo (HttpServletRequest request) {
        Long  bookId = 0L;
        try {
            bookId= Long.valueOf(request.getParameter("bookId"));
        }catch (Exception e){
            logger.error(e.toString());
        }
       Optional<SearchBook> book= searchResultRepository.findById(bookId);
        return book.orElse(null);
    }

    @Scheduled(initialDelay=1000*60*5, fixedRate=1000*60*10)
    private void checkUpdateSchedule() {
        List<SearchBook> currentBookList = searchResultRepository.findAll();
        for (SearchBook searchBook : currentBookList) {
            checkBookResultSingle(searchBook);
        }
    }

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private void checkBookResultSingle(SearchBook searchBook) {
        List<List<Chapter>>   chapterAllWebsite = new ArrayList<>();
        executorService.submit(() -> {
            for (SearchBook.SL searchBookSL: searchBook.getSources() ) {
                Crawler.catalog(searchBookSL, new ChapterCallback() {
                    @Override
                    public void onResponse(List<Chapter> chapters) {
                        for (Chapter chapter:chapters) {
                            chapter.setBookId(searchBook.getBookId());
                        }
                        chapterAllWebsite.add(chapters);
                    }
                    @Override
                    public void onError(String msg) {

                    }
                });
            }
            logger.info("最好的列表:"+"书名 "+searchBook.getTitle()+"  最新章节  "+getBestChapterList(chapterAllWebsite).get(getBestChapterList(chapterAllWebsite).size()-1)+"");
            List<Chapter> bestChapterList = getBestChapterList(chapterAllWebsite);
            ChapterList chapterList =   new ChapterList();
            chapterList.setBookId(searchBook.getBookId());
            chapterList.setChapterList(bestChapterList);
            searchBook.setLastChapter(bestChapterList.get(bestChapterList.size() - 1).getTitle()+"更新检测加上去的");
            searchResultRepository.save(searchBook);
            chapterRepository.saveAll(bestChapterList);
            chapterListRepository.save(chapterList);
        });
    }

    private List<Chapter> getBestChapterList(List<List<Chapter>> chapterAllWebsite) {
        if (1 == chapterAllWebsite.size()) {
            return chapterAllWebsite.get(0);
        }
        else  if(chapterAllWebsite.size()>2){
            return getBetterChapterList(chapterAllWebsite.get(0),getBestChapterList(chapterAllWebsite.subList(1,chapterAllWebsite.size()-1))) ;
        }else if (2 == chapterAllWebsite.size()){
            List<Chapter> chapterListA = chapterAllWebsite.get(0);
            List<Chapter> chapterListB = chapterAllWebsite.get(1);
            return   getBetterChapterList(chapterListA,chapterListB);
        }else {
            return chapterAllWebsite.get(0);
        }
    }

  private List<Chapter> getBetterChapterList( List<Chapter> chapterListA , List<Chapter> chapterListB){
        if(chapterListA.size()<chapterListB.size() - 30){
            return chapterListB;
        }
      List<Chapter> chapterListALast100 = chapterListA.subList(chapterListA.size()-1 - 100,chapterListA.size()-1);
      List<Chapter> chapterListBLast100 = chapterListB.subList(chapterListB.size()-1 - 100,chapterListB.size()-1);
      for (Chapter chapterA:chapterListALast100) {
          if (-1 == chapterListBLast100.indexOf(chapterA)){
              return  chapterListA;
          }
      }
      return  chapterListB;
  }

    /**
     * 获取目录
     *
     * @param request 请求
     * @return
     */

    @RequestMapping( "/getBookFolder")
    public List<Chapter>  getBookFolder (HttpServletRequest request) {
        Optional<SearchBook> book;
         List<Chapter> chaptersResult = new ArrayList<>();
        Long  bookId = 0L;
        try {
            bookId= Long.valueOf(request.getParameter("bookId"));
        }catch (Exception e){
            logger.error(e.toString());
        }
        Optional<ChapterList>  chapterList = chapterListRepository.findById(bookId);
        if (chapterList.isPresent()){
            logger.info("数据库拿的" +chapterList.get().getChapterList());
            return  chapterList.get().getChapterList();
        }else {
            book= searchResultRepository.findById(bookId);
            book.ifPresent(searchBook -> {
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
                        folderService.submit(() -> {
                            chapterRepository.saveAll(chapters);
                        });
                    }
                    @Override
                    public void onError(String msg) {

                    }
                });
            });
            logger.info("直接爬的" +chaptersResult);
            return  chaptersResult;
        }
    }


    /**
     * 获取内容
     *
     * @param request 请求
     * @return
     */
    @RequestMapping("/getBookContent")
    public Content getBookContent(HttpServletRequest request) {
        Optional<Chapter> chapter  ;
        Optional<SearchBook> book;
        final  Content  contentResponse = new Content() ;
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
                    contentResponse.setContent(content);
                    contentResponse.setLink( chapter.get().link);
                    contentResponse.setTitle( chapter.get().title);
                    contentRepository.save(contentResponse);
                }
                @Override
                public void onError(String msg) {

                }
            });
        }
        return contentResponse;
    }

}
