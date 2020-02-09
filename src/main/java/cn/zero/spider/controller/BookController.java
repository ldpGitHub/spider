package cn.zero.spider.controller;

import cn.zero.spider.crawler.Crawler;
import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.content.Content;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.pojo.*;
import cn.zero.spider.push.ApiException;
import cn.zero.spider.push.PushClient;
import cn.zero.spider.repository.ChapterRepository;
import cn.zero.spider.repository.ContentRepository;
import cn.zero.spider.repository.SearchResultRepository;
import cn.zero.spider.repository.UserRepository;
import cn.zero.spider.service.UserService;
import cn.zero.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


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
    private  UserService userService;
    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/synBookShelf")
    public ResponseData<User> synBookShelf(@RequestBody List<Long> ids) {
        System.out.println(ids);
        String username = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByUsername(username);
        List<UserBook> userBooks = ids.stream().map(e -> new UserBook(null, user.getUserId(), e)).collect(Collectors.toList());
        user.setUserBookList(userBooks);
        userRepository.saveAndFlush(user);
        System.out.println(username);
        return new ResponseData<>(true,"同步书架成功",200,user);
    }

    @PostMapping("/synBookShelfByMobile")
    public ResponseData<User> synBookShelfByMobile(@RequestBody DirectSycBookShelfBean directSycBookShelfBean) {
        List<Long> ids = directSycBookShelfBean.getBookIds();
        System.out.println(ids);
        User user = userService.getUserByUsername(directSycBookShelfBean.getMobile());
        if(!directSycBookShelfBean.getMobileToken().equals(user.getMobileToken())){
            return new ResponseData<>(true,"同步书架失败,运营商token失效",500,user);
        }
        List<UserBook> userBooks = ids.stream().map(e -> new UserBook(null, user.getUserId(), e)).collect(Collectors.toList());
        user.setUserBookList(userBooks);
        userRepository.saveAndFlush(user);
//        System.out.println(username);
        return new ResponseData<>(true,"同步书架成功",200,user);
    }

    @RequestMapping("/getBookShelf")
    public List<UserBook>  getBookShelf() {
        String username = (String)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getUserByUsername(username);
        logger.info(user + "");
        logger.info(user.getUserBookList() + "");
        return user.getUserBookList();
    }

    @RequestMapping("/getBookShelfByMobile")
    public List<UserBook> getBookShelfByMobile(HttpServletRequest request) throws Exception{
        String mobile = request.getParameter("mobile");
        String mobileToken = request.getParameter("mobileToken");

        User user = userService.getUserByUsername(mobile);
        if(!mobileToken.equals(user.getMobileToken())){
           throw  new Exception("运营商token失效");
        }

        logger.info(user + "");
        logger.info(user.getUserBookList() + "");
        return   user.getUserBookList();
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

    @Scheduled(initialDelay=1000*5, fixedRate=1000*60*7)
    private void checkUpdateSchedule() {
        PushClient pushClient = new PushClient();
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            pushClient.createPushDefaultNotify(System.currentTimeMillis()+"" ,"开始检查更新了"+simpleDateFormat.format(new Date()));
        } catch (ApiException e) {
            e.printStackTrace();
        }
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
//            logger.info(searchBook.title);
            for (SearchBook.SL searchBookSL: searchBook.getSources() ) {
                Crawler.catalog(searchBookSL, new ChapterCallback() {
                    @Override
                    public void onResponse(List<Chapter> chapters) {
                        logger.info("onResponse:" + chapters.get(chapters.size() - 1).link + chapters.get(chapters.size() - 1).title + "\n");
                        for (Chapter chapter:chapters) {
                            chapter.setBookId(searchBook.getBookId());
                        }
                        chapterAllWebsite.add(chapters);
                    }
                    @Override
                    public void onError(String msg) {
                        logger.error("爬取错误 书名" + searchBook.getTitle() + "     " + msg + "\n");
                    }
                });
            }
//            logger.info("最好的列表:"+"书名 "+searchBook.getTitle()+"  最新章节  "+getBestChapterList(chapterAllWebsite).get(getBestChapterList(chapterAllWebsite).size()-1)+"");


//            Random random = new Random();
//            for (int i = 0; i <=chapterAllWebsite.size()-1 ; i++) {
//                if (i ==0){
//                    List<Chapter> item0 =    chapterAllWebsite.get(0);
//                    item0 =  item0.subList(0,item0.size() - 1 - 7);
//                    chapterAllWebsite.set(0,item0);
//                    logger.info(""+chapterAllWebsite.get(i).get(chapterAllWebsite.get(i).size() -1));
//
//                    continue;
//                }
//                List<Chapter> item = chapterAllWebsite.get(i);
//                item = item.subList(0,item.size() -1- random.nextInt(i) - random.nextInt(chapterAllWebsite.size()));
//                chapterAllWebsite.set(i,item);
//
//                logger.info(""+chapterAllWebsite.get(i).get(chapterAllWebsite.get(i).size() -1));
//            }


            List<Chapter> bestChapterList  = baseChapterListModified(chapterAllWebsite);
            logger.info("最好的列表 原列表修正后:" + "书名 " + searchBook.getTitle() + "  最新章节  " + bestChapterList.get(bestChapterList.size() - 1) + " \r\n \r\n\n");
            String bestChapter = bestChapterList.get(bestChapterList.size() - 1).getTitle();
            if(!searchBook.getLastChapter().equals(bestChapter)){
                searchBook.setLastChapter(bestChapterList.get(bestChapterList.size() - 1).getTitle());
                PushClient pushClient = new PushClient();
                try {
                    pushClient.createPushDefaultNotify(System.currentTimeMillis()+""+searchBook.getLastChapter().hashCode() ,searchBook.title,searchBook.getLastChapter());
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }

            for (int i = 0; i <=bestChapterList.size() - 1 ; i++) {
                bestChapterList.get(i).setChapterIndex(i);
            }
            searchResultRepository.save(searchBook);
            chapterRepository.saveAll(bestChapterList);

//                PushWork pushWork = new PushWork();
//                pushWork.setContent(searchBook.getLastChapter());
//                pushWork.setAndroidTitle(searchBook.getTitle());
//                pushWork.setTarget(1);
//                pushWork.setAppkey(MobPushConfig.appkey);
//                pushWork.setType(1);
//                pushWork.setPlats(new Integer[1]);
//                pushClient.sendPush(pushWork);

        });
    }

    private  List<Chapter> baseChapterListModified(List<List<Chapter>> chapterAllWebsite) {

        List<Chapter> bestChapterList  =  getBestChapterList(chapterAllWebsite);
//        logger.info("算出来的最好章节" + bestChapterList.get(bestChapterList.size() - 1));
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

            List<Chapter> extraChapters = bestChapterList.subList(bestChapterList.size() - 1 - index, bestChapterList.size());
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
        if (chapterAllWebsite.size() <= 1) {
            return chapterAllWebsite.get(0);
        } else {
            return getBetterChapterList(chapterAllWebsite.get(0), getBestChapterList(chapterAllWebsite.subList(1, chapterAllWebsite.size())));
        }
    }

    private List<Chapter> getBetterChapterList(List<Chapter> chapterListA, List<Chapter> chapterListB) {
        logger.info("\n\r");

        logger.info("" + chapterListA.get(chapterListA.size() - 1) + "      " + chapterListB.get(chapterListB.size() - 1));
        logger.info("ASize:" + chapterListA.size() + "    " + "BSize:" + chapterListB.size());
        if (chapterListA.size() < chapterListB.size() - 10) {
            return chapterListB;
        } else if (chapterListB.size() < chapterListA.size() - 10) {
            return chapterListA;
        }
        if (chapterListA.get(chapterListA.size() - 1).getTitle().equals(chapterListB.get(chapterListB.size() - 1).getTitle())) {
            return chapterListA;
        }
        List<Chapter> chapterListALast10 = chapterListA.subList(chapterListA.size() - 1 - 10, chapterListA.size());
        List<Chapter> chapterListBLast100 = chapterListB.subList(chapterListB.size() - 1 - 100, chapterListB.size());
        for (Chapter chapterA : chapterListALast10) {
          if (-1 == chapterListBLast100.indexOf(chapterA)){
              logger.info("结果A" + chapterListA.get(chapterListA.size() - 1) + chapterA + "          " + chapterListBLast100 + "\n\r");

              return  chapterListA;
          }
      }
        logger.info("   结果B   " + chapterListB.get(chapterListB.size() - 1) + "\n\r");

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



    private static String appkey = "";
    private static String appSecret = "";
    private static String token = "";
    private static String opToken = "opToken";
    private static String operator = "CMCC";

    @RequestMapping("/directLogin")
    public JSONObject directLogin(HttpServletRequest uerRequest) throws Exception {
        appkey = uerRequest.getParameter("appkey");
        appSecret = uerRequest.getParameter("appSecret");
        token = uerRequest.getParameter("token");
        opToken = uerRequest.getParameter("opToken");
        operator = uerRequest.getParameter("operator");
        String authHost = "http://identify.verify.mob.com/";
        String url = authHost + "auth/auth/sdkClientFreeLogin";
        HashMap<String, Object> request = new HashMap<>();
        request.put("appkey", appkey);
        request.put("token", token);
        request.put("opToken", opToken);
        request.put("operator", operator);
        request.put("timestamp", System.currentTimeMillis());
        request.put("sign", SignUtil.getSign(request, appSecret));
        String response = postRequestNoSecurity(url, null, request);
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (200 == jsonObject.getInteger("status")) {
            String res = jsonObject.getString("res");
            byte[] decode = DES.decode(Base64Utils.decode(res.getBytes()), appSecret.getBytes());
            ResBean resBean = new Gson().fromJson(new String(decode),ResBean.class);
            resBean.setMobileToken(token);
//            jsonObject.put("res", JSONObject.parseObject(new String(decode)));
            jsonObject.put("res", resBean);
            userService.saveUserDirect(resBean.getPhone(),resBean.getMobileToken());
        }
        System.out.println(jsonObject);
        return jsonObject;
    }


    public static String postRequestNoSecurity(String url, Map<String, String> headers, Object data) throws Exception {
        String securityReq = JSON.toJSONString(data);
        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
        okhttp3.RequestBody body =okhttp3.RequestBody.create(MediaType.parse("application/json"), securityReq);
        Request.Builder builder = new Request.Builder();
        if (!BaseUtils.isEmpty(headers)) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        final Request request = builder.addHeader("Content-Length", String.valueOf(securityReq.length()))
                .url(url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        Response response = call.execute();

        String securityRes = response.body().string();
        return securityRes;
    }
}
