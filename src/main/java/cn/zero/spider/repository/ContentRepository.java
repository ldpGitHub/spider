package cn.zero.spider.repository;

import cn.zero.spider.crawler.entity.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content,Long> {
}
