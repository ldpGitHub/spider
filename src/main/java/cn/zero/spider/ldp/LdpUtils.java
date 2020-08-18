package cn.zero.spider.ldp;

import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.entity.source.SourceID;
import cn.zero.spider.crawler.xpath.exception.XpathSyntaxErrorException;
import cn.zero.spider.crawler.xpath.model.JXDocument;
import cn.zero.spider.crawler.xpath.model.JXNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ycj
 * @datetime 2020-6-14 17:11
 * @describe
 */
@Slf4j
public class LdpUtils {

    /**
     * 获取作者信息
     * @param id 数据源id
     * @param node jxNode
     * @param xpath xpath
     * @return 作者
     */
    static String getAuthor(int id, JXNode node, String xpath) {
        String author = getNodeStr(node, xpath);
        if (id == SourceID.CHINESEZHUOBI.getId() || id == SourceID.CHINESEXIAOSHUO.getId()) {
            return author.replace("作者：", "");
        }
        if (id == SourceID.YANMOXUAN.getId()) {
            return author.replace("作品大全", "");
        }
        return author;
    }

    static String getLink(SourceConfig source, String url, JXNode node) {
        // 获取链接，链接不存在则代表不需要搜索
        String link;
        if (source.getId() == SourceID.YANMOXUAN.getId()) {
            link = getNodeStr(node, source.getSearch().getLinkXpath());
            if (StringUtils.isBlank(link)) {
                return null;
            }
            link = "https://" + link.substring(2);
        } else {
            link = urlVerification(getNodeStr(node, source.getSearch().getLinkXpath()), url);
        }

        if (source.getId() == SourceID.CHINESEWUZHOU.getId() ||
                source.getId() == SourceID.QIANQIANXIAOSHUO.getId() ||
                source.getId() == SourceID.PIAOTIANWENXUE.getId()) {
            link = link.substring(0, link.lastIndexOf('/') + 1);
        }
        return link;
    }

    static String convertText(String text, String charsetName) {
        try {
            Charset c = Charset.forName(charsetName);
            return URLEncoder.encode(text, c.name());
        } catch (Exception e) {
            return text;
        }
    }
    /**
     * 获取 通过xpath 查找到的字符串
     *
     * @param startNode 只有JXDocument   和  JXNode 两种
     * @param xpath
     * @return
     */
    static String getNodeStr(Object startNode, String xpath) {
        try {
            List<?> list;
            if (startNode instanceof JXDocument) {
                list = ((JXDocument) startNode).sel(xpath);
            } else if (startNode instanceof JXNode) {
                list = ((JXNode) startNode).sel(xpath);
            } else {
                return "";
            }
            return list.stream().map(Object::toString).collect(Collectors.joining());

        } catch (XpathSyntaxErrorException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    static String urlVerification(String link, String linkWithHost) {
        if (StringUtils.isBlank(link)) {
            return link;
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
}
