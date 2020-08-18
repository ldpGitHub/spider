package cn.zero.spider.ldp;

import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.source.Source;
import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.xpath.model.JXDocument;
import cn.zero.spider.crawler.xpath.model.JXNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static cn.zero.spider.ldp.LdpUtils.*;

/**
 * @author ycj
 * @datetime 2020-6-14 16:09
 * @describe
 */
@Slf4j
@AllArgsConstructor
public class LdpSearch implements Callable<List<SearchBook>> {

    private final String keyWord;
    private final SourceConfig source;

    @Override
    public List<SearchBook> call() {
        try {
            int id = source.getId();
            SourceConfig.Search search = source.getSearch();

            // 书籍url
            String url = String.format(source.getSearchURL(), convertText(keyWord, search.getCharset()));

            JXDocument jxDocument = new JXDocument(Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537.36")
                    .validateTLSCertificates(false).get());
            List<JXNode> rs = jxDocument.selN(search.getXpath());

            List<SearchBook> books = rs.stream().map(jxNode -> {

                // 获取链接，链接不存在则代表不需要搜索
                String link = getLink(source, url, jxNode);
                if (StringUtils.isBlank(link)) return null;

                /*
                 * 收集书籍信息
                 */
                SearchBook book = new SearchBook();
                // 封面
                book.setCover(urlVerification(getNodeStr(jxNode, search.getCoverXpath()), url));
                // 书名
                book.setTitle(getNodeStr(jxNode, search.getTitleXpath()));
                // 作者
                book.setAuthor(getAuthor(id, jxNode, search.getAuthorXpath()));
                // sl
                SearchBook.SL slTemp = new SearchBook.SL(link,
                        new Source(id, source.getName(), source.getSearchURL(), source.getMinKeywords()));
                slTemp.setBookId(book.getBookId());
                book.getSources().add(slTemp);
                // 描述
                book.setDesc(getNodeStr(jxNode, search.getDescXpath()).trim());
                // 最新章节
                if (StringUtils.isNotBlank(search.getLastChapterXpath())) {
                    if (null == book.getLastChapter() || book.getLastChapter().isEmpty()) {
                        book.setLastChapter(getNodeStr(jxNode, search.getLastChapterXpath()).trim());
                        log.info("最新章节:     " + book.getLastChapter());
                    }
                }

                return book;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return books;
        } catch (Exception ignored) {
            // TODO 辣鸡ldp
        }
        return null;
    }

}
