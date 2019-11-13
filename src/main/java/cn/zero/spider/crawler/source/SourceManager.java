package cn.zero.spider.crawler.source;


import cn.zero.spider.crawler.entity.source.Source;
import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.entity.source.SourceEnable;
import cn.zero.spider.crawler.entity.source.SourceID;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * 书源管理
 * <p>
 * Created by yuyuhang on 2018/1/7.
 */
public class SourceManager {

    static {
        CONFIGS = new LinkedHashMap<SourceID,SourceConfig>(1);
        SourceManager.init();
    }

//    /**
//     * 所有书源
//     */
//    public static final LinkedHashMap<Integer,Source> SOURCES = new LinkedHashMap<SourceID,Source>() {
//        {
////            put(SourceID.LIEWEN, new Source(SourceID.LIEWEN, "猎文网", "https://www.liewen.cc/search.php?keyword=%s"));
////            put(SourceID.CHINESE81, new Source(SourceID.CHINESE81, "八一中文网", "https://www.zwdu.com/search.php?keyword=%s"));
////            put(SourceID.ZHUISHU, new Source(SourceID.ZHUISHU, "追书网", "https://www.zhuishu.tw/search.aspx?keyword=%s"));
////            put(SourceID.BIQUG, new Source(SourceID.BIQUG, "笔趣阁", "http://zhannei.baidu.com/cse/search?s=1393206249994657467&q=%s"));
////            put(SourceID.WENXUEMI, new Source(SourceID.WENXUEMI, "文学迷", "http://www.wenxuemi.com/search.php?keyword=%s"));
////            put(SourceID.CHINESEXIAOSHUO, new Source(SourceID.CHINESEXIAOSHUO, "小说中文网", "http://www.xszww.com/s.php?ie=gbk&s=10385337132858012269&q=%s"));
////            put(SourceID.DINGDIAN, new Source(SourceID.DINGDIAN, "顶点小说", "http://zhannei.baidu.com/cse/search?s=1682272515249779940&q=%s"));
////            put(SourceID.BIQUGER, new Source(SourceID.BIQUGER, "笔趣阁2", "http://zhannei.baidu.com/cse/search?s=7928441616248544648&ie=utf-8&q=%s"));
////            put(SourceID.CHINESEZHUOBI, new Source(SourceID.CHINESEZHUOBI, "着笔中文网", "http://www.zbzw.com/s.php?ie=utf-8&s=4619765769851182557&q=%s"));
////            put(SourceID.DASHUBAO, new Source(SourceID.DASHUBAO, "大书包", "http://zn.dashubao.net/cse/search?s=9410583021346449776&entry=1&ie=utf-8&q=%s"));
////            put(SourceID.CHINESEWUZHOU, new Source(SourceID.CHINESEWUZHOU, "梧州中文台", "http://www.gxwztv.com/search.htm?keyword=%s"));
////            put(SourceID.UCSHUMENG, new Source(SourceID.UCSHUMENG, "UC书盟", "http://www.uctxt.com/modules/article/search.php?searchkey=%s", 4));
////            put(SourceID.QUANXIAOSHUO, new Source(SourceID.QUANXIAOSHUO, "全小说", "http://qxs.la/s_%s"));
////            put(SourceID.YANMOXUAN, new Source(SourceID.YANMOXUAN, "衍墨轩", "http://www.ymoxuan.com/search.htm?keyword=%s"));
////            put(SourceID.AIQIWENXUE, new Source(SourceID.AIQIWENXUE, "爱奇文学", "http://m.i7wx.com/?m=book/search&keyword=%s"));
////            put(SourceID.QIANQIANXIAOSHUO, new Source(SourceID.QIANQIANXIAOSHUO, "千千小说", "http://www.xqqxs.com/modules/article/search.php?searchkey=%s", 4));
////            put(SourceID.PIAOTIANWENXUE, new Source(SourceID.PIAOTIANWENXUE, "飘天文学网", "http://www.piaotian.com/modules/article/search.php?searchtype=articlename&searchkey=%s"));
////            put(SourceID.SUIMENGXIAOSHUO, new Source(SourceID.SUIMENGXIAOSHUO, "随梦小说网", "http://m.suimeng.la/modules/article/search.php?searchkey=%s", 4));
////            put(SourceID.DAJIADUSHUYUAN, new Source(SourceID.DAJIADUSHUYUAN, "大家读书苑", "http://www.dajiadu.net/modules/article/searchab.php?searchkey=%s"));
////            put(SourceID.SHUQIBA, new Source(SourceID.SHUQIBA, "书旗吧", "http://www.shuqiba.com/modules/article/search.php?searchkey=%s", 4));
////            put(SourceID.XIAOSHUO52, new Source(SourceID.XIAOSHUO52, "小说52", "http://m.xs52.com/search.php?searchkey=%s"));
//        }
//    };

    public static final LinkedHashMap<SourceID,SourceConfig> CONFIGS;

    public static void init() {
        CONFIGS.clear();

        // 默认放于assets或者raw下
//        String json = AssetsUtils.readAssetsTxt(Global.getApplication(), "Template.json");
        File jsonFile = null;
        String json="";
        try {
            jsonFile = ResourceUtils.getFile("classpath:Template.json.json");
             json = FileUtils.readFileToString(jsonFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<SourceConfig> list = new Gson().fromJson(json,
                new TypeToken<List<SourceConfig>>() {
                }.getType());

        for (SourceConfig config : list) {
//            CONFIGS.put(config.id, config);
        }
    }

    public static void saveSourceEnable(LinkedHashMap booleanArray) {
        if (booleanArray != null) {
            List<SourceEnable> list = new ArrayList<>();
//            for (int i = 0; i < booleanArray.size(); i++) {
//                int key = booleanArray.keyAt(i)
//                int key = booleanArray.
//.
//                boolean value = booleanArray.valueAt(i);
//                SourceEnable sourceEnable = new SourceEnable(key, value);
//                list.add(sourceEnable);
//            }
            Iterator iter = booleanArray.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                int key = (int) entry.getKey();
                boolean value = (boolean) entry.getValue();
            }
//            SPUtils.getInstance().put("source_setting_list", new Gson().toJson(list));
            /**
             *
             *
             *
             * 待处理
             *
             *
             */
        }

    }

    private  String defaultJson = "";
    public  LinkedHashMap getSourceEnableSparseArray() {

        try {
            defaultJson = FileUtils.readFileToString(ResourceUtils.getFile("classpath:Template.json.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}
