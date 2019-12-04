package cn.zero.spider.controller;

import cn.zero.spider.crawler.Crawler;
import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.content.Content;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.repository.ChapterRepository;
import cn.zero.spider.repository.ContentRepository;
import cn.zero.spider.repository.SearchResultRepository;
import cn.zero.utils.SimilarityCharacterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.Console;
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


    /**
     * 搜索小说
     *
     * @param request 请求
     * @return userResult 搜索结果
     */
    private ExecutorService checkUpdateExecutorService = Executors.newFixedThreadPool(3);

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


        checkUpdateExecutorService.submit(() -> Crawler.search(bookName,false, new SearchCallback() {
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
        SearchBook searchBook = book.orElse(null);
        if(searchBook!=null){
            searchBook.setSelected(true);
            searchResultRepository.save(searchBook);

        }
        return searchBook;
    }

    @Scheduled(initialDelay=1000*5, fixedRate=1000*60*8)
    private void checkUpdateSchedule() {
        List<SearchBook> currentBookList = searchResultRepository.findAll();
        for (SearchBook searchBook : currentBookList) {
            if(!searchBook.isSelected()){
                continue;
            }

            checkBookResultSingle(searchBook);
        }
    }

    private void checkBookResultSingle(SearchBook searchBook) {
        List<List<Chapter>>   chapterAllWebsite = new ArrayList<>();
        checkUpdateExecutorService.submit(() -> {
            logger.info(searchBook.title);
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
                    logger.error("爬取错误 书名" + searchBook.getTitle() +"     " +msg);
                    }
                });
            }
//            logger.info("最好的列表:"+"书名 "+searchBook.getTitle()+"  最新章节  "+getBestChapterList(chapterAllWebsite).get(getBestChapterList(chapterAllWebsite).size()-1)+"");
            List<Chapter> bestChapterList  = baseChapterListModified(chapterAllWebsite);
            logger.info("最好的列表 原列表修正后:"+"书名 "+searchBook.getTitle()+"  最新章节  "+bestChapterList.get(bestChapterList.size() -1)+"");

            searchBook.setLastChapter(bestChapterList.get(bestChapterList.size() - 1).getTitle()+"更新检测加上去的");
            for (int i = 0; i <=bestChapterList.size() - 1 ; i++) {
                bestChapterList.get(i).setChapterIndex(i);
            }
            searchResultRepository.save(searchBook);
            chapterRepository.saveAll(bestChapterList);

        });
    }

    private  List<Chapter> baseChapterListModified(List<List<Chapter>> chapterAllWebsite) {
        List<Chapter> bestChapterList  =  getBestChapterList(chapterAllWebsite);
        List<Chapter> oriChapterList = chapterAllWebsite.get(0);
        if(bestChapterList.size() < 100||bestChapterList.size() - oriChapterList.size()>100){
            return  bestChapterList;
        }else {
            List<Chapter> oriChapterListLast10 = oriChapterList.subList(oriChapterList.size() - 11,oriChapterList.size() - 1);
            int index = 0;
            double maxSimilarity = 0;
            for (int i = 0; i <100 ; i++) {
                List<Chapter> bestChapterListLast10 = bestChapterList.subList(bestChapterList.size() - 11-i,bestChapterList.size() - 1-i);
                double currentSimilarity = getListSimilarity(oriChapterListLast10,bestChapterListLast10);
                if( currentSimilarity> maxSimilarity){
                    maxSimilarity = currentSimilarity;
                    index = i;
                }
            }

            List<Chapter> extraChapters =  bestChapterList.subList(bestChapterList.size() -1 - index,bestChapterList.size() - 1);
            if(extraChapters.size()> 0){
                logger.info("多加的列表"+extraChapters.toString());
            }
            oriChapterList.addAll(extraChapters);
            return oriChapterList;
        }
    }

    public double getListSimilarity(List<Chapter> oriChapterListLast10,List<Chapter> bestChapterListLast10){
        assert oriChapterListLast10.size() ==bestChapterListLast10.size();
        double result = 0;
        for (int i = 0; i <bestChapterListLast10.size() -1 ; i++) {
            result += SimilarityCharacterUtils.getSimilarity(bestChapterListLast10.get(i).title,oriChapterListLast10.get(i).title);
        }
       return  result;
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
    private ExecutorService folderService = Executors.newCachedThreadPool();
    @RequestMapping( "/getBookFolder")
    public List<Chapter>  getBookFolder  (HttpServletRequest request) {
        Optional<SearchBook> book;
        SearchBook searchBookResult;
        Long  bookId = 0L;
        try {
            bookId= Long.valueOf(request.getParameter("bookId"));
        }catch (Exception e){
            logger.error(e.toString());
        }
        book= searchResultRepository.findById(bookId);

        searchBookResult = book.orElse(null);
        assert searchBookResult != null;
        searchBookResult.setSelected(true);
        searchResultRepository.save(searchBookResult);
        Chapter chapterQuery = new Chapter();
        chapterQuery.setBookId(searchBookResult.getBookId());
        final List<Chapter>  chaptersResult ;
        Example<Chapter> exampleChapter= Example.of(chapterQuery);
        Sort chapterSort  =Sort.by(new Sort.Order(Sort.Direction.ASC,"chapterIndex"));
        chaptersResult = chapterRepository.findAll(exampleChapter,chapterSort);
        logger.info("结果大小:"+chaptersResult.size());
        if (chaptersResult.size()>0){
            return  chaptersResult;
        }else {
                Crawler.catalog(searchBookResult.sources.get(0), new ChapterCallback() {
                    @Override
                    public void onResponse(List<Chapter> chapters) {
                        for (Chapter chapter:chapters) {
                            chapter.setBookId(searchBookResult.getBookId());
                        }
                        chaptersResult.addAll(chapters);
                        for (int i = 0; i <=chapters.size() - 1 ; i++) {
                            chapters.get(i).setChapterIndex(i);
                        }
                        folderService.submit(() -> {
                            chapterRepository.saveAll(chapters);
                        });
                    }
                    @Override
                    public void onError(String msg) {

                    }
                });
            }
            logger.info("直接爬的" +chaptersResult);
            return  chaptersResult;
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
            logger.info(book.get().sources.toString());

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
