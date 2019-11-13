package cn.zero.spider.controller;

import cn.zero.spider.pojo.Article;
import cn.zero.spider.service.IArticleService;
import cn.zero.spider.webmagic.page.BiQuGePageProcessor;
import cn.zero.spider.webmagic.pipeline.BiQuGePipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.RedisScheduler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 章节控制器
 *
 * @author 蔡元豪
 * @date 2018/6/26 17:43
 */
@Controller
public class ArticleController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private IArticleService articleService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BiQuGePipeline biQuGePipeline;

    @Autowired
    private RedisScheduler redisScheduler;

    @Value("${spider.url}")
    private String spiderUrl;

    /**
     * 小说章节内容页面
     *
     * @param bookUrl    小说url
     * @param articleUrl 章节url
     * @return article
     */
    @RequestMapping(value = "/{bookUrl}/{articleUrl}.html")
    public ModelAndView article(@PathVariable("bookUrl") String bookUrl, @PathVariable("articleUrl") String articleUrl, HttpServletResponse response) {
        Cookie cookie = new Cookie(bookUrl, articleUrl);
        //30天过期
        cookie.setMaxAge(60 * 60 * 24 * 30);
        response.addCookie(cookie);
        ModelAndView modelAndView = new ModelAndView();
        Article article = articleService.getByUrl(bookUrl, articleUrl);
        if (article == null) {
            SetOperations<String, String> removeBookUrl = stringRedisTemplate.opsForSet();
            //移出已经爬取的小说章节记录 重新爬取章节
            logger.info("移出redis爬取章节记录：" + spiderUrl+"/" + bookUrl + "/" + articleUrl + ".html");
            removeBookUrl.remove("set_"+spiderUrl.replace("http://",""), spiderUrl+"/" + bookUrl + "/" + articleUrl + ".html");
           logger.info("要爬取的章节:"+spiderUrl+"/" + bookUrl + "/" + articleUrl + ".html");
            Spider.create(new BiQuGePageProcessor()).addUrl(spiderUrl+"/" + bookUrl + "/" + articleUrl + ".html")
                    .addPipeline(biQuGePipeline)
                    .setScheduler(redisScheduler)
                    .thread(1).run();
            modelAndView.addObject("article", articleService.getByUrl(bookUrl, articleUrl));
        } else {
            //   下一章
            Article next = articleService.getNext(bookUrl, articleUrl);
            if (next != null) {
                //下一章链接
                modelAndView.addObject("next", next.getBookUrl() + "/" + next.getUrl() + ".html");
            }
            Article previous = articleService.getPrevious(bookUrl, articleUrl);
            if (previous != null) {
                //上一章链接
                modelAndView.addObject("previous", previous.getBookUrl() + "/" + previous.getUrl() + ".html");
            }
            //当前章节
            modelAndView.addObject("article", article);

        }
        modelAndView.setViewName("book/article");
        return modelAndView;
    }

}
