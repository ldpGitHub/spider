package cn.zero.spider.ldp;

import cn.zero.spider.crawler.entity.book.SearchBook;
import cn.zero.spider.crawler.entity.source.Source;
import cn.zero.spider.crawler.entity.source.SourceConfig;
import cn.zero.spider.crawler.entity.source.SourceID;
import cn.zero.spider.crawler.xpath.exception.XpathSyntaxErrorException;
import cn.zero.spider.crawler.xpath.model.JXDocument;
import cn.zero.spider.crawler.xpath.model.JXNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author ycj
 * @datetime 2020-6-14 15:12
 * @describe
 */
@Slf4j
public class Ldp {

    private static final ExecutorService service
            = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final String CONFIG_FILE_PATH = "Template.json";
    private static final List<SourceConfig> CONFIGS = new ArrayList<>();
   // private static final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private static final ObjectMapper mapper = new ObjectMapper();
    static final BlockingQueue<Future<List<SearchBook>>> queue = new LinkedBlockingDeque<>();
    /**
     * 实例化CompletionService
     * 这个类会将完成的任务提交进去
     */
    static final CompletionService<List<SearchBook>> completionService = new ExecutorCompletionService<>(
            service, queue);

    static {
        try {
            Resource resource = new ClassPathResource(CONFIG_FILE_PATH);
            List<SourceConfig> list = mapper.readValue(resource.getInputStream(),
                    new TypeReference<List<SourceConfig>>() {});
            for (SourceConfig config : list) {
                if (config.isEnable())
                    CONFIGS.add(config);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static List<SearchBook> search(String keyWord, LjLdp<Future<List<SearchBook>>> callback) {

        // 提交任务
        CONFIGS.forEach(source ->
                completionService.submit(new LdpSearch(keyWord, source))
        );

        try {
            // 获取一个执行完成的任务
            // 此时get操作不会阻塞
            for (;;) {
                Future<List<SearchBook>> future = completionService.take();
                List<SearchBook> books = future.get();
                if (!CollectionUtils.isEmpty(books)) {
                    return books;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


}
