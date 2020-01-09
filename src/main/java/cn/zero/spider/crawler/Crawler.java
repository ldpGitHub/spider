package cn.zero.spider.crawler;


import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.source.Source;
import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.entity.source.SourceID;
import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.crawler.xpath.exception.XpathSyntaxErrorException;
import cn.zero.spider.crawler.xpath.model.JXDocument;
import cn.zero.spider.crawler.xpath.model.JXNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jsoup.Jsoup;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 爬虫
 * <p>
 * Created by ljldp on 2018/1/8.
 */
@Slf4j
public class Crawler {

    /**
     * 数据源配置文件路径
     */
    private static final String CONFIG_FILE_PATH = "Template.json";

    private static final LinkedHashMap<Integer, SourceConfig> CONFIGS = new LinkedHashMap<>(1);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    static {
        init();
    }

    /**
     * 加载初始配置信息
     */
    private static void init() {
        CONFIGS.clear();

        try {
            Resource resource = resolver.getResource(CONFIG_FILE_PATH);
            List<SourceConfig> list = mapper.readValue(resource.getInputStream(), new TypeReference<List<SourceConfig>>() {
            });
            for (SourceConfig config : list) {
                CONFIGS.put(config.getId(), config);
            }
            // log.info("数据源配置:  {}", CONFIGS);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 所有书源
     */
    private static final LinkedHashMap<Integer, Source> SOURCES = new LinkedHashMap<Integer, Source>() {
        {
            put(SourceID.CHINESE81.getId(), new Source(SourceID.CHINESE81.getId(), "八一中文网", "https://www.zwdu.com/search.php?keyword=%s"));
            put(SourceID.LIEWEN.getId(), new Source(SourceID.LIEWEN.getId(), "猎文网", "https://www.liewen.cc/search.php?keyword=%s"));
            put(SourceID.ZHUISHU.getId(), new Source(SourceID.ZHUISHU.getId(), "追书网", "https://www.mangg.net/search.aspx?keyword=%s"));
            put(SourceID.BIQUG.getId(), new Source(SourceID.BIQUG.getId(), "新笔趣阁", "https://www.xbiquge6.com/search.php?keyword=%s"));
            put(SourceID.WENXUEMI.getId(), new Source(SourceID.WENXUEMI.getId(), "文学迷", "http://www.wenxuemi.com/search.php?keyword=%s"));
            put(SourceID.CHINESEXIAOSHUO.getId(), new Source(SourceID.CHINESEXIAOSHUO.getId(), "小说中文网", "https://so.biqusoso.com/s.php?ie=utf-8&siteid=xszww.com&q=%s"));
            put(SourceID.DINGDIAN.getId(), new Source(SourceID.DINGDIAN.getId(), "顶点小说", "https://www.booktxt.com/search.php?keyword=%s"));
            put(SourceID.BIQUGER.getId(), new Source(SourceID.BIQUGER.getId(), "笔趣阁2", "https://www.biquge.com.cn/search.php?keyword=%s"));
            put(SourceID.CHINESEZHUOBI.getId(), new Source(SourceID.CHINESEZHUOBI.getId(), "着笔中文网", "https://so.biqusoso.com/s.php?ie=gbk&siteid=zbzw.la&q=%s"));
            put(SourceID.DASHUBAO.getId(), new Source(SourceID.DASHUBAO.getId(), "笔趣阁3", "https://www.biduo.cc/search.php?keyword=%s"));
//            put(SourceID.CHINESEWUZHOU.getId(), new Source(SourceID.CHINESEWUZHOU.getId(), "梧州中文台", "http://www.gxwztv.com/search.htm?keyword=%s"));
            put(SourceID.UCSHUMENG.getId(), new Source(SourceID.UCSHUMENG.getId(), "UC书盟", "http://www.uctxt.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.QUANXIAOSHUO.getId(), new Source(SourceID.QUANXIAOSHUO.getId(), "全小说", "http://qxs.la/s_%s"));
//            put(SourceID.YANMOXUAN.getId(), new Source(SourceID.YANMOXUAN.getId(), "衍墨轩", "http://www.ymoxuan.com/search.htm?keyword=%s"));
            put(SourceID.AIQIWENXUE.getId(), new Source(SourceID.AIQIWENXUE.getId(), "爱奇文学", "http://m.i7wx.com/?m=book/search&keyword=%s"));
            put(SourceID.QIANQIANXIAOSHUO.getId(), new Source(SourceID.QIANQIANXIAOSHUO.getId(), "千千小说", "http://www.xqqxs.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.PIAOTIANWENXUE.getId(), new Source(SourceID.PIAOTIANWENXUE.getId(), "飘天文学网", "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%s"));
            put(SourceID.SUIMENGXIAOSHUO.getId(), new Source(SourceID.SUIMENGXIAOSHUO.getId(), "随梦小说网", "http://m.suimeng.la/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.DAJIADUSHUYUAN.getId(), new Source(SourceID.DAJIADUSHUYUAN.getId(), "大家读书苑", "http://www.dajiadu.net/modules/article/searchab.php?searchkey=%s"));
            put(SourceID.SHUQIBA.getId(), new Source(SourceID.SHUQIBA.getId(), "书旗吧", "http://www.shuqiba.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.XIAOSHUO52.getId(), new Source(SourceID.XIAOSHUO52.getId(), "小说52", "http://m.xs52.com/search.php?searchkey=%s"));
            put(SourceID.XIAOSHUO98.getId(), new Source(SourceID.XIAOSHUO98.getId(), "小说98", "https://www.xs98.com/SearchBook.aspx?keyword=%s"));
            put(SourceID.QIQIXIAOSHUO.getId(), new Source(SourceID.QIQIXIAOSHUO.getId(), "奇奇小说", "https://www.qq717.com/search.php?keyword=%s"));


        }
    };

    /*public static Flux<SearchBook> search(@NonNull String bookName, SourceConfig source) {
        return Flux.fromIterable(CONFIGS.values()).filter(SourceConfig::isEnable).concatMap(source -> {
            log.info("数据源 Source: " + source);
            SourceConfig.Search search = source.getSearch(); // 数据搜索配置
            String url;
            try {
                url = StringUtils.isBlank(search.getCharset()) ? String.format(source.getSearchURL(), URLEncoder.encode(bookName, search.getCharset()))
                        : String.format(source.getSearchURL(), bookName);
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        });
    }*/
    private static int count = 2;

    /**
     * 获取网址中的内容
     * @param bookName
     * @param source
     * @return
     */
    private static JXDocument connect(String bookName, SourceConfig source) {
        String url = source.getSearchURL();
        try {
            SourceConfig.Search search = source.getSearch(); // 数据搜索配置
            url = StringUtils.isNotBlank(search.getCharset()) ? String.format(source.getSearchURL(), URLEncoder.encode(bookName, search.getCharset()))
                    : String.format(source.getSearchURL(), bookName);
            return new JXDocument(Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .validateTLSCertificates(false).get());
        } catch (IOException e) {
            log.error("连接异常: {} , 剩余重试次数 {} , 信息：{}", url, count, e.getMessage());
            //return count <= 0 ? null : this.connect(template, url, --count);
            return null;
        }
    }

    /**
     * 获取单个数据源查到的所有书籍
     * @param keyword 书籍查询关键词
     * @param source 数据源匹配模板
     * @return Flux<SearchBook>
     */
    private static Flux<SearchBook> getSearchBook(String keyword, SourceConfig source) {
        //log.info("数据源 Source: " + source);
        //log.info("开始爬取书籍，keyword：{}, url: {}", keyword, source.getSearchURL());
        return Mono.fromFuture(CompletableFuture.supplyAsync(() ->
            connect(keyword, source)
        )).flatMapMany(document ->
             document == null ? Mono.empty() : Flux.fromStream(
                    document.selN(source.getSearch().getXpath())
                            .stream().map(node -> createdSearchBook(node, source))
            )
        );
        /*return Mono.fromCallable(() ->
                connect(keyword, source)
        ).flatMapMany(document ->
                // 爬取到文档后进行解析
                document == null ? Mono.empty() : Flux.fromStream(
                        document.selN(source.getSearch().getXpath())
                                .stream().map(node -> createdSearchBook(node, source))
                )
        );*/

    }

    /**
     * 并行搜索所有启用的书源
     * @param bookName 想要搜索的书籍的书名
     * @return 书籍搜索模型，一旦搜索到就会返回
     */
    public static Flux<SearchBook> search(@NonNull String bookName) {
        return Flux.fromIterable(CONFIGS.values()).filter(SourceConfig::isEnable).parallel().concatMap(source ->
                getSearchBook(bookName, source)
        ).sequential();
    }

    public static SearchBook createdSearchBook(JXNode jxNode, SourceConfig source) {
        SourceConfig.Search search = source.getSearch();
        SearchBook book = new SearchBook();
        book.setCover(urlVerification(getNodeStr(jxNode, search.getCoverXpath()), source.getSearchURL())); //封面
        book.setTitle(getNodeStr(jxNode, search.getTitleXpath())); //书名
        int id = source.getId();
        String link = urlVerification(getNodeStr(jxNode, search.getLinkXpath()), source.getSearchURL());
        log.info("名称：{}， 网址：{}", source.getName() + " : " + source.getSearchURL(), link);

        if (source.getId() == SourceID.CHINESEWUZHOU.getId() ||
                source.getId() == SourceID.QIANQIANXIAOSHUO.getId() ||
                source.getId() == SourceID.PIAOTIANWENXUE.getId()) {
            link = link.substring(0, link.lastIndexOf('/') + 1);
        }

        book.setAuthor(getNodeStr(jxNode, search.getAuthorXpath())); //作者
        if (id == SourceID.CHINESEZHUOBI.getId() || id == SourceID.CHINESEXIAOSHUO.getId()) {
            book.setAuthor(book.getAuthor().replace("作者：", ""));
        }
        if (id == SourceID.YANMOXUAN.getId()) {
            book.setAuthor(book.getAuthor().replace("作品大全", ""));
        }

        SearchBook.SL slTemp = new SearchBook.SL(link, new Source(id, source.getName(), source.getSearchURL(), source.getMinKeywords()));
        slTemp.setBookId(book.getBookId());
        book.getSources().add(slTemp);

        book.setDesc(getNodeStr(jxNode, search.getDescXpath()).trim());

        if (StringUtils.isNotBlank(search.getLastChapterXpath())) {
            if (null == book.getLastChapter() || book.getLastChapter().isEmpty()) {
                book.setLastChapter(getNodeStr(jxNode, search.getLastChapterXpath()).trim());
               // log.info("最新章节:     " + book.getLastChapter());
            }
        }
        if (!TextUtils.isEmpty(link)) {//过滤无效信息
            book.setBookId((long)book.hashCode());
            return book;
        }
        return null;
    }

    public static void search(@NonNull String keyword, boolean isUserSearch, SearchCallback callback) {

        for (SourceConfig source : CONFIGS.values()) {
            int id = source.getId();
            log.info("source: " + source);
            if (!source.isEnable()) {
                log.info("跳过: " + source);
                continue;
            }
            List<JXNode> rs;
            String url;
            SourceConfig.Search search = source.getSearch();
            try {
                if (!TextUtils.isEmpty(source.getSearch().getCharset())) {
                    url = String.format(source.getSearchURL(), URLEncoder.encode(keyword, search.getCharset()));
                } else {
                    url = String.format(source.getSearchURL(), keyword);
                }
                log.info("url=   " + url);
                JXDocument jxDocument = new JXDocument(Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                        .validateTLSCertificates(false).get());
                rs = jxDocument.selN(search.getXpath());
                log.info("jxDocument: {}", jxDocument);
                log.info("rs: {}", rs);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                continue;
            }
            if (rs == null) {
                continue;
            }
            List<SearchBook> books = new ArrayList<>();
            try { // 提高容错性
                for (JXNode jxNode : rs) {
                    SearchBook book = new SearchBook();
                    book.setCover(urlVerification(getNodeStr(jxNode, search.getCoverXpath()), url)); //封面
                    book.setTitle(getNodeStr(jxNode, search.getTitleXpath())); //书名

                    String link = "";
                    if (source.getId() == SourceID.YANMOXUAN.getId()) {
                        link = getNodeStr(jxNode, search.getLinkXpath());
                        if (link.isEmpty()) {
                            break;
                        }
                        link = "https://" + link.substring(2);
                    } else {
                        link = urlVerification(getNodeStr(jxNode, search.getLinkXpath()), url);
                    }

                    if (source.getId() == SourceID.CHINESEWUZHOU.getId() ||
                            source.getId() == SourceID.QIANQIANXIAOSHUO.getId() ||
                            source.getId() == SourceID.PIAOTIANWENXUE.getId()) {
                        link = link.substring(0, link.lastIndexOf('/') + 1);
                    }

                    book.setAuthor(getNodeStr(jxNode, search.getAuthorXpath())); //作者
                    if (id == SourceID.CHINESEZHUOBI.getId() || id == SourceID.CHINESEXIAOSHUO.getId()) {
                        book.setAuthor(book.getAuthor().replace("作者：", ""));
                    }
                    if (id == SourceID.YANMOXUAN.getId()) {
                        book.setAuthor(book.getAuthor().replace("作品大全", ""));
                    }

                    SearchBook.SL slTemp = new SearchBook.SL(link, new Source(id, source.getName(), source.getSearchURL(), source.getMinKeywords()));
                    slTemp.setBookId(book.getBookId());
                    book.getSources().add(slTemp);

                    book.setDesc(getNodeStr(jxNode, search.getDescXpath()).trim());

                    if (StringUtils.isNotBlank(search.getLastChapterXpath())) {
                        if (null == book.getLastChapter() || book.getLastChapter().isEmpty()) {
                            book.setLastChapter(getNodeStr(jxNode, search.getLastChapterXpath()).trim());
                            log.info("最新章节:     " + book.getLastChapter());
                        }
                    }
                    if (!TextUtils.isEmpty(link)) {//过滤无效信息
                        book.setBookId((long)book.hashCode());
                        books.add(book);
                    }
                }
                if (callback != null) {
                    callback.onResponse(keyword, books);
                    if (isUserSearch && books.size() > 0) {
                        return;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                if (callback != null) {
                    callback.onError(e.toString());
                    return;
                }
            }
        }
        log.info(callback + "/n/n/n");
        if (callback != null) {
            callback.onFinish();
        }
    }


    public static void catalog(SearchBook.SL sl, ChapterCallback callback) {
        if (sl == null || sl.source == null || TextUtils.isEmpty(sl.link)) {
            callback.onError("sl == null || sl.source == null || TextUtils.isEmpty(sl.link)");
            return;
        }
        int sourceId = sl.source.id;
        SourceConfig config = CONFIGS.get(sourceId);
        if (config.getCatalog() == null) {
            return;
        }
        if (sourceId == SourceID.CHINESEWUZHOU.getId()) { // 梧州中文台
            int ba = sl.link.indexOf("ba");
            int shtml = sl.link.lastIndexOf(".");
            if (ba > 0 && shtml > ba) {
                String id = sl.link.substring(ba + 2, shtml);
                String front = id.substring(0, 2);
                try {
                    URI original = new URI(sl.link);
                    URI uri = new URI(original.getScheme(), original.getAuthority(), "/" + front + "/" + id + "/", null, null);
                    sl.link = uri.toString();
                } catch (URISyntaxException e) {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        } else if (sourceId == SourceID.AIQIWENXUE.getId()) { // https://m.i7wx.com/book/3787.html --> https://m.i7wx.com/3/3787/
            String id = sl.link.substring(sl.link.lastIndexOf("/") + 1, sl.link.lastIndexOf("."));
            String front = id.substring(0, 1);
            try {
                URI original = new URI(sl.link);
                URI uri = new URI(original.getScheme(), original.getAuthority(), "/" + front + "/" + id + "/", null, null);
                sl.link = uri.toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                log.error(e.getMessage(), e);
            }
        }
        List<JXNode> rs = null;
        try {
            JXDocument jxDocument = new JXDocument(Jsoup.connect(sl.link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .validateTLSCertificates(false).get());
            rs = jxDocument.selN(config.getCatalog().getXpath());
        } catch (Exception e) {
            log.error("desc catalog", e);
        }
        if (rs == null || rs.isEmpty()) {
            callback.onError("返回资源为空 请求失败" + "    " + sl.link + "    SlID: " + sl.source.id + "    " + config.getCatalog().getXpath());
            return;
        }
        List<Chapter> chapters = new ArrayList<>();
        try {
            for (JXNode jxNode : rs) {
                Chapter chapter = new Chapter();
                String link = getNodeStr(jxNode, config.getCatalog().getLinkXpath());
                if (!TextUtils.isEmpty(link)) {
                    chapter.link = urlVerification(link, sl.link);
                    chapter.title = getNodeStr(jxNode, config.getCatalog().getTitleXpath());
                }
                chapters.add(chapter);
            }
            if (callback != null) {
                callback.onResponse(chapters);
            }
        } catch (Exception e) {
            log.error("请求失败：" + sl.link, e);
            callback.onError("解析失败");
        }
    }

    public static void content(SearchBook.SL sl, String url, ContentCallback callback) {
        if (sl == null || sl.source == null || TextUtils.isEmpty(sl.link) || TextUtils.isEmpty(url)) {
            if (callback != null) {
                callback.onError("");
            }
            return;
        }
        int sourceId = sl.source.id;
        SourceConfig config = CONFIGS.get(sourceId);
        if (config.getContent() == null) {
            if (callback != null) {
                callback.onError("");
            }
            return;
        }
        try {
            String link = urlVerification(url, sl.link);
            JXDocument jxDocument = new JXDocument(Jsoup.connect(link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .validateTLSCertificates(false).get());
            String content = getNodeStr(jxDocument, config.getContent().getXpath());
            //logger.info(TAG+  link+"   "+config.content.xpath);

            // 换行
            StringBuilder builder = new StringBuilder();
            String[] lines = content.split(" ");
            for (String line : lines) {
                line = StringUtils.trim(line);
                if (!TextUtils.isEmpty(line)) {
                    builder.append("        ").append(line).append("\n");
                }
            }
            content = builder.toString();
            if (callback != null) {
                callback.onResponse(content);
            }
        } catch (Exception e) {
            //logger.error(TAG+  e.toString());
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 获取 通过xpath 查找到的字符串
     *
     * @param startNode 只有JXDocument   和  JXNode 两种
     * @param xpath
     * @return
     */
    private static String getNodeStr(Object startNode, String xpath) {
        StringBuilder rs = new StringBuilder();
        try {
            List<?> list;
            if (startNode instanceof JXDocument) {
                list = ((JXDocument) startNode).sel(xpath);
            } else if (startNode instanceof JXNode) {
                list = ((JXNode) startNode).sel(xpath);
            } else {
                return "";
            }

            for (Object node : list) {
                rs.append(node.toString());
            }

        } catch (XpathSyntaxErrorException e) {
            log.error(e.getMessage(), e);
        }
        return rs.toString();
    }

    private static String urlVerification(String link, String linkWithHost) {
        if (StringUtils.isBlank(link)) {
            return link;
        }
        try {
            if (link.startsWith("/")) {
                URI original = new URI(linkWithHost);
                //original.getHost()
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (link.startsWith("/")) {
                URI original = new URI(linkWithHost);
                URI uri = new URI(original.getScheme(), original.getAuthority(), link, null);
                link = uri.toString();
            } else if (!link.startsWith("http://") && !link.startsWith("https://")) {
                if (linkWithHost.endsWith("html") || linkWithHost.endsWith("htm")) {
                    linkWithHost = linkWithHost.substring(0, linkWithHost.lastIndexOf("/") + 1);
                } else if (!linkWithHost.endsWith("/")) {
                    linkWithHost = linkWithHost + "/";
                }
                link = linkWithHost + link;
            }
            return link;
        } catch (URISyntaxException e) {
            return link;
        }
    }

    public static void main(String [] args) throws Exception {
        String link = "/182377/";
        String linkWithHost = "http://qxs.la/s_";
        if (!link.startsWith("http")) {
            URL url = new URL(linkWithHost);
            if (link.startsWith("//")) {
                System.out.println(url.getProtocol() + ":"+ url.getHost() + link);
            } else if (link.startsWith("/")) {
                System.out.println(url.getProtocol() + "://"+ url.getHost() + link);
            }
        } else {
            System.out.println(link);
        }
        URI url = new URI(link);
        System.out.println(url.getHost());
        System.out.println(url.getPath());
        System.out.println(urlVerification(link, linkWithHost));
        Random random = new Random();
        /*List<String> list = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7"));
        Flux.fromIterable(list).parallel().concatMap(e -> {
            return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                try {
                   // Thread.sleep(random.nextInt(100) * 30);
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                return e ;
            }));

        }).sequential().subscribe(System.out::println);*/

       // search("逆天邪神").distinct(SearchBook::getBookId).subscribe(System.out::println);

        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //search("逆天邪神").distinct().subscribe(System.out::println);

        //search("逆天邪神").distinct(SearchBook::getBookId).subscribe();
        //Thread.sleep(10000);
        /*CompletableFuture completableFuture = new CompletableFuture<>();

        Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "233";
        })).subscribe(System.out::println);


        CompletableFuture completableFuture2 = new CompletableFuture<>();

        Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            return "123";
        })).subscribe(System.out::println);
        */
        //completableFuture.complete("21321");
        //completableFuture2.complete("666");
      //  System.out.println(completableFuture2.get());
      //  System.out.println(completableFuture.get());

    }
}
