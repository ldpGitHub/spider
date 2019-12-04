package cn.zero.spider.crawler;



import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.chapter.Chapter;
import cn.zero.spider.crawler.entity.source.Source;
import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.entity.source.SourceEnable;
import cn.zero.spider.crawler.entity.source.SourceID;


import cn.zero.spider.crawler.source.callback.ChapterCallback;
import cn.zero.spider.crawler.source.callback.ContentCallback;
import cn.zero.spider.crawler.source.callback.SearchCallback;
import cn.zero.spider.crawler.xpath.exception.XpathSyntaxErrorException;
import cn.zero.spider.crawler.xpath.model.JXDocument;
import cn.zero.spider.crawler.xpath.model.JXNode;
import cn.zero.spider.webmagic.page.BiQuGeIndexPageProcessor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.http.util.TextUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 爬虫
 * <p>
 * Created by yuyuhang on 2018/1/8.
 */
public class Crawler {

    private static final String TAG = Crawler.class.getSimpleName();
    private static  Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static final LinkedHashMap<Integer,SourceConfig> CONFIGS;


    static {
        CONFIGS = new LinkedHashMap<Integer,SourceConfig>(1);
        init();
    }

    private static   void init() {
        CONFIGS.clear();

        // 默认放于assets或者raw下
//        String json = AssetsUtils.readAssetsTxt(Global.getApplication(), "Template.json");
        File jsonFile = null;
        String json="";


        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream stream = Crawler.class.getClassLoader().getResourceAsStream("Template.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        json = stringBuffer.toString();

//        try {
//            jsonFile = ResourceUtils.getFile("classpath:Template.json");
//            json = FileUtils.readFileToString(jsonFile);
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//       logger.error("解析json"+json);
        List<SourceConfig> list = new Gson().fromJson(json,
                new TypeToken<List<SourceConfig>>() {
                }.getType());

        for (SourceConfig config : list) {
            CONFIGS.put(config.id, config);
//            logger.error(TAG+"  config:  "+config);

        }
//        logger.error(TAG+"CONFIGS:  "+CONFIGS.size());

    }

    private static String defaultJson = "";
    private static LinkedHashMap getSourceEnableSparseArray() {


        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream stream = Crawler.class.getClassLoader().getResourceAsStream("Template.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        defaultJson = stringBuffer.toString();

//        try {
//            defaultJson = FileUtils.readFileToString(ResourceUtils.getFile("classpath:Template.json"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        String json = SPUtils.getInstance().getString("source_setting_list",
//                defaultJson);
        String json = defaultJson;


        List<SourceEnable> enables = new Gson().fromJson(json,
                new TypeToken<List<SourceEnable>>() {
                }.getType());

        LinkedHashMap checkedMap = new LinkedHashMap();
        for (SourceEnable sourceEnable : enables) {
            checkedMap.put(sourceEnable.id, sourceEnable.enable);
        }
        return checkedMap;
    }


    /**
     * 所有书源
     */
    private static final LinkedHashMap<Integer,Source> SOURCES = new LinkedHashMap<Integer,Source>() {
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
            put(SourceID.CHINESEWUZHOU.getId(), new Source(SourceID.CHINESEWUZHOU.getId(), "梧州中文台", "http://www.gxwztv.com/search.htm?keyword=%s"));
            put(SourceID.UCSHUMENG.getId(), new Source(SourceID.UCSHUMENG.getId(), "UC书盟", "http://www.uctxt.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.QUANXIAOSHUO.getId(), new Source(SourceID.QUANXIAOSHUO.getId(), "全小说", "http://qxs.la/s_%s"));
            put(SourceID.YANMOXUAN.getId(), new Source(SourceID.YANMOXUAN.getId(), "衍墨轩", "http://www.ymoxuan.com/search.htm?keyword=%s"));
            put(SourceID.AIQIWENXUE.getId(), new Source(SourceID.AIQIWENXUE.getId(), "爱奇文学", "http://m.i7wx.com/?m=book/search&keyword=%s"));
            put(SourceID.QIANQIANXIAOSHUO.getId(), new Source(SourceID.QIANQIANXIAOSHUO.getId(), "千千小说", "http://www.xqqxs.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.PIAOTIANWENXUE.getId(), new Source(SourceID.PIAOTIANWENXUE.getId(), "飘天文学网", "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%s"));
            put(SourceID.SUIMENGXIAOSHUO.getId(), new Source(SourceID.SUIMENGXIAOSHUO.getId(), "随梦小说网", "http://m.suimeng.la/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.DAJIADUSHUYUAN.getId(), new Source(SourceID.DAJIADUSHUYUAN.getId(), "大家读书苑", "http://www.dajiadu.net/modules/article/searchab.php?searchkey=%s"));
            put(SourceID.SHUQIBA.getId(), new Source(SourceID.SHUQIBA.getId(), "书旗吧", "http://www.shuqiba.com/modules/article/search.php?searchkey=%s", 4));
            put(SourceID.XIAOSHUO52.getId(), new Source(SourceID.XIAOSHUO52.getId(), "小说52", "http://m.xs52.com/search.php?searchkey=%s"));
        }
    };



    public static void search(@NonNull String keyword ,boolean isUserSearch,SearchCallback callback) {
        LinkedHashMap checkedMap = getSourceEnableSparseArray();
        Iterator iter =CONFIGS.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Integer id = (Integer) entry.getKey();
            SourceConfig config= (SourceConfig) entry.getValue();
            Source source =SOURCES.get(id);
            logger.info(TAG+ "id" +id);
            logger.info(TAG+ "config" +config);
            logger.info(TAG+ "source" +source);
            if (null!=checkedMap.get(id)) {
                logger.info(TAG+ "跳过"+checkedMap.get(id) );
                continue;
            }
            List<JXNode> rs;
            String url;
            try {
                if (!TextUtils.isEmpty(config.search.charset)) {
                    url = String.format(source.searchURL, URLEncoder.encode(keyword, config.search.charset));
                    logger.info(TAG+"url   "+url );
                } else {
                    url = String.format(source.searchURL, keyword);
                    logger.info(TAG+"url   "+url );
                }
                logger.info(TAG, "url=   " + url);
                JXDocument jxDocument = new JXDocument(Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                        .validateTLSCertificates(false).get());
                rs = jxDocument.selN(config.search.xpath);
                logger.info(TAG+"jxDocument"+jxDocument );
                logger.info(TAG+"rs"+rs );
            } catch (Exception e) {
                logger.error(TAG+ "Exception=" + e.toString());
                continue;
            }
            if (rs == null) {
                continue;
            }
            List<SearchBook> books = new ArrayList<>();
            try { // 提高容错性
                for (JXNode jxNode : rs) {
                    SearchBook book = new SearchBook();
                    book.cover = urlVerification(getNodeStr(jxNode, config.search.coverXpath), url);
                    logger.info(TAG+ "url=" +"cover=" + book.cover);
                    book.title = getNodeStr(jxNode, config.search.titleXpath);
                    logger.info(TAG+ "title=" + book.title);
                    logger.info(TAG+ "原始link=   " + getNodeStr(jxNode, config.search.linkXpath));
                    String link ="" ;
                    if (source.id == SourceID.YANMOXUAN .getId()){
                         link =getNodeStr(jxNode, config.search.linkXpath);
                        if (link.isEmpty()){
                            break;
                        }
                        link = link.substring(2);
                        link = "https://"+link;

                    }else {
                        link = urlVerification(getNodeStr(jxNode, config.search.linkXpath), url);
                    }
                    if (source.id == SourceID.CHINESEWUZHOU .getId()||
                            source.id == SourceID.QIANQIANXIAOSHUO .getId()||
                            source.id == SourceID.PIAOTIANWENXUE.getId()) {
                        link = link.substring(0, link.lastIndexOf('/') + 1);
                    }
                    logger.info(TAG+ "处理后link=   " + link);
                    book.author = getNodeStr(jxNode, config.search.authorXpath);
                    if (source.id == SourceID.CHINESEZHUOBI.getId() || source.id == SourceID.CHINESEXIAOSHUO.getId()) {
                        book.author = book.author.replace("作者：", "");
                    }
                    if (source.id == SourceID.YANMOXUAN.getId()) {
                        book.author = book.author.replace("作品大全", "");
                    }
                    SearchBook.SL slTemp = new SearchBook.SL(link, source);
                    slTemp.setBookId(book.getBookId());
                    book.sources.add(slTemp);
                    logger.info(TAG+  "author=" + book.author);
                    book.desc = getNodeStr(jxNode, config.search.descXpath).trim();
                 if (null!=config.search.getLastChapterXpath()&&!config.search.getLastChapterXpath().isEmpty()){
                     if(null==book.getLastChapter()||book.getLastChapter().isEmpty()){
                         book.setLastChapter(getNodeStr(jxNode, config.search.lastChapterXpath).trim());
                         logger.info("最新章节:     "+book.getLastChapter());
                     }
                 }
                    logger.info(TAG+  "desc=" + book.desc );
                    if (!TextUtils.isEmpty(link)) {//过滤无效信息
                        books.add(book);
                    }
                }
                if (callback != null) {
                    callback.onResponse(keyword, books);
                    if(isUserSearch && books.size()>0){
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error(TAG+  e.toString());
                if (callback != null) {
                    callback.onError(e.toString());
                    return;
                }
            }
        }
        logger.info(TAG+callback+"/n/n/n");
        if (callback != null) {
            callback.onFinish();
        }
    }


    public static void catalog(SearchBook.SL sl, ChapterCallback callback) {
        if (sl == null || sl.source == null || TextUtils.isEmpty(sl.link)) {
            callback.onError("");
            return;
        }
        int sourceId = sl.source.id;
        SourceConfig config = CONFIGS.get(sourceId);
        if (config.catalog == null) {
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
                    logger.error(TAG+  "URISyntaxException" +e.toString());
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
                logger.error(TAG+  "错误" +e.toString());
            }
        }
        List<JXNode> rs = null;
        try {
            JXDocument jxDocument = new JXDocument(Jsoup.connect(sl.link)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .validateTLSCertificates(false).get());
            rs = jxDocument.selN(config.catalog.xpath);
        } catch (Exception e) {
            logger.error(TAG+ "desc catalog =" +e.toString() +"   ");
        }
        if (rs == null || rs.isEmpty()) {
            callback.onError("返回资源为空 请求失败"  + "    "+sl.link);
            return;
        }
        List<Chapter> chapters = new ArrayList<>();
        try {
            for (JXNode jxNode : rs) {
                Chapter chapter = new Chapter();
                String link = getNodeStr(jxNode, config.catalog.linkXpath);
                if (!TextUtils.isEmpty(link)) {
                    chapter.link = urlVerification(link, sl.link);
                    chapter.title = getNodeStr(jxNode, config.catalog.titleXpath);
                }
                chapters.add(chapter);
            }
            if (callback != null) {
                callback.onResponse(chapters);
            }
        } catch (Exception e) {
            logger.error(TAG+ "请求失败" + e.toString());
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
        SourceConfig config =CONFIGS.get(sourceId);
        if (config.content == null) {
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
            String content = getNodeStr(jxDocument, config.content.xpath);
            logger.info(TAG+  link+"   "+config.content.xpath);

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
            logger.error(TAG+  e.toString());

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
            logger.error(TAG+"XpathSyntaxErrorException "+e.toString());
        }
        return rs.toString();
    }

    private static String urlVerification(String link, String linkWithHost) throws URISyntaxException {
        if (TextUtils.isEmpty(link)) {
            return link;
        }
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
    }

    public static void main(String args[]) {

    }
}
