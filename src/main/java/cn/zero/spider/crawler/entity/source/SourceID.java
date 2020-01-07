package cn.zero.spider.crawler.entity.source;

//import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public enum SourceID{
    CHINESE81(1),
    LIEWEN(2)  ,
    ZHUISHU(3),
       BIQUG(4),
       WENXUEMI(5),
       CHINESEXIAOSHUO(6),
        DINGDIAN(7),
        BIQUGER(8),
        CHINESEZHUOBI(9),
       DASHUBAO(10),
        CHINESEWUZHOU(11),
      UCSHUMENG(12),
     QUANXIAOSHUO(13),
       YANMOXUAN(14),
       AIQIWENXUE(15),
       QIANQIANXIAOSHUO(16),
       PIAOTIANWENXUE(17),
   SUIMENGXIAOSHUO(18),
       DAJIADUSHUYUAN(19),
       SHUQIBA(20),
    XIAOSHUO52(21),
    XIAOSHUO98(22),
    QIQIXIAOSHUO(23);



    public int getId() {
        return id;
    }

    private int id;
    SourceID(int id) {
        this.id = id;
    }

    //    class  SourceIDNum {
//
//        /**
//         * 猎文网
//         */
//       int LIEWEN = 1;
//
//        /**
//         * 81中文网
//         */
//        int CHINESE81 = 2;
//
//        /**
//         * 追书网
//         */
//        int ZHUISHU = 3;
//
//        /**
//         * 笔趣阁
//         */
//        int BIQUG = 4;
//
//        /**
//         * 文学迷
//         */
//        int WENXUEMI = 5;
//
//        /**
//         * 小说中文网
//         */
//        int CHINESEXIAOSHUO = 6;
//
//        /**
//         * 顶点小说
//         */
//        int DINGDIAN = 7;
//
//        /**
//         * 笔趣阁儿
//         */
//        int BIQUGER = 8;
//
//        /**
//         * 着笔中文网
//         */
//        int CHINESEZHUOBI = 9;
//
//        /**
//         * 大书包
//         */
//        int DASHUBAO = 10;
//
//        /**
//         * 梧州中文台
//         */
//        int CHINESEWUZHOU = 11;
//
//        /**
//         * UC书盟
//         */
//        int UCSHUMENG = 12;
//
//        /**
//         * 全小说
//         */
//        int QUANXIAOSHUO = 13;
//
//        /**
//         * 衍墨轩
//         */
//        int YANMOXUAN = 14;
//
//        /**
//         * 爱奇文学
//         */
//        int AIQIWENXUE = 15;
//
//        /**
//         * 千千小说
//         */
//        int QIANQIANXIAOSHUO = 16;
//
//        /**
//         * 飘天文学网
//         */
//        int PIAOTIANWENXUE = 17;
//
//        /**
//         * 随梦小说网
//         */
//        int SUIMENGXIAOSHUO = 18;
//
//        /**
//         * 大家读书苑
//         */
//        int DAJIADUSHUYUAN = 19;
//
//        /**
//         * 书旗吧
//         */
//        int SHUQIBA = 20;
//
//        /**
//         * 小说52
//         */
//        int XIAOSHUO52 = 21;
//    }
}

