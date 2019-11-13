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

    private static final String TAG = "qy.Crawler";
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
            logger.error(TAG+"  config:  "+config);

        }
        logger.error(TAG+"CONFIGS:  "+CONFIGS.size());

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
            put(SourceID.LIEWEN.getId(), new Source(SourceID.LIEWEN.getId(), "猎文网", "https://www.liewen.cc/search.php?keyword=%s"));
            put(SourceID.CHINESE81.getId(), new Source(SourceID.CHINESE81.getId(), "八一中文网", "https://www.zwdu.com/search.php?keyword=%s"));
            put(SourceID.ZHUISHU.getId(), new Source(SourceID.ZHUISHU.getId(), "追书网", "https://www.zhuishu.tw/search.aspx?keyword=%s"));
            put(SourceID.BIQUG.getId(), new Source(SourceID.BIQUG.getId(), "笔趣阁", "http://zhannei.baidu.com/cse/search?s=1393206249994657467&q=%s"));
            put(SourceID.WENXUEMI.getId(), new Source(SourceID.WENXUEMI.getId(), "文学迷", "http://www.wenxuemi.com/search.php?keyword=%s"));
            put(SourceID.CHINESEXIAOSHUO.getId(), new Source(SourceID.CHINESEXIAOSHUO.getId(), "小说中文网", "http://www.xszww.com/s.php?ie=gbk&s=10385337132858012269&q=%s"));
            put(SourceID.DINGDIAN.getId(), new Source(SourceID.DINGDIAN.getId(), "顶点小说", "http://zhannei.baidu.com/cse/search?s=1682272515249779940&q=%s"));
            put(SourceID.BIQUGER.getId(), new Source(SourceID.BIQUGER.getId(), "笔趣阁2", "http://zhannei.baidu.com/cse/search?s=7928441616248544648&ie=utf-8&q=%s"));
            put(SourceID.CHINESEZHUOBI.getId(), new Source(SourceID.CHINESEZHUOBI.getId(), "着笔中文网", "http://www.zbzw.com/s.php?ie=utf-8&s=4619765769851182557&q=%s"));
            put(SourceID.DASHUBAO.getId(), new Source(SourceID.DASHUBAO.getId(), "大书包", "http://zn.dashubao.net/cse/search?s=9410583021346449776&entry=1&ie=utf-8&q=%s"));
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



    public static void search(@NonNull String keyword, SearchCallback callback) {

        LinkedHashMap checkedMap = getSourceEnableSparseArray();
        logger.info(TAG +"checkedMap:  "+checkedMap );
        logger.info(TAG +"CONFIGS:  "+CONFIGS.size());

        Iterator iter =CONFIGS.entrySet().iterator();
        logger.info(TAG+"iter"+iter );

        while (iter.hasNext()) {
            logger.info(TAG+ "iter"+iter );

            Map.Entry entry = (Map.Entry) iter.next();
            Integer id = (Integer) entry.getKey();
            SourceConfig config= (SourceConfig) entry.getValue();
            Source source =SOURCES.get(id);
            logger.info(TAG+ "id" +id);
            logger.info(TAG+ "config" +config);
            logger.info(TAG+ "source" +source);

//            logger.info(TAG+ "跳过"+!(boolean)checkedMap.get(id) );

            if (null!=checkedMap.get(id)) {
                logger.info(TAG+ "跳过"+checkedMap.get(id) );

                continue;
            }




//        for (int i = 0; i < SourceManager.CONFIGS.size(); i++) {
//            int id = SourceManager.CONFIGS.keyAt(i);
//            SourceConfig config = SourceManager.CONFIGS.valueAt(i);
//            Source source = SourceManager.SOURCES.get(id);
//            if (!checkedMap.get(id)) {
//                continue;
//            }

            List<JXNode> rs;
            String url;

            try {
                if (!TextUtils.isEmpty(config.search.charset)) {
                    url = String.format(source.searchURL, URLEncoder.encode(keyword, config.search.charset));
                    logger.info(TAG+"url"+url );

                } else {
                    url = String.format(source.searchURL, keyword);
                    logger.info(TAG+"url"+url );


                }
                logger.info(TAG, "url=" + url);
                JXDocument jxDocument = new JXDocument(Jsoup.connect(url).validateTLSCertificates(false).get());
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
                    logger.error(TAG+ "url=" +"cover=" + book.cover);
                    book.title = getNodeStr(jxNode, config.search.titleXpath);
                    logger.error(TAG+ "title=" + book.title);
                    String link = urlVerification(getNodeStr(jxNode, config.search.linkXpath), url);
                    if (source.id == SourceID.CHINESEWUZHOU .getId()||
                            source.id == SourceID.YANMOXUAN .getId()||
                            source.id == SourceID.QIANQIANXIAOSHUO .getId()||
                            source.id == SourceID.PIAOTIANWENXUE.getId()) {
                        link = link.substring(0, link.lastIndexOf('/') + 1);
                    }
                    logger.error(TAG+ "link=" + link);
                    book.sources.add(new SearchBook.SL(link, source));
                    book.author = getNodeStr(jxNode, config.search.authorXpath);
                    if (source.id == SourceID.CHINESEZHUOBI.getId() || source.id == SourceID.CHINESEXIAOSHUO.getId()) {
                        book.author = book.author.replace("作者：", "");
                    }
                    logger.error(TAG+  "author=" + book.author);
                    book.desc = getNodeStr(jxNode, config.search.descXpath).trim();
                    logger.error(TAG+  "desc=" + book.desc);
                    if (!TextUtils.isEmpty(link)) {//过滤无效信息
                        books.add(book);
                    }
                }
                if (callback != null) {
                    callback.onResponse(keyword, books);
                }
            } catch (Exception e) {
                logger.error(TAG+  e.toString());

                if (callback != null) {
                    callback.onError(e.toString());
                    return;
                }
            }
        }
        logger.info(TAG, callback);

        if (callback != null) {
            callback.onFinish();
        }
    }

    /**
     * 用户搜索需要尽快返回
     */

    public static void userSearch(@NonNull String keyword, SearchCallback callback) {

        LinkedHashMap checkedMap = getSourceEnableSparseArray();
        logger.info(TAG +"checkedMap:  "+checkedMap );
        logger.info(TAG +"CONFIGS:  "+CONFIGS.size());

        Iterator iter =CONFIGS.entrySet().iterator();
        logger.info(TAG+"iter"+iter );

        while (iter.hasNext()) {
            logger.info(TAG+ "iter"+iter );

            Map.Entry entry = (Map.Entry) iter.next();
            Integer id = (Integer) entry.getKey();
            SourceConfig config= (SourceConfig) entry.getValue();
            Source source =SOURCES.get(id);
            logger.info(TAG+ "id" +id);
            logger.info(TAG+ "config" +config);
            logger.info(TAG+ "source" +source);

//            logger.info(TAG+ "跳过"+!(boolean)checkedMap.get(id) );

            if (null!=checkedMap.get(id)) {
                logger.info(TAG+ "跳过"+checkedMap.get(id) );

                continue;
            }




//        for (int i = 0; i < SourceManager.CONFIGS.size(); i++) {
//            int id = SourceManager.CONFIGS.keyAt(i);
//            SourceConfig config = SourceManager.CONFIGS.valueAt(i);
//            Source source = SourceManager.SOURCES.get(id);
//            if (!checkedMap.get(id)) {
//                continue;
//            }

            List<JXNode> rs;
            String url;

            try {
                if (!TextUtils.isEmpty(config.search.charset)) {
                    url = String.format(source.searchURL, URLEncoder.encode(keyword, config.search.charset));
                    logger.info(TAG+"url"+url );

                } else {
                    url = String.format(source.searchURL, keyword);
                    logger.info(TAG+"url"+url );


                }
                logger.info(TAG, "url=" + url);
                JXDocument jxDocument = new JXDocument(Jsoup.connect(url).validateTLSCertificates(false).get());
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
                    logger.error(TAG+ "url=" +"cover=" + book.cover);
                    book.title = getNodeStr(jxNode, config.search.titleXpath);
                    logger.error(TAG+ "title=" + book.title);
                    String link = urlVerification(getNodeStr(jxNode, config.search.linkXpath), url);
                    if (source.id == SourceID.CHINESEWUZHOU .getId()||
                            source.id == SourceID.YANMOXUAN .getId()||
                            source.id == SourceID.QIANQIANXIAOSHUO .getId()||
                            source.id == SourceID.PIAOTIANWENXUE.getId()) {
                        link = link.substring(0, link.lastIndexOf('/') + 1);
                    }
                    logger.error(TAG+ "link=" + link);
                    book.sources.add(new SearchBook.SL(link, source));
                    book.author = getNodeStr(jxNode, config.search.authorXpath);
                    if (source.id == SourceID.CHINESEZHUOBI.getId() || source.id == SourceID.CHINESEXIAOSHUO.getId()) {
                        book.author = book.author.replace("作者：", "");
                    }
                    logger.error(TAG+  "author=" + book.author);
                    book.desc = getNodeStr(jxNode, config.search.descXpath).trim();
                    logger.error(TAG+  "desc=" + book.desc);
                    if (!TextUtils.isEmpty(link)) {//过滤无效信息
                        books.add(book);
                    }
                }
                if (callback != null) {
                    callback.onResponse(keyword, books);
                    if(books.size()>0){
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
        logger.info(TAG, callback);

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
        Source source = SOURCES.get(sourceId);

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
            }
        }

        List<JXNode> rs = null;
        try {
            JXDocument jxDocument = new JXDocument(Jsoup.connect(sl.link).validateTLSCertificates(false).get());
            rs = jxDocument.selN(config.catalog.xpath);
        } catch (Exception e) {
//            Log.e(TAG, e.toString());
            logger.error(TAG,  "desc=" +e.toString());

        }

        if (rs == null || rs.isEmpty()) {
            callback.onError("请求失败");
            return;
        }

        List<Chapter> chapters = new ArrayList<>();
        try {
            for (JXNode jxNode : rs) {
                Chapter chapter = new Chapter();

                String link = getNodeStr(jxNode, config.catalog.linkXpath);
                if (!TextUtils.isEmpty(link)) {
                    chapter.link = urlVerification(link, sl.link);
//                    Log.i(TAG, "link=" + chapter.link);
                    logger.error(TAG,  "link=" + chapter.link);

                    chapter.title = getNodeStr(jxNode, config.catalog.titleXpath);
//                    Log.i(TAG, "title=" + chapter.title);
                }

                chapters.add(chapter);
            }

            if (callback != null) {
                callback.onResponse(chapters);
            }
        } catch (Exception e) {
//            Log.e(TAG, e.toString());
            logger.error(TAG,   e.toString());

            callback.onError("请求失败");
        }
    }

    public static void content(SearchBook.SL sl, String url, ContentCallback callback) {
//        Log.i(TAG, "content  url=" + url);
        logger.error(TAG,   "content  url=" + url);

        if (sl == null || sl.source == null || TextUtils.isEmpty(sl.link) || TextUtils.isEmpty(url)) {
            if (callback != null) {
                callback.onError("");
            }
            return;
        }
        int sourceId = sl.source.id;
        SourceConfig config =CONFIGS.get(sourceId);
        Source source =SOURCES.get(sourceId);

        if (config.content == null) {
            if (callback != null) {
                callback.onError("");
            }
            return;
        }

        try {
            String link = urlVerification(url, sl.link);
//            Log.i(TAG, "link =   " + link);
            JXDocument jxDocument = new JXDocument(Jsoup.connect(link).validateTLSCertificates(false).get());

            String content = getNodeStr(jxDocument, config.content.xpath);

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
//            Log.i(TAG, "content =" + content);
            logger.error(TAG,    "content =" + content);

            if (callback != null) {
                callback.onResponse(content);
            }
        } catch (Exception e) {
//            Log.e(TAG, e.toString());
            logger.error(TAG,   e.toString());

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
//            Log.e(TAG, e.toString());
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
